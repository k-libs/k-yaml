package io.foxcapades.lib.k.yaml.scan

import io.foxcapades.lib.k.yaml.util.max
import io.foxcapades.lib.k.yaml.util.min

class TokenQueue(
      initialCapacity: Int   = 16,
  val scaleFactor:     Float = 1.5F,
  val maxCapacity:     Int   = Int.MAX_VALUE,
) {

  private var raw  = arrayOfNulls<YAMLToken>(initialCapacity)

  private var head = 0

  var size = 0
    private set

  inline val isEmpty
    get() = size == 0

  inline val isNotEmpty
    get() = size > 0

  inline val lastIndex
    get() = size - 1

  init {
    if (initialCapacity < maxCapacity)
      throw IllegalArgumentException("attempted to construct a TokenQueue instance with an initial capacity value ($initialCapacity) greater than the given max size value ($maxCapacity)")
    if (scaleFactor <= 1)
      throw IllegalArgumentException("attempted to construct a TokenQueue instance with a scale factor value that is less than or equal to 1")
  }

  fun ensureCapacity(minCapacity: Int) {
    if (minCapacity > maxCapacity)
      throw IllegalArgumentException("attempted to resize a TokenQueue to a new size ($minCapacity) that is greater than the set max size ($maxCapacity)")

    if (raw.size >= minCapacity)
      return

    val new = arrayOfNulls<YAMLToken>(min(max(minCapacity, (raw.size.toFloat() * scaleFactor).toInt()), maxCapacity))

    if (isNotEmpty) {
      val tail = head + lastIndex
      if (head <= tail) {
        raw.copyInto(new, 0, head, tail + 1)
      } else {
        raw.copyInto(new, 0, head, raw.size)
        raw.copyInto(new, raw.size - head, 0, tail + 1)
      }
    }

    raw = new
  }

  fun peek(): YAMLToken =
    if (isEmpty)
      throw NoSuchElementException("attempted to peek the first value in an empty TokenQueue")
    else
      raw[head]!!

  fun pop(): YAMLToken {
    if (isEmpty)
      throw NoSuchElementException("attempted to pop the first value from an empty TokenQueue")

    val out = raw[head]!!
    raw[head] = null
    head = idx(1)
    size--
    return out
  }

  fun push(token: YAMLToken) {
    ensureCapacity(size + 1)
    raw[idx(size++)] = token
  }

  fun clear() {
    var i = 0
    while (i < size) {
      raw[idx(i)] = null
      i++
    }

    head = 0
    size = 0
  }

  operator fun get(i: Int) =
    if (i < 0 || i >= size)
      throw IndexOutOfBoundsException("attempted to access an item at index $i in a TokenQueue of size $size")
    else
      raw[idx(i)]!!

  @Suppress("NOTHING_TO_INLINE")
  private inline fun idx(i: Int) = if (head + i >= raw.size) head + i - raw.size else head + i
}