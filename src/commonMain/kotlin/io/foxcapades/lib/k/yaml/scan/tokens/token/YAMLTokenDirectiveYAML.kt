package io.foxcapades.lib.k.yaml.scan.tokens.token

import io.foxcapades.lib.k.yaml.warn.SourceWarning
import io.foxcapades.lib.k.yaml.util.SourcePosition
import io.foxcapades.lib.k.yaml.util.toFlowSequence

class YAMLTokenDirectiveYAML(
           val majorVersion: UInt,
           val minorVersion: UInt,
  override val start:        SourcePosition,
  override val end:          SourcePosition,
  override val warnings:     Array<SourceWarning>,
) : YAMLTokenDirective {
  override fun toString() =
    "YAMLDirective(majorVersion=$majorVersion, minorVersion=$minorVersion, start=$start, end=$end, warnings=${warnings.toFlowSequence()})"

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