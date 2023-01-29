package io.foxcapades.lib.k.yaml.token

import io.foxcapades.lib.k.yaml.warn.SourceWarning
import io.foxcapades.lib.k.yaml.util.SourcePosition
import io.foxcapades.lib.k.yaml.util.UByteString
import io.foxcapades.lib.k.yaml.util.toFlowSequence

data class YAMLTokenAnchor(
           val anchor:   UByteString,
  override val start:    SourcePosition,
  override val end:      SourcePosition,
  override val indent:   UInt,
  override val warnings: Array<SourceWarning>
) : YAMLTokenNodeProperty {
  override fun toString() =
    "Anchor(anchor=$anchor, start=$start, end=$end, indent=$indent, warnings=${warnings.toFlowSequence()})"

  override fun hashCode() =
    anchor.contentHashCode() +
    start.hashCode() +
    end.hashCode() +
    indent.hashCode() +
    warnings.contentHashCode()

  override fun equals(other: Any?) =
    this === other
    || (
      other is YAMLTokenAnchor
      && this.anchor.contentEquals(other.anchor)
      && this.start == other.start
      && this.end == other.end
      && this.indent == other.indent
      && this.warnings.contentEquals(other.warnings)
    )
}