package io.foxcapades.lib.k.yaml.scan

import io.foxcapades.lib.k.yaml.util.SourcePosition

class YAMLToken(
  val type: YAMLTokenType,
  val data: YAMLTokenData?,
  val start: SourcePosition,
  val end: SourcePosition,
  val warnings: Array<SourceWarning>,
) {
  override fun toString() =
    "YAMLToken(type: $type, data: $data, start: $start, end: $end, warnings: ${warnings.contentToString()})"
}