package io.foxcapades.lib.k.yaml.scan


internal fun YAMLScannerImpl.fetchMappingKeyIndicatorToken() {
  val start = position.mark()
  skipASCII()
  tokens.push(newMappingKeyIndicatorToken(start, position.mark()))
}
