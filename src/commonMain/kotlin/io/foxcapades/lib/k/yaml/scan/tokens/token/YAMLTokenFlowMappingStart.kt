package io.foxcapades.lib.k.yaml.scan.tokens.token

import io.foxcapades.lib.k.yaml.warn.SourceWarning
import io.foxcapades.lib.k.yaml.util.SourcePosition
import io.foxcapades.lib.k.yaml.util.toFlowSequence

class YAMLTokenFlowMappingStart(
  override val start:    SourcePosition,
  override val end:      SourcePosition,
           val indent:   UInt,
  override val warnings: Array<SourceWarning>
) : YAMLTokenFlow {
  override fun toString() =
    "FlowMappingStart(start=$start, end=$end, indent=$indent, warnings=${warnings.toFlowSequence()})"

  override fun hashCode() =
    this.start.hashCode() + this.end.hashCode() + this.indent.hashCode() + this.warnings.contentHashCode()

  override fun equals(other: Any?) =
    this === other
    || (
      other is YAMLTokenFlowMappingStart
      && this.start == other.start
      && this.end == other.end
      && this.indent == other.indent
      && this.warnings.contentEquals(other.warnings)
    )
}