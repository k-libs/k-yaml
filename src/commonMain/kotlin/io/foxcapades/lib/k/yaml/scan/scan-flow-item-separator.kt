package io.foxcapades.lib.k.yaml.scan

import io.foxcapades.lib.k.yaml.token.YAMLTokenFlowItemSeparator

internal fun YAMLScannerImpl.fetchFlowItemSeparatorToken() {
  // We have content on this line.
  this.haveContentOnThisLine = true

  val start = this.position.mark()
  skipASCII(this.reader, this.position)
  tokens.push(YAMLTokenFlowItemSeparator(start, this.position.mark(), this.getWarnings()))
}
