package io.foxcapades.lib.k.yaml.token

import io.foxcapades.lib.k.yaml.YAMLEncoding
import io.foxcapades.lib.k.yaml.scan.BlockScalarChompMode
import kotlin.jvm.JvmInline

sealed interface YAMLTokenData

@JvmInline
value class YAMLTokenDataStreamStart(val encoding: YAMLEncoding) : YAMLTokenData

data class YAMLTokenDataVersionDirective(val major: UInt, val minor: UInt) : YAMLTokenData

@JvmInline
@OptIn(ExperimentalUnsignedTypes::class)
value class YAMLTokenDataComment(val value: UByteArray) : YAMLTokenData

@OptIn(ExperimentalUnsignedTypes::class)
data class BlockScalarTokenData(
  val value:      UByteArray,
  val style:      BlockScalarStyle,
  val indent:     UInt,
  val chomping:   BlockScalarChompMode,
  val indentHint: UInt,
) : YAMLTokenData
