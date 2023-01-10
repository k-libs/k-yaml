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
@Suppress("NOTHING_TO_INLINE")
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
@Suppress("NOTHING_TO_INLINE")
internal inline fun UByteBuffer.uCheck(octet: UByte, offset: Int = 0) = get(offset) == octet

/**
 * Is ASCII
 *
 * Tests whether the byte at the given offset in the buffer is a valid ASCII
 * character value.
 *
 * If the buffer contains [offset] or fewer bytes, this function will return
 * `false`.
 *
 * @param offset Offset of the byte to test.
 *
 * Defaults to `0`
 *
 * @return `true` if the buffer contains more than [offset] bytes and the byte
 * at the given `offset` is a valid ASCII character.
 */
@Suppress("NOTHING_TO_INLINE")
internal inline fun UByteBuffer.isASCII(offset: Int = 0) = size > offset && get(offset) <= Ub7F

/**
 * Is Alphanumeric
 *
 * Tests whether the byte at the given offset in the buffer is a valid ASCII
 * character in the following character set:
 *
 * ```
 * a b c d e f g h i j k l m n o p q r s t u v w x y z
 * A B C D E F G H I J K L M N O P Q R S T U V W X Y Z
 * 0 1 2 3 4 5 6 7 8 9
 * _ -
 * ```
 *
 * If the buffer contains [offset] or fewer bytes, this function will return
 * `false`.
 *
 * @param offset Offset of the byte to test.
 *
 * Defaults to `0`
 *
 * @return `true` if the buffer contains more than [offset] bytes and the byte
 * at the given `offset` is an alphanumeric character as defined above,
 * otherwise `false`
 */
@Suppress("NOTHING_TO_INLINE")
internal inline fun UByteBuffer.isAlphanumeric(offset: Int = 0) =
  size > offset
    && when (get(offset)) {
      in A_LO_A .. A_LO_Z -> true
      in A_UP_A .. A_UP_Z -> true
      in A_0    .. A_9    -> true
      A_DASH, A_UNDER     -> true
      else                -> false
    }

/**
 * Is Decimal Digit
 *
 * Tests whether the byte at the given offset in the buffer is a valid ASCII
 * decimal digit character from the following character set:
 *
 * ```
 * 0 1 2 3 4 5 6 7 8 9
 * ```
 *
 * If the buffer contains [offset] or fewer bytes, this function will return
 * `false`.
 *
 * @param offset Offset of the byte to test.
 *
 * Defaults to `0`
 *
 * @return `true` if the buffer contains more than [offset] bytes and the byte
 * at the given `offset` is a decimal digit character as defined above,
 * otherwise `false`.
 */
@Suppress("NOTHING_TO_INLINE")
internal inline fun UByteBuffer.isDecDigit(offset: Int = 0) =
  size > offset && get(offset) in A_0 .. A_9

/**
 * As Decimal Digit
 *
 * Parses the byte at the given [offset] in the buffer as an ASCII decimal digit
 * character from the following character set:
 *
 * ```
 * 0 1 2 3 4 5 6 7 8 9
 * ```
 *
 * @param offset Offset of the byte to parse.
 *
 * Defaults to `0`
 *
 * @return The parsed value of the byte at the given offset.
 */
@Suppress("NOTHING_TO_INLINE")
internal inline fun UByteBuffer.asDecDigit(offset: Int = 0) =
  get(offset) - A_0

/**
 * Is Hex Digit
 *
 * Tests whether the byte at the given offset in the buffer is a valid ASCII
 * hex digit character from the following character set:
 *
 * ```
 * 0 1 2 3 4 5 6 7 8 9
 * A B C D E F
 * a b c d e f
 * ```
 *
 * If the buffer contains [offset] or fewer bytes, this function will return
 * `false`.
 *
 * @param offset Offset of the byte to test.
 *
 * Defaults to `0`
 *
 * @return `true` if the buffer contains more than [offset] bytes and the byte
 * at the given `offset` is a hex digit character as defined above, otherwise
 * `false`.
 */
@Suppress("NOTHING_TO_INLINE")
internal inline fun UByteBuffer.isHexDigit(offset: Int = 0) =
  size > offset && when (get(offset)) {
    in A_0    .. A_9    -> true
    in A_UP_A .. A_UP_F -> true
    in A_LO_A .. A_LO_F -> true
    else                -> false
  }

