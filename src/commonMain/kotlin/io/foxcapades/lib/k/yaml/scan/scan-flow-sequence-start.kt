package io.foxcapades.lib.k.yaml.scan

import io.foxcapades.lib.k.yaml.token.YAMLTokenFlowSequenceStart


internal fun YAMLScannerImpl.fetchFlowSequenceStartToken() {
  // We have content on this line.
  this.haveContentOnThisLine = true

  val start = this.position.mark()

  skipASCII(this.reader, this.position)

  this.flows.push(FlowTypeSequence)
  this.tokens.push(YAMLTokenFlowSequenceStart(start, this.position.mark(), this.getWarnings()))
}
