package io.foxcapades.lib.k.yaml.token

import io.foxcapades.lib.k.yaml.util.decodeToString

@OptIn(ExperimentalUnsignedTypes::class)
class YAMLTokenDataTag(
  private val handle: UByteArray,
  private val suffix: UByteArray
) : YAMLTokenData {

  val handleRaw
    get() = handle

  val handleString
    get() = handle.decodeToString()

  val suffixRaw
    get() = suffix

  val suffixString
    get() = suffix.decodeToString()

  override fun toString() =
    "TokenDataTag(handle=\"${handle.decodeToString()}\", suffix=\"${suffix.decodeToString()}\")"

  override fun equals(other: Any?) =
    this === other
      || (other is YAMLTokenDataTag && handle.contentEquals(other.handle) && suffix.contentEquals(other.suffix))

  override fun hashCode() =
    31 * handle.contentHashCode() + suffix.contentHashCode()
}