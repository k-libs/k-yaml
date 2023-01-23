package io.foxcapades.lib.k.yaml.token

import io.foxcapades.lib.k.yaml.warn.SourceWarning
import io.foxcapades.lib.k.yaml.util.SourcePosition
import io.foxcapades.lib.k.yaml.util.toFlowSequence

class YAMLTokenDocumentEnd(
  override val start:    SourcePosition,
  override val end:      SourcePosition,
  override val warnings: Array<SourceWarning>
) : YAMLToken {
  override fun toString() =
    "DocumentEnd(start=$start, end=$end, warnings=${warnings.toFlowSequence()})"

  override fun equals(other: Any?) =
    this === other
    || (
      other is YAMLTokenDocumentEnd
      && this.start == other.start
      && this.end == other.end
      && this.warnings.contentEquals(other.warnings)
    )

  override fun hashCode() =
    start.hashCode() +
    end.hashCode() +
    warnings.contentHashCode()
}