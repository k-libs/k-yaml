package io.foxcapades.lib.k.yaml.scan

import io.foxcapades.lib.k.yaml.token.YAMLTokenDocumentEnd
import io.foxcapades.lib.k.yaml.token.YAMLTokenScalarPlain
import io.foxcapades.lib.k.yaml.util.isPeriod

/**
 * # Fetch Ambiguous Period Token
 *
 * Determines the meaning of an encountered period (`.`) character and emits the
 * correct token based on the context.
 *
 * If the period character is followed immediately by 2 additional period
 * characters then a blank space, a line break, or the end of the input stream,
 * this method will cause a [document end token][YAMLTokenDocumentEnd] to be
 * emitted.
 *
 * If the period character is followed by anything else, this method will cause
 * a [plain scalar token][YAMLTokenScalarPlain] to be emitted.
 *
 * @since 0.1.0
 * @author Elizabeth Paige Harper - https://github.com/foxcapades
 */
internal fun YAMLScannerImpl.fetchAmbiguousPeriodToken() {
  this.reader.cache(4)
  return if (
    this.atStartOfLine
    && this.reader.isPeriod(1)
    && this.reader.isPeriod(2)
    && this.reader.isBlankAnyBreakOrEOF(3)
  )
    this.fetchDocumentEndToken()
  else
    this.fetchPlainScalar()
}
