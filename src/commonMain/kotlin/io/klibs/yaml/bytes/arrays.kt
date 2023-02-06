package io.klibs.yaml.bytes

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
  get() = ubyteArrayOf(_root_ide_package_.io.klibs.yaml.bytes.A_EXCLAIM)

/**
 * Secondary Tag Prefix UByte String
 *
 * ```
 * "!!"
 * ```
 */
@OptIn(ExperimentalUnsignedTypes::class)
internal inline val StrSecondaryTagPrefix
  get() = ubyteArrayOf(
    _root_ide_package_.io.klibs.yaml.bytes.A_EXCLAIM,
    _root_ide_package_.io.klibs.yaml.bytes.A_EXCLAIM
  )
