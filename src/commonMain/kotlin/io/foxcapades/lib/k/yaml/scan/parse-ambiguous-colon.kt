package io.foxcapades.lib.k.yaml.scan

import io.foxcapades.lib.k.yaml.token.YAMLTokenMappingValue
import io.foxcapades.lib.k.yaml.token.YAMLTokenScalarPlain

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
internal fun YAMLScannerImpl.parseAmbiguousColonToken() {
  this.reader.cache(2)

  if (!(this.inFlowMapping || this.reader.isBlankAnyBreakOrEOF(1)))
    return this.parsePlainScalar()

  this.fetchMappingValueIndicatorToken()
}



