package io.foxcapades.lib.k.yaml.warn

import io.foxcapades.lib.k.yaml.util.SourcePosition

/**
 * # YAML Source Warning
 *
 * Represents a warning for something that wasn't quite right about the YAML in
 * a YAML stream being processed.
 */
data class SourceWarning(
  /**
   * Warning message.
   */
  val message: String,

  /**
   * Start position in the source stream of the warning.
   */
  val start: SourcePosition,

  /**
   * End position in the source stream of the warning.
   */
  val end: SourcePosition,
)
