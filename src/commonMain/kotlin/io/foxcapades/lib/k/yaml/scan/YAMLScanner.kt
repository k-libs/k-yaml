package io.foxcapades.lib.k.yaml.scan

import io.foxcapades.lib.k.yaml.YAMLVersion
import io.foxcapades.lib.k.yaml.bytes.*
import io.foxcapades.lib.k.yaml.err.YAMLScannerException
import io.foxcapades.lib.k.yaml.read.*
import io.foxcapades.lib.k.yaml.read.isCRLF
import io.foxcapades.lib.k.yaml.read.isNEL
import io.foxcapades.lib.k.yaml.util.*
import io.foxcapades.lib.k.yaml.util.UByteBuffer


@Suppress("NOTHING_TO_INLINE")
class YAMLScanner {

  /**
   * Whether the STREAM-START token has been returned to the consumer of the
   * YAMLScanner via the [nextToken] method.
   */
  private var streamStartProduced = false

  /**
   * Whether the STREAM-END token has been returned to the consumer of the
   * YAMLScanner via the [nextToken] method.
   */
  private var streamEndProduced = false

  /**
   * Tracker for our current position in the YAML stream.
   *
   * This is tracked as a "human-friendly" position with overall index, line
   * number, and column number.
   */
  private val position = SourcePositionTracker()

  /**
   * Queue of warnings that have been encountered while scanning through the
   * YAML stream for tokens.
   */
  private val warnings = Queue<ScannerWarning>()

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
   * start token.
   */
  private val tokens = Queue<YAMLToken>(8)

  // region Context Indicators

  private var inDocument = false

  private var flowLevel = 0u

  private val inFlow: Boolean
    get() = flowLevel == 0u

  private val atStartOfLine: Boolean
    get() = position.column == 0u

  // endregion Context Indicators

  private inline val haveMoreInBuffer
    get() = !reader.atEOF || reader.isNotEmpty

  private var version = YAMLVersion.VERSION_1_2

  private val reader: YAMLReader

  private val lineBreakType: LineBreakType

  constructor(reader: YAMLReader, lineBreak: LineBreakType) {
    this.reader = reader
    this.lineBreakType = lineBreak
  }

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

  fun nextWarning(): ScannerWarning = warnings.pop()

  private fun fetchNextToken() {
    if (!streamStartProduced)
      return fetchStreamStartToken()

    skipToNextToken()

    cache(1)

    if (!haveMoreInBuffer)
      return fetchStreamEndToken()

    when (reader[0]) {
      // Good boy characters: % - . # & * [ ] { } | : ' " > , ?
      A_PERCENT   -> fetchDirectiveToken()
      A_DASH      -> fetchDashToken()
      A_PERIOD    -> fetchPeriodToken()
      A_POUND     -> fetchCommentToken()
      A_AMP       -> fetchAnchorToken()
      A_ASTERISK  -> fetchAliasToken()
      A_SQ_OP     -> fetchFlowSequenceStartToken()
      A_SQ_CL     -> fetchFlowSequenceEndToken()
      A_CU_OP     -> fetchFlowMappingStartToken()
      A_CU_CL     -> fetchFlowMappingEndToken()
      A_PIPE      -> fetchLiteralStringToken()
      A_COLON     -> fetchColonToken()
      A_APOS      -> fetchFlowStringToken(true)
      A_DBL_QUOTE -> fetchFlowStringToken(false)
      A_GREATER   -> fetchFoldedStringToken()
      A_COMMA     -> fetchFlowItemSeparatorToken()
      A_QUESTION  -> fetchQuestionToken()
      A_TILDE     -> fetchTildeToken()

      // BAD NONO CHARACTERS: @ `
      A_AT        -> fetchAtToken()
      A_GRAVE     -> fetchGraveToken()

      // Meh characters: ~ $ ^ ( ) _ + = \ ; < /
      // And everything else...
      else        -> fetchPlainScalar()
    }
  }

