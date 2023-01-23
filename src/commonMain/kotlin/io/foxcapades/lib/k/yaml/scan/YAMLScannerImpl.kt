package io.foxcapades.lib.k.yaml.scan

import io.foxcapades.lib.k.yaml.LineBreakType
import io.foxcapades.lib.k.yaml.YAMLScanner
import io.foxcapades.lib.k.yaml.YAMLVersion
import io.foxcapades.lib.k.yaml.bytes.*
import io.foxcapades.lib.k.yaml.read.*
import io.foxcapades.lib.k.yaml.token.*
import io.foxcapades.lib.k.yaml.util.*
import io.foxcapades.lib.k.yaml.warn.SourceWarning


@Suppress("NOTHING_TO_INLINE")
@OptIn(ExperimentalUnsignedTypes::class)
internal class YAMLScannerImpl : YAMLScanner {

  internal val position = SourcePositionTracker()
  internal var streamStartProduced = false
  internal var streamEndProduced = false
  internal val warnings = Queue<SourceWarning>(4)

  internal val tokens = Queue<YAMLToken>(4)
  internal lateinit var lastToken: YAMLToken

  internal var haveContentOnThisLine = false

  internal val indents = UIntStack()
  internal var indent = 0u

  // region Context Indicators

  // TODO: implement this check, right now nothing writes to it.
  internal var inDocument = false

  // region Flows

  internal val flows = ByteStack(4)

  internal inline val inFlow: Boolean
    get() = flows.isNotEmpty

  internal inline val inFlowSequence: Boolean
    get() = inFlow && flows.peek() == FlowTypeSequence

  internal inline val inFlowMapping
    get() = inFlow && flows.peek() == FlowTypeMapping

  // endregion Flows

  internal val atStartOfLine: Boolean
    get() = position.column == 0u

  // endregion Context Indicators

  internal inline val haveMoreCharactersAvailable
    get() = !reader.atEOF || reader.isNotEmpty

  internal var version = YAMLVersion.VERSION_1_2

  internal val reader: BufferedUTFStreamReader

  internal val lineBreakType: LineBreakType

  // region Reusable Buffers

  internal val contentBuffer1   = UByteBuffer(1024)
  internal val contentBuffer2   = UByteBuffer(1024)
  internal val trailingWSBuffer = UByteBuffer(16)
  internal val trailingNLBuffer = UByteBuffer(4)

  // endregion Reusable Buffers


  constructor(reader: BufferedUTFStreamReader, lineBreak: LineBreakType) {
    this.reader = reader
    this.lineBreakType = lineBreak
  }

  // region Public Methods

  override val hasNextToken: Boolean
    get() = !streamEndProduced

  override fun nextToken(): YAMLToken {
    if (streamEndProduced)
      throw IllegalStateException("nextToken called on a YAML scanner that has already produced the end of the input YAML stream")

    while (tokens.isEmpty) {
      fetchNextToken()
    }

    lastToken = tokens.pop()

    if (lastToken is YAMLTokenStreamEnd)
      streamEndProduced = true

    return lastToken
  }

  // endregion Public Methods

  // region Reader Wrapping



  // endregion Reader Wrapping

  internal fun fetchNextToken() {
    if (!streamStartProduced)
      return fetchStreamStartToken()

    // TODO: SKIP TO NEXT TOKEN NEEDS TO HANDLE INDENT TAB DETECTION
    skipToNextToken()

    reader.cache(1)

    if (!haveMoreCharactersAvailable)
      return fetchStreamEndToken()

    when {
      reader.uIsDash()        -> fetchAmbiguousDashToken()
      reader.uIsColon()       -> fetchAmbiguousColonToken()
      reader.uIsComma()       -> fetchFlowItemSeparatorToken()
      reader.uIsPound()       -> fetchCommentToken()
      reader.uIsApostrophe()  -> fetchSingleQuotedStringToken()
      reader.uIsDoubleQuote() -> fetchDoubleQuotedStringToken()
      reader.uIsSquareOpen()  -> fetchFlowSequenceStartToken()
      reader.uIsCurlyOpen()   -> fetchFlowMappingStartToken()
      reader.uIsPipe()        -> fetchLiteralStringToken()
      reader.uIsGreaterThan() -> fetchFoldedStringToken()
      reader.uIsSquareClose() -> fetchFlowSequenceEndToken()
      reader.uIsCurlyClose()  -> fetchFlowMappingEndToken()
      reader.uIsExclamation() -> fetchTagToken()
      reader.uIsPercent()     -> fetchAmbiguousPercent()
      reader.uIsPeriod()      -> fetchAmbiguousPeriodToken()
      reader.uIsAmpersand()   -> fetchAnchorToken()
      reader.uIsAsterisk()    -> fetchAliasToken()
      reader.uIsQuestion()    -> fetchAmbiguousQuestionToken()

      // BAD NONO CHARACTERS: @ `
      reader.isAt()          -> fetchAmbiguousAtToken()
      reader.isGrave()       -> fetchAmbiguousGraveToken()

      // Meh characters: ~ $ ^ ( ) _ + = \ ; < /
      // And everything else...
      else                    -> fetchPlainScalar()
    }
  }


  // region Warning Helpers

  internal fun warn(
    message: String,
    start:   SourcePosition = position.mark(),
    end:     SourcePosition = position.mark(),
  ): SourcePosition {
    warnings.push(SourceWarning(message, start, end))
    return end
  }

  internal fun getWarnings(): Array<SourceWarning> = warnings.popToArray { arrayOfNulls(it) }

  // endregion Warning Helpers

  // region Buffer Writing Helpers
  //
  // TODO:
  //   | This whole region is suspect, these things should not be here, or at
  //   | least should be reworked.

  internal fun UByteBuffer.claimUTF8() {
    if (!takeCodepointFrom(reader))
      throw IllegalStateException("invalid utf-8 codepoint in the reader buffer or buffer is offset")
    position.incPosition()
  }

  @Deprecated("use the other one")
  internal fun UByteBuffer.claimASCII() {
    push(reader.pop())
    position.incPosition()
  }

  // endregion Buffer Writing Helpers

  internal fun UByteBuffer.takeASCIIFrom(count: Int, reader: BufferedUTFStreamReader, position: SourcePositionTracker) {
    var i = 0
    while (i++ < count)
      push(reader.pop())

    position.incPosition(count.toUInt())
  }

  @Deprecated("use takeASCIIFrom")
  internal fun UByteBuffer.claimASCII(bytes: Int, other: UByteBuffer, position: SourcePositionTracker) {
    var i = 0
    while (i++ < bytes)
      push(other.pop())

    position.incPosition(bytes.toUInt())
  }

  internal fun eatSpaces(): UInt {
    var count = 0u

    reader.cache(1)
    while (reader.isSpace()) {
      count++
      skipASCII()
      reader.cache(1)
    }

    return count
  }

  internal fun fetchLiteralStringToken() {
    // TODO: chomping indicator
    // TODO: indent indicator
    TODO("fetch pipe string")
  }
}
