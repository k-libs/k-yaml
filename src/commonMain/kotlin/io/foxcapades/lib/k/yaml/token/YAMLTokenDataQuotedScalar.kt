package io.foxcapades.lib.k.yaml.token

@ExperimentalUnsignedTypes
data class YAMLTokenDataQuotedScalar(
  override val value: UByteArray,
           val style: QuotedScalarStyle
) : YAMLTokenDataScalar
