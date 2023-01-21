package io.foxcapades.lib.k.yaml.scan

import io.foxcapades.lib.k.yaml.util.SourcePosition


internal fun YAMLScannerImpl.fetchFlowSequenceStartToken() {
  val start = position.mark()

  reader.skip(1)
  position.incPosition()

  emitFlowSequenceStartToken(start, position.mark())
}

private fun YAMLScannerImpl.emitFlowSequenceStartToken(start: SourcePosition, end: SourcePosition) {
  flows.push(FlowTypeSequence)
  tokens.push(newFlowSequenceStartToken(start, end))
}
