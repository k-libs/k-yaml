package io.foxcapades.lib.k.yaml.scan

internal fun YAMLScanner.fetchFlowMappingEndToken() {
  val start = this.position.mark()
  this.reader.skip(1)
  this.position.incPosition()
  this.tokens.push(this.newFlowMappingEndToken(start, this.position.mark()))
}