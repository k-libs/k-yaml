package io.foxcapades.lib.k.yaml.scan.events

import io.foxcapades.lib.k.yaml.YAMLMappingStyle
import io.foxcapades.lib.k.yaml.YAMLScalarStyle
import io.foxcapades.lib.k.yaml.YAMLSequenceStyle
import io.foxcapades.lib.k.yaml.YAMLStreamTokenizer
import io.foxcapades.lib.k.yaml.err.YAMLScannerException
import io.foxcapades.lib.k.yaml.scan.events.event.*
import io.foxcapades.lib.k.yaml.scan.tokens.token.*
import io.foxcapades.lib.k.yaml.util.ImmutableArray
import io.foxcapades.lib.k.yaml.util.SourcePosition
import io.foxcapades.lib.k.yaml.util.UByteString
import io.foxcapades.lib.k.yaml.util.collections.Queue
import io.foxcapades.lib.k.yaml.warn.SourceWarning
import io.klibs.collections.Stack

class YAMLEventStream {
  private var streamHasEnded = false

  private val context = Stack<EventStreamContext>(8)

  private val tokens = Queue<YAMLToken>(4)

  private val events = Queue<YAMLEvent>(4)

  private val warnings = Queue<SourceWarning>(4)

  private var expectation = ExpectEvent.StreamStart

  private val scanner: YAMLStreamTokenizer

  val hasNextEvent
    get() = !streamHasEnded

  fun nextEvent(): YAMLEvent {
    if (streamHasEnded)
      throw IllegalStateException("called nextEvent when the YAML input stream has already been consumed")

    if (events.isEmpty)
      fetchNextEvent()

    val out = events.pop()

    if (out.type == YAMLEventTypeStreamEnd)
      streamHasEnded = true

    return out
  }

  private fun cacheTokens(count: Int): Boolean {
    if (tokens.size >= count)
      return true

    while (scanner.hasNextToken && tokens.size < count) {
      tokens.push(scanner.nextToken())
    }

    return tokens.size >= count
  }

  private fun flowContext(): YAMLTokenFlow? {
    for (i in 0 .. context.lastIndex)
      if (context[i] is YAMLTokenFlow)
        return context[i] as YAMLTokenFlow

    return null
  }

  private fun popToken(): YAMLToken {
    val out = tokens.pop()
    out.warnings.forEach { warnings.push(it) }
  }

  private fun fetchNextEvent() {
    cacheTokens(1) || throw IllegalStateException("failed to queue up next token")

    when (expectation) {
      ExpectEvent.StreamStart           -> expectStreamStart()
      ExpectEvent.ImplicitDocumentStart -> TODO()
      ExpectEvent.ExplicitDocumentStart -> TODO()
      ExpectEvent.DocumentContent       -> TODO()
      ExpectEvent.DocumentEnd           -> TODO()
      ExpectEvent.StreamEnd             -> TODO()
      ExpectEvent.Scalar                -> TODO("""
        Expect a scalar value token or a node property
        In a flow map also expect a colon or a comma
        In a flow sequence also expect a comma
      """.trimIndent())
    }
  }

  // Expect stream start
  private fun expectStreamStart() {
    if (tokens[0] is YAMLTokenStreamStart) {
      val tmp = tokens.pop() as YAMLTokenStreamStart
      events.push(YAMLEvent(YAMLEventType.StreamStart, StreamStartEventData(tmp.encoding), tmp.start, tmp.end))
      expectation = ExpectEvent.ImplicitDocumentStart
    } else {
      throw YAMLScannerException("unexpected token", tokens[0].start)
    }
  }