  @OptIn(ExperimentalUnsignedTypes::class)
  private fun fetchPlainScalar() {
    // Record the position of the first character in the plain scalar.
    val startMark = position.mark()

    // Create a rolling tracker that will be used to keep track of the position
    // of the last non-blank, non-break character seen before the next token is
    // encountered.
    val endMark = position.copy()

    // Whitespaces at the end of a line
    val trailingWS = UByteBuffer()

    // Line breaks in between
    val lineBreaks = UByteBuffer()

    // Buffer for the token value
    val tokenBuffer = UByteBuffer(2048)

    while (true) {
      // Load the next codepoint into the reader buffer
      cache(1)

      // If the reader is empty, then whatever we currently have in our token
      // buffer
      if (!haveMoreInBuffer)
        return tokens.push(newPlainScalarToken(tokenBuffer.popToArray(), startMark, endMark.mark()))

      // When we hit one of the following characters, then we pay attention to
      // what's going on because we may have hit the start of a new token:
      //
      //   `:` `,` `?` `#`
      //
      // When we hit a newline, or whitespace character, buffer it on the side
      // in case we need it.
      when {

        // If we hit a whitespace character
        isBlank() -> {
          // And the last character was not a line break
          if (lineBreaks.isEmpty)
            // Pop it from the reader buffer and append it to our trailing
            // whitespace buffer for possible use if we encounter a non-space
            // character on this line.
            trailingWS.claimASCII()

          // Skip to the next character
          continue
        }

        // If we hit a newline character
        isAnyBreak() -> {
          // Append it to our newline buffer in case we need it to collapse into
          // a space or shortened set of newlines as per the YAML specification.
          lineBreaks.claimNewLine()

          // Continue to the next character.
          continue
        }

        // If we hit a `:` character
        isColon() -> {
          // Attempt to cache another codepoint in the buffer, we need to look
          // ahead to the next character to determine if we've reached the end
          // of this scalar.
          cache(2)

          // If the colon character is followed by any of this junk, then IT IS
          // THE END! (of the scalar we've been chewing on, but the beginning of
          // a mapping value indicator)
          if (isBlank(1) || isAnyBreak(1) || isEOF(1))
            return tokens.push(newPlainScalarToken(tokenBuffer.popToArray(), startMark, endMark.mark()))
        }

        // If we hit a `,` character AND we are in a flow context
        isComma() && inFlow -> {
          // Then we have reached the end of our scalar token (and the start of
          // a flow entry separator token
          return tokens.push(newPlainScalarToken(tokenBuffer.popToArray(), startMark, endMark.mark()))
        }

        // If we hit a `?` character AND we are at the start of a line AND we
        // are NOT in a flow context.
        isQuestion() && !inFlow && atStartOfLine -> {
          // Attempt to cache another codepoint in the buffer, we need to look
          // ahead to the next character to determine whether we've reached the
          // end of this scalar
          cache(2)

          // If the question mark character is followed by any of this stuff,
          // then it is, in fact, the start of a complex mapping key indicator.
          if (isBlank(1) || isAnyBreak(1) || isEOF(1)) {
            return tokens.push(newPlainScalarToken(tokenBuffer.popToArray(), startMark, endMark.mark()))
          }
        }

        // If we hit a `#` character AND we were preceded by a whitespace or
        // line breaks
        isPound() && (trailingWS.isNotEmpty || lineBreaks.isNotEmpty) -> {
          // Then we've found the start of a comment, so wrap up our scalar.
          return tokens.push(newPlainScalarToken(tokenBuffer.popToArray(), startMark, endMark.mark()))
        }
      }

      // If we didn't hit `continue` or `return` above, then we can just append
      // it to the token value buffer.

      // If there were no line breaks in the pile of whitespaces, then we
      // only may have trailing whitespace characters:
      if (lineBreaks.isEmpty) {
        tokenBuffer.takeFrom(trailingWS)
      } else {
        // Ignore the first line break
        lineBreaks.skip(lineBreaks.utf8Width())

        // If there was only one line break, convert it to a single space
        if (lineBreaks.isEmpty) {
          tokenBuffer.push(A_SPACE)
        }

        // If there were additional line breaks then append them to the
        // token buffer
        else {
          while (lineBreaks.isNotEmpty)
            tokenBuffer.claimNewLine(lineBreaks)
        }

        // TODO:
        //   | Before popping the next character from the reader to the token
        //   | buffer:
        //   |
        //   | What are the limitations or rules placed on what characters are
        //   | allowed in a plain scalar?  Do they have to be displayable?
        //   |
        //   | We're gonna take the codepoint no matter what, but we should emit
        //   | a warning about the invalid characters, and potentially escape
        //   | control characters?

        tokenBuffer.claimUTF8()
        endMark.become(position)
      }
    }
  }

  // region Directive Tokens

  /**
   * Attempts to parse the rest of the current line as a directive.
   *
   * At the time this function is called, all we know is that current reader
   * buffer character is `%`.  This could be the start of a YAML directive, a
   * tag directive, or just junk.
   */
  private fun fetchDirectiveToken() {
    // TODO:
    //  | if the percent symbol is not at the beginning of the line then it is
    //  | invalid.

    // Record the start position
    val startMark = position.mark()

    // Skip the `%` character.
    skipASCII()

    // Attempt to load 5 codepoints into the reader buffer so we can do the
    // following tests.
    cache(5)

    // Nothing more in the buffer?  That means the stream ended on a `%`
    // character which means an invalid token directive.
    if (!haveMoreInBuffer)
      throw YAMLScannerException("stream ended on an incomplete or invalid directive", startMark)

    // See if the next 5 characters are "YAML<WS>"
    if (check(A_UP_Y, A_UP_A, A_UP_M, A_UP_L) && isBlank(4)) {
      skipASCII(5)
      return fetchYAMLDirectiveToken(startMark)
    }

    // See if the next 4 characters are "TAG<WS>"
    if (check(A_UP_T, A_UP_A, A_UP_G) && isBlank(3)) {
      skipASCII(4)
      return fetchTagDirectiveToken(startMark)
    }

    // If it's not YAML or TAG then it's invalid
    return fetchInvalidDirectiveToken(startMark)
  }

