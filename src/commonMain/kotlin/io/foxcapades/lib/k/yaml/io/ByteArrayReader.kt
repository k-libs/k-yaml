package io.foxcapades.lib.k.yaml.io

import kotlin.math.min

class ByteArrayReader(private val input: ByteArray) : ByteReader {
  private var position = 0

  override fun read(buffer: ByteArray, offset: Int, maxLen: Int): Int {
    if (buffer.isEmpty() || maxLen == 0)
      return 0

    if (offset >= buffer.size)
      throw IllegalArgumentException(
        "attempted to call ByteArrayReader#read with an offset value " +
          "($offset) that is greater than or equal to the size of the given " +
          "buffer (${buffer.size})"
      )

    if (offset < 0)
      throw IllegalArgumentException("attempted to call ByteArrayReader#read with a negative offset value ($offset)")

    if (position >= input.size)
      return -1

    val availableLength = input.size - position
    val fillableLength = buffer.size - offset

    val targetLength = min(min(availableLength, fillableLength), maxLen)

    input.copyInto(buffer, offset, position, position + targetLength)
    position += targetLength

    return targetLength
  }
}