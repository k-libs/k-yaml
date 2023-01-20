package io.foxcapades.lib.k.yaml.scan

import io.foxcapades.lib.k.yaml.util.SourcePosition

internal fun YAMLScannerImpl.fetchFlowSequenceStartToken() {
  val start = this.position.mark()

  this.reader.skip(1)
  this.position.incPosition()

  this.emitFlowSequenceStartToken(start, this.position.mark())
}

internal fun YAMLScannerImpl.emitFlowSequenceStartToken(start: SourcePosition, end: SourcePosition) {
  this.flows.push(FlowTypeSequence)
  this.tokens.push(this.newFlowSequenceStartToken(start, end))
}