package io.foxcapades.lib.k.yaml.scan

import io.foxcapades.lib.k.yaml.util.SourcePosition

internal fun YAMLScanner.fetchFlowSequenceEndToken() {
  val start = this.position.mark()

  this.reader.skip(1)
  this.position.incPosition()

  this.emitFlowSequenceEndToken(start, this.position.mark())
}

internal fun YAMLScanner.emitFlowSequenceEndToken(start: SourcePosition, end: SourcePosition) {
  if (this.inFlowSequence)
    this.flows.pop()

  this.tokens.push(this.newFlowSequenceEndToken(start, end))

}