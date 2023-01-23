package io.foxcapades.lib.k.yaml.scan

import io.foxcapades.lib.k.yaml.token.YAMLTokenStreamStart

internal fun YAMLScannerImpl.fetchStreamStartToken() {
  reader.cache(1)
  streamStartProduced = true
  val mark = position.mark()
  tokens.push(YAMLTokenStreamStart(reader.encoding, mark, mark, getWarnings()))
}
