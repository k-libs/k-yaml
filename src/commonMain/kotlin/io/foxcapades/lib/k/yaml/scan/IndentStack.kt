package io.foxcapades.lib.k.yaml.scan

import io.foxcapades.lib.k.yaml.util.max
import io.foxcapades.lib.k.yaml.util.min

class IndentStack(
      initialCapacity: Int   = 16,
  val scaleFactor:     Float = 1.5F,
  val maxCapacity:     Int   = Int.MAX_VALUE
) {
  private var raw = IntArray(initialCapacity)

  var size = 0
    private set

  inline val isEmpty
    get() = size == 0

  inline val isNotEmpty
    get() = size > 0

  inline val lastIndex
    get() = size - 1

  init {
    if (initialCapacity > maxCapacity)
      throw IllegalArgumentException("attempted to construct an IndentStack instance with an initial capacity value ($initialCapacity) greater than the given max size value ($maxCapacity)")
    if (scaleFactor <= 1)
      throw IllegalArgumentException("attempted to construct an IndentStack instance with a scale factor value that is less than or equal to 1")
  }

  fun pop() =
    if (isEmpty)
      throw NoSuchElementException("attempted to pop a value from an empty IndentStack")
    else
      raw[--size]

  fun peek() =
    if (isEmpty)
      throw NoSuchElementException("attempted to peek a value in an empty IndentStack")
    else
      raw[size - 1]

  fun push(indent: Int) {
    ensureCapacity(size + 1)
    raw[size++] = indent
  }

  fun ensureCapacity(minCapacity: Int) {
    if (minCapacity > maxCapacity)
      throw IllegalArgumentException("attempted to grow a SimpleKeyStack of size $size to new size $minCapacity which is greater than the max allowed size $maxCapacity")

    if (raw.size >= minCapacity)
      return

    raw = raw.copyInto(IntArray(min(max(minCapacity, (raw.size.toFloat() * scaleFactor).toInt()), maxCapacity)))
  }

  operator fun get(i: Int) =
    if (i < 0 || i >= size)
      throw IndexOutOfBoundsException("attempted to access index $i in an IndentStack of size $size")
    else
      raw[lastIndex - i]

  operator fun set(i: Int, indent: Int) {
    if (i < 0 || i >= size)
      throw IndexOutOfBoundsException("attempted to set a value at index $i in an IndentStack of size $size")
    else
      raw[lastIndex - i] = indent
  }
}