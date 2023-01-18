package io.foxcapades.lib.k.yaml.scan

import io.foxcapades.lib.k.yaml.util.SourcePosition

internal fun YAMLScanner.fetchFlowMappingStartToken() {
  val start = this.position.mark()

  this.reader.skip(1)
  this.position.incPosition()

  this.emitFlowMappingStartToken(start, this.position.mark())
}

internal fun YAMLScanner.emitFlowMappingStartToken(start: SourcePosition, end: SourcePosition) {
  this.flows.push(FlowTypeMapping)
  this.tokens.push(this.newFlowMappingStartToken(start, end))
}