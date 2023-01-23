package io.foxcapades.lib.k.yaml.token

import io.foxcapades.lib.k.yaml.util.SourcePosition
import io.foxcapades.lib.k.yaml.util.UByteString
import io.foxcapades.lib.k.yaml.util.toFlowSequence
import io.foxcapades.lib.k.yaml.warn.SourceWarning

// TODO: Tag should probably have indent as well since it can start a line
data class YAMLTokenTag(
           val handle:   UByteString,
           val suffix:   UByteString,
  override val start:    SourcePosition,
  override val end:      SourcePosition,
  override val warnings: Array<SourceWarning>,
) : YAMLToken {
  override fun toString() =
    "Tag(handle=$handle, suffix=$suffix, start=$start, end=$end, warnings=${warnings.toFlowSequence()})"

  override fun hashCode() =
    handle.contentHashCode() +
    suffix.contentHashCode() +
    start.hashCode() +
    end.hashCode() +
    warnings.contentHashCode()

  override fun equals(other: Any?) =
    this === other
    || (
      other is YAMLTokenTag
      && this.handle.contentEquals(other.handle)
      && this.suffix.contentEquals(other.suffix)
      && this.start == other.start
      && this.end == other.end
      && this.warnings.contentEquals(other.warnings)
    )
}