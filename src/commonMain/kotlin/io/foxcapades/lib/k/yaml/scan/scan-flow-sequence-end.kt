package io.foxcapades.lib.k.yaml.scan

internal fun YAMLScanner.fetchFlowSequenceEndToken() {
  val start = this.position.mark()
  this.reader.skip(1)
  this.position.incPosition()
  this.tokens.push(this.newFlowSequenceEndToken(start, this.position.mark()))
}