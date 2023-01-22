package io.foxcapades.lib.k.yaml.scan

import io.foxcapades.lib.k.yaml.token.YAMLToken
import io.foxcapades.lib.k.yaml.token.YAMLTokenType
import io.foxcapades.lib.k.yaml.util.SourcePosition


internal fun YAMLScannerImpl.fetchAmbiguousColonToken() {
  this.reader.cache(2)

  if (!(this.inFlow || this.reader.isBlankAnyBreakOrEOF(1)))
    return this.fetchPlainScalar()

  val start = this.position.mark()
  skipASCII(this.reader, this.position)
  return this.emitMappingValueIndicatorToken(start)
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun YAMLScannerImpl.emitMappingValueIndicatorToken(
  start: SourcePosition,
  end:   SourcePosition = this.position.mark(),
) {
  this.tokens.push(newMappingValueIndicatorToken(start, end))
}

/**
 * [MAPPING-VALUE][YAMLTokenType.MappingValue]
 */
@Suppress("NOTHING_TO_INLINE")
internal inline fun YAMLScannerImpl.newMappingValueIndicatorToken(start: SourcePosition, end: SourcePosition) =
  YAMLToken(YAMLTokenType.MappingValue, null, start, end, getWarnings())


