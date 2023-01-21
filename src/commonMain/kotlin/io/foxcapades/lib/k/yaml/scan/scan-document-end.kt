package io.foxcapades.lib.k.yaml.scan

internal fun YAMLScannerImpl.fetchDocumentEndToken() {
  val start = position.mark()
  skipASCII(3)
  tokens.push(newDocumentEndToken(start, position.mark()))
}
