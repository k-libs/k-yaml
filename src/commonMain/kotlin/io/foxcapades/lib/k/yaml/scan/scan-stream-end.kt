package io.foxcapades.lib.k.yaml.scan

internal fun YAMLScannerImpl.fetchStreamEndToken() {
  val mark = position.mark()
  tokens.push(newStreamEndToken(mark, mark))
}