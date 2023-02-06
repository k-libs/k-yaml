package io.klibs.yaml.util

@Suppress("NOTHING_TO_INLINE")
@OptIn(ExperimentalUnsignedTypes::class)
internal inline fun UByteArray.decodeToString() = asByteArray().decodeToString()