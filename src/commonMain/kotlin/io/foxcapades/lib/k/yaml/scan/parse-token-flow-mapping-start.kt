package io.foxcapades.lib.k.yaml.scan

import io.foxcapades.lib.k.yaml.token.YAMLTokenFlowMappingStart

internal fun YAMLStreamTokenizerImpl.parseFlowMappingStartToken() {
  val start = this.position.mark()
  skipASCII(this.reader, this.position)
  lineContentIndicator = LineContentIndicatorContent
  this.flows.push(FlowTypeMapping)
  this.tokens.push(YAMLTokenFlowMappingStart(start, this.position.mark(), this.indent, this.popWarnings()))
}
