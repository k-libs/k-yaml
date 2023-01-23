package io.foxcapades.lib.k.yaml.token

import io.foxcapades.lib.k.yaml.warn.SourceWarning
import io.foxcapades.lib.k.yaml.util.SourcePosition
import io.foxcapades.lib.k.yaml.util.UByteString
import io.foxcapades.lib.k.yaml.util.toFlowSequence

data class YAMLTokenAlias(
           val alias:    UByteString,
  override val start:    SourcePosition,
  override val end:      SourcePosition,
           val indent:   UInt,
  override val warnings: Array<SourceWarning>
) : YAMLToken {
  override fun toString() =
    "Alias(alias=$alias, start=$start, end=$end, indent=$indent, warnings=${warnings.toFlowSequence()})"

  override fun hashCode() =
    alias.contentHashCode() +
    start.hashCode() +
    end.hashCode() +
    indent.hashCode() +
    warnings.contentHashCode()

  override fun equals(other: Any?) =
    this === other
    || (
      other is YAMLTokenAlias
      && this.alias.contentEquals(other.alias)
      && this.start == other.start
      && this.end == other.end
      && this.indent == other.indent
      && this.warnings.contentEquals(other.warnings)
    )
}