package io.foxcapades.lib.k.yaml.token

import io.foxcapades.lib.k.yaml.util.SourcePosition
import io.foxcapades.lib.k.yaml.util.UByteString
import io.foxcapades.lib.k.yaml.util.toFlowSequence
import io.foxcapades.lib.k.yaml.warn.SourceWarning

data class YAMLTokenTag(
           val handle:   UByteString,
           val suffix:   UByteString,
  override val indent:   UInt,
  override val start:    SourcePosition,
  override val end:      SourcePosition,
  override val warnings: Array<SourceWarning>,
) : YAMLTokenNodeProperty {
  override fun toString() =
    "Tag(handle=$handle, suffix=$suffix, indent=$indent, start=$start, end=$end, warnings=${warnings.toFlowSequence()})"

  override fun hashCode() =
    handle.contentHashCode() +
    suffix.contentHashCode() +
    indent.hashCode() +
    start.hashCode() +
    end.hashCode() +
    warnings.contentHashCode()

  override fun equals(other: Any?) =
    this === other
    || (
      other is YAMLTokenTag
      && this.handle.contentEquals(other.handle)
      && this.suffix.contentEquals(other.suffix)
      && this.indent == other.indent
      && this.start == other.start
      && this.end == other.end
      && this.warnings.contentEquals(other.warnings)
    )
}