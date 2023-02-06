package io.klibs.yaml.util

import kotlin.jvm.JvmInline

@Suppress("NOTHING_TO_INLINE")
internal inline fun <T> Array<T>.toFlowSequence() = this.joinToString(", ", "[", "]")

@JvmInline
value class ImmutableArray<T>(private val raw: Array<T>) {
  val size
    get() = raw.size

  val isEmpty
    get() = raw.size == 0

  val isNotEmpty
    get() = raw.size > 0

  val lastIndex
    get() = raw.size - 1

  operator fun get(i: Int) = raw[i]

  override fun toString(): String {
    return raw.toString()
  }

  fun contentToString() = raw.joinToString(", ", "[", "]")

  fun contentEquals(other: ImmutableArray<T>) = this.raw.contentEquals(other.raw)

  fun contentEquals(other: Array<T>) = this.raw.contentEquals(other)

  fun contentHashCode() = this.raw.contentHashCode()
}