package io.foxcapades.lib.k.yaml.scan

import io.foxcapades.lib.k.yaml.DefaultYAMLVersion
import io.foxcapades.lib.k.yaml.YAMLVersion
import io.foxcapades.lib.k.yaml.bytes.*
import io.foxcapades.lib.k.yaml.err.UIntOverflowException
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
  private val warnings = Queue<SourceWarning>()

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

  private inline val haveMoreCharactersAvailable
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

  fun nextWarning(): SourceWarning = warnings.pop()

  private fun fetchNextToken() {
    if (!streamStartProduced)
      return fetchStreamStartToken()

    skipToNextToken()

    cache(1)

    if (!haveMoreCharactersAvailable)
      return fetchStreamEndToken()

    when {
      // Good boy characters: % - . # & * [ ] { } | : ' " > , ?
      uIsPercent() && atStartOfLine -> fetchDirectiveToken()
      uIsDash()                     -> fetchDashToken()
      uIsPeriod()                   -> fetchPeriodToken()
      uIsPound()                    -> fetchCommentToken()
      uIsAmp()                      -> fetchAnchorToken()
      uIsAsterisk()                 -> fetchAliasToken()
    }

    when (reader[0]) {
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
      if (!haveMoreCharactersAvailable)
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
          if (isBlankBreakOrEOF(1)) {
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
    if (!haveMoreCharactersAvailable)
      throw YAMLScannerException("stream ended on an incomplete or invalid directive", startMark)

    // See if the next 5 characters are "YAML<WS>"
    if (testReaderOctets(A_UP_Y, A_UP_A, A_UP_M, A_UP_L) && isBlank(4)) {
      skipASCII(5)
      return fetchYAMLDirectiveToken(startMark)
    }

    // See if the next 4 characters are "TAG<WS>"
    if (testReaderOctets(A_UP_T, A_UP_A, A_UP_G) && isBlank(3)) {
      skipASCII(4)
      return fetchTagDirectiveToken(startMark)
    }

    // If it's not YAML or TAG then it's invalid
    return fetchInvalidDirectiveToken(startMark)
  }

  // region YAML Directive Token

  /**
   * # Fetch `YAML-DIRECTIVE` Token
   *
   * Attempts to parse the rest of the current line as a `YAML-DIRECTIVE` token
   * assuming the start of the line is `%YAML<WS>`.
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
    var trailingSpaceCount = eatBlanks()

    // If after skipping over the blank space characters after `%YAML` we hit
    // the EOF, a line break, or a `#` character (the start of a comment), then
    // we have an incomplete token because there can be no version number
    // following on this line.
    if (isPound() || isBreakOrEOF())
      return fetchIncompleteYAMLDirectiveToken(
        startMark,
        position.mark(modIndex = -trailingSpaceCount, modColumn = -trailingSpaceCount)
      )

    // If the next character we see is not a decimal digit, then we've got some
    // junk characters instead of a version number.
    if (!isDecimal())
      return fetchMalformedYAMLDirectiveToken(startMark)

    // Okay, so we are on a decimal digit, that is _hopefully_ the start of our
    // major version.

    // Try and parse the major version as a UInt from the stream.
    val major = try {
      parseUInt()
    } catch (e: UIntOverflowException) {
      version = DefaultYAMLVersion
      return fetchOverflowYAMLDirectiveToken(startMark, e.startMark, true)
    }

    // Now we have to ensure that the value right after the major version int
    // value is a period character.

    cache(1)
    if (isBreakOrEOF())
      return fetchIncompleteYAMLDirectiveToken(startMark, position.mark())
    if (!isPeriod())
      return fetchMalformedYAMLDirectiveToken(startMark)

    // Skip the `.` character.
    skipASCII()

    cache(1)
    if (isBreakOrEOF())
      return fetchIncompleteYAMLDirectiveToken(startMark, position.mark())
    if (!isDecimal())
      return fetchMalformedYAMLDirectiveToken(startMark)

    val minor = try {
      parseUInt()
    } catch (e: UIntOverflowException) {
      version = DefaultYAMLVersion
      return fetchOverflowYAMLDirectiveToken(startMark, e.startMark, false)
    }

    // Okay, so at this point we have parsed the following:
    //
    //   ^%YAML[ \t]+\d+\.\d+
    //
    // Now we need to make sure that there is nothing else on this line except
    // for maybe trailing whitespace characters and optionally a comment.

    cache(1)
    trailingSpaceCount = 0

    // If the next character after the minor version int is a whitespace
    // character:
    if (isBlank()) {
      // Eat the whitespace(s) until we hit something else.
      trailingSpaceCount = eatBlanks()

      // Attempt to cache a character in our reader buffer
      cache(1)

      // If the next character after the whitespace(s) is NOT a `#`, is NOT a
      // line break, and is NOT the EOF, then we have some extra junk at the
      // end of our token line and the token is considered malformed.
      if (!(isPound() || isBreakOrEOF()))
        return fetchMalformedYAMLDirectiveToken(startMark)
    }

    // Else (meaning we're not at a whitespace), if the next thing in the reader
    // buffer is NOT a line break and is NOT the EOF, then we have extra junk
    // right after our minor version number, meaning the token is malformed.
    else if (!isBreakOrEOF()) {
      return fetchMalformedYAMLDirectiveToken(startMark)
    }

    // If we've made it this far then we have parsed the following:
    //
    //   ^%YAML[ \t]+\d+\.\d+[ \t]*$
    //
    // This means that we have a valid _looking_ YAML directive.  We now need to
    // verify that the major and minor version numbers that the input gave us
    // actually amount to a valid YAML version.

    val endMark = position.mark(modIndex = -trailingSpaceCount, modColumn = -trailingSpaceCount)

    // If the major version is not `1`, then we should emit a warning for an
    // unsupported version and attempt to parse it as the default YAML version.
    if (major != 1u)
      return fetchUnsupportedYAMLDirectiveToken(major, minor, startMark, endMark)

    if (minor == 1u)
      version = YAMLVersion.VERSION_1_1
    else if (minor == 2u)
      version = YAMLVersion.VERSION_1_2
    else
      return fetchUnsupportedYAMLDirectiveToken(major, minor, startMark, endMark)

    tokens.push(newYAMLDirectiveToken(major, minor, startMark, endMark))
  }

  /**
   * # Fetch Invalid YAML Token for UInt Overflow
   *
   * Emits a warning, then fetches an `<INVALID>` token for the situation where
   * we were attempting to parse a part of the version number segment of a
   * `%YAML` token and either the major or minor version number overflows a the
   * `uint32` type.
   *
   * ## Generating the Warning
   *
   * The primary purpose of this function is to generate a helpful warning
   * before passing the invalid token up to the caller.  This warning is
   * generated by counting the number digits in the total major/minor version
   * number as it appears in the source YAML to provide a start and end mark for
   * the number value that would have overflowed our `uint32`.
   *
   * This allows consumers of these tokens to generate markup to render errors
   * with context hints such as underlining the invalid value.
   *
   * **Example Error Hint**
   * ```log
   * WARN: uint32 overflow while attempting to parse a %YAML directive major version
   * ----
   * %YAML 99999999999999999999.1 # this is an invalid YAML version
   *       ^^^^^^^^^^^^^^^^^^^^
   * ----
   * ```
   *
   * ## Generating the Invalid Token
   *
   * This function simply calls out to [finishInvalidDirectiveToken] to wrap up
   * finding the end of the invalid token.
   *
   * @param tokenStartMark Mark for the beginning of the `%YAML` token in the
   * source stream.  This value is passed through to
   * [finishInvalidDirectiveToken].
   *
   * @param intStartMark Mark for the beginning of the `int` value that is too
   * long to be a `uint32`.  This value is used for creating the warning which
   * will have the start and end marks for the invalid `int` value.
   *
   * @param isMajor Whether this was the major section of the version number or
   * not.  `true` if it was the major version, `false` if it was the minor
   * version.
   */
  private fun fetchOverflowYAMLDirectiveToken(
    tokenStartMark: SourcePosition,
    intStartMark:   SourcePosition,
    isMajor:        Boolean,
  ) {
    // Ensure that we have a character in the buffer to test against.
    cache(1)

    // Skip over all the decimal digit characters until we hit the end of this
    // absurdly long int value.
    while (isDecimal()) {
      // Skip it as ASCII because if it's a decimal digit then we know it's an
      // ASCII character
      skipASCII()

      // Cache another character to test on the next pass of the loop
      cache(1)
    }

    // Emit a warning about the overflow, passing in:
    //
    // - A warning message that is slightly customized based on whether it is a
    //   major or minor version number that was overflowed,
    // - A mark for the start of the absurdly long int value
    // - The current position (which will be the first non-digit character after
    //   the start of the long int, or the EOF
    warn(
      "uint32 overflow while attempting to parse a %YAML directive ${if (isMajor) "major" else "minor"} version",
      intStartMark,
      position.mark()
    )

    // Skip to the end of the directive
    var trailingWS = 0
    val endMark: SourcePosition
    while (true) {
      cache(1)
      if (isBlank()) {
        trailingWS++
        skipASCII()
      } else if (isBreakOrEOF()) {
        endMark = position.mark(modIndex = -trailingWS, modColumn = -trailingWS)
        break
      } else if (isPound() && trailingWS > 0) {
        endMark = position.mark(modIndex = -trailingWS, modColumn = -trailingWS)
        break
      } else {
        trailingWS = 0
        skipUTF8()
      }
    }

    tokens.push(newInvalidToken(tokenStartMark, endMark))
  }

  /**
   * Fetches an [`INVALID`][YAMLTokenType.Invalid] token for the case where the
   * YAML directive token contains some junk characters.
   *
   * When called, the reader buffer cursor will be on the first junk character.
   *
   * The job of this method is to find the end of the junk on the line and
   * create a warning highlighting that junk before queueing up an `INVALID`
   * token and returning.
   */
  private fun fetchMalformedYAMLDirectiveToken(tokenStartMark: SourcePosition) {
    val junkStart = position.mark()
    val junkEnd: SourcePosition

    var trailingWhitespace = 0

    while (true) {
      cache(1)

      if (isBlank()) {
        trailingWhitespace++
        skipASCII()
      } else if (isBreakOrEOF()) {
        junkEnd = position.mark(modIndex = -trailingWhitespace, modColumn = -trailingWhitespace)
        break
      } else if (isPound() && trailingWhitespace > 0) {
        junkEnd = position.mark(modIndex = -trailingWhitespace, modColumn = -trailingWhitespace)
        break
      } else {
        trailingWhitespace = 0
        skipUTF8()
      }
    }

    warn("malformed %YAML directive", junkStart, junkEnd)
    tokens.push(newInvalidToken(tokenStartMark, junkEnd))
  }

  private fun fetchIncompleteYAMLDirectiveToken(start: SourcePosition, end: SourcePosition) {
    version = DefaultYAMLVersion
    warn("incomplete %YAML directive; assuming YAML version $DefaultYAMLVersion", start, end)
    tokens.push(newInvalidToken(start, end))
  }

  private fun fetchUnsupportedYAMLDirectiveToken(
    major: UInt,
    minor: UInt,
    start: SourcePosition,
    end:   SourcePosition
  ) {
    version = DefaultYAMLVersion
    warn("unsupported YAML version $major.$minor; attempting to scan input as YAML version $DefaultYAMLVersion")
    tokens.push(newYAMLDirectiveToken(major, minor, start, end))
  }

  // endregion YAML Directive Token

  // region Tag Directive Token

  private fun fetchTagDirectiveToken(startMark: SourcePosition) {
    TODO("fetch tag directive token")
  }

  private fun fetchInvalidTagDirectiveToken(startMark: SourcePosition) {
    warn("malformed %TAG token", startMark)
    finishInvalidDirectiveToken(startMark)
  }

  // endregion Tag Directive Token

  private fun fetchInvalidDirectiveToken(startMark: SourcePosition) {
    TODO("fetch invalid directive token")
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

  private fun parseUInt(): UInt {
    val intStart = position.mark()
    var intValue = 0u
    var addValue: UInt

    while (true) {
      cache(1)

      if (isDecimal()) {
        if (intValue > UInt.MAX_VALUE / 10u)
          throw UIntOverflowException(intStart)

        intValue *= 10u
        addValue = asDecimal()

        if (intValue > UInt.MAX_VALUE - addValue)
          throw UIntOverflowException(intStart)

        intValue += addValue
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
  private fun eatBlanks(): Int {
    var out = 0

    cache(1)
    while (isBlank()) {
      skipASCII()
      cache(1)
      out++
    }

    return out
  }

  // region Warning Helpers

  private fun warn(
    message: String,
    start:   SourcePosition = position.mark(),
    end:     SourcePosition = position.mark(),
  ) {
    warnings.push(SourceWarning(message, start, end))
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

  // region Size Checking

  private inline fun hasOffset(offset: Int) = reader.buffered > offset

  // endregion Size Checking

  // region Octet Checking

  // region Safe Octet Checking

  /**
   * Tests the `UByte` at the given [offset] in the [reader] against the given
   * [octet] value to see if they are equal.
   *
   * If reader buffer does not contain enough characters to contain [offset],
   * this method returns `false`.
   *
   * **Examples**
   * ```
   * // Given the following reader buffer:
   * // YAMLReader('A', 'B', 'C')
   *
   * // The following will return true
   * testReaderOctet('A')
   * testReaderOctet('B', 1)
   * testReaderOctet('C', 2)
   *
   * // And the following will return false
   * testReader('D', 3)  // false because the reader buffer does not contain
   *                     // offset 3 (would require 4 characters)
   * testReader('B')     // false because reader[0] != 'B'
   * ```
   *
   * @param octet Octet to compare the [reader] value to.
   *
   * @param offset Offset in the reader buffer of the `UByte` value to test.
   *
   * @return `true` if the reader buffer contains enough characters to have a
   * value at [offset] and the value at `offset` is equal to the given [octet]
   * value.  `false` if the reader buffer does not contain enough characters to
   * contain `offset` or if the value at `offset` in the reader buffer is not
   * equal to the given `octet` value.
   */
  private inline fun testReaderOctet(octet: UByte, offset: Int = 0) =
    hasOffset(offset) && unsafeTestReaderOctet(octet, offset)

  /**
   * Tests the `UByte` values at offsets `0` and `1` in the reader buffer
   * against the given octet values to see if they are equal.
   */
  private inline fun testReaderOctets(octet1: UByte, octet2: UByte) =
    hasOffset(1) && unsafeTestReaderOctet(octet1) && unsafeTestReaderOctet(octet2, 1)

  /**
   * Tests the `UByte` values at offsets `0`, `1`, and `2` in the reader buffer
   * against the given octet values to see if they are equal.
   */
  private inline fun testReaderOctets(octet1: UByte, octet2: UByte, octet3: UByte) =
    hasOffset(2)
      && unsafeTestReaderOctet(octet1)
      && unsafeTestReaderOctet(octet2, 1)
      && unsafeTestReaderOctet(octet3, 2)

  /**
   * Tests the `UByte` values at offsets `0`, `1`, `2`, and `3` in the reader
   * buffer against the given octet values to see if they are equal.
   */
  private inline fun testReaderOctets(octet1: UByte, octet2: UByte, octet3: UByte, octet4: UByte) =
    hasOffset(3)
      && unsafeTestReaderOctet(octet1)
      && unsafeTestReaderOctet(octet2, 1)
      && unsafeTestReaderOctet(octet3, 2)
      && unsafeTestReaderOctet(octet4, 3)

  // endregion Safe Octet Checking

  // region Unsafe Octet Checking

  private inline fun unsafeTestReaderOctet(octet: UByte, offset: Int = 0) = reader[offset] == octet

  // endregion Unsafe Octet Checking

  // endregion Octet Checking

  private inline fun isDecimal(offset: Int = 0) = reader.isDecDigit(offset)
  private inline fun asDecimal(offset: Int = 0) = reader.asDecDigit(offset)

  /** `<CR> | <LF> | <NEL> | <LS> | <PS>` */
  private inline fun isAnyBreak(offset: Int = 0) = reader.isBreak_1_1(offset)

  /** `<SPACE> | <TAB>` */
  private inline fun isBlank(offset: Int = 0) = reader.isBlank(offset)

  /** `SPACE | TAB | CR | LF | NEL | LS | PS` */
  private inline fun isBlankOrBreak(offset: Int = 0) = reader.isBlankOrBreak(offset)

  /** `SPACE | TAB | CR | LF | NEL | LS | PS` or `EOF` */
  private inline fun isBlankBreakOrEOF(offset: Int = 0) = reader.isBlankBreakOrEOF(offset)

  private inline fun isBreakOrEOF(offset: Int = 0) = reader.isBreakOrEOF(offset)
  private inline fun isEOF(offset: Int = 0) = reader.isEOF(offset)

  private inline fun isCRLF      (offset: Int = 0) = reader.isCRLF(offset)

  // region Single Character Tests

  // region Safe Tests

  private inline fun isAmp        (offset: Int = 0) = reader.isAmp(offset)
  private inline fun isAsterisk   (offset: Int = 0) = reader.isAsterisk(offset)
  private inline fun isBackslash  (offset: Int = 0) = reader.isBackslash(offset)
  private inline fun isColon      (offset: Int = 0) = reader.isColon(offset)
  private inline fun isComma      (offset: Int = 0) = reader.isComma(offset)
  private inline fun isCR         (offset: Int = 0) = reader.isCR(offset)
  private inline fun isCurlyClose (offset: Int = 0) = reader.isCurlyClose(offset)
  private inline fun isCurlyOpen  (offset: Int = 0) = reader.isCurlyOpen(offset)
  private inline fun isDash       (offset: Int = 0) = reader.isDash(offset)
  private inline fun isLF         (offset: Int = 0) = reader.isLF(offset)
  private inline fun isLS         (offset: Int = 0) = reader.isLS(offset)
  private inline fun isNEL        (offset: Int = 0) = reader.isNEL(offset)
  private inline fun isPercent    (offset: Int = 0) = reader.isPercent(offset)
  private inline fun isPeriod     (offset: Int = 0) = reader.isPeriod(offset)
  private inline fun isPound      (offset: Int = 0) = reader.isPound(offset)
  private inline fun isPS         (offset: Int = 0) = reader.isPS(offset)
  private inline fun isQuestion   (offset: Int = 0) = reader.isQuestion(offset)
  private inline fun isSquareClose(offset: Int = 0) = reader.isSquareClose(offset)
  private inline fun isSquareOpen (offset: Int = 0) = reader.isSquareOpen(offset)

  // endregion Safe Tests

  // region Unsafe Tests

  private inline fun uIsAmp(offset: Int = 0) = reader.uIsAmp(offset)
  private inline fun uIsAsterisk(offset: Int = 0) = reader.uIsAsterisk(offset)
  private inline fun uIsColon(offset: Int = 0) = reader.uIsColon(offset)
  private inline fun uIsComma(offset: Int = 0) = reader.uIsComma(offset)
  private inline fun uIsCurlyClose (offset: Int = 0) = reader.uIsCurlyClose(offset)
  private inline fun uIsCurlyOpen  (offset: Int = 0) = reader.uIsCurlyOpen(offset)
  private inline fun uIsDash(offset: Int = 0) = reader.uIsDash(offset)
  private inline fun uIsPercent(offset: Int = 0) = reader.uIsPercent(offset)
  private inline fun uIsPeriod(offset: Int = 0) = reader.uIsPeriod(offset)
  private inline fun uIsPound(offset: Int = 0) = reader.uIsPound(offset)

  // endregion Unsafe Tests


  // endregion Single Character Tests

  // endregion Reader Tests

  // region Token Constructors

  @OptIn(ExperimentalUnsignedTypes::class)
  private inline fun newPlainScalarToken(value: UByteArray, start: SourcePosition, end: SourcePosition) =
    YAMLToken(YAMLTokenType.Scalar, YAMLTokenDataScalar(value, YAMLScalarStyle.Plain), start, end, warnings.popToArray())

  private inline fun newInvalidToken(start: SourcePosition, end: SourcePosition) =
    YAMLToken(YAMLTokenType.Invalid, null, start, end, warnings.popToArray())

  private inline fun newYAMLDirectiveToken(major: UInt, minor: UInt, start: SourcePosition, end: SourcePosition) =
    YAMLToken(YAMLTokenType.VersionDirective, YAMLTokenDataVersionDirective(major, minor), start, end, warnings.popToArray())

  // endregion Token Constructors
}