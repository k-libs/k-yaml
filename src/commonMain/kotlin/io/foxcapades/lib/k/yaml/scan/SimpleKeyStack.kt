package io.foxcapades.lib.k.yaml.scan

import io.foxcapades.lib.k.yaml.util.max
import io.foxcapades.lib.k.yaml.util.min

class SimpleKeyStack(
      initialCapacity: Int   = 16,
  val scaleFactor:     Float = 1.5F,
  val maxSize:         Int   = Int.MAX_VALUE,
) {
  private var raw = arrayOfNulls<SimpleKey>(initialCapacity)

  var size = 0
    private set

  inline val isEmpty
    get() = size == 0

  inline val isNotEmpty
    get() = size > 0

  inline val lastIndex
    get() = size - 1

  fun pop(): SimpleKey {
    if (isEmpty)
      throw NoSuchElementException("attempted to pop a value from an empty SimpleKeyStack")

    val out = raw[--size]!!
    raw[size + 1] = null
    return out
  }

  fun peek() =
    if (isEmpty)
      throw NoSuchElementException("attempted to peek a value from an empty SimpleKeyStack")
    else
      raw[lastIndex]!!

  fun push(key: SimpleKey) {
    ensureCapacity(size + 1)
    raw[size++] = key
  }

  fun ensureCapacity(capacity: Int) {
    if (raw.size >= capacity)
      return

    if (capacity > maxSize)
      throw IllegalArgumentException("attempted to grow a SimpleKeyStack of size $size to new size $capacity which is greater than the max allowed size $maxSize")

    raw = raw.copyInto(arrayOfNulls(min(max(capacity, (raw.size.toFloat() * scaleFactor).toInt()), maxSize)))
  }

  operator fun get(i: Int) =
    if (i < 0 || i >= size)
      throw IndexOutOfBoundsException("attempted to access index $i in a SimpleKeyStack of size $size")
    else
      raw[lastIndex - i]!!

}