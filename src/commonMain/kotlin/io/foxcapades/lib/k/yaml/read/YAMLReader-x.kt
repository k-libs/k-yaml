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
internal inline fun YAMLReader.check(octet: UByte, offset: Int = 0) = has(offset) && get(offset) == octet


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
      in A_LO_A .. A_LO_Z -> true
      in A_UP_A .. A_UP_Z -> true
      in A_0    .. A_9    -> true
      A_DASH, A_UNDER     -> true
      else                -> false
    }


// region Numeric Values

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
internal inline fun YAMLReader.isDecDigit(offset: Int = 0) = has(offset) && get(offset) in A_0 .. A_9


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
internal inline fun YAMLReader.isHexDigit(offset: Int = 0) =
  buffered > offset && when (get(offset)) {
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
internal inline fun YAMLReader.asHexDigit(offset: Int = 0) =
  when (val x = get(offset)) {
    in A_0    .. A_9    -> x - A_0
    in A_UP_A .. A_UP_F -> x - A_UP_A + 10u
    in A_LO_A .. A_LO_F -> x - A_LO_A + 10u
    else                -> throw IllegalStateException("attempted to parse a non-hex digit as a base-16 int")
  }

// endregion Numeric Values

// region Safe Indicator Character Tests

internal inline fun YAMLReader.isBackslash(offset: Int = 0) = check(A_BACKSLASH, offset)
internal inline fun YAMLReader.isColon(offset: Int = 0)     = check(A_COLON, offset)
internal inline fun YAMLReader.isComma(offset: Int = 0)     = check(A_COMMA, offset)
internal inline fun YAMLReader.isDash(offset: Int = 0)      = check(A_DASH, offset)
internal inline fun YAMLReader.isPercent(offset: Int = 0)   = check(A_PERCENT, offset)
internal inline fun YAMLReader.isPeriod(offset: Int = 0)    = check(A_PERIOD, offset)
internal inline fun YAMLReader.isPound(offset: Int = 0)     = check(A_POUND, offset)
internal inline fun YAMLReader.isQuestion(offset: Int = 0)  = check(A_QUESTION, offset)

// endregion Safe Indicator Character Tests

// region Unsafe Indicator Character Tests

internal inline fun YAMLReader.uIsBackslash(offset: Int = 0) = uCheck(A_BACKSLASH, offset)
internal inline fun YAMLReader.uIsColon(offset: Int = 0)     = uCheck(A_COLON, offset)
internal inline fun YAMLReader.uIsComma(offset: Int = 0)     = uCheck(A_COMMA, offset)
internal inline fun YAMLReader.uIsDash(offset: Int = 0)      = uCheck(A_DASH, offset)
internal inline fun YAMLReader.uIsPercent(offset: Int = 0)   = uCheck(A_PERCENT, offset)
internal inline fun YAMLReader.uIsPeriod(offset: Int = 0)    = uCheck(A_PERIOD, offset)
internal inline fun YAMLReader.uIsPound(offset: Int = 0)     = uCheck(A_POUND, offset)
internal inline fun YAMLReader.uIsQuestion(offset: Int = 0)  = uCheck(A_QUESTION, offset)

// endregion Unsafe Indicator Character Tests

// region Whitespace Checks

/** `<SPACE>` */
internal inline fun YAMLReader.isSpace(offset: Int = 0) = check(A_SPACE, offset)

/** `<SPACE>` */
internal inline fun YAMLReader.uIsSpace(offset: Int = 0) = uCheck(A_SPACE, offset)

/** `<TAB>` */
internal inline fun YAMLReader.isTab(offset: Int = 0) = check(A_TAB, offset)

/** `<TAB>` */
internal inline fun YAMLReader.uIsTab(offset: Int = 0) = uCheck(A_TAB, offset)

/** `<SPACE>` or `<TAB>` */
internal inline fun YAMLReader.isBlank(offset: Int = 0) = has(offset) && (uIsSpace(offset) || uIsTab(offset))

/** `<SPACE>` or `<TAB>` */
internal inline fun YAMLReader.uIsBlank(offset: Int = 0) = uIsSpace(offset) || uCheck(A_TAB, offset)

// endregion Whitespace Checks

// region Newline Checks

/** `\r` */
internal inline fun YAMLReader.isCR(offset: Int = 0) = check(A_CR, offset)

/** `\r` */
internal inline fun YAMLReader.uIsCR(offset: Int = 0) = uCheck(A_CR, offset)

/** `\n` */
internal inline fun YAMLReader.isLF(offset: Int = 0) = check(A_LF, offset)

/** `\n` */
internal inline fun YAMLReader.uIsLF(offset: Int = 0) = uCheck(A_LF, offset)

/** `\r` or `\n` */
internal inline fun YAMLReader.isCROrLF(offset: Int = 0) = has(offset) && (uIsLF(offset) || uIsCR(offset))

/** `\r` or `\n` */
internal inline fun YAMLReader.uIsCROrLF(offset: Int = 0) = uIsLF(offset) || uIsCR(offset)

/** `<NEL>` */
internal inline fun YAMLReader.isNEL(offset: Int = 0) =
  has(offset + 1) && uCheck(UbC2, offset) && uCheck(Ub85, offset + 1)

/** `<LS>` */
internal inline fun YAMLReader.isLS(offset: Int = 0) =
  has(offset + 2)  && uCheck(UbE2, offset) && uCheck(Ub80, offset + 1) && uCheck(UbA8, offset + 2)

/** `<PS>` */
internal inline fun YAMLReader.isPS(offset: Int = 0) =
  has(offset + 2)  && uCheck(UbE2, offset) && uCheck(Ub80, offset + 1) && uCheck(UbA9, offset + 2)

internal inline fun YAMLReader.isLSOrPS(i: Int = 0) =
  has(i + 2) && uCheck(UbE2, i) && uCheck(Ub80, i + 1) && (uCheck(UbA8, i + 2) || uCheck(UbA9, i + 2))

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
 * If the buffer contains [i] or fewer bytes, this function will return
 * `false`.
 *
 * If this buffer contains greater than [i] bytes but not enough bytes to
 * form a full line break UTF-8 sequence, this function will return `false`.
 *
 * @param i Offset of the byte(s) to test.
 *
 * Defaults to `0`
 *
 * @return `true` if the buffer contains more than [i] bytes and contains
 * a complete UTF-8 line break sequence from the list of valid line breaks
 * defined above at the given `offset`.  `false` if the buffer contains fewer
 * than [i] bytes or does not contain a valid line break sequence at the
 * given `offset`.
 */
@Suppress("NOTHING_TO_INLINE")
internal inline fun YAMLReader.isBreak_1_1(i: Int = 0) = isCROrLF(i) || isNEL(i) || isLSOrPS(i)

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
 * If the buffer contains [i] or fewer bytes, this function will return
 * `false`.
 *
 * @param i Offset of the byte(s) to test.
 *
 * Defaults to `0`
 *
 * @return `true` if the buffer contains more than [i] bytes and contains
 * a valid line break indicator byte at the given offset, otherwise `false`.
 */
internal inline fun YAMLReader.isBreak_1_2(i: Int = 0) =
  when {
    isCROrLF(i) -> true
    else        -> false
  }

/** `\r\n` */
internal inline fun YAMLReader.isCRLF(offset: Int = 0) =
  has(offset + 1) && uCheck(A_CR, offset) && uCheck(A_LF, offset + 1)

// endregion Newline Checks

internal inline fun YAMLReader.isEOF(offset: Int = 0) = atEOF && buffered <= offset

internal inline fun YAMLReader.isBreakOrEOF(offset: Int = 0) =
  isBreak_1_1(offset) || isEOF(offset)