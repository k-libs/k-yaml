package io.foxcapades.lib.k.yaml.scan

import io.foxcapades.lib.k.yaml.token.YAMLTokenFlowItemSeparator

internal fun YAMLScannerImpl.fetchFlowItemSeparatorToken() {
  val start = this.position.mark()
  skipASCII(this.reader, this.position)
  this.lineContentIndicator = LineContentIndicatorContent
  tokens.push(YAMLTokenFlowItemSeparator(start, this.position.mark(), this.popWarnings()))
}
