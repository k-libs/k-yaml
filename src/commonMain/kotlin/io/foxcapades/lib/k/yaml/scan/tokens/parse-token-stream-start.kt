package io.foxcapades.lib.k.yaml.scan.tokens

import io.foxcapades.lib.k.yaml.token.YAMLTokenStreamStart

internal fun YAMLStreamTokenizerImpl.parseStreamStartToken() {
  buffer.cache(1)
  streamStartProduced = true
  val mark = position.mark()
  tokens.push(YAMLTokenStreamStart(buffer.encoding, mark, mark, popWarnings()))
}