  // Expect document start, directives, or document content
  private fun expectImplicitDocumentStart() {
    when (val token = tokens[0]) {
      // Boring
      is YAMLTokenComment            -> TODO("Emit a comment event but don't change the state")
      is YAMLTokenScalar             -> TODO("emit a document start event, then figure out what we're looking at")
      is YAMLTokenSequenceEntry      -> TODO("emit a document start event, emit a sequence start event, then set us up to expect a scalar value")

      // Interesting
      is YAMLTokenFlowMappingStart -> {
        tokens.pop()
        emitImplicitDocumentStartEvent(token.start)
        emitFlowMappingStart(token.start, token.end)
        expectation = ExpectEvent.FlowMappingKey
        return
      }

      is YAMLTokenFlowSequenceStart -> {
        tokens.pop()
        emitImplicitDocumentStartEvent(token.start)
        emitFlowSequenceStart(token.start, token.end)
        expectation = ExpectEvent.FlowSequenceEntry
        return
      }

      is YAMLTokenDocumentStart -> {
        emitExplicitDocumentStartEvent(tokens.pop())
        TODO()
      }

      // Spicy
      is YAMLTokenDirectiveTag       -> TODO()
      is YAMLTokenDirectiveYAML      -> TODO()
      is YAMLTokenMappingKey         -> TODO()
      is YAMLTokenAnchor             -> TODO()
      is YAMLTokenTag                -> TODO()

      // Weird, but okay.
      is YAMLTokenStreamEnd    -> TODO()
      is YAMLTokenDocumentEnd  -> TODO()
      is YAMLTokenMappingValue -> {
        tokens.pop()
        emitImplicitDocumentStartEvent(token.start)
        emitBlockMappingStart(token.start, token.indent)
        emitEmptyScalar(token.start)
        expectation = ExpectEvent.BlockMappingValue
        return
      }

      // Camp icky-bad-boys
      is YAMLTokenAlias,
      is YAMLTokenStreamStart,
      is YAMLTokenFlowItemSeparator,
      is YAMLTokenFlowMappingEnd,
      is YAMLTokenFlowSequenceEnd,
      is YAMLTokenInvalid -> {
        throw YAMLScannerException("unexpected token", token.start)
      }
    }
  }

  // Expect directives, or an explicit document start
  private fun expectExplicitDocumentStart() {}

  private fun expectStreamEnd() {}

  // region Event Emitters

  private fun emitImplicitDocumentStartEvent(position: SourcePosition) {
    context.push(EventStreamContext(EventStreamContextTypeDocument, 0u))
    events.push(YAMLEvent(
      YAMLEventType.DocumentStart,
      DocumentStartEventData(null, ImmutableArray(emptyArray()), true),
      position,
      position
    ))
  }

  private fun emitExplicitDocumentStartEvent(token: YAMLToken) {
    context.push(EventStreamContext(EventStreamContextTypeDocument, 0u))
    events.push(YAMLEvent(
      YAMLEventType.DocumentStart,
      DocumentStartEventData(null, ImmutableArray(emptyArray()), false),
      token.start,
      token.end
    ))
  }

  private fun emitBlockMappingStart(position: SourcePosition, indent: UInt) {
    context.push(EventStreamContext(EventStreamContextTypeBlockMapping, indent))
    events.push(YAMLEvent(
      YAMLEventType.MappingStart,
      MappingStartEventData(null, null, YAMLMappingStyle.Block),
      position,
      position
    ))
  }

  @OptIn(ExperimentalUnsignedTypes::class)
  private fun emitEmptyScalar(position: SourcePosition) {
    events.push(YAMLEvent(
      YAMLEventType.Scalar,
      ScalarEventData(null, null, YAMLScalarStyle.Plain, UByteString(UByteArray(0))),
      position,
      position
    ))
  }

  private fun emitFlowMappingStart(start: SourcePosition, end: SourcePosition) {
    context.push(EventStreamContext(EventStreamContextTypeFlowMapping, 0u))
    events.push(YAMLEvent(
      YAMLEventType.MappingStart,
      MappingStartEventData(null, null, YAMLMappingStyle.Flow),
      start,
      end
    ))
  }

  private fun emitFlowSequenceStart(start: SourcePosition, end: SourcePosition) {
    context.push(EventStreamContext(EventStreamContextTypeFlowSequence, 0u))
    events.push(YAMLEvent(
      YAMLEventType.SequenceStart,
      SequenceStartEventData(null, null, YAMLSequenceStyle.Flow),
      start,
      end
    ))
  }

  // endregion
}