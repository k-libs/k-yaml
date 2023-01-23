package io.foxcapades.lib.k.yaml.scan

import io.foxcapades.lib.k.yaml.token.YAMLTokenFlowSequenceEnd


internal fun YAMLScannerImpl.fetchFlowSequenceEndToken() {
  // We have content on this line.
  this.haveContentOnThisLine = true

  val start = this.position.mark()

  skipASCII(this.reader, this.position)

  if (this.inFlowSequence)
    this.flows.pop()

  this.tokens.push(YAMLTokenFlowSequenceEnd(start, this.position.mark(), this.getWarnings()))
}
