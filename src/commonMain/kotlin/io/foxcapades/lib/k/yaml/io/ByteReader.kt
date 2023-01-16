package io.foxcapades.lib.k.yaml.io

/**
 * Reader Function
 *
 * Function used to read bytes from an arbitrary source into a given byte array.
 */
fun interface ByteReader {

  /**
   * Fill Buffer
   *
   * Fills the given buffer starting at position [offset] in that buffer with a
   * maximum of [maxLen] bytes read from a source.
   *
   * If `offset` is greater than or equal to the length of `buffer`, an
   * exception will be thrown.
   *
   * If `buffer` has a size of `0` or `maxLen` is equal to `0`, then this
   * function will do nothing and return `0`.
   *
   * @param buffer Byte array into which bytes will be read from the source.
   *
   * @param offset Position in [buffer] that the first byte should be written
   * to.  This is to allow filling sub-portions of the buffer.
   *
   * @param maxLen Maximum number of bytes to read from the source into the given
   * [buffer].
   *
   * @return The number of bytes read from the source into the buffer, or `-1`
   * if the end of the source has been reached AND `0` bytes were read because
   * of it.
   */
  fun read(buffer: ByteArray, offset: Int, maxLen: Int): Int
}

