package io.foxcapades.lib.k.yaml.scan

import io.foxcapades.lib.k.yaml.token.YAMLToken
import io.foxcapades.lib.k.yaml.token.YAMLTokenType
import io.foxcapades.lib.k.yaml.util.SourcePosition

internal fun YAMLScannerImpl.fetchFlowItemSeparatorToken() {
  val start = position.mark()
  skipASCII()
  tokens.push(newFlowItemSeparatorToken(start, position.mark()))
}

private fun YAMLScannerImpl.newFlowItemSeparatorToken(start: SourcePosition, end: SourcePosition) =
  YAMLToken(YAMLTokenType.FlowEntry, null, start, end, getWarnings())
