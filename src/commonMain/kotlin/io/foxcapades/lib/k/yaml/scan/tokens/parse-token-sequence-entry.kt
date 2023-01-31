package io.foxcapades.lib.k.yaml.scan.tokens

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
internal fun YAMLStreamTokenizerImpl.fetchSequenceEntryIndicator() {
  val start = this.position.mark()

  skipASCII(this.buffer, this.position)

  this.emitSequenceEntryIndicator(this.indent, start)

  if (!lineContentIndicator.haveHardContent) {
    lineContentIndicator = LineContentIndicatorBlanksAndIndicators
    indent++
  }
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun YAMLStreamTokenizerImpl.emitSequenceEntryIndicator(
  indent: UInt,
  start:  SourcePosition,
  end:    SourcePosition = this.position.mark()
)
  = this.tokens.push(YAMLTokenSequenceEntry(indent, start, end, this.popWarnings()))
