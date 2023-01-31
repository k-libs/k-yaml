package io.foxcapades.lib.k.yaml.scan.tokens.token

import io.foxcapades.lib.k.yaml.warn.SourceWarning
import io.foxcapades.lib.k.yaml.util.SourcePosition
import io.foxcapades.lib.k.yaml.util.UByteString
import io.foxcapades.lib.k.yaml.util.toFlowSequence

class YAMLTokenDirectiveTag(
           val handle:   UByteString,
           val prefix:   UByteString,
  override val start:    SourcePosition,
  override val end:      SourcePosition,
  override val warnings: Array<SourceWarning>,
) : YAMLTokenDirective {
  override fun toString() =
    "TagDirective(handle=$handle, prefix=$prefix, start=$start, end=$end, warnings=${warnings.toFlowSequence()})"

  override fun hashCode() =
    handle.contentHashCode() +
    prefix.contentHashCode() +
    start.hashCode() +
    end.hashCode() +
    warnings.contentHashCode()

  override fun equals(other: Any?) =
    this === other
    || (
      other is YAMLTokenDirectiveTag
      && this.handle.contentEquals(other.handle)
      && this.prefix.contentEquals(other.prefix)
      && this.start == other.start
      && this.end == other.end
      && this.warnings.contentEquals(other.warnings)
    )
}