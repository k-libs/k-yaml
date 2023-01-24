package io.foxcapades.lib.k.yaml.scan

import io.foxcapades.lib.k.yaml.token.YAMLTokenStreamStart

internal fun YAMLStreamTokenizerImpl.parseStreamStartToken() {
  reader.cache(1)
  streamStartProduced = true
  val mark = position.mark()
  tokens.push(YAMLTokenStreamStart(reader.encoding, mark, mark, popWarnings()))
}
