package io.foxcapades.lib.k.yaml.token

import io.foxcapades.lib.k.yaml.scan.SourceWarning
import io.foxcapades.lib.k.yaml.util.SourcePosition

class YAMLTokenDirectiveYAML(
           val majorVersion: UInt,
           val minorVersion: UInt,
  override val start:        SourcePosition,
  override val end:          SourcePosition,
  override val warnings:     Array<SourceWarning>,
) : YAMLToken {
  override fun toString() =
    "YAMLTokenDirectiveYAML(" +
      "majorVersion=$majorVersion, " +
      "minorVersion=$minorVersion, " +
      "start=$start, " +
      "end=$end, " +
      "warnings=${warnings.joinToString(", ", "[", "]")}" +
      ")"

  override fun hashCode() =
    majorVersion.hashCode() +
    minorVersion.hashCode() +
    start.hashCode() +
    end.hashCode() +
    warnings.contentHashCode()

  override fun equals(other: Any?) =
    this === other
    || (
      other is YAMLTokenDirectiveYAML
      && this.majorVersion == other.majorVersion
      && this.minorVersion == other.minorVersion
      && this.start == other.start
      && this.end == other.end
      && this.warnings.contentEquals(other.warnings)
    )
}