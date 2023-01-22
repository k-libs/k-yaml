package io.foxcapades.lib.k.yaml.token

import io.foxcapades.lib.k.yaml.scan.SourceWarning
import io.foxcapades.lib.k.yaml.util.SourcePosition
import io.foxcapades.lib.k.yaml.util.UByteString

class YAMLTokenScalarFolded(
  override val value:    UByteString,
           val indent:   UInt,
  override val start:    SourcePosition,
  override val end:      SourcePosition,
  override val warnings: Array<SourceWarning>,
) : YAMLTokenScalar {
  override fun toString() =
    "YAMLTokenScalarFolded(" +
      "value=$value" +
      ", " +
      "indent=$indent" +
      ", " +
      "start=$start" +
      ", " +
      "end=$end" +
      ", " +
      "warnings=${warnings.joinToString(", ", "[", "]")}" +
      ")"

  override fun equals(other: Any?) =
    this === other
    || (
      other is YAMLTokenScalarFolded
      && this.value.contentEquals(other.value)
      && this.indent == other.indent
      && this.start == other.start
      && this.end == other.end
      && this.warnings.contentEquals(other.warnings)
    )

  override fun hashCode() =
    this.value.contentHashCode() +
    this.indent.hashCode() +
    this.start.hashCode() +
    this.end.hashCode() +
    this.warnings.contentHashCode()
}