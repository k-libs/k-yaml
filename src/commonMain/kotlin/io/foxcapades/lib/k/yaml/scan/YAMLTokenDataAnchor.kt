package io.foxcapades.lib.k.yaml.scan

import io.foxcapades.lib.k.yaml.util.decodeToString
import kotlin.jvm.JvmInline

@JvmInline
@OptIn(ExperimentalUnsignedTypes::class)
value class YAMLTokenDataAnchor(val value: UByteArray) : YAMLTokenData {
  override fun toString() = "\"${value.decodeToString()}\""
}