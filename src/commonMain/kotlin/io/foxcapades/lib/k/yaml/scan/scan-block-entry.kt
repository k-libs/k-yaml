package io.foxcapades.lib.k.yaml.scan

import io.foxcapades.lib.k.yaml.token.YAMLToken
import io.foxcapades.lib.k.yaml.token.YAMLTokenDataSequenceEntry
import io.foxcapades.lib.k.yaml.token.YAMLTokenTypeBlockEntryIndicator
import io.foxcapades.lib.k.yaml.util.SourcePosition

internal fun YAMLScannerImpl.fetchBlockEntryIndicatorToken() {
  val start = this.position.mark()
  skipASCII(this.reader, this.position)
  emitSequenceEntryIndicator(this.indent, start)
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun YAMLScannerImpl.emitSequenceEntryIndicator(
  indent: UInt,
  start:  SourcePosition,
  end:    SourcePosition = this.position.mark()
)
  = this.tokens.push(newSequenceEntryIndicatorToken(indent, start, end, this.getWarnings()))

@Suppress("NOTHING_TO_INLINE")
private inline fun newSequenceEntryIndicatorToken(
  indent:   UInt,
  start:    SourcePosition,
  end:      SourcePosition,
  warnings: Array<SourceWarning>,
) =
  YAMLToken(YAMLTokenTypeBlockEntryIndicator, YAMLTokenDataSequenceEntry(indent), start, end, warnings)
