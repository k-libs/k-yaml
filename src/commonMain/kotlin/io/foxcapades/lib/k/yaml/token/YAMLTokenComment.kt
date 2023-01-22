package io.foxcapades.lib.k.yaml.token

import io.foxcapades.lib.k.yaml.scan.SourceWarning
import io.foxcapades.lib.k.yaml.util.SourcePosition
import io.foxcapades.lib.k.yaml.util.UByteString

class YAMLTokenComment(
           val value:    UByteString,
           val indent:   UInt,
           val trailing: Boolean,
  override val start:    SourcePosition,
  override val end:      SourcePosition,
  override val warnings: Array<SourceWarning>,
) : YAMLToken {
  override fun toString() =
    "YAMLTokenComment(" +
      "value=$value" +
      ", " +
      "indent=$indent" +
      ", " +
      "trailing=$trailing" +
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
      other is YAMLTokenComment
      && this.value.contentEquals(other.value)
      && this.indent == other.indent
      && this.trailing == other.trailing
      && this.start == other.start
      && this.end == other.end
      && this.warnings.contentEquals(other.warnings)
    )

  override fun hashCode() =
    this.value.contentHashCode() +
    this.indent.hashCode() +
    this.trailing.hashCode() +
    this.start.hashCode() +
    this.end.hashCode() +
    this.warnings.contentHashCode()
}