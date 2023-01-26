package io.foxcapades.lib.k.yaml.scan

import io.foxcapades.lib.k.yaml.token.YAMLTokenMappingKey
import io.foxcapades.lib.k.yaml.token.YAMLTokenScalarPlain

/**
 * # Fetch Ambiguous Question Mark Token
 *
 * Determines the meaning of an encountered question mark (`?`) character and
 * emits the correct token based on the context.
 *
 * If the question mark character is followed immediately by a blank space, a
 * line break, or the end of the input stream, then this method will cause a
 * [mapping key token][YAMLTokenMappingKey] to be emitted.
 *
 * If the question mark character is followed immediately by anything else, then
 * this method will cause a [plain scalar token][YAMLTokenScalarPlain] to be
 * emitted.
 *
 * @since 0.1.0
 * @author Elizabeth Paige Harper - https://github.com/foxcapades
 */
internal fun YAMLStreamTokenizerImpl.parseAmbiguousQuestionToken() {
  // TODO:
  //   | This behavior does not take into account whether we are in a flow
  //   | context or not when making the following determinations.  Determine if
  //   | handling needs to be different for flow contexts and update if
  //   | necessary

  this.buffer.cache(2)

  return if (this.buffer.isBlankAnyBreakOrEOF(1))
    this.fetchMappingKeyIndicatorToken()
  else
    this.parsePlainScalar()
}
