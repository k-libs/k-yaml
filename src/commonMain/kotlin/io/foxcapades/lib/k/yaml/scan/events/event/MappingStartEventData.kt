package io.foxcapades.lib.k.yaml.scan.events.event

import io.foxcapades.lib.k.yaml.YAMLMappingStyle
import io.foxcapades.lib.k.yaml.YAMLTag

data class MappingStartEventData(
  val tag: YAMLTag?,
  val anchor: String?,
  val style: YAMLMappingStyle,
) : YAMLEventData