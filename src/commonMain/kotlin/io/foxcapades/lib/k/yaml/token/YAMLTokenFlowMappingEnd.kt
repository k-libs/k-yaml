package io.foxcapades.lib.k.yaml.token

import io.foxcapades.lib.k.yaml.warn.SourceWarning
import io.foxcapades.lib.k.yaml.util.SourcePosition
import io.foxcapades.lib.k.yaml.util.toFlowSequence

class YAMLTokenFlowMappingEnd(
  override val start:    SourcePosition,
  override val end:      SourcePosition,
  override val warnings: Array<SourceWarning>
) : YAMLToken {
  override fun toString() =
    "FlowMappingEnd(start=$start, end=$end, warnings=${warnings.toFlowSequence()})"

  override fun hashCode() =
    this.start.hashCode() + this.end.hashCode() + this.warnings.contentHashCode()

  override fun equals(other: Any?) =
    this === other
    || (
      other is YAMLTokenFlowMappingEnd
      && this.start == other.start
      && this.end == other.end
      && this.warnings.contentEquals(other.warnings)
    )
}