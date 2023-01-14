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

  private val tokenBuffer = UByteBuffer(2048)

  private var inDocument = false

  private var inFlow = false

  private inline val haveMoreInBuffer
    get() = !reader.atEOF || reader.isNotEmpty

  private var version = YAMLVersion.VERSION_1_2

  private val reader: YAMLReader

  private val lineBreak: LineBreakType

  constructor(reader: YAMLReader, lineBreak: LineBreakType) {
    this.reader    = reader
    this.lineBreak = lineBreak
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
      A_AT    -> fetchAtToken()
      A_GRAVE -> fetchGraveToken()

      // Meh characters: ~ $ ^ ( ) _ + = \ ; < /
      // And everything else...
      else -> fetchPlainScalar()
    }
  }

  @OptIn(ExperimentalUnsignedTypes::class)
  private fun fetchPlainScalar() {
    val startMark = position.toSourcePosition()

    val whitespaces  = UByteBuffer()
    var lastWasBreak = true

    while (true) {
      reader.cache(1)

      if (!haveMoreInBuffer)
        TODO("handle EOF in what is probably a scalar")

      when {
        // Glorious!  A blank space!  A clearing in the noise!
        reader.isBlank() -> {
          // Append it to our whitespaces array because it may be a leading or
          // trailing whitespace character that we want to omit
          lastWasBreak = false
          whitespaces.push(reader.pop())
        }

        // A colon.  Respectable.
        reader.isColon() -> {
          // So we've hit a colon character... this could be the break we've
          // been looking for!   Perhaps it's a sign!  The end may be nigh!
          lastWasBreak = false

          // Attempt to cache another codepoint in the buffer, we need to look
          // ahead to the next character to determine if we've reached the end
          // of this scalar.
          reader.cache(2)

          // If the colon character is followed by any of this junk, then IT IS
          // THE END! (of the scalar we've been chewing on)
          if (
            reader.isBlank(1)
            || reader.isBreak_1_1(1)
            || reader.isEOF(1)
          ) {
            tokens.push(
              YAMLToken(
                YAMLTokenType.Scalar,
                YAMLTokenDataScalar(tokenBuffer.toArray(), YAMLScalarStyle.Plain),
                startMark,
                position.toSourcePosition(),
                warnings.popToArray()
              )
            )
          }

          // Alas, it was just another false hope, write it, and all the
          // preceding whitespaces to the token buffer and on we drudge.
          else {
            while (whitespaces.isNotEmpty)
              tokenBuffer.push(whitespaces.pop())

            writeASCII(tokenBuffer)
          }
        }

        // `, ` && inFlow
        reader.isComma() && inFlow -> TODO("fetch mystery comma")

        // `? ` && !inFlow
        reader.isQuestion() && !inFlow -> {
          TODO("""
            If we hit a "\n?<WS>" combo and we are not in a flow context, then
            we done found ourselves at the beginning of
          """.trimIndent())
        }

        reader.isPound() -> {
          if (whitespaces.isNotEmpty) {
            // So what we had was a freakin scalar the whole dang time!

            TODO("OOPS WE'VE HIT A COMMENT, TIME TO END THIS TOKEN WHICH IS JUST A STRING OR SOMETHING?")
          } else {
            writeUTF8(tokenBuffer)
          }
        }

        reader.isBreak_1_2() -> {
          whitespaces.clear()

          if (lastWasBreak)
            writeLine(whitespaces)
          else
            lastWasBreak = true
        }

        reader.isBreak_1_1() -> {
          whitespaces.clear()

          if (version == YAMLVersion.VERSION_1_2)
            warn("illegal line break character; YAML 1.2 permits only the line breaks CRLF, CR, or LF for compatibility with JSON")

          if (lastWasBreak)
            writeLine(whitespaces)
          else
            lastWasBreak = true
        }
      }

      // Is it a valid character to be a plain scalar or mapping key?
      // Eat it and look for `:<WS>` to form a mapping key, unless it goes to
      // beyond 1024 characters at which point it must be a scalar value (which
      // may not be allowed if we are looking for a continuation of a block
      // mapping)

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

  private fun writeASCII(buffer: UByteBuffer) {
    buffer.push(reader.pop())
    position.incPosition()
  }

  private fun writeUTF8(buffer: UByteBuffer) {
    if (!buffer.takeCodepointFrom(reader.utf8Buffer))
      throw IllegalStateException("invalid utf-8 codepoint in the reader buffer or buffer is offset")
    position.incPosition()
  }

  private fun writeLine(buffer: UByteBuffer) {
    reader.cache(4)

    if (reader.isCRLF()) {
      reader.skip(2)
      // What kind of line break do we write to the stream?
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
      reader.skip(2)
      position.incLine(2u)
    }

    else if (reader.isCROrLF()) {
      reader.skip(1)
      position.incLine()
    }

    // Separate block for the rest as they are not supported by YAML 1.2.
    // This block emits a warning if the current line break is occurring in a
    else {

      if (reader.isNEL()) {
        reader.skip(2)
      } else if (reader.isLSOrPS()) {
        reader.skip(3)
      } else {
        throw IllegalStateException("called #skipLine() when the reader was not on a newline character")
      }

      if (inDocument && version == YAMLVersion.VERSION_1_2)
        warn("invalid line break character; YAML 1.2 only permits line breaks consisting of CRLF, CR, or LF")

      position.incLine()
    }
  }

  // endregion Reader Helpers
}