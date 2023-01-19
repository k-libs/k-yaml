package io.foxcapades.lib.k.yaml.scan

import io.foxcapades.lib.k.yaml.util.decodeToString

@OptIn(ExperimentalUnsignedTypes::class)
data class YAMLTokenDataScalar(
  val value: UByteArray,
  val style: YAMLScalarStyle
) : YAMLTokenData {

  fun valueString() = value.decodeToString()

  override fun toString() =
    "ScalarData(value=\"${value.decodeToString()}\", style=$style)"
}