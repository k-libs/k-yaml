package io.foxcapades.lib.k.yaml.scan

internal fun YAMLScannerImpl.fetchStreamStartToken() {
  reader.cache(1)
  streamStartProduced = true
  val mark = position.mark()
  tokens.push(newStreamStartToken(reader.encoding, mark, mark))
}
