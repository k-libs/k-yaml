package io.foxcapades.lib.k.yaml.token

import io.foxcapades.lib.k.yaml.warn.SourceWarning
import io.foxcapades.lib.k.yaml.util.SourcePosition
import io.foxcapades.lib.k.yaml.util.toFlowSequence

class YAMLTokenMappingValue(
  override val start:    SourcePosition,
  override val end:      SourcePosition,
           val indent:   UInt,
  override val warnings: Array<SourceWarning>
) : YAMLToken {
  override fun toString() =
    "MappingValueIndicator(start=$start, end=$end, indent=$indent, warnings=${warnings.toFlowSequence()}"

  override fun hashCode() =
    this.start.hashCode() + this.end.hashCode() + this.indent.hashCode() + this.warnings.contentHashCode()

  override fun equals(other: Any?) =
    this === other
    || (
      other is YAMLTokenMappingValue
      && this.start == other.start
      && this.end == other.end
      && this.indent == other.indent
      && this.warnings.contentEquals(other.warnings)
    )
}