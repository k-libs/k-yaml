package io.foxcapades.lib.k.yaml.scan

import io.foxcapades.lib.k.yaml.token.YAMLTokenFlowMappingStart

internal fun YAMLScannerImpl.fetchFlowMappingStartToken() {
  // We have content on this line.
  this.haveContentOnThisLine = true

  val start = this.position.mark()
  skipASCII(this.reader, this.position)
  this.flows.push(FlowTypeMapping)
  this.tokens.push(YAMLTokenFlowMappingStart(start, this.position.mark(), this.indent, this.getWarnings()))
}
