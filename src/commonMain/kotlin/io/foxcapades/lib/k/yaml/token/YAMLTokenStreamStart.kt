package io.foxcapades.lib.k.yaml.token

import io.foxcapades.lib.k.yaml.YAMLEncoding
import io.foxcapades.lib.k.yaml.scan.SourceWarning
import io.foxcapades.lib.k.yaml.util.SourcePosition

class YAMLTokenStreamStart(
           val encoding: YAMLEncoding,
  override val start:    SourcePosition,
  override val end:      SourcePosition,
  override val warnings: Array<SourceWarning>,
): YAMLToken {
  override fun toString() =
    "YAMLTokenStreamStart(" +
      "encoding=$encoding" +
      ", " +
      "start=$start" +
      ", " +
      "end=$end" +
      ", " +
      "warnings=${warnings.joinToString(", ", "[", "]")}" +
      ")"

  override fun hashCode() =
    encoding.hashCode() + start.hashCode() + end.hashCode() + warnings.contentHashCode()

  override fun equals(other: Any?) =
    (this === other)
    || (
      other is YAMLTokenStreamStart
      && this.encoding == other.encoding
      && this.start == other.start
      && this.end == other.end
      && this.warnings.contentEquals(other.warnings)
    )
}