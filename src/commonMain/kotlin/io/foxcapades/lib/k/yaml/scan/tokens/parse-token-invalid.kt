package io.foxcapades.lib.k.yaml.scan.tokens

import io.foxcapades.lib.k.yaml.token.YAMLTokenInvalid
import io.foxcapades.lib.k.yaml.util.SourcePosition
import io.foxcapades.lib.k.yaml.warn.SourceWarning

@Suppress("NOTHING_TO_INLINE")
internal inline fun YAMLStreamTokenizerImpl.emitInvalidToken(
  warning: String,
  start:   SourcePosition,
  end:     SourcePosition = this.position.mark(),
) {
  this.warnings.push(SourceWarning(warning, start, end))
  this.tokens.push(YAMLTokenInvalid(start, end, this.indent, popWarnings()))
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun YAMLStreamTokenizerImpl.emitInvalidToken(start: SourcePosition, end: SourcePosition) {
  this.tokens.push(YAMLTokenInvalid(start, end, this.indent, popWarnings()))
}