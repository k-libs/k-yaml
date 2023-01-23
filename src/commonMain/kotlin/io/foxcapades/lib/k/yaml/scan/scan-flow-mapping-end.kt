package io.foxcapades.lib.k.yaml.scan

import io.foxcapades.lib.k.yaml.token.YAMLTokenFlowMappingEnd
import io.foxcapades.lib.k.yaml.util.SourcePosition


internal fun YAMLScannerImpl.fetchFlowMappingEndToken() {
  // We have content on this line.
  this.haveContentOnThisLine = true

  val start = position.mark()

  reader.skip(1)
  position.incPosition()

  emitFlowMappingEndToken(start, position.mark())
}

internal fun YAMLScannerImpl.emitFlowMappingEndToken(start: SourcePosition, end: SourcePosition) {
  if (inFlowMapping)
    flows.pop()

  tokens.push(YAMLTokenFlowMappingEnd(start, end, getWarnings()))
}
