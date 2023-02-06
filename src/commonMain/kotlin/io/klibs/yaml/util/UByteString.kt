package io.klibs.yaml.util

import kotlin.jvm.JvmInline

@JvmInline
@OptIn(ExperimentalUnsignedTypes::class)
value class UByteString(private val rawValue: UByteArray) {
  val size
    get() = rawValue.size

  inline val isEmpty
    get() = size == 0

  inline val isNotEmpty
    get() = size > 0

  operator fun get(i: Int) = rawValue[i]

  override fun toString() = rawValue.decodeToString()

  fun contentHashCode() = rawValue.contentHashCode()

  fun contentEquals(other: UByteString) = rawValue.contentEquals(other.rawValue)

  fun contentEquals(other: UByteArray) = rawValue.contentEquals(other)
}