package io.foxcapades.lib.k.yaml.token

import io.foxcapades.lib.k.yaml.warn.SourceWarning
import io.foxcapades.lib.k.yaml.util.SourcePosition
import io.foxcapades.lib.k.yaml.util.toFlowSequence

/**
 * # Invalid YAML Token
 *
 * Represents a YAML token that is invalid according to the YAML specification.
 *
 * @since 0.1.0
 * @author Elizabeth Paige Harper - https://github.com/foxcapades
 */
data class YAMLTokenInvalid(
  override val start:    SourcePosition,
  override val end:      SourcePosition,
  override val warnings: Array<SourceWarning>
) : YAMLToken {
  override fun toString() =
    "Invalid(start=$start, end=$end, warnings=${warnings.toFlowSequence()})"

  override fun hashCode() =
    start.hashCode() + end.hashCode() + warnings.contentHashCode()

  override fun equals(other: Any?) =
    this === other
    || (
      other is YAMLTokenInvalid
      && this.start == other.start
      && this.end == other.end
      && this.warnings.contentEquals(other.warnings)
    )
}