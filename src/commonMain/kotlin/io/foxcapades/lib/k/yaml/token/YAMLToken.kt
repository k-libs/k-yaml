package io.foxcapades.lib.k.yaml.token

import io.foxcapades.lib.k.yaml.scan.SourceWarning
import io.foxcapades.lib.k.yaml.util.SourcePosition
import io.foxcapades.lib.k.yaml.util.UByteString

sealed interface YAMLToken {
  val start:    SourcePosition
  val end:      SourcePosition
  val warnings: Array<SourceWarning>
}

sealed interface YAMLTokenScalar : YAMLToken {
  val value: UByteString
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