package io.foxcapades.lib.k.yaml.scan.tokens.token

import io.foxcapades.lib.k.yaml.YAMLTokenScanner
import io.foxcapades.lib.k.yaml.util.SourcePosition
import io.foxcapades.lib.k.yaml.warn.SourceWarning

/**
 * # YAML Token
 *
 * Represents a generic YAML token which is to be emitted by the YAML stream
 * scanner & tokenizer [YAMLTokenScanner].
 *
 * This is a sealed interface, meaning comprehensive when switches may be
 * constructed on the type of value implementing this interface.
 *
 * @since 0.1.0
 * @author Elizabeth Paige Harper - https://github.com/foxcapades
 */
sealed interface YAMLToken {

  /**
   * Token start position.
   */
  val start: SourcePosition

  /**
   * Token end position.
   */
  val end: SourcePosition

  /**
   * Warnings kicked up by the [YAMLTokenScanner] while parsing this token.
   */
  val warnings: Array<SourceWarning>
}

sealed interface YAMLTokenBlock : io.foxcapades.lib.k.yaml.scan.tokens.token.YAMLToken