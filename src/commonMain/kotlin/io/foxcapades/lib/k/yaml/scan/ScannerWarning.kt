package io.foxcapades.lib.k.yaml.scan

import io.foxcapades.lib.k.yaml.util.SourcePosition

data class ScannerWarning(
  val message: String,
  val position: SourcePosition,
)
