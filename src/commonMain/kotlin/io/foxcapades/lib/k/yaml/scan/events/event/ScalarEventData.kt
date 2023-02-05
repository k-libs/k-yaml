package io.foxcapades.lib.k.yaml.scan.events.event

import io.foxcapades.lib.k.yaml.YAMLScalarStyle
import io.foxcapades.lib.k.yaml.YAMLTag
import io.foxcapades.lib.k.yaml.util.UByteString

data class ScalarEventData(
  val tag: YAMLTag?,
  val anchor: String?,
  val style: YAMLScalarStyle,
  val value: UByteString,
) : YAMLEventData