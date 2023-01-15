package io.foxcapades.lib.k.yaml.scan

import io.foxcapades.lib.k.yaml.YAMLEncoding
import kotlin.jvm.JvmInline

sealed interface YAMLTokenData

@JvmInline
value class YAMLTokenDataDocumentStart(val explicit: Boolean) : YAMLTokenData

@JvmInline
@OptIn(ExperimentalUnsignedTypes::class)
value class YAMLTokenDataAlias(val value: UByteArray) : YAMLTokenData

@JvmInline
@OptIn(ExperimentalUnsignedTypes::class)
value class YAMLTokenDataAnchor(val value: UByteArray) : YAMLTokenData

@OptIn(ExperimentalUnsignedTypes::class)
class YAMLTokenDataScalar(val value: UByteArray, val style: YAMLScalarStyle) : YAMLTokenData

@JvmInline
value class YAMLTokenDataStreamStart(val encoding: YAMLEncoding) : YAMLTokenData

@OptIn(ExperimentalUnsignedTypes::class)
class YAMLTokenDataTag(val handle: UByteArray, val suffix: UByteArray) : YAMLTokenData

@OptIn(ExperimentalUnsignedTypes::class)
class YAMLTokenDataTagDirective(val handle: UByteArray, val prefix: UByteArray) : YAMLTokenData

class YAMLTokenDataVersionDirective(val major: UInt, val minor: UInt) : YAMLTokenData