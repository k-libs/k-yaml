package io.foxcapades.lib.k.yaml.scan

import io.foxcapades.lib.k.yaml.token.YAMLTokenFlowMappingEnd
import io.foxcapades.lib.k.yaml.util.SourcePosition


internal fun YAMLScannerImpl.parseFlowMappingEndToken() {
  val start = position.mark()
  skipASCII(reader, position)
  lineContentIndicator = LineContentIndicatorContent
  emitFlowMappingEndToken(start, position.mark())
}

internal fun YAMLScannerImpl.emitFlowMappingEndToken(start: SourcePosition, end: SourcePosition) {
  if (inFlowMapping)
    flows.pop()

  tokens.push(YAMLTokenFlowMappingEnd(start, end, popWarnings()))
}
