package io.foxcapades.lib.k.yaml.scan

import io.foxcapades.lib.k.yaml.util.SourcePosition

data class SimpleKey(
  var possible: Boolean,
  var required: Boolean,
  var tokenNumber: Int,
  var mark: SourcePosition,
)
