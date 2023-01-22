package io.foxcapades.lib.k.yaml.token

@ExperimentalUnsignedTypes
data class YAMLTokenDataPlainScalar(
  override val value:  UByteArray,
           val indent: UInt
) : YAMLTokenDataScalar