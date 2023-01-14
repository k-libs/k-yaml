package io.foxcapades.lib.k.yaml.scan

import io.foxcapades.lib.k.yaml.YAMLVersion
import io.foxcapades.lib.k.yaml.bytes.*
import io.foxcapades.lib.k.yaml.read.*
import io.foxcapades.lib.k.yaml.read.isCRLF
import io.foxcapades.lib.k.yaml.read.isCROrLF
import io.foxcapades.lib.k.yaml.read.isNEL
import io.foxcapades.lib.k.yaml.util.*
import io.foxcapades.lib.k.yaml.util.UByteBuffer


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

  private var inDocument = false

  private var inFlow = false

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

    reader.cache(1)

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
    val startMark = position.toSourcePosition()

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
      reader.cache(1)

      // If the reader is empty, then whatever we currently have in our token
      // buffer
      if (!haveMoreInBuffer)
        return tokens.push(newPlainScalarToken(tokenBuffer.popToArray(), startMark, endMark.toSourcePosition()))

      // When we hit one of the following characters, then we pay attention to
      // what's going on because we may have hit the start of a new token:
      //
      //   : , ? #
      //
      // When we hit a newline, or whitespace character, buffer it on the side
      // in case we need it.
      when {
        reader.isBlank()               -> {
          if (lineBreaks.isEmpty)
            trailingWS.claimASCII()

          continue
        }

        reader.isBreak_1_1()              -> {
          lineBreaks.claimNewLine()
          continue
        }

        reader.isColon()               -> {
          // Attempt to cache another codepoint in the buffer, we need to look
          // ahead to the next character to determine if we've reached the end
          // of this scalar.
          reader.cache(2)

          // If the colon character is followed by any of this junk, then IT IS
          // THE END! (of the scalar we've been chewing on)
          if (reader.isBlank(1) || reader.isBreak_1_1(1) || reader.isEOF(1))
            return tokens.push(newPlainScalarToken(tokenBuffer.popToArray(), startMark, endMark.toSourcePosition())
        }

        reader.isComma() && inFlow     -> {
          return tokens.push(newPlainScalarToken(tokenBuffer.popToArray(), startMark, endMark.toSourcePosition()))
        }

        reader.isQuestion() && !inFlow && position.column == 0u -> {
          // If we are in a block context, the question mark was at column 0 and
          // it is followed immediately by a whitespace, a newline, or the EOF
          // then consider it the start of a new token (a complex mapping key).
          reader.cache(2)

          if (
            reader.isBlank(1)
            || reader.isBreak_1_1(1)
            || reader.isEOF(1)
          ) {
            return tokens.push(newPlainScalarToken(tokenBuffer.popToArray(), startMark, endMark.toSourcePosition())
          }
        }

        reader.isPound()               -> {
          // If we've hit a `#` character that was preceded immediately by a
          // whitespace or newline character, then we are starting a comment.
          if (trailingWS.isNotEmpty || lineBreaks.isNotEmpty)
            return tokens.push(newPlainScalarToken(tokenBuffer.popToArray(), startMark, endMark.toSourcePosition()))
        }
      }

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

        tokenBuffer.claimUTF8()
        endMark.become(position)
      }
    }
  }

  // region Directive Tokens

  private fun fetchDirectiveToken()

  private fun fetchTagDirectiveToken()

  private fun fetchYAMLDirectiveToken()

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

  // region Warning Helpers

  private fun warn(message: String, mark: SourcePosition = position.toSourcePosition()) {
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

  private fun skipCharacter() {
    reader.skipCodepoint()
    position.incPosition()
  }

  private fun skipCharacters(count: Int) {
    var i = 0
    while (i++ < count)
      skipCharacter()
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

  // region Token Constructors

  @Suppress("NOTHING_TO_INLINE")
  @OptIn(ExperimentalUnsignedTypes::class)
  private inline fun newPlainScalarToken(value: UByteArray, start: SourcePosition, end: SourcePosition) =
    YAMLToken(YAMLTokenType.Scalar, YAMLTokenDataScalar(value, YAMLScalarStyle.Plain), start, end, warnings.popToArray())

  // endregion Token Constructors
}