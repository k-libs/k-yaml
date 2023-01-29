package io.foxcapades.lib.k.yaml.scan.stream

import io.foxcapades.lib.k.yaml.token.YAMLTokenFlowMappingEnd
import io.foxcapades.lib.k.yaml.util.SourcePosition


internal fun YAMLStreamTokenizerImpl.parseFlowMappingEndToken() {
  val start = position.mark()
  skipASCII(buffer, position)
  lineContentIndicator = LineContentIndicatorContent
  emitFlowMappingEndToken(start, position.mark())
}

internal fun YAMLStreamTokenizerImpl.emitFlowMappingEndToken(start: SourcePosition, end: SourcePosition) {
  if (inFlowMapping)
    flows.pop()

  tokens.push(YAMLTokenFlowMappingEnd(start, end, popWarnings()))
}
