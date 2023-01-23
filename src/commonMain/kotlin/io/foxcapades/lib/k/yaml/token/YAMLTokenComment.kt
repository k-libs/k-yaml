package io.foxcapades.lib.k.yaml.token

import io.foxcapades.lib.k.yaml.warn.SourceWarning
import io.foxcapades.lib.k.yaml.util.SourcePosition
import io.foxcapades.lib.k.yaml.util.UByteString
import io.foxcapades.lib.k.yaml.util.toFlowSequence

class YAMLTokenComment(
           val value:    UByteString,
           val indent:   UInt,
           val trailing: Boolean,
  override val start:    SourcePosition,
  override val end:      SourcePosition,
  override val warnings: Array<SourceWarning>,
) : YAMLToken {
  override fun toString() =
    "Comment(value=$value, indent=$indent, trailing=$trailing, start=$start, end=$end, warnings=${warnings.toFlowSequence()})"

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