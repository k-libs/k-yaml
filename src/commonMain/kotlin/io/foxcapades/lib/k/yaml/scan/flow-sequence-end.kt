package io.foxcapades.lib.k.yaml.scan

import io.foxcapades.lib.k.yaml.util.SourcePosition


internal fun YAMLScannerImpl.fetchFlowSequenceEndToken() {
  val start = position.mark()

  reader.skip(1)
  position.incPosition()

  emitFlowSequenceEndToken(start, position.mark())
}

private fun YAMLScannerImpl.emitFlowSequenceEndToken(start: SourcePosition, end: SourcePosition) {
  if (inFlowSequence)
    flows.pop()

  tokens.push(newFlowSequenceEndToken(start, end))
}
