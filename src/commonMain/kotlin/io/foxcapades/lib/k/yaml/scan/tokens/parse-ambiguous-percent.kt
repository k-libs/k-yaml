package io.foxcapades.lib.k.yaml.scan.tokens

import io.foxcapades.lib.k.yaml.bytes.A_UPPER_A
import io.foxcapades.lib.k.yaml.bytes.A_UPPER_G
import io.foxcapades.lib.k.yaml.bytes.A_UPPER_L
import io.foxcapades.lib.k.yaml.bytes.A_UPPER_M
import io.foxcapades.lib.k.yaml.bytes.A_UPPER_T
import io.foxcapades.lib.k.yaml.bytes.A_UPPER_Y
import io.foxcapades.lib.k.yaml.token.YAMLTokenDirectiveTag
import io.foxcapades.lib.k.yaml.token.YAMLTokenDirectiveYAML
import io.foxcapades.lib.k.yaml.token.YAMLTokenInvalid
import io.foxcapades.lib.k.yaml.util.isBlank
import io.foxcapades.lib.k.yaml.util.test


/**
 * # Fetch Ambiguous Percent Token
 *
 * Determines the meaning of the encountered percent (`%`) character and emits
 * the correct token based on the context.
 *
 * The percent character can only validly be the beginning of a directive token,
 * and more specifically, must be immediately followed by the characters `YAML`
 * or `TAG` followed by a blank character.  Anything else is an invalid token.
 *
 * If the percent character is immediately followed by the characters `YAML`
 * then a blank character, this method will cause a
 * [YAML directive token][YAMLTokenDirectiveYAML] to be emitted.
 *
 * If the percent character is immediately followed by the characters `TAG` then
 * a blank character, this method will cause a
 * [tag directive token][YAMLTokenDirectiveTag] to be emitted.
 *
 * If the percent character is followed by anything else, this method will cause
 * an [invalid token][YAMLTokenInvalid] to be emitted.
 *
 * @since 0.1.0
 * @author Elizabeth Paige Harper
 */
internal fun YAMLStreamTokenizerImpl.parseAmbiguousPercent() {
  // Record the start position
  val start = position.mark()

  // If the token is not at the start of a line, then it is an invalid token as
  // directive tokens must start at column 0.
  if (!atStartOfLine) {
    emitInvalidToken(
      "invalid token: YAML directive tokens must start at the beginning of the line",
      start,
      skipUntilCommentBreakOrEOF()
    )

    return
  }

  // Skip the `%` character.
  skipASCII(this.buffer, this.position)

  // Attempt to load 5 codepoints into the reader buffer so that we can do the
  // following tests.
  this.buffer.cache(5)

  // Nothing more in the buffer?  That means the stream ended on a `%`
  // character which means an invalid token directive.
  if (!this.haveMoreCharactersAvailable) {
    emitInvalidToken(
      "invalid token: YAML directive token is incomplete due to reaching the end of the input stream",
      start
    )

    return
  }

  // See if the next 5 characters are "YAML<WS>"
  if (
       this.buffer.test(A_UPPER_Y, 0)
    && this.buffer.test(A_UPPER_A, 1)
    && this.buffer.test(A_UPPER_M, 2)
    && this.buffer.test(A_UPPER_L, 3)
    && this.buffer.isBlank(4)
  ) {
    skipASCII(this.buffer, this.position, 5)
    return this.fetchYAMLDirectiveToken(start)
  }

  // See if the next 4 characters are "TAG<WS>"
  if (
       this.buffer.test(A_UPPER_T, 0)
    && this.buffer.test(A_UPPER_A, 1)
    && this.buffer.test(A_UPPER_G, 2)
    && this.buffer.isBlank(3)
  ) {
    skipASCII(this.buffer, this.position, 4)
    return fetchTagDirectiveToken(start)
  }

  // If it's not YAML or TAG then it's invalid
  lineContentIndicator = LineContentIndicatorContent
  emitInvalidToken("invalid token: unrecognized directive token", start, skipUntilCommentBreakOrEOF())
}
