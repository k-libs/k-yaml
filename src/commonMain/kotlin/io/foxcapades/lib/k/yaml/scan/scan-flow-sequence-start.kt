package io.foxcapades.lib.k.yaml.scan

import io.foxcapades.lib.k.yaml.token.YAMLTokenFlowSequenceStart


internal fun YAMLScannerImpl.fetchFlowSequenceStartToken() {
  val start = this.position.mark()
  skipASCII(this.reader, this.position)
  lineContentIndicator = LineContentIndicatorContent
  this.flows.push(FlowTypeSequence)
  this.tokens.push(YAMLTokenFlowSequenceStart(start, this.position.mark(), this.indent, this.popWarnings()))
}
