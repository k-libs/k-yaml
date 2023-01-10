package io.foxcapades.lib.k.yaml.io

/**
 * Writer Function
 *
 * Function used to write bytes from a given byte array to an arbitrary target.
 */
fun interface WriterFn {

  /**
   * Write from Buffer
   *
   * Writes a maximum of [len] bytes from the given buffer, starting at position
   * [offset] in that buffer, to the target destination.
   *
   * If `offset` is greater than or equal to the length of `buffer`, an
   * exception will be thrown.
   *
   * If `offset + len` is greater than the length of `buffer`, an exception will
   * be thrown.
   *
   * If `buffer` has a size of `0` or `len` is equal to `0`, this function will
   * do nothing.
   *
   * @param buffer Byte array source from which bytes will be written to the
   * target.
   *
   * @param offset Starting position in the [buffer] array that should be read
   * from.  This is to allow writing from sub-portions of the buffer.
   *
   * @param len Number of bytes from [buffer] that should be copied to the
   * target destination.
   */
  fun write(buffer: ByteArray, offset: Int, len: Int)
}