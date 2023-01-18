package io.foxcapades.lib.k.yaml.scan

internal fun YAMLScanner.fetchFlowMappingStartToken() {
  val start = this.position.mark()
  this.reader.skip(1)
  this.position.incPosition()
  this.tokens.push(this.newFlowMappingStartToken(start, this.position.mark()))
}