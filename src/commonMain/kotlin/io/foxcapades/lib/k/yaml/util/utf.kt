package io.foxcapades.lib.k.yaml.util

import io.foxcapades.lib.k.yaml.bytes.*

internal class UTFException(message: String) : Throwable(message)

/**
 * Determine UTF-8 Codepoint Width
 *
 * Intended to be used on the first byte of a UTF-8 codepoint, this function
 * tests the byte to determine the expected width of the codepoint.
 *
 * If the byte is not a valid codepoint leader byte, then this function will
 * throw an exception.
 *
 * @return The width of the expected codepoint.  An `Int` value of `1`, `2`,
 * `3`, or `4`.
 *
 * @throws UTFException If the target byte is not a valid codepoint leader byte.
 */
@Throws(UTFException::class)
@Suppress("NOTHING_TO_INLINE")
internal inline fun UByte.utf8Width() =
  when {
    // Prefix: 0xxxxxxx
    this and Ub80 == Ub00 -> 1
    // Prefix: 110xxxxx
    this and UbE0 == UbC0 -> 2
    // Prefix: 1110xxxx
    this and UbF0 == UbE0 -> 3
    // Prefix: 11110xxx
    this and UbF8 == UbF0 -> 4
    // ????
    else                  -> throw UTFException("invalid UTF-8 codepoint")
  }

/**
 * Determine UTF-16 Codepoint Width
 *
 * Intended to be used on the 'leader' byte of a UTF-16 codepoint, this function
 * tests the byte to determine the expected width of the codepoint.  For normal
 * UTF-16 codepoints, this width is `2`, but for surrogate pairs, the expected
 * width will be `4`.
 *
 * @return The width of the expected codepoint.  An `Int` value of `2` or `4`.
 */
@Suppress("NOTHING_TO_INLINE")
internal inline fun UByte.utf16Width() =
  when {
    // First segment of a surrogate pair
    this >= 0xD8u && this <= 0xDBu -> 4
    // Second segment of a surrogate pair (invalid)
    this >= 0xDCu && this <= 0xDFu -> 0
    // Everything else
    else                           -> 2
  }

/**
 * Writes the target UTF binary codepoint to the given buffer as UTF-8 bytes.
 *
 * If the target codepoint is invalid (`> 0x10FFFF`) then this function will
 * throw an exception.
 *
 * @param into Buffer the UTF-8 bytes will be written to.
 *
 * @throws UTFException If the target UTF binary codepoint is invalid.
 */
internal fun UInt.toUTF8(into: UByteBuffer) {
  when {
    this <= 0x7Fu     -> {
      into.push(toUByte())
    }

    this <= 0x7FFu    -> {
      into.push(UbC0 or (this shr 6).toUByte())
      into.push(Ub80 or (this and 0x3Fu).toUByte())
    }

    this <= 0xFFFFu   -> {
      into.push(UbE0 or (this shr 12).toUByte())
      into.push(Ub80 or ((this shr 6) and 0x3Fu).toUByte())
      into.push(Ub80 or (this and 0x3Fu).toUByte())
    }

    this <= 0x10FFFFu -> {
      into.push(UbF0 or (this shr 18).toUByte())
      into.push(Ub80 or ((this shr 12) and 0x3Fu).toUByte())
      into.push(Ub80 or ((this shr 6) and 0x3Fu).toUByte())
      into.push(Ub80 or (this and 0x3Fu).toUByte())
    }

    else              -> {
      throw UTFException("Illegal UTF codepoint (0x${this.toString(16)})")
    }
  }
}

/**
 * Calls [UByte.utf8Width] on the byte at the given offset in the buffer.
 *
 * @param offset Offset of the byte to test.
 *
 * @return The UTF-8 width of the codepoint starting at the byte at the given
 * offset.
 */
@Suppress("NOTHING_TO_INLINE")
internal inline fun UByteSource.utf8Width(offset: Int = 0) = get(offset).utf8Width()

/**
 * Calls [UByte.utf16Width] on the byte at the given offset in the buffer.
 *
 * @param offset Offset of the byte to test.
 *
 * @return The UTF-16 width of the codepoint starting at the byte at the given
 * offset.
 */
@Suppress("NOTHING_TO_INLINE")
internal inline fun UByteSource.utf16Width(offset: Int = 0) = get(offset).utf16Width()

/**
 * Pops the first 4 bytes from the buffer and translates them into a UTF
 * codepoint from the UTF-32 BE source encoding.
 *
 * @return The first 4 bytes in the buffer as a UTF codepoint.
 */
@Suppress("NOTHING_TO_INLINE")
internal inline fun UByteSource.popUTF32BE() =
  (pop().toUInt() shl 24) or (pop().toUInt() shl 16) or (pop().toUInt() shl 8) or pop().toUInt()

/**
 * Pops the first 4 bytes from the buffer and translates them into a UTF
 * codepoint from the UTF-32 LE source encoding.
 *
 * @return The first 4 bytes in the buffer as a UTF codepoint.
 */
@Suppress("NOTHING_TO_INLINE")
internal inline fun UByteSource.popUTF32LE() =
  pop().toUInt() or (pop().toUInt() shl 8) or (pop().toUInt() shl 16) or (pop().toUInt() shl 24)

/**
 * Pops the first 2 or 4 bytes from the buffer and translates them into a UTF
 * codepoint from the UTF-16 BE source encoding.
 */
@Suppress("NOTHING_TO_INLINE")
internal inline fun UByteSource.popUTF16BE() =
  when (utf16Width()) {
    // Standard UTF-16 character.
    2    -> (pop().toUInt() shl 8) or pop().toUInt()
    // Surrogate pair
    4    -> (((((pop().toUInt() shl 8) or pop().toUInt()) - 0xD800u) shl 10) or (((pop().toUInt() shl 8) or pop().toUInt()) - 0xDC00u)) + 0x10000u
    // Invalid
    else -> throw IllegalStateException("Invalid UTF-16 BE surrogate pair")
  }

internal inline fun UByteSource.popUTF16LE() =
  when (utf16Width(1)) {
    // Standard UTF-16 character.
    2    -> pop().toUInt() or (pop().toUInt() shl 8)
    // Surrogate pair
    4    -> ((((pop().toUInt() or (pop().toUInt() shl 8)) - 0xD800u) shl 10) or ((pop().toUInt() or (pop().toUInt() shl 8)) - 0xDC00u)) + 0x10000u
    // Invalid
    else -> throw IllegalStateException("Invalid UTF-16 LE surrogate pair")
  }

@Suppress("NOTHING_TO_INLINE")
internal inline fun UByteBuffer.takeCodepointFrom(other: UByteSource) =
  when (val c = other[0].utf8Width()) {
    0    -> false
    else -> {
      takeFrom(other, c)
      true
    }
  }