  /**
   * Attempts to parse the rest of the current line as a YAML directive.
   *
   * At the time this function is called, we know that we have seen what appears
   * to be the beginning of a valid `%YAML` directive.
   *
   * The next step is to try and parse the version value which _should_ be one
   * of `1.1` or `1.2` but it could be anything because input be weird
   * sometimes.
   *
   * @param startMark Mark of the start position of the token.
   */
  private fun fetchYAMLDirectiveToken(startMark: SourcePosition) {
    // We have already skipped over `%YAML<WS>`.  Eat any extra whitespaces
    // until we encounter something else, which will hopefully be a decimal
    // digit.
    eatBlanks()

    var major = 0u
    var minor = 0u
    var width = 0

    val versionStartMark = position.mark()

    while (true) {
      cache(1)

      if (isDecimal()) {
        // If this is a decimal digit then we have to append it to our running
        // major value.
        //
        // Since we are parsing a series of decimal digits of an unknown size,
        // it is possible that the input contains a value that would overflow
        // the UInt data type and cause undefined behavior.
        //
        // This means we have to do overflow detection to catch that case.

        // Overflow detection:
        //
        // Verify that we can safely multiply our major version by 10 to
        // accommodate the next digit we will be adding.
        if (major > UInt.MAX_VALUE / 10u)
          TODO("we overflowed the UInt value attempting to parse the major version, this is a malformed token. assume version 1.2")

        major *= 10u

        val add = asDecimal()

        // Overflow detection:
        //
        // Verify we can safely add the next digit to our major version.
        if (major > UInt.MAX_VALUE - add)
          TODO("we overflowed the UInt value attempting to parse the major version, this is a malformed token. assume version 1.2")

        major += add
      }

      else if (isPeriod()) {
        // Skip the period character and break from the loop to move on to
        // parsing the minor version.
        skipASCII()
        break
      }

      else {
        TODO("we've ")
      }
    }



  }


  private fun fetchTagDirectiveToken(startMark: SourcePosition) {
    TODO()
  }

  private fun fetchInvalidDirectiveToken(startMark: SourcePosition)

  private fun fetchInvalidTagDirectiveToken(startMark: SourcePosition) {
    warn("malformed %TAG token", startMark)
    finishInvalidDirectiveToken(startMark)
  }

  private fun finishInvalidDirectiveToken(startMark: SourcePosition) {
    TODO("eat the rest of the line (or find a comment) then return an invalid token")
  }

  // endregion Directive Tokens

  // region Ambiguous Character Tokens

  private fun fetchDashToken()

  private fun fetchPeriodToken()

  private fun fetchColonToken()

  private fun fetchAtToken()

  private fun fetchGraveToken()

  private fun fetchTildeToken()

  private fun fetchQuestionToken()

  // endregion Ambiguous Character Tokens

  /**
   * Skips over whitespace characters in the reader buffer, incrementing the
   * position tracker as it does.
   */
  private fun eatBlanks() {
    cache(1)
    while (isBlank()) {
      skipASCII()
      cache(1)
    }
  }

  // region Warning Helpers

  private fun warn(message: String, mark: SourcePosition = position.mark()) {
    warnings.push(ScannerWarning(message, mark))
  }

  // endregion Warning Helpers

  // region Buffer Writing Helpers

  private fun UByteBuffer.claimASCII() {
    push(reader.pop())
    position.incPosition()
  }

  private fun UByteBuffer.claimUTF8() {
    if (!takeCodepointFrom(reader.utf8Buffer))
      throw IllegalStateException("invalid utf-8 codepoint in the reader buffer or buffer is offset")
    position.incPosition()
  }

  private fun UByteBuffer.claimNewLine() {
    reader.cache(4)
    claimNewLine(reader.utf8Buffer)
  }

