@file:Suppress("NOTHING_TO_INLINE")

package io.foxcapades.lib.k.yaml.read

import io.foxcapades.lib.k.yaml.bytes.*

private inline fun YAMLReader.has(offset: Int) = buffered >= offset

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
internal inline fun YAMLReader.testReaderOctet(octet: UByte, offset: Int = 0) = has(offset) && get(offset) == octet


/**
 * Unsafe Check Octet
 *
 * Tests whether the byte at the given offset in the buffer is equal to the
 * given test octet.
 *
 * Unlike [testReaderOctet], this method does not verify that the buffer is long enough to
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
internal inline fun YAMLReader.uCheck(octet: UByte, offset: Int = 0) = get(offset) == octet


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
internal inline fun YAMLReader.isASCII(offset: Int = 0) = has(offset) && get(offset) <= Ub7F


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
internal inline fun YAMLReader.isAlphanumeric(offset: Int = 0) =
  buffered > offset
    && when (get(offset)) {
      in A_LO_A .. A_LO_Z    -> true
      in A_UPPER_A .. A_UP_Z -> true
      in A_0    .. A_9     -> true
      A_DASH, A_UNDERSCORE -> true
      else                 -> false
    }


// region Numeric Values


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
internal inline fun YAMLReader.asDecDigit(offset: Int = 0) = get(offset) - A_0


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
internal inline fun YAMLReader.bufferHasHexDigit(offset: Int = 0) =
  buffered > offset && when (get(offset)) {
    in A_0    .. A_9       -> true
    in A_UPPER_A .. A_UP_F -> true
    in A_LO_A .. A_LO_F    -> true
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
internal inline fun YAMLReader.asHexDigit(offset: Int = 0) =
  when (val x = get(offset)) {
    in A_0    .. A_9       -> x - A_0
    in A_UPPER_A .. A_UP_F -> x - A_UPPER_A + 10u
    in A_LO_A .. A_LO_F    -> x - A_LO_A + 10u
    else                -> throw IllegalStateException("attempted to parse a non-hex digit as a base-16 int")
  }

// endregion Numeric Values

// region Newline Checks

/** `\r` */
internal inline fun YAMLReader.isCR(offset: Int = 0) = testReaderOctet(A_CARRIAGE_RETURN, offset)

/** `\n` */
internal inline fun YAMLReader.isLF(offset: Int = 0) = testReaderOctet(A_LINE_FEED, offset)

/** `<NEL>` */
internal inline fun YAMLReader.isNEL(offset: Int = 0) =
  has(offset + 1) && uCheck(UbC2, offset) && uCheck(Ub85, offset + 1)

/** `<LS>` */
internal inline fun YAMLReader.isLS(offset: Int = 0) =
  has(offset + 2)  && uCheck(UbE2, offset) && uCheck(Ub80, offset + 1) && uCheck(UbA8, offset + 2)

/** `<PS>` */
internal inline fun YAMLReader.isPS(offset: Int = 0) =
  has(offset + 2)  && uCheck(UbE2, offset) && uCheck(Ub80, offset + 1) && uCheck(UbA9, offset + 2)

/** `\r\n` */
internal inline fun YAMLReader.isCRLF(offset: Int = 0) =
  has(offset + 1) && uCheck(A_CARRIAGE_RETURN, offset) && uCheck(A_LINE_FEED, offset + 1)

// endregion Newline Checks
