package io.foxcapades.lib.k.yaml.token

import io.foxcapades.lib.k.yaml.warn.SourceWarning
import io.foxcapades.lib.k.yaml.util.SourcePosition
import io.foxcapades.lib.k.yaml.util.UByteString
import io.foxcapades.lib.k.yaml.util.toFlowSequence

data class YAMLTokenAnchor(
           val anchor:   UByteString,
  override val start:    SourcePosition,
  override val end:      SourcePosition,
  override val warnings: Array<SourceWarning>
) : YAMLToken {
  override fun toString() =
    "Anchor(anchor=$anchor, start=$start, end=$end, warnings=${warnings.toFlowSequence()})"

  override fun hashCode() =
    anchor.contentHashCode() +
    start.hashCode() +
    end.hashCode() +
    warnings.contentHashCode()

  override fun equals(other: Any?) =
    this === other
    || (
      other is YAMLTokenAnchor
      && this.anchor.contentEquals(other.anchor)
      && this.start == other.start
      && this.end == other.end
      && this.warnings.contentEquals(other.warnings)
    )
}