package io.foxcapades.lib.k.yaml.scan.tokens

import io.foxcapades.lib.k.yaml.YAMLStreamTokenizer
import io.foxcapades.lib.k.yaml.YAMLVersion
import io.foxcapades.lib.k.yaml.read.BufferedUTFStreamReader
import io.foxcapades.lib.k.yaml.scan.tokens.token.YAMLToken
import io.foxcapades.lib.k.yaml.token.YAMLTokenComment
import io.foxcapades.lib.k.yaml.token.YAMLTokenStreamEnd
import io.foxcapades.lib.k.yaml.util.*
import io.foxcapades.lib.k.yaml.warn.SourceWarning


@Suppress("NOTHING_TO_INLINE")
internal class YAMLStreamTokenizerImpl : YAMLStreamTokenizer {

  /**
   * Whether the stream start token has yet been emitted.
   */
  internal var streamStartProduced = false

  /**
   * Whether the stream end token has yet been emitted.
   */
  internal var streamEndProduced = false

  /**
   * Current "cursor" position tracker in the source stream.
   */
  internal val position = SourcePositionTracker()

  /**
   * Warnings that have been emitted by parser functions that have not yet been
   * claimed.
   *
   * This queue will generally be empty between [nextToken] calls as the token
   * emitting process involves grabbing all the warnings that were kicked up
   * while parsing the token being emitted.
   */
  internal val warnings = Queue<SourceWarning>(4)

  internal val tokens = Queue<io.foxcapades.lib.k.yaml.scan.tokens.token.YAMLToken>(4)
  internal lateinit var lastToken: io.foxcapades.lib.k.yaml.scan.tokens.token.YAMLToken

  internal var lineContentIndicator = LineContentIndicatorBlanksOnly

  internal var indent = 0u

  internal val flows = ByteStack(4)

  internal var version = YAMLVersion.VERSION_1_2

  internal val buffer: BufferedUTFStreamReader

  internal val contentBuffer1   = UByteBuffer(1024)
  internal val contentBuffer2   = UByteBuffer(1024)
  internal val trailingWSBuffer = UByteBuffer(16)
  internal val trailingNLBuffer = UByteBuffer(4)

  internal inline val inFlow: Boolean
    get() = flows.isNotEmpty

  internal inline val inFlowSequence: Boolean
    get() = inFlow && flows.peek() == FlowTypeSequence

  internal inline val inFlowMapping
    get() = inFlow && flows.peek() == FlowTypeMapping

  internal val atStartOfLine: Boolean
    get() = position.column == 0u

  internal inline val haveMoreCharactersAvailable
    get() = !buffer.atEOF || buffer.isNotEmpty

  constructor(reader: BufferedUTFStreamReader) {
    this.buffer = reader
  }

  // region Public Methods

  override val hasNextToken: Boolean
    get() = !streamEndProduced

  override fun nextToken(): io.foxcapades.lib.k.yaml.scan.tokens.token.YAMLToken {
    if (streamEndProduced)
      throw IllegalStateException("nextToken called on a YAML scanner that has already produced the end of the input YAML stream")

    while (tokens.isEmpty) {
      parseNextToken()
    }

    val token = tokens.pop()

    if (token !is YAMLTokenComment)
      lastToken = token

    if (token is YAMLTokenStreamEnd)
      streamEndProduced = true

    return token
  }

  // endregion Public Methods

  // region Warning Helpers

  internal fun warn(
    message: String,
    start:   SourcePosition = position.mark(),
    end:     SourcePosition = position.mark(),
  ): SourcePosition {
    warnings.push(SourceWarning(message, start, end))
    return end
  }

  internal fun popWarnings(): Array<SourceWarning> = warnings.popToArray { arrayOfNulls(it) }

  // endregion Warning Helpers

  private fun parseNextToken() {
    if (!streamStartProduced)
      return parseStreamStartToken()

    // TODO: SKIP TO NEXT TOKEN NEEDS TO HANDLE INDENT TAB DETECTION
    skipToNextToken()

    buffer.cache(1)

    if (!haveMoreCharactersAvailable)
      return parseStreamEndToken()

    when {
      buffer.uIsDash()        -> parseAmbiguousDashToken()
      buffer.uIsColon()       -> parseAmbiguousColonToken()
      buffer.uIsComma()       -> parseFlowItemSeparatorToken()
      buffer.uIsPound()       -> parseCommentToken()
      buffer.uIsApostrophe()  -> parseSingleQuotedStringToken()
      buffer.uIsDoubleQuote() -> parseDoubleQuotedStringToken()
      buffer.uIsSquareOpen()  -> parseFlowSequenceStartToken()
      buffer.uIsCurlyOpen()   -> parseFlowMappingStartToken()
      buffer.uIsPipe()        -> parseBlockScalar(true)
      buffer.uIsGreaterThan() -> parseBlockScalar(false)
      buffer.uIsSquareClose() -> parseFlowSequenceEndToken()
      buffer.uIsCurlyClose()  -> parseFlowMappingEndToken()
      buffer.uIsExclamation() -> parseTagToken()
      buffer.uIsPercent()     -> parseAmbiguousPercent()
      buffer.uIsPeriod()      -> parseAmbiguousPeriodToken()
      buffer.uIsAmpersand()   -> parseAnchorToken()
      buffer.uIsAsterisk()    -> parseAliasToken()
      buffer.uIsQuestion()    -> parseAmbiguousQuestionToken()

      // BAD NONO CHARACTERS: @ `
      buffer.uIsAt()          -> parseAmbiguousAtToken()
      buffer.uIsGrave()       -> parseAmbiguousGraveToken()

      // Meh characters: ~ $ ^ ( ) _ + = \ ; < /
      // And everything else...
      else                    -> parsePlainScalar()
    }
  }
}
