package io.foxcapades.lib.k.yaml.scan.tokens.token

import io.foxcapades.lib.k.yaml.warn.SourceWarning
import io.foxcapades.lib.k.yaml.util.SourcePosition
import io.foxcapades.lib.k.yaml.util.toFlowSequence

class YAMLTokenStreamEnd(
  override val start:    SourcePosition,
  override val end:      SourcePosition,
  override val warnings: Array<SourceWarning>
) : io.foxcapades.lib.k.yaml.scan.tokens.token.YAMLToken {
  override fun toString() =
    "StreamEnd(start=$start, end=$end, warnings=${warnings.toFlowSequence()}"

  override fun hashCode() =
    this.start.hashCode() + this.end.hashCode() + this.warnings.contentHashCode()

  override fun equals(other: Any?) =
    this === other
    || (
      other is YAMLTokenStreamEnd
      && this.start == other.start
      && this.end == other.end
      && this.warnings.contentEquals(other.warnings)
    )
}