/**
 * As Hex Digit
 *
 * Parses the byte at the given [offset] in the buffer as an ASCII hex digit
 * character from the following character set:
 *
 * ```
 * 0 1 2 3 4 5 6 7 8 9
 * A B C D E F
 * a b c d e f
 * ```
 *
 * @param offset Offset of the byte to parse.
 *
 * Defaults to `0`
 *
 * @return The parsed value of the byte at the given offset.
 */
@Suppress("NOTHING_TO_INLINE")
internal inline fun UByteBuffer.asHexDigit(offset: Int = 0) =
  when (val x = get(offset)) {
    in A_0    .. A_9    -> x - A_0
    in A_UP_A .. A_UP_F -> x - A_UP_A + 10u
    in A_LO_A .. A_LO_F -> x - A_LO_A + 10u
    else                -> throw IllegalStateException("attempted to parse a non-hex digit as a base-16 int")
  }

/**
 * Is Blank
 *
 * Tests whether the byte at the given offset in the buffer is an ASCII `SPACE`
 * or ASCII `TAB` character.
 *
 * If the buffer contains [offset] or fewer bytes, this function will return
 * `false`.
 *
 * @param offset Offset of the byte to test.
 *
 * Defaults to `0`
 *
 * @return `true` if the buffer contains more than [offset] bytes and the byte
 * at the given `offset` is an ASCII `SPACE` or ASCII `TAB` character, otherwise
 * `false`.
 */
@Suppress("NOTHING_TO_INLINE")
internal inline fun UByteBuffer.isBlank(offset: Int = 0) =
  size > offset && (uCheck(A_SPACE, offset) || uCheck(A_TAB, offset))

/**
 * Is Break (YAML 1.1)
 *
 * Tests whether the buffer contains a line break indicator byte or bytes at the
 * given offset.
 *
 * Line breaks in YAML 1.1 are defined as:
 *
 * - CR
 * - LF
 * - Next Line
 * - Line Separator
 * - Paragraph Separator
 *
 * If the buffer contains [offset] or fewer bytes, this function will return
 * `false`.
 *
 * If this buffer contains greater than [offset] bytes but not enough bytes to
 * form a full line break UTF-8 sequence, this function will return `false`.
 *
 * @param offset Offset of the byte(s) to test.
 *
 * Defaults to `0`
 *
 * @return `true` if the buffer contains more than [offset] bytes and contains
 * a complete UTF-8 line break sequence from the list of valid line breaks
 * defined above at the given `offset`.  `false` if the buffer contains fewer
 * than [offset] bytes or does not contain a valid line break sequence at the
 * given `offset`.
 */
@Suppress("NOTHING_TO_INLINE")
internal inline fun UByteBuffer.isBreak_1_1(offset: Int = 0) =
  // LF | CR
  (size > offset && (uCheck(A_LF, offset) || uCheck(A_CR, offset)))
  // Next Line (NEL)
  || (size > offset + 1 && uCheck(UbC2, offset) && uCheck(Ub85, offset + 1))
  // Line Separator (LS) | Paragraph Separator (PS)
  || (
    size > offset + 2
    && uCheck(UbE2, offset)
    && uCheck(Ub80, offset + 1)
    && (uCheck(UbA8, offset + 2) || uCheck(UbA9, offset + 2))
  )

/**
 * Is Break (YAML 1.2)
 *
 * Tests whether the buffer contains a line break indicator byte at the given
 * offset.
 *
 * Line breaks in YAML 1.2 are defined as:
 *
 * - CR
 * - LF
 *
 * If the buffer contains [offset] or fewer bytes, this function will return
 * `false`.
 *
 * @param offset Offset of the byte(s) to test.
 *
 * Defaults to `0`
 *
 * @return `true` if the buffer contains more than [offset] bytes and contains
 * a valid line break indicator byte at the given offset, otherwise `false`.
 */
@Suppress("NOTHING_TO_INLINE")
internal inline fun UByteBuffer.isBreak_1_2(offset: Int = 0) =
  // LF | CR
  size > offset && (uCheck(A_LF, offset) || uCheck(A_CR, offset))

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
@Suppress("NOTHING_TO_INLINE")
internal inline fun UByteBuffer.isCRLF(offset: Int = 0) =
  size > offset + 1 && uCheck(A_CR, offset) && uCheck(A_LF, offset + 1)

