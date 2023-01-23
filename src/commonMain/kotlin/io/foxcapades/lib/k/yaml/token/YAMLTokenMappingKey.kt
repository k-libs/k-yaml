package io.foxcapades.lib.k.yaml.token

import io.foxcapades.lib.k.yaml.util.SourcePosition
import io.foxcapades.lib.k.yaml.util.toFlowSequence
import io.foxcapades.lib.k.yaml.warn.SourceWarning

data class YAMLTokenMappingKey(
  override val start:    SourcePosition,
  override val end:      SourcePosition,
  override val warnings: Array<SourceWarning>
) : YAMLToken {
  override fun toString() =
    "MappingKeyIndicator(start=$start, end=$end, warnings=${warnings.toFlowSequence()})"

  override fun hashCode() =
    start.hashCode() + end.hashCode() + warnings.contentHashCode()

  override fun equals(other: Any?) =
    this === other
    || (
      other is YAMLTokenMappingKey
      && this.start == other.start
      && this.end == other.end
      && this.warnings.contentEquals(other.warnings)
    )
}