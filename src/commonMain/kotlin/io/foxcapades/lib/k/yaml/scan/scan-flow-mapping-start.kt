package io.foxcapades.lib.k.yaml.scan

import io.foxcapades.lib.k.yaml.util.SourcePosition

internal fun YAMLScannerImpl.fetchFlowMappingStartToken() {
  val start = position.mark()

  reader.skip(1)
  position.incPosition()

  emitFlowMappingStartToken(start, position.mark())
}

private fun YAMLScannerImpl.emitFlowMappingStartToken(start: SourcePosition, end: SourcePosition) {
  flows.push(FlowTypeMapping)
  tokens.push(newFlowMappingStartToken(start, end))
}
