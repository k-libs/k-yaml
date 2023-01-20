package io.foxcapades.lib.k.yaml.scan

import io.foxcapades.lib.k.yaml.YAMLVersion
import io.foxcapades.lib.k.yaml.bytes.*
import io.foxcapades.lib.k.yaml.err.UIntOverflowException
import io.foxcapades.lib.k.yaml.read.*
import io.foxcapades.lib.k.yaml.util.*

@Suppress("NOTHING_TO_INLINE")
class YAMLScanner {

  /**
   * Whether the STREAM-START token has been returned to the consumer of the
   * YAMLScanner via the [nextToken] method.
   */
  internal var streamStartProduced = false

  /**
   * Whether the STREAM-END token has been returned to the consumer of the
   * YAMLScanner via the [nextToken] method.
   */
  internal var streamEndProduced = false

  /**
   * Tracker for our current position in the YAML stream.
   *
   * This is tracked as a "human-friendly" position with overall index, line
   * number, and column number.
   */
  internal val position = SourcePositionTracker()

  /**
   * Queue of warnings that have been encountered while scanning through the
   * YAML stream for tokens.
   */
  internal val warnings = Queue<SourceWarning>()

  /**
   * Queue of tokens that have been generated but not yet returned to the
   * consumer of the YAMLScanner.
   *
   * This is a queue as encountering a single value in the underlying YAML
   * stream may cause the creation of multiple tokens.
   *
   * For example, if the YAML stream contains an implicit document with a
   * mapping, such as:
   *
   * ```yaml
   * foo: bar
   * ```
   *
   * When the scanner encountered the simple key `foo:` it would not only
   * generate a mapping key token, but would also generate an implicit document
   * start token as well as a block mapping start token.
   */
  internal val tokens = Queue<YAMLToken>(8)

  // region Context Indicators

  private var inDocument = false

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

  internal val reader: YAMLReader

  internal val lineBreakType: LineBreakType

  constructor(reader: YAMLReader, lineBreak: LineBreakType) {
    this.reader = reader
    this.lineBreakType = lineBreak
  }

  // region Public Methods

  val hasMoreTokens: Boolean
    get() = !streamEndProduced

  val hasWarnings: Boolean
    get() = warnings.isNotEmpty

