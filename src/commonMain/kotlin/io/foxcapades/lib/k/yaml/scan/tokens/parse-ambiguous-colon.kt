package io.foxcapades.lib.k.yaml.scan.tokens

import io.foxcapades.lib.k.yaml.token.*

/**
 * # Fetch Ambiguous Colon Token
 *
 * Determines the meaning of an encountered `:` character and either parses it
 * as a mapping value indicator token or the beginning of a plain scalar value.
 *
 * If the scanner currently happens to be in a flow mapping, then the colon will
 * be considered a [mapping value indicator][YAMLTokenMappingValue] and this
 * method will queue up the corresponding token.
 *
 * If the scanner currently is NOT in flow mapping, then the character _after_
 * the colon will be tested.  If the character _after_ the colon character is a
 * blank space, a line break, or the end of the input stream, then the colon
 * character will be considered a
 * [mapping value indicator][YAMLTokenMappingValue] and this method will queue
 * up the corresponding token.  If the character _after_ the colon character is
 * anything else, this method will consider the colon character the start of a
 * [plain scalar][YAMLTokenScalarPlain] and parse and emit that token.
 *
 * @since 0.1.0
 * @author Elizabeth Paige Harper - https://github.com/foxcapades
 */
internal fun YAMLStreamTokenizerImpl.parseAmbiguousColonToken() {
  this.buffer.cache(2)

  if (this.buffer.isBlankAnyBreakOrEOF(1))
    return this.fetchMappingValueIndicatorToken()

  /*
  So the character after the colon is not a blank, a line break, or the EOF.

  This complicates things a little.  In some cases this means that the colon
  should be considered part of a plain scalar, and in other cases it is just
  adjacent value.
  */
  if (
    inFlow
    && lastToken !is YAMLTokenFlowSequenceStart
    && lastToken !is YAMLTokenFlowMappingStart
    && lastToken !is YAMLTokenFlowItemSeparator
  )
    return this.fetchMappingValueIndicatorToken()


  return this.parsePlainScalar()
}