  private fun UByteBuffer.claimNewLine(from: UByteBuffer) {
    if (from.isCRLF()) {
      appendNewLine(NL.CRLF)
      skipLine(NL.CRLF)
    } else if (from.isCR()) {
      appendNewLine(NL.CR)
      skipLine(NL.CR)
    } else if (from.isLF()) {
      appendNewLine(NL.LF)
      skipLine(NL.LF)
    } else if (from.isNEL()) {
      appendNewLine(NL.NEL)
      skipLine(NL.NEL)
    } else if (from.isLS()) {
      appendNewLine(NL.LS)
      skipLine(NL.LS)
    } else if (from.isPS()) {
      appendNewLine(NL.PS)
      skipLine(NL.PS)
    } else {
      throw IllegalStateException("called #claimNewLine() when the reader was not on a new line character")
    }
  }

  private fun UByteBuffer.appendNewLine(nl: NL) {
    when (lineBreakType) {
      LineBreakType.CRLF        -> NL.CRLF.writeUTF8(this)
      LineBreakType.CR          -> NL.CR.writeUTF8(this)
      LineBreakType.LF          -> NL.LF.writeUTF8(this)
      LineBreakType.SameAsInput -> nl.writeUTF8(this)
    }
  }

  // endregion Buffer Writing Helpers

  // region Reader Helpers

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
  private inline fun cache(codepoints: Int) = reader.cache(codepoints)

  /**
   * Skips over the given number of ASCII characters in the reader buffer and
   * update the position tracker.
   *
   * @param count Number of ASCII characters (or bytes) in the reader buffer to
   * skip.  This is also the amount that the current column index is increased
   * by.
   */
  private fun skipASCII(count: Int = 1) {
    reader.skip(count)
    position.incPosition(count.toUInt())
  }

  private fun skipUTF8(count: Int = 1) {
    reader.skipCodepoints(count)
    position.incPosition(count.toUInt())
  }

  private fun skipLine() {
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

  private fun skipLine(nl: NL) {
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
        // CRinternal fun newPlainScalarYAMLToken()
        // LF
        else -> { /* nothing special to for these line breaks */ }
      }
    }

    reader.skip(nl.width)
    position.incLine(nl.characters.toUInt())
  }

  // endregion Reader Helpers

  // region Reader Tests

  private inline fun check(octet: UByte, offset: Int = 0) = reader.check(octet, offset)

  private inline fun check(octet1: UByte, octet2: UByte) =
    reader.buffered > 1 && reader.uCheck(octet1, 0) && reader.uCheck(octet2, 1)

  private inline fun check(octet1: UByte, octet2: UByte, octet3: UByte) =
    reader.buffered > 2 && reader.uCheck(octet1, 0) && reader.uCheck(octet2, 1) && reader.uCheck(octet3, 2)

  private inline fun check(octet1: UByte, octet2: UByte, octet3: UByte, octet4: UByte) =
    reader.buffered > 3
      && reader.uCheck(octet1, 0)
      && reader.uCheck(octet2, 1)
      && reader.uCheck(octet3, 2)
      && reader.uCheck(octet4, 3)

  private inline fun asDecimal(offset: Int = 0) = reader.asDecDigit(offset)

  private inline fun isAnyBreak  (offset: Int = 0) = reader.isBreak_1_1(offset)
  private inline fun isBlank     (offset: Int = 0) = reader.isBlank(offset)
  private inline fun isBreakOrEOF(offset: Int = 0) = reader.isBreakOrEOF(offset)
  private inline fun isColon     (offset: Int = 0) = reader.isColon(offset)
  private inline fun isComma     (offset: Int = 0) = reader.isComma(offset)
  private inline fun isCR        (offset: Int = 0) = reader.isCR(offset)
  private inline fun isCRLF      (offset: Int = 0) = reader.isCRLF(offset)
  private inline fun isDecimal   (offset: Int = 0) = reader.isDecDigit(offset)
  private inline fun isEOF       (offset: Int = 0) = reader.isEOF(offset)
  private inline fun isLF        (offset: Int = 0) = reader.isLF(offset)
  private inline fun isLS        (offset: Int = 0) = reader.isLS(offset)
  private inline fun isNEL       (offset: Int = 0) = reader.isNEL(offset)
  private inline fun isPound     (offset: Int = 0) = reader.isPound(offset)
  private inline fun isPeriod    (offset: Int = 0) = reader.isPeriod(offset)
  private inline fun isPS        (offset: Int = 0) = reader.isPS(offset)
  private inline fun isQuestion  (offset: Int = 0) = reader.isQuestion(offset)



  // endregion Reader Tests

  // region Token Constructors

  @Suppress("NOTHING_TO_INLINE")
  @OptIn(ExperimentalUnsignedTypes::class)
  private inline fun newPlainScalarToken(value: UByteArray, start: SourcePosition, end: SourcePosition) =
    YAMLToken(YAMLTokenType.Scalar, YAMLTokenDataScalar(value, YAMLScalarStyle.Plain), start, end, warnings.popToArray())

  // endregion Token Constructors
}