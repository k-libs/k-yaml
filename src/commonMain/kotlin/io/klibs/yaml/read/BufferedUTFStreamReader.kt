package io.klibs.yaml.read

import io.foxcapades.lib.k.yaml.err.YAMLReaderException
import io.foxcapades.lib.k.yaml.util.*
import io.foxcapades.lib.k.yaml.util.collections.UByteBuffer
import io.klibs.io.ByteReader
import io.klibs.yaml.YAMLEncoding
import io.klibs.yaml.bytes.*
import io.klibs.yaml.util.*
import io.klibs.yaml.util.UByteSource
import io.klibs.yaml.util.popUTF16LE
import io.klibs.yaml.util.utf16Width
import io.klibs.yaml.util.utf8Width

internal class BufferedUTFStreamReader : UByteSource {
  private val rawBuffer: UByteBuffer

  private val reader: ByteReader

  private var index: ULong = 0UL

  private val utf8Buffer: UByteBuffer

  var atEOF: Boolean = false
    private set

  var encoding: YAMLEncoding = YAMLEncoding.Any
    private set

  override val size: Int
    get() = utf8Buffer.size

  inline val isEmpty
    get() = size == 0

  inline val isNotEmpty
    get() = size > 0

  constructor(capacity: Int, reader: ByteReader) {
    this.rawBuffer  = UByteBuffer(capacity)
    this.utf8Buffer = UByteBuffer(capacity * 2)
    this.reader     = reader
  }

  fun cache(count: Int): Boolean {
    if (utf8Buffer.size >= count)
      return true

    while (!atEOF || rawBuffer.isNotEmpty) {
      fillUTF8Buffer()

      if (utf8Buffer.size >= count)
        return true
    }

    return false
  }

  override fun pop() = utf8Buffer.pop()

  override fun peek() = utf8Buffer.peek()

  override fun skip(count: Int) = utf8Buffer.skip(count)

  fun skipCodepoint() {
    if (utf8Buffer.isEmpty)
      throw IllegalStateException("attempted to skip a codepoint in an empty YAMLReader")

    val width = peek().utf8Width()

    if (width == 0)
      throw IllegalStateException("invalid utf-8 codepoint in the utf-8 buffer in YAMLReader")

    skip(width)
  }

  fun skipCodepoints(count: Int) {
    for (i in 0 until count) {
      if (utf8Buffer.isEmpty)
        throw IllegalStateException("attempted to skip $count codepoints from a buffer that only contained $i codepoints")

      val width = peek().utf8Width()

      if (width == 0)
        throw IllegalStateException("invalid utf-8 codepoint in the utf-8 buffer in YAMLReader")

      skip(width)
    }
  }

  override fun get(offset: Int) = utf8Buffer[offset]

  private fun cacheRaw(count: Int): Boolean {
    while (!atEOF && rawBuffer.size < count)
      fillRawBuffer()

    return rawBuffer.size >= count
  }

  private fun fillRawBuffer() {
    if (rawBuffer.fill(reader) == -1)
      atEOF = true
  }

  private fun fillUTF8Buffer() {
    if (atEOF) {
      if (rawBuffer.isEmpty)
        return
    } else {
      fillRawBuffer()
    }

    if (encoding == YAMLEncoding.Any) {
      cacheRaw(4)
      encoding = rawBuffer.determineEncoding()
    }

    when (encoding) {
      YAMLEncoding.UTF8    -> fillFromUTF8()
      YAMLEncoding.UTF16LE -> fillFromUTF16LE()
      YAMLEncoding.UTF16BE -> fillFromUTF16BE()
      YAMLEncoding.UTF32LE -> fillFromUTF32LE()
      YAMLEncoding.UTF32BE -> fillFromUTF32BE()
      YAMLEncoding.Any     -> throw IllegalStateException("encoding type was not selected after calling determineEncoding")
    }
  }

