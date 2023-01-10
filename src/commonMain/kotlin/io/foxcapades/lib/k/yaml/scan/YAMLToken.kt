package io.foxcapades.lib.k.yaml.scan

import io.foxcapades.lib.k.yaml.util.SourcePosition

data class YAMLToken(
  val type: YAMLTokenType,
  val data: YAMLTokenData,
  val start: SourcePosition,
  val end: SourcePosition,
)

