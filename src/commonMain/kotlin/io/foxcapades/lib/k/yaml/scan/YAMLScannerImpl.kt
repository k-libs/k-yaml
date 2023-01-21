package io.foxcapades.lib.k.yaml.scan

import io.foxcapades.lib.k.yaml.LineBreakType
import io.foxcapades.lib.k.yaml.YAMLScanner
import io.foxcapades.lib.k.yaml.YAMLVersion
import io.foxcapades.lib.k.yaml.bytes.*
import io.foxcapades.lib.k.yaml.err.UIntOverflowException
import io.foxcapades.lib.k.yaml.read.*
import io.foxcapades.lib.k.yaml.token.*
import io.foxcapades.lib.k.yaml.util.*


@Suppress("NOTHING_TO_INLINE")
@OptIn(ExperimentalUnsignedTypes::class)
internal class YAMLScannerImpl : YAMLScanner {

  internal val position = SourcePositionTracker()
  internal var streamStartProduced = false
  internal var streamEndProduced = false
  internal val warnings = Queue<SourceWarning>(4)
  internal val tokens = Queue<YAMLToken>(4)
  internal var contentOnThisLine = false

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

    val out = tokens.pop()

    if (out.type == YAMLTokenType.StreamEnd)
      streamEndProduced = true

    return out
  }

  // endregion Public Methods

  // region Reader Wrapping


  internal fun skipLine() {
    reader.cache(4)

    if (reader.isCRLF()) {
      skipLine(NL.CRLF)
    } else if (reader.isCarriageReturn()) {
      skipLine(NL.CR)
    } else if (reader.isLineFeed()) {
      skipLine(NL.LF)
    } else if (reader.isNextLine()) {
      skipLine(NL.NEL)
    } else if (reader.isLineSeparator()) {
      skipLine(NL.LS)
    } else if (reader.isParagraphSeparator()) {
      skipLine(NL.PS)
    } else {
      throw IllegalStateException("called #skipLine() when the reader was not on a newline character")
    }
  }

  internal fun skipLine(nl: NL) {
    if (inDocument) {
      when (nl) {
        NL.NEL,
        NL.LS,
        NL.PS,
             -> {
          if (version != YAMLVersion.VERSION_1_1)
            warn("invalid line break character; YAML 1.2 only permits line breaks consisting of CRLF, CR, or LF")
        }

        // CRLF
        // CR
        // LF
        else -> { /* nothing special to for these line breaks */ }
      }
    }

    reader.skip(nl.width)
    position.incLine(nl.characters.toUInt())
  }

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
      reader.uIsPercent()     -> fetchDirectiveToken()
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


  internal fun parseUInt(): UInt {
    val intStart = position.mark()
    var intValue = 0u
    var addValue: UInt

    while (true) {
      reader.cache(1)

      if (reader.isDecimalDigit()) {
        if (intValue > UInt.MAX_VALUE / 10u)
          throw UIntOverflowException(intStart)

        intValue *= 10u
        addValue = reader.asDecimalDigit()

        if (intValue > UInt.MAX_VALUE - addValue)
          throw UIntOverflowException(intStart)

        intValue += addValue

        skipASCII()
      } else {
        break
      }
    }

    return intValue
  }

  /**
   * Skips over `<SPACE>` and `<TAB>` characters in the reader buffer,
   * incrementing the position tracker as it goes.
   *
   * @return The number of blank characters that were skipped.
   */
  internal fun eatBlanks(): Int {
    var out = 0

    reader.cache(1)
    while (reader.isBlank()) {
      skipASCII()
      reader.cache(1)
      out++
    }

    return out
  }

  // region Warning Helpers

  internal fun warn(
    message: String,
    start:   SourcePosition = position.mark(),
    end:     SourcePosition = position.mark(),
  ) {
    warnings.push(SourceWarning(message, start, end))
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

  internal fun UByteBuffer.claimASCII() {
    push(reader.pop())
    position.incPosition()
  }

  internal fun UByteSource.detectNewLineType() =
    when {
      isLineFeed()           -> NL.LF
      isCRLF()               -> NL.CRLF
      isCarriageReturn() -> NL.CR
      isNextLine()       -> NL.NEL
      isLineSeparator()  -> NL.LS
      isParagraphSeparator() -> NL.PS
      else                   -> throw IllegalStateException(
        "called #detectNewLineType when the reader was not on a new line character"
      )
    }

  internal inline fun UByteBuffer.claimNewLine() = claimNewLine(reader, position)

  internal fun UByteBuffer.claimNewLine(from: UByteSource) {
    if (from.isCRLF()) {
      appendNewLine(NL.CRLF)
      from.skipLine(NL.CRLF)
    } else if (from.isCarriageReturn()) {
      appendNewLine(NL.CR)
      from.skipLine(NL.CR)
    } else if (from.isLineFeed()) {
      appendNewLine(NL.LF)
      from.skipLine(NL.LF)
    } else if (from.isNextLine()) {
      appendNewLine(NL.NEL)
      from.skipLine(NL.NEL)
    } else if (from.isLineSeparator()) {
      appendNewLine(NL.LS)
      from.skipLine(NL.LS)
    } else if (from.isParagraphSeparator()) {
      appendNewLine(NL.PS)
      from.skipLine(NL.PS)
    } else {
      throw IllegalStateException(
        "called #claimNewLine(UByteSource) when the given buffer was not on a new line character"
      )
    }
  }

  internal fun UByteBuffer.claimNewLine(from: UByteSource, position: SourcePositionTracker) {
    if (from.isCRLF()) {
      appendNewLine(NL.CRLF)
      from.skipLine(NL.CRLF)
      position.incLine(NL.CRLF.characters.toUInt())
    } else if (from.isCarriageReturn()) {
      appendNewLine(NL.CR)
      from.skipLine(NL.CR)
      position.incLine(NL.CR.characters.toUInt())
    } else if (from.isLineFeed()) {
      appendNewLine(NL.LF)
      from.skipLine(NL.LF)
      position.incLine(NL.LF.characters.toUInt())
    } else if (from.isNextLine()) {
      appendNewLine(NL.NEL)
      from.skipLine(NL.NEL)
      position.incLine(NL.NEL.characters.toUInt())
    } else if (from.isLineSeparator()) {
      appendNewLine(NL.LS)
      from.skipLine(NL.LS)
      position.incLine(NL.LS.characters.toUInt())
    } else if (from.isParagraphSeparator()) {
      appendNewLine(NL.PS)
      from.skipLine(NL.PS)
      position.incLine(NL.PS.characters.toUInt())
    } else {
      throw IllegalStateException(
        "called #claimNewLine(UByteSource, SourcePositionTracker) when the given buffer was not on a new line character"
      )
    }
  }

  internal fun UByteBuffer.skipNewLine(position: SourcePositionTracker) {
    skipNewLine(detectNewLineType(), position)
  }

  internal fun UByteBuffer.skipNewLine(type: NL, position: SourcePositionTracker) {
    skipLine(type)

    if (type == NL.CRLF) {
      position.incLine(2u)
    } else {
      position.incLine(1u)
    }
  }

  internal fun UByteBuffer.appendNewLine(nl: NL) {
    when (lineBreakType) {
      LineBreakType.CRLF        -> NL.CRLF.writeUTF8(this)
      LineBreakType.CR          -> NL.CR.writeUTF8(this)
      LineBreakType.LF          -> NL.LF.writeUTF8(this)
      LineBreakType.SameAsInput -> nl.writeUTF8(this)
    }
  }

  internal fun UByteSource.skipLine(nl: NL) {
    if (inDocument) {
      when (nl) {
        NL.NEL,
        NL.LS,
        NL.PS,
             -> {
          if (version != YAMLVersion.VERSION_1_1)
            warn("invalid line break character; YAML 1.2 only permits line breaks consisting of CRLF, CR, or LF")
        }

        // CRLF
        // CR
        // LF
        else -> { /* nothing special to for these line breaks */ }
      }
    }

    skip(nl.width)
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
