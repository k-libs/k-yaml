package io.foxcapades.lib.k.yaml.scan

import io.foxcapades.lib.k.yaml.DefaultYAMLVersion
import io.foxcapades.lib.k.yaml.YAMLVersion
import io.foxcapades.lib.k.yaml.bytes.*
import io.foxcapades.lib.k.yaml.err.UIntOverflowException
import io.foxcapades.lib.k.yaml.err.YAMLScannerException
import io.foxcapades.lib.k.yaml.read.*
import io.foxcapades.lib.k.yaml.util.*

@Suppress("NOTHING_TO_INLINE")
internal class YAMLScannerImpl : YAMLScanner {

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

  internal val reader: YAMLReaderBuffer

  internal val lineBreakType: LineBreakType

  constructor(reader: YAMLReaderBuffer, lineBreak: LineBreakType) {
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
    } else if (reader.isCarriageReturn()) {
      skipLine(NL.CR)
    } else if (reader.isLineFeed()) {
      skipLine(NL.LF)
    } else if (reader.isNewLine()) {
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

  internal inline fun asDecimal(offset: Int = 0) = reader.asDecimalDigit(offset)

  // endregion Reader Wrapping

  private fun fetchNextToken() {
    if (!streamStartProduced)
      return fetchStreamStartToken()

    // TODO: SKIP TO NEXT TOKEN NEEDS TO HANDLE INDENT TAB DETECTION
    skipToNextToken()

    reader.cache(1)

    if (!haveMoreCharactersAvailable)
      return fetchStreamEndToken()

    when {
      // Good boy characters: % - . # & * [ ] { } | : ' " > , ?
      reader.uIsPercent()     -> fetchDirectiveToken()
      reader.uIsDash()        -> fetchAmbiguousDashToken()
      reader.uIsPeriod()      -> fetchAmbiguousPeriodToken()
      reader.uIsPound()       -> fetchCommentToken()
      reader.uIsAmpersand()   -> fetchAnchorToken()
      reader.uIsAsterisk()    -> fetchAliasToken()
      reader.uIsSquareOpen()  -> fetchFlowSequenceStartToken()
      reader.uIsSquareClose() -> fetchFlowSequenceEndToken()
      reader.uIsCurlyOpen()   -> fetchFlowMappingStartToken()
      reader.uIsCurlyClose()  -> fetchFlowMappingEndToken()
      reader.uIsPipe()        -> fetchLiteralStringToken()
      reader.uIsColon()       -> fetchAmbiguousColonToken()
      reader.uIsApostrophe()  -> fetchSingleQuotedStringToken()
      reader.uIsDoubleQuote() -> fetchDoubleQuotedStringToken()
      reader.uIsGreaterThan() -> fetchFoldedStringToken()
      reader.uIsComma()       -> fetchFlowItemSeparatorToken()
      reader.uIsQuestion()    -> fetchAmbiguousQuestionToken()

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
      reader.cache(1)

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
    if (!takeCodepointFrom(reader.utf8Buffer))
      throw IllegalStateException("invalid utf-8 codepoint in the reader buffer or buffer is offset")
    position.incPosition()
  }

  internal fun UByteContainer.detectNewLineType() =
    when {
      isLineFeed()           -> NL.LF
      isCRLF()               -> NL.CRLF
      isCarriageReturn()     -> NL.CR
      isNewLine()            -> NL.NEL
      isLineSeparator()      -> NL.LS
      isParagraphSeparator() -> NL.PS
      else                   -> throw IllegalStateException(
        "called #detectNewLineType when the reader was not on a new line character"
      )
    }

  internal fun UByteBuffer.claimNewLine(from: UByteBuffer, position: SourcePositionTracker) {
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
    } else if (from.isNewLine()) {
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

  // region Token Fetching

  // region Stream Indicators

  internal fun fetchStreamStartToken() {
    reader.cache(1)
    streamStartProduced = true
    val mark = position.mark()
    tokens.push(newStreamStartToken(reader.encoding, mark, mark))
  }

  internal fun fetchStreamEndToken() {
    val mark = position.mark()
    tokens.push(newStreamEndToken(mark, mark))
  }

  // endregion Stream Indicators

  // region Directives

  /**
   * Attempts to parse the rest of the current line as a directive.
   *
   * At the time this function is called, all we know is that current reader
   * buffer character is `%`.  This could be the start of a YAML directive, a
   * tag directive, or just junk.
   */
  internal fun fetchDirectiveToken() {
    // Record the start position
    val startMark = position.mark()

    if (!atStartOfLine) {
      tokens.push(newInvalidToken(startMark, skipUntilCommentBreakOrEOF()))
      return
    }

    // Skip the `%` character.
    skipASCII()

    // Attempt to load 5 codepoints into the reader buffer so we can do the
    // following tests.
    reader.cache(5)

    // Nothing more in the buffer?  That means the stream ended on a `%`
    // character which means an invalid token directive.
    if (!haveMoreCharactersAvailable)
      throw YAMLScannerException("stream ended on an incomplete or invalid directive", startMark)

    // See if the next 5 characters are "YAML<WS>"
    if (testReaderOctets(A_UPPER_Y, A_UPPER_A, A_UPPER_M, A_UPPER_L) && haveBlank(4)) {
      skipASCII(5)
      return fetchYAMLDirectiveToken(startMark)
    }

    // See if the next 4 characters are "TAG<WS>"
    if (testReaderOctets(A_UPPER_T, A_UPPER_A, A_UPPER_G) && haveBlank(3)) {
      skipASCII(4)
      return fetchTagDirectiveToken(startMark)
    }

    // If it's not YAML or TAG then it's invalid
    tokens.push(newInvalidToken(startMark, skipUntilCommentBreakOrEOF()))
  }

  // region YAML Directive

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
  internal fun fetchYAMLDirectiveToken(startMark: SourcePosition) {
    // We have already skipped over `%YAML<WS>`.  Eat any extra whitespaces
    // until we encounter something else, which will hopefully be a decimal
    // digit.
    var trailingSpaceCount = eatBlanks()

    // If after skipping over the blank space characters after `%YAML` we hit
    // the EOF, a line break, or a `#` character (the start of a comment), then
    // we have an incomplete token because there can be no version number
    // following on this line.
    if (reader.isPound() || haveAnyBreakOrEOF())
      return fetchIncompleteYAMLDirectiveToken(
        startMark,
        position.mark(modIndex = -trailingSpaceCount, modColumn = -trailingSpaceCount)
      )

    // If the next character we see is not a decimal digit, then we've got some
    // junk characters instead of a version number.
    if (!reader.isDecimalDigit())
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

    reader.cache(1)
    if (haveAnyBreakOrEOF())
      return fetchIncompleteYAMLDirectiveToken(startMark, position.mark())
    if (!reader.isPeriod())
      return fetchMalformedYAMLDirectiveToken(startMark)

    // Skip the `.` character.
    skipASCII()

    reader.cache(1)
    if (haveAnyBreakOrEOF())
      return fetchIncompleteYAMLDirectiveToken(startMark, position.mark())
    if (!reader.isDecimalDigit())
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
    // for maybe trailing whitespace characters and possibly a comment.

    reader.cache(1)
    trailingSpaceCount = 0

    // If the next character after the minor version int is a whitespace
    // character:
    if (reader.isBlank()) {
      // Eat the whitespace(s) until we hit something else.
      trailingSpaceCount = eatBlanks()

      // Attempt to cache a character in our reader buffer
      reader.cache(1)

      // If the next character after the whitespace(s) is NOT a `#`, is NOT a
      // line break, and is NOT the EOF, then we have some extra junk at the
      // end of our token line and the token is considered malformed.
      if (!(reader.isPound() || haveAnyBreakOrEOF()))
        return fetchMalformedYAMLDirectiveToken(startMark)
    }

    // Else (meaning we're not at a whitespace), if the next thing in the reader
    // buffer is NOT a line break and is NOT the EOF, then we have extra junk
    // right after our minor version number, meaning the token is malformed.
    else if (!haveAnyBreakOrEOF()) {
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
    intStartMark: SourcePosition,
    isMajor:        Boolean,
  ) {
    // Ensure that we have a character in the buffer to test against.
    reader.cache(1)

    // Skip over all the decimal digit characters until we hit the end of this
    // absurdly long int value.
    while (reader.isDecimalDigit()) {
      // Skip it as ASCII because if it's a decimal digit then we know it's an
      // ASCII character
      skipASCII()

      // Cache another character to test on the next pass of the loop
      reader.cache(1)
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

    tokens.push(newInvalidToken(tokenStartMark, skipUntilCommentBreakOrEOF()))
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
    val junkEnd   = skipUntilCommentBreakOrEOF()

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
    end: SourcePosition
  ) {
    version = DefaultYAMLVersion
    warn("unsupported YAML version $major.$minor; attempting to scan input as YAML version $DefaultYAMLVersion")
    tokens.push(newYAMLDirectiveToken(major, minor, start, end))
  }

  // endregion YAML Directive

  // region Tag Directive

  @OptIn(ExperimentalUnsignedTypes::class)
  internal fun fetchTagDirectiveToken(startMark: SourcePosition) {
    // At this point we've already skipped over `%TAG<WS>`.
    //
    // Skip over any additional blank spaces which will hopefully leave us at the
    // start of our tag handle.
    //
    // We add 1 to the space count to account for the one space we've already seen
    // before this function was called.
    var infixSpace = 1 + eatBlanks()

    // If, after skipping over the empty spaces, we hit a `#`, line break, or the
    // EOF, then we have an incomplete token.
    if (reader.isPound() || haveAnyBreakOrEOF())
      return fetchIncompleteTagDirectiveToken(startMark, position.mark(modIndex = -infixSpace, modColumn = -infixSpace))

    // If the next character is not an exclamation mark, then we have a malformed
    // Tag Directive.
    if (!reader.isExclamation())
      return fetchInvalidTagDirectiveToken("unexpected character that cannot start a tag handle", startMark)

    // So at this point, we have seen `%TAG !`.  Now we have to determine whether
    // this is a primary tag handle (`!`), a secondary tag handle (`!!`), or a
    // named tag handle (`!<ns-word-char>!`).

    // Create a buffer to store our handle value in.
    val handleBuffer = UByteBuffer(16)

    // Claim the first `!` character from the reader into our buffer.
    handleBuffer.takeASCIIFrom(1, reader, position)

    while (true) {
      reader.cache(1)

      // If we have our ending exclamation mark:
      if (reader.isExclamation()) {
        // eat it
        handleBuffer.takeASCIIFrom(1, reader, position)
        // break out of the loop because we are done with the handle
        break
      }

      if (havePercent()) {
        reader.cache(3)

        if (reader.isHexDigit(1) && reader.isHexDigit(2)) {
          handleBuffer.takeASCIIFrom(3, reader, position)
          continue
        }

        // So it was a `%` character followed by something other than 2 decimal
        // digits.
        return fetchInvalidTagDirectiveToken(
          "invalid URI escape; '%' character not followed by 2 hex digits",
          startMark
        )
      }

      if (haveBlankAnyBreakOrEOF())
        return fetchIncompleteTagDirectiveToken(startMark, position.mark())

      // TODO: should we recover by converting the invalid character into a hex
      //       escape sequence and tossing up a warning?
      if (!reader.uIsNsWordChar())
        return fetchInvalidTagDirectiveToken("tag handle contained an invalid character", startMark)

      // If it _is_ an `ns-word-char` then it is in the ASCII range and is a
      // single byte.
      handleBuffer.takeASCIIFrom(1, reader, position)
    }

    // Okay, so if we've made it this far, we've seen at least `%TAG !` and at
    // at most `%TAG !<ns-word-char>!`.  At this point we are expecting to see one
    // or more blank characters followed by the prefix value.

    // If we _don't_ have a blank character then the directive is junk.
    if (!reader.isBlank())
      return fetchInvalidTagDirectiveToken("unexpected character after tag handle", startMark)

    // Skip the whitespaces until we encounter something else.
    infixSpace = eatBlanks()

    // If the next thing after the blanks was a linebreak or EOF then we have an
    // incomplete directive.
    if (haveAnyBreakOrEOF() || reader.isPound())
      return fetchIncompleteTagDirectiveToken(startMark, position.mark(modIndex =  -infixSpace, modColumn = -infixSpace))

    // Okay so we have another character in the buffer.  It _should_ be either an
    // exclamation mark (for a local tag prefix) followed by zero or more
    // `<ns-uri-char>` characters, or any `<ns-tag-char>` followed by zero or
    // `<ns-uri-char>` characters.

    // If we hit something else, other than an exclamation mark or an
    // `<ns-tag-char>` character, then we have an invalid tag directive.
    if (!(reader.isExclamation() || reader.isNsTagChar()))
      return fetchInvalidTagDirectiveToken("unexpected first character of tag prefix", startMark)

    // So we have a valid starting character for our prefix, lets create a buffer
    // and read any remaining characters in the prefix into it.
    val prefixBuffer = UByteBuffer(16)

    // Claim the starting character that we already inspected.
    prefixBuffer.takeASCIIFrom(1, reader, position)

    while (true) {
      reader.cache(1)

      if (haveBlankAnyBreakOrEOF())
        break

      // If we encounter a non-URI character than we have an invalid tag
      // directive.
      //
      // Unsafe call because we know based on the previous check that there is at
      // least one byte in the buffer.
      if (!reader.uIsNsURIChar())
        return fetchInvalidTagDirectiveToken("unexpected non-URI safe character in tag prefix", startMark)

      prefixBuffer.takeASCIIFrom(1, reader, position)
    }

    infixSpace = 0

    // Okay so we've successfully read our tag handle and our tag prefix.  Trouble
    // is, the line isn't over yet.  There could be a heap of junk waiting for us,
    // causing this directive line to be invalid.
    if (reader.isBlank()) {
      // We have more spaces after the prefix value.  This could be valid if the
      // spaces are followed by a line break (useless trailing spaces) or if the
      // spaces are followed by a comment line.
      //
      // Skip the spaces and see what's next.  If it is something other than a
      // comment or a newline, then we have an invalid tag directive.
      infixSpace = eatBlanks()

      if (!(haveAnyBreakOrEOF() || reader.isPound()))
        return fetchInvalidTagDirectiveToken("unexpected character after prefix value", startMark)
    }

    // If we've made it this far, then yay!  We did it!  We found and successfully
    // parsed a valid tag token, now we just have to assemble it and queue it up.

    tokens.push(newTagDirectiveToken(
      handleBuffer.popToArray(),
      prefixBuffer.popToArray(),
      startMark,
      position.mark(modIndex = -infixSpace, modColumn = -infixSpace)
    ))
  }

  private fun fetchInvalidTagDirectiveToken(reason: String, start: SourcePosition) {
    val end = skipUntilCommentBreakOrEOF()
    warn("malformed %TAG token: $reason", start, end)
    tokens.push(newInvalidToken(start, end))
  }

  private fun fetchIncompleteTagDirectiveToken(start: SourcePosition, end: SourcePosition) {
    warn("incomplete %TAG directive", start, end)
    tokens.push(newInvalidToken(start, end))
  }

  private fun UByteBuffer.takeASCIIFrom(count: Int, reader: YAMLReaderBuffer, position: SourcePositionTracker) {
    var i = 0
    while (i++ < count)
      push(reader.pop())

    position.incPosition(count.toUInt())
  }

  @Deprecated("use takeASCIIFrom")
  private fun UByteBuffer.claimASCII(bytes: Int, other: UByteBuffer, position: SourcePositionTracker) {
    var i = 0
    while (i++ < bytes)
      push(other.pop())

    position.incPosition(bytes.toUInt())
  }

  // endregion Tag Directive

  // endregion Directives

  // region Ambiguous Colon

  internal fun fetchAmbiguousColonToken() {
    // So we've hit a colon character.  If it is followed by a space, linebreak
    // or EOF then it is a mapping value indicator token.  If it is followed by
    // anything else then it is the start of a plain scalar token.

    // Cache the character after the colon in the buffer.
    reader.cache(2)

    // If we are in a flow, then a colon automatically means value separator.
    //
    // If we are not in a flow, then the colon is only a value separator if it is
    // followed by a blank, a line break, or the EOF
    if (!(inFlow || haveBlankAnyBreakOrEOF(1)))
      return fetchPlainScalar()

    // Record the start position for our token (the position of the colon
    // character)
    val start = position.mark()

    // Skip over the colon character in the stream
    skipASCII()

    // Record the end position for our token (the position immediately after the
    // colon character)
    val end = position.mark()

    // Generate and queue up the token
    tokens.push(newMappingValueIndicatorToken(start, end))
  }

  // endregion Ambiguous Colon

  // region Ambiguous Dash

  internal fun fetchAmbiguousDashToken() {
    // If we've hit a `-` character then we could be at the start of a block
    // sequence entry, a document start, a plain scalar, or junk

    // TODO:
    //   | if we are in a flow context and we encounter "- ", what the fudge do
    //   | we do with that?

    // Cache the next 3 characters in the buffer to accommodate the size of the
    // document start token `^---(?:\s|$)`
    reader.cache(4)

    // If we have `-(?:\s|$)`
    if (haveBlankAnyBreakOrEOF(1))
      return fetchBlockEntryIndicatorToken()

    // See if we are at the start of the line and next up is `--(?:\s|$)`
    if (atStartOfLine && reader.isDash(1) && reader.isDash(2) && haveBlankAnyBreakOrEOF(3)) {
      return fetchDocumentStartToken()
    }

    fetchPlainScalar()
  }

  private fun fetchBlockEntryIndicatorToken() {
    val start = position.mark()
    skipASCII()
    tokens.push(newSequenceEntryIndicatorToken(start, position.mark()))
  }

  private fun fetchDocumentStartToken() {
    val start = position.mark()
    skipASCII(3)
    tokens.push(newDocumentStartToken(start, position.mark()))
  }

  // endregion Ambiguous Dash

  // region Ambiguous Period

  internal fun fetchAmbiguousPeriodToken() {
    reader.cache(4)

    if (atStartOfLine && reader.isPeriod(1) && reader.isPeriod(2) && haveBlankAnyBreakOrEOF(3))
      fetchDocumentEndToken()
    else
      fetchPlainScalar()
  }

  private fun fetchDocumentEndToken() {
    val start = position.mark()
    skipASCII(3)
    tokens.push(newDocumentEndToken(start, position.mark()))
  }

  // endregion Ambiguous Period

  // region Ambiguous Question Mark

  internal fun fetchAmbiguousQuestionToken() {
    // If:      we are in a block context
    //   If:      the question mark is followed by a space, newline, or EOF, it is
    //            a mapping key indicator
    //   Else If: the question mark is followed by anything else, it is a plain
    //            scalar
    // Else If: we are in a flow context
    //   ????????

    // TODO:
    //   | This behavior does not take into account whether we are in a flow
    //   | context or not when making the following determinations.  Determine if
    //   | handling needs to be different for flow contexts and update if
    //   | necessary

    reader.cache(2)

    return if (haveBlankAnyBreakOrEOF(1))
      fetchMappingKeyIndicatorToken()
    else
      fetchPlainScalar()
  }

  private fun fetchMappingKeyIndicatorToken() {
    val start = position.mark()
    skipASCII()
    tokens.push(newMappingKeyIndicatorToken(start, position.mark()))
  }

  // endregion Ambiguous Question Mark

  // region Flow Map Indicators

  internal fun fetchFlowMappingStartToken() {
    val start = position.mark()

    reader.skip(1)
    position.incPosition()

    emitFlowMappingStartToken(start, position.mark())
  }

  internal fun emitFlowMappingStartToken(start: SourcePosition, end: SourcePosition) {
    flows.push(FlowTypeMapping)
    tokens.push(newFlowMappingStartToken(start, end))
  }

  internal fun fetchFlowMappingEndToken() {
    val start = position.mark()

    reader.skip(1)
    position.incPosition()

    emitFlowMappingEndToken(start, position.mark())
  }

  internal fun emitFlowMappingEndToken(start: SourcePosition, end: SourcePosition) {
    if (inFlowMapping)
      flows.pop()

    tokens.push(newFlowMappingEndToken(start, end))
  }

  // endregion Flow Map Indicators

  // region Flow Sequence Indicators

  internal fun fetchFlowSequenceStartToken() {
    val start = position.mark()

    reader.skip(1)
    position.incPosition()

    emitFlowSequenceStartToken(start, position.mark())
  }

  internal fun emitFlowSequenceStartToken(start: SourcePosition, end: SourcePosition) {
    flows.push(FlowTypeSequence)
    tokens.push(newFlowSequenceStartToken(start, end))
  }

  internal fun fetchFlowSequenceEndToken() {
    val start = position.mark()

    reader.skip(1)
    position.incPosition()

    emitFlowSequenceEndToken(start, position.mark())
  }

  internal fun emitFlowSequenceEndToken(start: SourcePosition, end: SourcePosition) {
    if (inFlowSequence)
      flows.pop()

    tokens.push(newFlowSequenceEndToken(start, end))
  }

  // endregion Flow Sequence Indicators

  // region Plain Scalars

  private val psConfirmedBuffer  = UByteBuffer(1024)
  private val psAmbiguousBuffer  = UByteBuffer(1024)
  private val psTrailingWSBuffer = UByteBuffer(16)
  private val psTrailingNLBuffer = UByteBuffer(4)

  @OptIn(ExperimentalUnsignedTypes::class)
  internal fun fetchPlainScalar() {
    val startMark = position.mark()

    // Rolling end position of this scalar value (don't create a mark every time
    // because that's a new class instantiation per stream character).
    val endPosition = position.copy()

    val startOfLinePosition = position.copy()

    while (true) {
      reader.cache(1)

      if (haveEOF()) {
        if (psAmbiguousBuffer.size == 1) {
          if (psAmbiguousBuffer.uIsSquareClose()) {
            if (psConfirmedBuffer.isNotEmpty)
              tokens.push(newPlainScalarToken(psConfirmedBuffer.popToArray(), startMark, endPosition.mark()))

            this.emitFlowSequenceEndToken(startOfLinePosition.mark(), startOfLinePosition.mark(1, 0, 1))
            return
          } else if (psAmbiguousBuffer.uIsCurlyClose()) {
            if (psConfirmedBuffer.isNotEmpty)
              tokens.push(newPlainScalarToken(psConfirmedBuffer.popToArray(), startMark, endPosition.mark()))

            this.emitFlowMappingEndToken(startOfLinePosition.mark(), startOfLinePosition.mark(1, 0, 1))
            return
          }
        }

        collapseNewlinesAndMergeBuffers(endPosition, psConfirmedBuffer, psAmbiguousBuffer, psTrailingWSBuffer, psTrailingNLBuffer)
        tokens.push(newPlainScalarToken(psConfirmedBuffer.popToArray(), startMark, endPosition.mark()))
        return
      }

      if (haveBlank()) {
        psTrailingWSBuffer.push(reader.pop())
        position.incPosition()
        continue
      }

      if (haveAnyBreak()) {
        // Here we examine if the entire line last line we just ate (not counting
        // whitespaces) was a closing square or curly bracket character.
        //
        // This is handled as a special case just for trying to make sense of an
        // invalid, multiline plain scalar value.  In this special case,
        // regardless of whether we are in a flow, we will consider the closing
        // bracket a flow end token.
        //
        // In addition to this, because the closing brace will have already been
        // "consumed" from the reader, we will need to also emit the appropriate
        // token.
        //
        // This logic and comment appear twice in this file and nowhere else.
        if (psAmbiguousBuffer.size == 1) {
          if (psAmbiguousBuffer.uIsSquareClose()) {
            if (psConfirmedBuffer.isNotEmpty)
              tokens.push(newPlainScalarToken(psConfirmedBuffer.popToArray(), startMark, endPosition.mark()))

            this.emitFlowSequenceEndToken(startOfLinePosition.mark(), startOfLinePosition.mark(1, 0, 1))
            return
          } else if (psAmbiguousBuffer.uIsCurlyClose()) {
            if (psConfirmedBuffer.isNotEmpty)
              tokens.push(newPlainScalarToken(psConfirmedBuffer.popToArray(), startMark, endPosition.mark()))

            this.emitFlowMappingEndToken(startOfLinePosition.mark(), startOfLinePosition.mark(1, 0, 1))
            return
          }
        }

        psTrailingWSBuffer.clear()
        collapseNewlinesAndMergeBuffers(endPosition, psConfirmedBuffer, psAmbiguousBuffer, psTrailingWSBuffer, psTrailingNLBuffer)
        psTrailingNLBuffer.claimNewLine(reader.utf8Buffer, position)
        continue
      }

      if (haveColon()) {
        reader.cache(2)

        // If we are not in a flow mapping, and the colon is followed by a
        // whitespace, then split this plain scalar on the last newline to make
        // the previous plain scalar value, and the new mapping key value.
        //
        // For example, the following:
        // ```
        // hello
        // goodbye: taco
        // ```
        //
        // would become:
        // 1. Plain scalar: "hello"
        // 2. Plain scalar: "goodbye"
        // 3. Mapping value indicator
        // 4. Plain scalar: "taco"
        if (!inFlowMapping && haveBlankAnyBreakOrEOF(1)) {
          if (psConfirmedBuffer.isNotEmpty)
            tokens.push(newPlainScalarToken(psConfirmedBuffer.popToArray(), startMark, endPosition.mark()))

          tokens.push(newPlainScalarToken(psAmbiguousBuffer.popToArray(), startOfLinePosition.mark(), position.mark()))

          return
        }

        // If we are in a flow mapping, then unlike the block mapping, we A) don't
        // care if there is a space following the colon character, and B) don't
        // want to split the plain scalar on newlines.
        else if (inFlowMapping) {
          psTrailingWSBuffer.clear()

          collapseNewlinesAndMergeBuffers(endPosition, psConfirmedBuffer, psAmbiguousBuffer, psTrailingWSBuffer, psTrailingNLBuffer)

          tokens.push(newPlainScalarToken(psConfirmedBuffer.popToArray(), startMark, endPosition.mark()))

          return
        }
      }

      if (reader.isDash() && psTrailingNLBuffer.isNotEmpty) {
        reader.cache(4)

        if (haveBlankAnyBreakOrEOF(1)) {
          tokens.push(newPlainScalarToken(psConfirmedBuffer.popToArray(), startMark, endPosition.mark()))
          return
        }
      }

      if (inFlow && haveComma()) {
        collapseNewlinesAndMergeBuffers(endPosition, psConfirmedBuffer, psAmbiguousBuffer, psTrailingWSBuffer, psTrailingNLBuffer)
        tokens.push(newPlainScalarToken(psConfirmedBuffer.popToArray(), startMark, endPosition.mark()))
        return
      }

      if (inFlowMapping && haveCurlyClose()) {
        collapseNewlinesAndMergeBuffers(endPosition, psConfirmedBuffer, psAmbiguousBuffer, psTrailingWSBuffer, psTrailingNLBuffer)
        tokens.push(newPlainScalarToken(psConfirmedBuffer.popToArray(), startMark, endPosition.mark()))
        return
      }

      if (inFlowSequence && haveSquareClose()) {
        collapseNewlinesAndMergeBuffers(endPosition, psConfirmedBuffer, psAmbiguousBuffer, psTrailingWSBuffer, psTrailingNLBuffer)
        tokens.push(newPlainScalarToken(psConfirmedBuffer.popToArray(), startMark, endPosition.mark()))
        return
      }

      if (atStartOfLine) {
        startOfLinePosition.become(position)

        // If we are in this block, then the last character we saw was a newline
        // character.  This means that we don't need to worry about the contents
        // of the ambiguous buffer inside this if block as that buffer will always
        // be empty.

        if (reader.isDash()) {
          reader.cache(4)

          if (reader.isDash(1) && reader.isDash(2) && haveBlankAnyBreakOrEOF(3)) {
            tokens.push(newPlainScalarToken(psConfirmedBuffer.popToArray(), startMark, endPosition.mark()))
            return
          }
        }

        if (reader.isPeriod()) {
          reader.cache (4)

          if (reader.isPeriod(1) && reader.isPeriod(2) && haveBlankAnyBreakOrEOF(3)) {
            tokens.push(newPlainScalarToken(psConfirmedBuffer.popToArray(), startMark, endPosition.mark()))
            return
          }
        }

        if (reader.isQuestion()) {
          reader.cache(2)

          if (haveBlankAnyBreakOrEOF(1)) {
            tokens.push(newPlainScalarToken(psConfirmedBuffer.popToArray(), startMark, endPosition.mark()))
            return
          }
        }

        if (
          havePercent()
          || haveSquareOpen()
          || haveCurlyOpen()
        ) {
          tokens.push(newPlainScalarToken(psConfirmedBuffer.popToArray(), startMark, endPosition.mark()))
          return
        }
      } // end if (atStartOfLine)

      // Catch-all: append it to the ambiguous buffer

      // If we have any trailing whitespaces, then append them to the ambiguous
      // buffer because we just hit a non-blank character
      if (psTrailingNLBuffer.isEmpty)
        while (psTrailingWSBuffer.isNotEmpty)
          psAmbiguousBuffer.push(psTrailingWSBuffer.pop())

      psAmbiguousBuffer.claimUTF8()
    }
  }

  private fun collapseNewlinesAndMergeBuffers(
    endPosition: SourcePositionTracker,
    to: UByteBuffer,
    from: UByteBuffer,
    spaces: UByteBuffer,
    newLines: UByteBuffer,
  ) {
    if (from.isEmpty)
      return

    if (newLines.isNotEmpty) {

      if (newLines.size == 1) {
        to.push(A_SPACE)
        newLines.skipNewLine(endPosition)
      } else {
        newLines.skipNewLine(endPosition)
        while (newLines.isNotEmpty)
          to.claimNewLine(newLines, endPosition)
      }

    } else {
      while (spaces.isNotEmpty) {
        to.push(spaces.pop())
        endPosition.incPosition()
      }
    }

    while (from.isNotEmpty) {
      to.push(from.pop())
      endPosition.incPosition()
    }
  }

  // endregion Plain Scalars

  // endregion Token Fetching
}

