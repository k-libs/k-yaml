package io.foxcapades.lib.k.yaml.scan.tokens.token

import io.foxcapades.lib.k.yaml.warn.SourceWarning
import io.foxcapades.lib.k.yaml.util.SourcePosition
import io.foxcapades.lib.k.yaml.util.toFlowSequence

class YAMLTokenFlowSequenceEnd(
  override val start:    SourcePosition,
  override val end:      SourcePosition,
  override val warnings: Array<SourceWarning>
) : YAMLTokenFlow {
  override fun toString() =
    "FlowSequenceEnd(start=$start, end=$end, warnings=${warnings.toFlowSequence()})"

  override fun hashCode() =
    this.start.hashCode() + this.end.hashCode() + this.warnings.contentHashCode()

  override fun equals(other: Any?) =
    this === other
    || (
      other is YAMLTokenFlowSequenceEnd
      && this.start == other.start
      && this.end == other.end
      && this.warnings.contentEquals(other.warnings)
    )
}