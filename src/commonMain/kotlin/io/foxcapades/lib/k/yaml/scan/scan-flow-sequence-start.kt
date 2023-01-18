package io.foxcapades.lib.k.yaml.scan

internal fun YAMLScanner.fetchFlowSequenceStartToken() {
  val start = this.position.mark()
  this.reader.skip(1)
  this.position.incPosition()
  this.tokens.push(this.newFlowSequenceStartToken(start, this.position.mark()))
}