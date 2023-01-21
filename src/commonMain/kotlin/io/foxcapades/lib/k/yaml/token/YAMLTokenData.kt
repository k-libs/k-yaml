package io.foxcapades.lib.k.yaml.token

import io.foxcapades.lib.k.yaml.YAMLEncoding
import kotlin.jvm.JvmInline

sealed interface YAMLTokenData

@JvmInline
value class YAMLTokenDataStreamStart(val encoding: YAMLEncoding) : YAMLTokenData

data class YAMLTokenDataVersionDirective(val major: UInt, val minor: UInt) : YAMLTokenData

@JvmInline
@OptIn(ExperimentalUnsignedTypes::class)
value class YAMLTokenDataComment(val value: UByteArray) : YAMLTokenData