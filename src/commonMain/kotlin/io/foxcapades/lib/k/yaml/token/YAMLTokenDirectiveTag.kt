package io.foxcapades.lib.k.yaml.token

import io.foxcapades.lib.k.yaml.scan.SourceWarning
import io.foxcapades.lib.k.yaml.util.SourcePosition
import io.foxcapades.lib.k.yaml.util.UByteString

class YAMLTokenDirectiveTag(
           val handle:   UByteString,
           val prefix:   UByteString,
  override val start:    SourcePosition,
  override val end:      SourcePosition,
  override val warnings: Array<SourceWarning>,
) : YAMLToken {
  override fun toString() =
    "YAMLTokenDirectiveTag(" +
      "handle=$handle" +
      ", " +
      "prefix=$prefix" +
      ", " +
      "start=$start" +
      ", " +
      "end=$end" +
      ", " +
      "warnings=${warnings.joinToString(", ", "[", "]")}" +
      ")"

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