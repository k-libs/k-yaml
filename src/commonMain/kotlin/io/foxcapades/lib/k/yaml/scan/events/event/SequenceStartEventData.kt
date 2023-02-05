package io.foxcapades.lib.k.yaml.scan.events.event

import io.foxcapades.lib.k.yaml.YAMLSequenceStyle
import io.foxcapades.lib.k.yaml.YAMLTag

data class SequenceStartEventData(
  val tag: YAMLTag?,
  val anchor: String?,
  val style: YAMLSequenceStyle,
) : YAMLEventData