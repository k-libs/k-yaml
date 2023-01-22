package io.foxcapades.lib.k.yaml.token

import io.foxcapades.lib.k.yaml.util.decodeToString

@OptIn(ExperimentalUnsignedTypes::class)
class YAMLTokenDataDirectiveTag(
  private val handle: UByteArray,
  private val prefix: UByteArray
) : YAMLTokenData {

  val handleBytes
    get() = handle

  val handleString
    get() = handle.decodeToString()

  val prefixBytes
    get() = prefix

  val prefixString
    get() = prefix.decodeToString()

  override fun toString() =
    "YAMLTokenDataTagDirective(handle=\"${handle.decodeToString()}\", prefix=\"${prefix.decodeToString()}\")"

  override fun equals(other: Any?) =
    (this === other)
    || (
      other is YAMLTokenDataDirectiveTag
      && handle.contentEquals(other.handle)
      && prefix.contentEquals(other.prefix)
    )

  override fun hashCode() =
    31 * handle.hashCode() + prefix.hashCode()
}