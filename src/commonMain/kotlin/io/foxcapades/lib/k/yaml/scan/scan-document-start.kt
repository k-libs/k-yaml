package io.foxcapades.lib.k.yaml.scan

internal fun YAMLScannerImpl.fetchDocumentStartToken() {
  val start = position.mark()
  skipASCII(3)
  tokens.push(newDocumentStartToken(start, position.mark()))
}
