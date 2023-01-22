package io.foxcapades.lib.k.yaml.scan

import io.foxcapades.lib.k.yaml.token.YAMLToken
import io.foxcapades.lib.k.yaml.token.YAMLTokenTypeMappingValue
import io.foxcapades.lib.k.yaml.util.SourcePosition

internal fun YAMLScannerImpl.fetchMappingValueIndicatorToken() {
  haveContentOnThisLine = true

  val start = this.position.mark()
  skipASCII(this.reader, this.position)
  return this.emitMappingValueIndicatorToken(start)
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun YAMLScannerImpl.emitMappingValueIndicatorToken(
  start: SourcePosition,
  end: SourcePosition = this.position.mark(),
) {
  this.tokens.push(newMappingValueIndicatorToken(start, end))
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun YAMLScannerImpl.newMappingValueIndicatorToken(start: SourcePosition, end: SourcePosition) =
  YAMLToken(YAMLTokenTypeMappingValue, null, start, end, getWarnings())
