package io.foxcapades.lib.k.yaml.scan

import io.foxcapades.lib.k.yaml.util.SourcePosition

internal fun YAMLScannerImpl.fetchFlowMappingEndToken() {
  val start = this.position.mark()

  this.reader.skip(1)
  this.position.incPosition()

  this.emitFlowMappingEndToken(start, this.position.mark())
}

internal fun YAMLScannerImpl.emitFlowMappingEndToken(start: SourcePosition, end: SourcePosition) {
  if (this.inFlowMapping)
    this.flows.pop()

  this.tokens.push(this.newFlowMappingEndToken(start, end))
}