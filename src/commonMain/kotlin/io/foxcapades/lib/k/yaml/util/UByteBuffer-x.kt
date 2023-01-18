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

// region Single Byte Checks

// region Safe Single Byte Checks

// endregion Safe Single Byte Checks

// region Unsafe Single Byte Checks

internal inline fun UByteBuffer.uIsTab(offset: Int = 0) = uCheck(A_TAB, offset)
internal inline fun UByteBuffer.uIsLineFeed(offset: Int = 0) = uCheck(A_LINE_FEED, offset)
internal inline fun UByteBuffer.uIsCarriageReturn(offset: Int = 0) = uCheck(A_CARRIAGE_RETURN, offset)
internal inline fun UByteBuffer.uIsSpace(offset: Int = 0) = uCheck(A_SPACE, offset)
internal inline fun UByteBuffer.uIsExclaim(offset: Int = 0) = uCheck(A_EXCLAIM, offset)
internal inline fun UByteBuffer.uIsDoubleQuote(offset: Int = 0) = uCheck(A_DOUBLE_QUOTE, offset)
internal inline fun UByteBuffer.uIsPound(offset: Int = 0) = uCheck(A_POUND, offset)
internal inline fun UByteBuffer.uIsPercent(offset: Int = 0) = uCheck(A_PERCENT, offset)
internal inline fun UByteBuffer.uIsAmpersand(offset: Int = 0) = uCheck(A_AMPERSAND, offset)
internal inline fun UByteBuffer.uIsApostrophe(offset: Int = 0) = uCheck(A_APOSTROPHE, offset)
internal inline fun UByteBuffer.uIsAsterisk(offset: Int = 0) = uCheck(A_ASTERISK, offset)
internal inline fun UByteBuffer.uIsPlus(offset: Int = 0) = uCheck(A_PLUS, offset)
internal inline fun UByteBuffer.uIsComma(offset: Int = 0) = uCheck(A_COMMA, offset)
internal inline fun UByteBuffer.uIsDash(offset: Int = 0) = uCheck(A_DASH, offset)
internal inline fun UByteBuffer.uIsPeriod(offset: Int = 0) = uCheck(A_PERIOD, offset)
internal inline fun UByteBuffer.uIsColon(offset: Int = 0) = uCheck(A_COLON, offset)
internal inline fun UByteBuffer.uIsGreaterThan(offset: Int = 0) = uCheck(A_GREATER_THAN, offset)
internal inline fun UByteBuffer.uIsQuestion(offset: Int = 0) = uCheck(A_QUESTION, offset)
internal inline fun UByteBuffer.uIsAt(offset: Int = 0) = uCheck(A_AT, offset)
internal inline fun UByteBuffer.uIsSquareBracketOpen(offset: Int = 0) = uCheck(A_SQUARE_BRACKET_OPEN, offset)
internal inline fun UByteBuffer.uIsBackslash(offset: Int = 0) = uCheck(A_BACKSLASH, offset)
internal inline fun UByteBuffer.uIsSquareBracketClose(offset: Int = 0) = uCheck(A_SQUARE_BRACKET_CLOSE, offset)
internal inline fun UByteBuffer.uIsUnderscore(offset: Int = 0) = uCheck(A_UNDERSCORE, offset)
internal inline fun UByteBuffer.uIsGrave(offset: Int = 0) = uCheck(A_GRAVE, offset)
internal inline fun UByteBuffer.uIsCurlyBracketOpen(offset: Int = 0) = uCheck(A_CURLY_BRACKET_OPEN, offset)
internal inline fun UByteBuffer.uIsPipe(offset: Int = 0) = uCheck(A_PIPE, offset)
internal inline fun UByteBuffer.uIsCurlyBracketClose(offset: Int = 0) = uCheck(A_CURLY_BRACKET_CLOSE, offset)
internal inline fun UByteBuffer.uIsTilde(offset: Int = 0) = uCheck(A_TILDE, offset)

// endregion Unsafe Single Byte Checks

// endregion Single Byte Checks


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