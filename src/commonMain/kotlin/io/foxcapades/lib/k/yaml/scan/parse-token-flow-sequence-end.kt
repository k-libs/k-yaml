package io.foxcapades.lib.k.yaml.scan

import io.foxcapades.lib.k.yaml.token.YAMLTokenFlowSequenceEnd


internal fun YAMLScannerImpl.parseFlowSequenceEndToken() {
  val start = this.position.mark()

  skipASCII(this.reader, this.position)

  lineContentIndicator = LineContentIndicatorContent

  if (this.inFlowSequence)
    this.flows.pop()

  this.tokens.push(YAMLTokenFlowSequenceEnd(start, this.position.mark(), this.popWarnings()))
}