  private fun fillFromUTF8() {
    var width: Int
    var i: Int

    // While we have bytes in the raw buffer and room for at least one more
    // UTF-8 codepoint in the working buffer.
    while (rawBuffer.isNotEmpty && utf8Buffer.freeSpace >= 4) {

      // Get the width of the next character in the raw buffer
      width = rawBuffer.utf8Width()

      // If the width is `0` then it is an invalid codepoint
      if (width == 0)
        throw YAMLReaderException(index, "invalid UTF-8 codepoint")

      // If width is more bytes than we have available in the raw buffer
      if (width > rawBuffer.size) {
        // And we've reached the end of the stream
        if (atEOF) {
          // Then the stream ended with an incomplete UTF-8 codepoint.
          throw YAMLReaderException(index, "stream ended with an incomplete UTF-8 codepoint")
        }

        // And we haven't reached the end of the stream yet, break here.  We can
        // complete the next codepoint on the next call to this function.
        break
      }

      // Copy the codepoint from the raw buffer to the UTF-8 buffer.
      i = 0
      while (i < width) {
        utf8Buffer.push(rawBuffer.pop())
        index++
        i++
      }

    }
  }

  private fun fillFromUTF16LE() {
    var width: Int

    // While we have bytes in the raw buffer and room for at least one more
    // UTF-8 codepoint in the working buffer.
    while (rawBuffer.isNotEmpty && utf8Buffer.freeSpace >= 4) {

      // Get the width of the LE 'first' byte
      width = rawBuffer.utf16Width(1)

      // If the width is `0` then it is an invalid codepoint
      if (width == 0)
        throw YAMLReaderException(index, "invalid UTF-16 codepoint")

      // If width is more bytes than we have available in the raw buffer
      if (width > rawBuffer.size) {
        // and we've reached the end of the stream
        if (atEOF) {
          // then the stream ended with an incomplete UTF-16 codepoint
          // FIXME: Surrogate pairs will cause this error if the stream ends on
          //        just the high surrogate
          throw YAMLReaderException(index, "stream ended with an incomplete UTF-16 codepoint")
        }

        // and we haven't reached the end of the stream yet, break here.  We can
        // complete the next codepoint on the next call to this function.
        break
      }

      // Copy the codepoint from the raw buffer to the UTF-8 buffer.
      rawBuffer.popUTF16LE().toUTF8(utf8Buffer)
      index += width.toULong()
    }
  }

  private fun fillFromUTF16BE() {
    var width: Int

    // While we have bytes in the raw buffer and room for at least one more
    // UTF-8 codepoint in the working buffer.
    while (rawBuffer.isNotEmpty && utf8Buffer.freeSpace >= 4) {

      // Get the width of the BE first byte
      width = rawBuffer.utf16Width()

      // If the width is `0` then it is an invalid codepoint
      if (width == 0)
        throw YAMLReaderException(index, "invalid UTF-16 codepoint")

      // If width is more bytes than we have available in the raw buffer
      if (width > rawBuffer.size) {
        // and we've reached the end of the stream
        if (atEOF) {
          // then the stream ended with an incomplete UTF-16 codepoint
          // FIXME: Surrogate pairs will cause this error if the stream ends on
          //        just the high surrogate
          throw YAMLReaderException(index, "stream ended with an incomplete UTF-16 codepoint")
        }

        // and we haven't reached the end of the stream yet, break here.  We can
        // complete the next codepoint on the next call to this function.
        break
      }

      // Copy the codepoint from the raw buffer to the UTF-8 buffer.
      rawBuffer.popUTF16BE().toUTF8(utf8Buffer)
      index += width.toULong()
    }
  }

  private fun fillFromUTF32LE() {
    while (rawBuffer.isNotEmpty && utf8Buffer.freeSpace >= 4) {
      if (rawBuffer.size < 4) {
        if (atEOF) {
          throw YAMLReaderException(index, "stream ended with ")
        }
        break
      }

      rawBuffer.popUTF32LE().toUTF8(utf8Buffer)
      index += 4uL
    }
  }

  private fun fillFromUTF32BE() {
    while (rawBuffer.isNotEmpty && utf8Buffer.freeSpace >= 4) {
      if (rawBuffer.size < 4) {
        if (atEOF) {
          throw YAMLReaderException(index, "stream ended with ")
        }
        break
      }

      rawBuffer.popUTF32BE().toUTF8(utf8Buffer)
      index += 4uL
    }
  }
}


