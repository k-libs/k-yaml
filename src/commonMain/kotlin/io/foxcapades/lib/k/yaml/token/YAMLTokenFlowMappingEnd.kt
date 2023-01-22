package io.foxcapades.lib.k.yaml.token

import io.foxcapades.lib.k.yaml.scan.SourceWarning
import io.foxcapades.lib.k.yaml.util.SourcePosition

class YAMLTokenFlowMappingEnd(
  override val start:    SourcePosition,
  override val end:      SourcePosition,
  override val warnings: Array<SourceWarning>
) : YAMLToken {
  override fun toString() =
    "YAMLTokenFlowMappingEnd(start=$start, end=$end, warnings=${warnings.joinToString(", ", "[", "]")}"

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