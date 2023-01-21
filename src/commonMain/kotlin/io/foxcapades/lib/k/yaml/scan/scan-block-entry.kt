package io.foxcapades.lib.k.yaml.scan

internal fun YAMLScannerImpl.fetchBlockEntryIndicatorToken() {
  val start = position.mark()
  skipASCII()
  tokens.push(newSequenceEntryIndicatorToken(start, position.mark()))
}
