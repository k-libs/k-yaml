package io.foxcapades.lib.k.yaml.scan.tokens

import io.foxcapades.lib.k.yaml.token.YAMLTokenFlowItemSeparator

internal fun YAMLStreamTokenizerImpl.parseFlowItemSeparatorToken() {
  val start = this.position.mark()
  skipASCII(this.buffer, this.position)
  this.lineContentIndicator = LineContentIndicatorContent
  tokens.push(YAMLTokenFlowItemSeparator(start, this.position.mark(), this.popWarnings()))
}
