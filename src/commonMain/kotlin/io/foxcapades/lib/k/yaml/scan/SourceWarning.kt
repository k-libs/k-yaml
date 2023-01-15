package io.foxcapades.lib.k.yaml.scan

import io.foxcapades.lib.k.yaml.util.SourcePosition

data class SourceWarning(
  val message: String,
  val start: SourcePosition,
  val end: SourcePosition,
)
