package io.foxcapades.lib.k.yaml.scan.events.event

import io.foxcapades.lib.k.yaml.util.ImmutableArray
import io.foxcapades.lib.k.yaml.util.SourcePosition

data class YAMLEvent(
  var type:     YAMLEventType,
  var data:     YAMLEventData?,
  var start:    SourcePosition,
  var end:      SourcePosition,
  var warnings: ImmutableArray<String>,
)

