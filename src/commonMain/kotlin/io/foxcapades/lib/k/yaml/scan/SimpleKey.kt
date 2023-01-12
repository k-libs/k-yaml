package io.foxcapades.lib.k.yaml.scan

import io.foxcapades.lib.k.yaml.util.SourcePosition

data class SimpleKey(
  var possible:    Boolean        = false,
  var required:    Boolean        = false,
  var tokenNumber: Int            = 0,
  var mark:        SourcePosition = SourcePosition(0u, 0u, 0u),
)
