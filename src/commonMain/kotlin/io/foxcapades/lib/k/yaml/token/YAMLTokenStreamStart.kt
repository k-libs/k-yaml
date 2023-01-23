package io.foxcapades.lib.k.yaml.token

import io.foxcapades.lib.k.yaml.YAMLEncoding
import io.foxcapades.lib.k.yaml.warn.SourceWarning
import io.foxcapades.lib.k.yaml.util.SourcePosition
import io.foxcapades.lib.k.yaml.util.toFlowSequence

class YAMLTokenStreamStart(
  val encoding: YAMLEncoding,
  override val start:    SourcePosition,
  override val end:      SourcePosition,
  override val warnings: Array<SourceWarning>,
): YAMLToken {
  override fun toString() =
    "StreamStart(encoding=$encoding, start=$start, end=$end, warnings=${warnings.toFlowSequence()})"

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