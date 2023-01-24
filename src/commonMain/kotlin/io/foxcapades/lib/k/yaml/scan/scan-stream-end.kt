package io.foxcapades.lib.k.yaml.scan

import io.foxcapades.lib.k.yaml.token.YAMLTokenStreamEnd
import io.foxcapades.lib.k.yaml.util.SourcePosition


internal fun YAMLScannerImpl.parseStreamEndToken() {
  val mark = position.mark()
  tokens.push(newStreamEndToken(mark, mark))
}

@Suppress("NOTHING_TO_INLINE")
private inline fun YAMLScannerImpl.newStreamEndToken(start: SourcePosition, end: SourcePosition) =
  YAMLTokenStreamEnd(start, end, popWarnings())
