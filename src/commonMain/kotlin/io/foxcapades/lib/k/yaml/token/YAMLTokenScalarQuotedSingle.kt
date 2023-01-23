package io.foxcapades.lib.k.yaml.token

import io.foxcapades.lib.k.yaml.warn.SourceWarning
import io.foxcapades.lib.k.yaml.util.SourcePosition
import io.foxcapades.lib.k.yaml.util.UByteString
import io.foxcapades.lib.k.yaml.util.toFlowSequence

data class YAMLTokenScalarQuotedSingle(
  override val value:    UByteString,
  override val start:    SourcePosition,
  override val end:      SourcePosition,
  override val indent:   UInt,
  override val warnings: Array<SourceWarning>,
) : YAMLTokenScalar {
  override fun toString() =
    "SingleQuotedScalar(value=$value, start=$start, end=$end, indent=$indent, warnings=${warnings.toFlowSequence()})"

  override fun hashCode() =
    value.contentHashCode() +
    start.hashCode() +
    end.hashCode() +
    indent.hashCode() +
    warnings.contentHashCode()

  override fun equals(other: Any?) =
    this === other
    || (
      other is YAMLTokenScalarQuotedSingle
      && this.value.contentEquals(other.value)
      && this.start == other.start
      && this.end == other.end
      && this.indent == other.indent
      && this.warnings.contentEquals(other.warnings)
    )
}