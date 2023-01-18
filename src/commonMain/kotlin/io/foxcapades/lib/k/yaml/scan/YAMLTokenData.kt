package io.foxcapades.lib.k.yaml.scan

import io.foxcapades.lib.k.yaml.YAMLEncoding
import kotlin.jvm.JvmInline

sealed interface YAMLTokenData

@JvmInline
@OptIn(ExperimentalUnsignedTypes::class)
value class YAMLTokenDataAlias(val value: UByteArray) : YAMLTokenData

@JvmInline
@OptIn(ExperimentalUnsignedTypes::class)
value class YAMLTokenDataAnchor(val value: UByteArray) : YAMLTokenData

@OptIn(ExperimentalUnsignedTypes::class)
data class YAMLTokenDataScalar(val value: UByteArray, val style: YAMLScalarStyle) : YAMLTokenData {

  override fun toString() =
    "ScalarData(value=\"${value.asByteArray().decodeToString()}\", style=$style)"
}

@JvmInline
value class YAMLTokenDataStreamStart(val encoding: YAMLEncoding) : YAMLTokenData

@OptIn(ExperimentalUnsignedTypes::class)
class YAMLTokenDataTag(val handle: UByteArray, val suffix: UByteArray) : YAMLTokenData

@OptIn(ExperimentalUnsignedTypes::class)
class YAMLTokenDataTagDirective(val handle: UByteArray, val prefix: UByteArray) : YAMLTokenData

data class YAMLTokenDataVersionDirective(val major: UInt, val minor: UInt) : YAMLTokenData