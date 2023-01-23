package io.foxcapades.lib.k.yaml.token

import io.foxcapades.lib.k.yaml.warn.SourceWarning
import io.foxcapades.lib.k.yaml.util.SourcePosition
import io.foxcapades.lib.k.yaml.util.UByteString
import io.foxcapades.lib.k.yaml.util.toFlowSequence

data class YAMLTokenScalarPlain(
  override val value:    UByteString,
  override val indent:   UInt,
  override val start:    SourcePosition,
  override val end:      SourcePosition,
  override val warnings: Array<SourceWarning>
) : YAMLTokenScalar {
  override fun toString() =
    "PlainScalar(value=$value, indent=$indent, start=$start, end=$end, $warnings=${warnings.toFlowSequence()})"

  override fun hashCode() =
    value.contentHashCode() +
    indent.hashCode() +
    start.hashCode() +
    end.hashCode() +
    warnings.contentHashCode()

  override fun equals(other: Any?) =
    this === other
    || (
      other is YAMLTokenScalarPlain
      && this.value.contentEquals(other.value)
      && this.indent == other.indent
      && this.start == other.start
      && this.end == other.end
      && this.warnings.contentEquals(other.warnings)
    )
}