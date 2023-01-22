package io.foxcapades.lib.k.yaml.token

import io.foxcapades.lib.k.yaml.scan.SourceWarning
import io.foxcapades.lib.k.yaml.util.SourcePosition

class YAMLTokenDocumentEnd(
  override val start:    SourcePosition,
  override val end:      SourcePosition,
  override val warnings: Array<SourceWarning>
) : YAMLToken {
  override fun toString() =
    "YAMLTokenDocumentEnd(" +
      "start=$start" +
      ", " +
      "end=$end" +
      ", " +
      "warnings=${warnings.joinToString(", ", "[", "]")}" +
      ")"

  override fun equals(other: Any?) =
    this === other
    || (
      other is YAMLTokenDocumentEnd
      && this.start == other.start
      && this.end == other.end
      && this.warnings.contentEquals(other.warnings)
    )

  override fun hashCode() =
    start.hashCode() +
    end.hashCode() +
    warnings.contentHashCode()
}