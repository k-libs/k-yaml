package io.foxcapades.lib.k.yaml.scan

internal fun YAMLScanner.fetchStreamStartToken() {
  cache(1)
  streamStartProduced = true
  val mark = position.mark()
  tokens.push(newStreamStartToken(reader.encoding, mark, mark))
}