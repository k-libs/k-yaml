package io.foxcapades.lib.k.yaml.scan

import io.foxcapades.lib.k.yaml.token.YAMLTokenSequenceEntry
import io.foxcapades.lib.k.yaml.util.SourcePosition

/**
 * # Fetch Sequence Entry Indicator Token
 *
 * Emits a [sequence entry indicator token][YAMLTokenSequenceEntry].
 *
 * @since 0.1.0
 * @author Elizabeth Paige Harper - https://github.com/foxcapades
 */
internal fun YAMLScannerImpl.fetchSequenceEntryIndicator() {
  this.haveContentOnThisLine = true

  val start = this.position.mark()
  skipASCII(this.reader, this.position)
  this.emitSequenceEntryIndicator(this.indent, start)
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun YAMLScannerImpl.emitSequenceEntryIndicator(
  indent: UInt,
  start:  SourcePosition,
  end:    SourcePosition = this.position.mark()
)
  = this.tokens.push(YAMLTokenSequenceEntry(indent, start, end, this.getWarnings()))
