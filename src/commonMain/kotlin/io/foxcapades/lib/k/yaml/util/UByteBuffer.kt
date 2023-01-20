package io.foxcapades.lib.k.yaml.util

import io.foxcapades.lib.k.yaml.io.ByteReader

@OptIn(ExperimentalUnsignedTypes::class)
internal class UByteBuffer : UByteSource {
  private var raw: UByteArray

  private var head: Int = 0

  /**
   * The current number of bytes held in the buffer.
   */
  override var size: Int = 0
    private set

  /**
   * The max number of bytes that may be stored in the buffer without incurring
   * a buffer reallocation.
   */
  val capacity: Int
    get() = raw.size

  /**
   * Index of the last item in the buffer.
   */
  inline val lastIndex: Int
    get() = size - 1

  /**
   * Whether this buffer contains `0` bytes.
   */
  inline val isEmpty: Boolean
    get() = size == 0

  /**
   * Whether this buffer contains `1` or more bytes.
   */
  inline val isNotEmpty: Boolean
    get() = size > 0

  /**
   * The amount of space available in the buffer at its current [capacity].
   */
  val freeSpace: Int
    get() = raw.size - size

  constructor(initialCapacity: Int = 16) {
    raw = UByteArray(initialCapacity)
  }

  override fun pop(): UByte {
    if (isEmpty)
      throw IllegalStateException("attempted to pop a value from an empty UByteBuffer")

    val out = raw[head]

    head = idx(1)
    size--

    return out
  }

  override fun peek(): UByte {
    if (isEmpty)
      throw IllegalStateException("attempted to peek a value from an empty UByteBuffer")

    return raw[head]
  }

  override fun skip(count: Int) {
    if (count >= size)
      clear()
    else {
      head = idx(count)
      size -= count
    }
  }

  fun push(value: UByte) {
    ensureCapacity(size + 1)
    raw[idx(size++)] = value
  }

  fun clear() {
    head = 0
    size = 0
  }

  fun ensureCapacity(capacity: Int) {
    if (raw.size < capacity)
      raw = toArray(max(capacity, raw.size * 2))
  }

  fun fill(readerFn: ByteReader): Int {
    // If the buffer is already full, then we can't read anything more.
    if (raw.size - size == 0)
      return 0

    // If for some reason the head of the buffer is at position 0, then we can
    // do the whole job in one read.
    if (head == 0) {
      // Read
      val r = readerFn.read(raw.asByteArray(), idx(size), raw.size - size)

      if (r > 0)
        size += r

      return r
    }

    // Since the head is not at the zero position, we have to do some wonkiness
    // to fill the buffer (because the buffer's empty spaces are at both the end
    // of and the beginning of the raw buffer).

    // Figure out how many bytes we can cram into the end of the raw buffer.
    var fillable = raw.size - idx(size)

    // Try to fill the end of the buffer in one shot
    var read = readerFn.read(raw.asByteArray(), idx(size), fillable)

    // If the first read was already at an EOF and we got no bytes, then return
    // -1 to indicate as such.
    if (read == -1)
      return -1

    // Remove the read count from the amount we actually needed.
    fillable -= read

    // Increase the size by the amount we read.
    size += read

    // While we haven't read enough to fill the end of the buffer yet
    while (fillable > 0) {
      val r = readerFn.read(raw.asByteArray(), idx(size), fillable)

      if (r == -1)
        return read

      fillable -= r
      read += r
      size += r
    }

    // Now that we have filled the end of the raw buffer, we can proceed to
    // filling the empty space at the beginning of the raw buffer.

    // The number of fillable slots is equal to the count of spaces before the
    // head position.
    fillable = head

    // Current position tracker.
    var pos = 0

    while (fillable > 0) {
      val r = readerFn.read(raw.asByteArray(), pos, fillable)

      if (r == -1)
        return read

      fillable -= r
      read += r
      pos += r
      size += r
    }

    return read
  }

  fun toArray() = toArray(size)

  fun popToArray(): UByteArray {
    val out = toArray()
    clear()
    return out
  }

  override fun get(offset: Int): UByte = raw[vidx(offset)]

  fun takeFrom(other: UByteSource, count: Int = other.size) {
    if (other.size < count)
      throw IllegalArgumentException("cannot take $count bytes from a UByteBuffer of size ${other.size}")

    var i = 0
    while (i++ < count)
      push(other.pop())
  }

  private fun idx(index: Int): Int {
    val tmp = index + head
    return if (tmp >= raw.size) tmp - raw.size else tmp
  }

  @Suppress("NOTHING_TO_INLINE")
  private inline fun vidx(index: Int): Int =
    if (index < 0 || index >= size)
      throw IndexOutOfBoundsException("attempted to access index $index of a UByteBuffer with a size of $size")
    else
      idx(index)

  private fun toArray(size: Int): UByteArray {
    val new  = UByteArray(size)

    if (isNotEmpty) {
      val tail = idx(lastIndex)

      if (head <= tail) {
        raw.copyInto(new, 0, head, tail + 1)
      } else {
        raw.copyInto(new, 0, head, raw.size)
        raw.copyInto(new, raw.size - head, 0, tail + 1)
      }
    }

    return new
  }
}