package io.foxcapades.lib.k.yaml.scan

import io.foxcapades.lib.k.yaml.token.YAMLTokenFlowSequenceStart


internal fun YAMLStreamTokenizerImpl.parseFlowSequenceStartToken() {
  val start = this.position.mark()
  skipASCII(this.buffer, this.position)
  lineContentIndicator = LineContentIndicatorContent
  this.flows.push(FlowTypeSequence)
  this.tokens.push(YAMLTokenFlowSequenceStart(start, this.position.mark(), this.indent, this.popWarnings()))
}
