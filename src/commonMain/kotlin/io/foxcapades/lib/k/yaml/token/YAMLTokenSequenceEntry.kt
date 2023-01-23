package io.foxcapades.lib.k.yaml.token

import io.foxcapades.lib.k.yaml.warn.SourceWarning
import io.foxcapades.lib.k.yaml.util.SourcePosition
import io.foxcapades.lib.k.yaml.util.toFlowSequence

data class YAMLTokenSequenceEntry(
           val indent:   UInt,
  override val start:    SourcePosition,
  override val end:      SourcePosition,
  override val warnings: Array<SourceWarning>
) : YAMLToken {
  override fun toString() =
    "SequenceEntryIndicator(indent=$indent, start=$start, end=$end, warnings=${warnings.toFlowSequence()})"

  override fun hashCode() =
    indent.hashCode() +
    start.hashCode() +
    end.hashCode() +
    warnings.contentHashCode()

  override fun equals(other: Any?) =
    this === other
    || (
      other is YAMLTokenSequenceEntry
      && this.indent == other.indent
      && this.start == other.start
      && this.end == other.end
      && this.warnings.contentEquals(other.warnings)
    )
}