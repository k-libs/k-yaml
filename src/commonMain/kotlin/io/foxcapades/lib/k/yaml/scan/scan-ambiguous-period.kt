package io.foxcapades.lib.k.yaml.scan

import io.foxcapades.lib.k.yaml.util.isPeriod

internal fun YAMLScannerImpl.fetchAmbiguousPeriodToken() {
  reader.cache(4)

  if (atStartOfLine && reader.isPeriod(1) && reader.isPeriod(2) && reader.isBlankAnyBreakOrEOF(3))
    fetchDocumentEndToken()
  else
    fetchPlainScalar()
}
