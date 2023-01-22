package io.foxcapades.lib.k.yaml.scan


internal fun YAMLScannerImpl.fetchAmbiguousColonToken() {
  this.reader.cache(2)

  if (!(this.inFlow || this.reader.isBlankAnyBreakOrEOF(1)))
    return this.fetchPlainScalar()

  this.fetchMappingValueIndicatorToken()
}