private fun UByteSource.determineEncoding(): YAMLEncoding {
  return when {
    size > 3 -> detEnc4Byte()
    size > 2 -> detEnc3Byte()
    size > 1 -> detEnc2Byte()
    else     -> YAMLEncoding.UTF8
  }
}

@Suppress("NOTHING_TO_INLINE")
private inline fun UByteSource.detEnc4Byte() =
// Implicit UTF-32 BE: 0x00 0x00 0x00 <ASCII>
// Explicit UTF-32 BE: 0x00 0x00 0xFE 0xFF
// Implicit UTF-16 BE: 0x00 <ASCII>
  // Fallback: UTF-8
  if (this[0] == Ub00) {
    // Implicit UTF-32 BE: 0x00 0x00 0x00 <ASCII>
    // Explicit UTF-32 BE: 0x00 0x00 0xFE 0xFF
    if (this[1] == Ub00) {
      if (this[2] == Ub00 && this[3] <= Ub7F)
        YAMLEncoding.UTF32BE
      else if (this[2] == UbFE && this[3] == UbFF)
        YAMLEncoding.UTF32BE
      else
        YAMLEncoding.UTF8
    } else if (this[1] <= Ub7F) {
      YAMLEncoding.UTF16BE
    } else {
      YAMLEncoding.UTF8
    }
  }

  // Explicit UTF-32 LE: 0xFF 0xFE 0x00 0x00
  // Explicit UTF-16 LE: 0xFF 0xFE
  // Fallback: UTF-8
  else if (this[0] == UbFF) {
    if (this[1] == UbFE) {
      if (this[2] == Ub00 && this[3] == Ub00)
        YAMLEncoding.UTF32LE
      else
        YAMLEncoding.UTF16LE
    } else {
      YAMLEncoding.UTF8
    }
  }

  // Explicit UTF-16 BE: 0xFE 0xFF
  else if (this[0] == UbFE && this[1] == UbFF) {
    YAMLEncoding.UTF16BE
  }

  // Explicit UTF-8: 0xEF 0xBB 0xBF
  else if (this[0] == UbEF && this[1] == UbBB && this[2] == UbBF) {
    YAMLEncoding.UTF8
  }

  // Implicit UTF-32 LE: <ASCII> 0x00 0x00 0x00
  // Implicit UTF-16 LE: <ASCII> 0x00
  else if (this[0] <= Ub7F && this[1] == Ub00) {
    if (this[2] == Ub00 && this[3] == Ub00)
      YAMLEncoding.UTF32LE
    else
      YAMLEncoding.UTF16LE
  }

  // Fallback UTF-8
  else {
    YAMLEncoding.UTF8
  }

@Suppress("NOTHING_TO_INLINE")
private inline fun UByteSource.detEnc3Byte() =
  when {
    this[0] == UbEF && this[1] == UbBB && this[2] == UbBF -> YAMLEncoding.UTF8
    this[0] == UbFE && this[1] == UbFF                    -> YAMLEncoding.UTF16BE
    this[0] == UbFF && this[1] == UbFE                    -> YAMLEncoding.UTF16LE
    this[0] == Ub00 && this[1] <= Ub7F                    -> YAMLEncoding.UTF16BE
    this[0] <= Ub7F && this[1] == Ub00                    -> YAMLEncoding.UTF16LE
    else                                                  -> YAMLEncoding.UTF8
  }

@Suppress("NOTHING_TO_INLINE")
private inline fun UByteSource.detEnc2Byte() =
  when {
    this[0] == UbFE && this[1] == UbFF -> YAMLEncoding.UTF16BE
    this[0] == UbFF && this[1] == UbFE -> YAMLEncoding.UTF16LE
    this[0] == Ub00 && this[1] <= Ub7F -> YAMLEncoding.UTF16BE
    this[0] <= Ub7F && this[1] == Ub00 -> YAMLEncoding.UTF16LE
    else                               -> YAMLEncoding.UTF8
  }