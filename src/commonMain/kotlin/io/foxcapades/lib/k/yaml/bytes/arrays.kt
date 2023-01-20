package io.foxcapades.lib.k.yaml.bytes

/**
 * Empty UByte String
 *
 * ```
 * ""
 * ```
 */
@OptIn(ExperimentalUnsignedTypes::class)
internal inline val StrEmpty
  get() = ubyteArrayOf()

/**
 * Primary Tag Prefix UByte String
 *
 * ```
 * "!"
 * ```
 */
@OptIn(ExperimentalUnsignedTypes::class)
internal inline val StrPrimaryTagPrefix
  get() = ubyteArrayOf(A_EXCLAIM)

/**
 * Secondary Tag Prefix UByte String
 *
 * ```
 * "!!"
 * ```
 */
@OptIn(ExperimentalUnsignedTypes::class)
internal inline val StrSecondaryTagPrefix
  get() = ubyteArrayOf(A_EXCLAIM, A_EXCLAIM)
