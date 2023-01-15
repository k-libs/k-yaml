@file:Suppress("NOTHING_TO_INLINE")

package io.foxcapades.lib.k.yaml.util

import io.foxcapades.lib.k.yaml.bytes.*

/**
 * Check Octet
 *
 * Tests whether the byte at the given offset in the buffer is equal to the
 * given test octet.
 *
 * If the buffer contains [offset] or fewer bytes, this function will return
 * `false`.
 *
 * @param octet Test value that the byte at the given [offset] should be equal
 * to.
 *
 * @param offset Offset of the value to test against in the buffer.
 *
 * Defaults to `0`
 *
 * @return `true` if the buffer contains more than [offset] bytes and the
 * specific byte at [offset] is equal to [octet], otherwise `false`.
 */
internal inline fun UByteBuffer.check(octet: UByte, offset: Int = 0) = size > offset && get(offset) == octet

/**
 * Unsafe Check Octet
 *
 * Tests whether the byte at the given offset in the buffer is equal to the
 * given test octet.
 *
 * Unlike [check], this method does not verify that the buffer is long enough to
 * contain a value at [offset], and instead relies on the calling function to
 * perform that check.
 *
 * @param octet Test value that the byte at the given [offset] should be equal
 * to.
 *
 * @param offset Offset of the value to test against in the buffer.
 *
 * Defaults to `0`
 *
 * @return `true` if the value at the given [offset] equals the given test
 * [octet], otherwise `false`.
 */
internal inline fun UByteBuffer.uCheck(octet: UByte, offset: Int = 0) = get(offset) == octet

/**
 * Is CRLF
 *
 * Tests whether the buffer contains the ASCII character combination CR+LF at
 * the given [offset].
 *
 * If the buffer contains `offset + 1` bytes or fewer, this function will return
 * `false`.
 *
 * @param offset Offset of the bytes to test.
 *
 * Defaults to `0`
 *
 * @return `true` if the buffer contains the ASCII character combination CR+LF
 * at the given offset, otherwise `false`.
 */
internal inline fun UByteBuffer.isCRLF(offset: Int = 0) =
  size > offset + 1 && uCheck(A_CARRIAGE_RETURN, offset) && uCheck(A_LINE_FEED, offset + 1)

internal inline fun UByteBuffer.isCR(offset: Int = 0) = check(A_CARRIAGE_RETURN, offset)
internal inline fun UByteBuffer.isLF(offset: Int = 0) = check(A_LINE_FEED, offset)

internal inline fun UByteBuffer.isNEL(offset: Int = 0) =
  size > offset + 1 && uCheck(UbC2, offset) && uCheck(Ub85, offset + 1)

internal inline fun UByteBuffer.isLS(offset: Int = 0) =
  size > offset + 2 && uCheck(UbE2, offset) && uCheck(Ub80, offset + 1) && uCheck(UbA8, offset + 2)

internal inline fun UByteBuffer.isPS(offset: Int = 0) =
  size > offset + 2 && uCheck(UbE2, offset) && uCheck(Ub80, offset + 1) && uCheck(UbA9, offset + 2)