  fun nextToken(): YAMLToken {
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

  fun nextWarning(): SourceWarning = warnings.pop()

  // endregion Public Methods

  // region Reader Wrapping

  /**
   * Attempts to ensure that the given number of UTF-8 codepoints are cached in
   * the reader buffer.
   *
   * @param codepoints The number of codepoints that the reader should attempt
   * to cache.
   *
   * @return `true` if the requested number of codepoints could be cached.
   * `false` if fewer than the requested number of codepoints could be cached.
   */
  internal inline fun cache(codepoints: Int) = reader.cache(codepoints)

  /**
   * Skips over the given number of ASCII characters in the reader buffer and
   * update the position tracker.
   *
   * @param count Number of ASCII characters (or bytes) in the reader buffer to
   * skip.  This is also the amount that the current column index is increased
   * by.
   */
  internal fun skipASCII(count: Int = 1) {
    reader.skip(count)
    position.incPosition(count.toUInt())
  }

  internal fun skipUTF8(count: Int = 1) {
    reader.skipCodepoints(count)
    position.incPosition(count.toUInt())
  }

  internal fun skipLine() {
    reader.cache(4)

    if (reader.isCRLF()) {
      skipLine(NL.CRLF)
    } else if (reader.isCR()) {
      skipLine(NL.CR)
    } else if (reader.isLF()) {
      skipLine(NL.LF)
    } else if (reader.isNEL()) {
      skipLine(NL.NEL)
    } else if (reader.isLS()) {
      skipLine(NL.LS)
    } else if (reader.isPS()) {
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

  internal inline fun asDecimal(offset: Int = 0) = reader.asDecDigit(offset)

  // endregion Reader Wrapping

  private fun fetchNextToken() {
    if (!streamStartProduced)
      return fetchStreamStartToken()

    // TODO: SKIP TO NEXT TOKEN NEEDS TO HANDLE INDENT TAB DETECTION
    skipToNextToken()

    cache(1)

    if (!haveMoreCharactersAvailable)
      return fetchStreamEndToken()

    when {
      // Good boy characters: % - . # & * [ ] { } | : ' " > , ?
      unsafeHavePercent()     -> fetchDirectiveToken()
      unsafeHaveDash()        -> fetchAmbiguousDashToken()
      unsafeHavePeriod()      -> fetchAmbiguousPeriodToken()
      unsafeHavePound()       -> fetchCommentToken()
      unsafeHaveAmp()         -> fetchAnchorToken()
      unsafeHaveAsterisk()    -> fetchAliasToken()
      unsafeHaveSquareOpen()  -> fetchFlowSequenceStartToken()
      unsafeHaveSquareClose() -> fetchFlowSequenceEndToken()
      unsafeHaveCurlyOpen()   -> fetchFlowMappingStartToken()
      unsafeHaveCurlyClose()  -> fetchFlowMappingEndToken()
      unsafeHavePipe()        -> fetchLiteralStringToken()
      unsafeHaveColon()       -> fetchAmbiguousColonToken()
      unsafeHaveApostrophe()  -> fetchSingleQuotedStringToken()
      unsafeHaveDoubleQuote() -> fetchDoubleQuotedStringToken()
      unsafeHaveGreaterThan() -> fetchFoldedStringToken()
      unsafeHaveComma()       -> fetchFlowItemSeparatorToken()
      unsafeHaveQuestion()    -> fetchAmbiguousQuestionToken()

      // BAD NONO CHARACTERS: @ `
      unsafeHaveAt()          -> fetchAmbiguousAtToken()
      unsafeHaveGrave()       -> fetchAmbiguousGraveToken()

      // Meh characters: ~ $ ^ ( ) _ + = \ ; < /
      // And everything else...
      else                    -> fetchPlainScalar()
    }
  }

  private fun skipToNextToken() {
    // TODO:
    //   | This method needs to differentiate between tabs and spaces when
    //   | slurping up those delicious, delicious bytes.
    //   |
    //   | This is because TAB characters are not permitted as part of
    //   | indentation.
    //   |
    //   | If we choose to warn about tab characters rather than throwing an
    //   | error, we need to determine the width of the tab character so as to
    //   | keep the column index correct...

    while (true) {
      cache(1)

      when {
        // We found the end of the stream.
        haveEOF()       -> break
        haveSpace()     -> skipASCII()
        haveTab()       -> TODO("What manner of tomfuckery is this")
        haveAnyBreak() -> skipLine()
        else           -> break
      }
    }
  }

  internal fun parseUInt(): UInt {
    val intStart = position.mark()
    var intValue = 0u
    var addValue: UInt

    while (true) {
      cache(1)

      if (haveDecimalDigit()) {
        if (intValue > UInt.MAX_VALUE / 10u)
          throw UIntOverflowException(intStart)

        intValue *= 10u
        addValue = asDecimal()

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

    cache(1)
    while (haveBlank()) {
      skipASCII()
      cache(1)
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
    if (!takeCodepointFrom(reader.utf8Buffer))
      throw IllegalStateException("invalid utf-8 codepoint in the reader buffer or buffer is offset")
    position.incPosition()
  }

  internal fun UByteBuffer.detectNewLineType() =
    when {
      isLF()   -> NL.LF
      isCRLF() -> NL.CRLF
      isCR()   -> NL.CR
      isNEL()  -> NL.NEL
      isLS()   -> NL.LS
      isPS()   -> NL.PS
      else     -> throw IllegalStateException("called #detectNewLineType when the reader was not on a new line character")
    }

  internal fun UByteBuffer.claimNewLine(from: UByteBuffer, position: SourcePositionTracker) {
    if (from.isCRLF()) {
      appendNewLine(NL.CRLF)
      from.skipLine(NL.CRLF)
      position.incLine(NL.CRLF.characters.toUInt())
    } else if (from.isCR()) {
      appendNewLine(NL.CR)
      from.skipLine(NL.CR)
      position.incLine(NL.CR.characters.toUInt())
    } else if (from.isLF()) {
      appendNewLine(NL.LF)
      from.skipLine(NL.LF)
      position.incLine(NL.LF.characters.toUInt())
    } else if (from.isNEL()) {
      appendNewLine(NL.NEL)
      from.skipLine(NL.NEL)
      position.incLine(NL.NEL.characters.toUInt())
    } else if (from.isLS()) {
      appendNewLine(NL.LS)
      from.skipLine(NL.LS)
      position.incLine(NL.LS.characters.toUInt())
    } else if (from.isPS()) {
      appendNewLine(NL.PS)
      from.skipLine(NL.PS)
      position.incLine(NL.PS.characters.toUInt())
    } else {
      throw IllegalStateException("called #claimNewLine() when the reader was not on a new line character")
    }
  }

  internal fun UByteBuffer.claimNewLine(type: NL, from: UByteBuffer, position: SourcePositionTracker) {
    appendNewLine(type)
    from.skipLine(type)

    if (type == NL.CRLF) {
      position.incLine(2u)
    } else {
      position.incLine(1u)
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

  internal fun UByteBuffer.skipLine(nl: NL) {
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
}