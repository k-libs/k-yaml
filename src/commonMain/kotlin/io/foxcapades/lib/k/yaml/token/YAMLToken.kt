package io.foxcapades.lib.k.yaml.token

import io.foxcapades.lib.k.yaml.scan.SourceWarning
import io.foxcapades.lib.k.yaml.util.SourcePosition

sealed interface YAMLToken {
  val start:    SourcePosition
  val end:      SourcePosition
  val warnings: Array<SourceWarning>
}

class YAMLTokenz(
  val type: YAMLTokenType,
  val data: YAMLTokenData?,
  val start: SourcePosition,
  val end: SourcePosition,
  val warnings: Array<SourceWarning>,
) {
  override fun toString() =
    "YAMLToken(type: $type, data: $data, start: $start, end: $end, warnings: ${warnings.contentToString()})"
}