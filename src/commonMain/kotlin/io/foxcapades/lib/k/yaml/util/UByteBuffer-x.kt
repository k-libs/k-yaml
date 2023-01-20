@file:Suppress("NOTHING_TO_INLINE")

package io.foxcapades.lib.k.yaml.util

import io.foxcapades.lib.k.yaml.bytes.*

internal inline fun UByteBuffer.hasOffset(offset: Int = 0) = size > offset

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
internal inline fun UByteBuffer.check(octet: UByte, offset: Int = 0) = hasOffset(offset) && get(offset) == octet

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

// region Category Checks

// region Safe Category Checks

internal inline fun UByteBuffer.isUpperAlpha(offset: Int = 0) = hasOffset(offset) && uIsUpperAlpha(offset)
internal inline fun UByteBuffer.isLowerAlpha(offset: Int = 0) = hasOffset(offset) && uIsLowerAlpha(offset)
internal inline fun UByteBuffer.isDecimalDigit(offset: Int = 0) = hasOffset(offset) && uIsDecimalDigit(offset)

internal inline fun UByteBuffer.isBlank(offset: Int = 0) = hasOffset(offset) && uIsBlank(offset)

internal inline fun UByteBuffer.isNsURIChar(offset: Int = 0) = hasOffset(offset) && uIsNsURIChar(offset)
internal inline fun UByteBuffer.isNsTagChar(offset: Int = 0) = hasOffset(offset) && uIsNsTagChar(offset)

// endregion Safe Category Checks

// region Unsafe Category Checks

internal inline fun UByteBuffer.uIsUpperAlpha(offset: Int = 0): Boolean {
  val v = get(offset)
  return v > A_AT && v < A_SQUARE_BRACKET_OPEN
}

internal inline fun UByteBuffer.uIsLowerAlpha(offset: Int = 0): Boolean {
  val v = get(offset)
  return v > A_GRAVE && v < A_CURLY_BRACKET_OPEN
}

internal inline fun UByteBuffer.uIsDecimalDigit(offset: Int = 0): Boolean {
  val v = get(offset)
  return v > A_SLASH && v < A_COLON
}

internal inline fun UByteBuffer.uIsAlphanumeric(offset: Int = 0) =
  uIsLowerAlpha(offset) || uIsDecimalDigit(offset) || uIsUpperAlpha(offset)

internal inline fun UByteBuffer.uIsBlank(offset: Int = 0) =
  uIsSpace(offset) || uIsTab(offset)

/**
 * YAML 1.2.2 Spec: `ns-ascii-letter`
 *
 * ASCII letter (alphabetic) characters:
 *
 * ```
 * [37] ns-ascii-letter ::=
 *     [x41-x5A]           # A-Z
 *   | [x61-x7A]           # a-z
 * ```
 */
internal inline fun UByteBuffer.uIsNsAsciiLetter(offset: Int = 0) =
  uIsLowerAlpha(offset) || uIsUpperAlpha(offset)

/**
 * YAML 1.2.2 Spec: `ns-word-char`
 *
 * Word (alphanumeric) characters for identifiers:
 *
 * ```
 * [38] ns-word-char ::=
 *     ns-dec-digit        # 0-9
 *   | ns-ascii-letter     # A-Z a-z
 *   | '-'                 # '-'
 * ```
 */
internal inline fun UByteBuffer.uIsNsWordChar(offset: Int = 0) =
  uIsLowerAlpha(offset) || uIsDecimalDigit(offset) || uIsUpperAlpha(offset) || uIsDash(offset)

/**
 * YAML 1.2.2 Spec: `ns-uri-char`
 *
 * **WARNING** This method **DOES NOT** return true for `%` characters.  The
 * scope of this method is simply the next singular byte and thus the 3 byte
 * combination that makes up a valid `%\d\d` sequence is out of scope for what
 * this function tests.
 *
 * URI characters for tags, as defined in the URI specification18.
 *
 * By convention, any URI characters other than the allowed printable ASCII
 * characters are first encoded in UTF-8 and then each byte is escaped using the
 * “%” character. The YAML processor must not expand such escaped characters.
 * Tag characters must be preserved and compared exactly as presented in the
 * YAML stream, without any processing.
 *
 * ```
 * [39] ns-uri-char ::=
 *     (
 *       '%'
 *       ns-hex-digit{2}
 *     )
 *   | ns-word-char
 *   | '#'
 *   | ';'
 *   | '/'
 *   | '?'
 *   | ':'
 *   | '@'
 *   | '&'
 *   | '='
 *   | '+'
 *   | '$'
 *   | ','
 *   | '_'
 *   | '.'
 *   | '!'
 *   | '~'
 *   | '*'
 *   | "'"
 *   | '('
 *   | ')'
 *   | '['
 *   | ']'
 * ```
 */
internal inline fun UByteBuffer.uIsNsURIChar(offset: Int = 0): Boolean
{
  val v = get(offset)

  // `a`..`z`
  return if (v > A_GRAVE && v < A_CURLY_BRACKET_OPEN)
    true
  // '&'..';'
  else if (v > A_PERCENT && v < A_LESS_THAN)
    true
  // `?`..`[`
  else if (v > A_GREATER_THAN && v < A_BACKSLASH)
    true
  else
    v == A_EXCLAIM
      || v == A_POUND
      || v == A_DOLLAR
      || v == A_EQUALS
      || v == A_SQUARE_BRACKET_CLOSE
      || v == A_UNDERSCORE
      || v == A_TILDE
}

/**
 * YAML Spec 1.2.2: `ns-tag-char`
 *
 * **WARNING** This method **DOES NOT** return true for `%` characters.  The
 * scope of this method is simply the next singular byte and thus the 3 byte
 * combination that makes up a valid `%\d\d` sequence is out of scope for what
 * this function tests.
 *
 * The “!” character is used to indicate the end of a named tag handle; hence
 * its use in tag shorthands is restricted. In addition, such shorthands must
 * not contain the “[”, “]”, “{”, “}” and “,” characters. These characters would
 * cause ambiguity with flow collection structures.
 *
 * ```
 * [40] ns-tag-char ::=
 *     ns-uri-char
 *   - c-tag               # '!'
 *   - c-flow-indicator
 * ```
 */
internal inline fun UByteBuffer.uIsNsTagChar(offset: Int = 0): Boolean {
  val v = get(offset)

  // `a`..`z`
  return if (v > A_GRAVE && v < A_CURLY_BRACKET_OPEN)
    true
  // `&`..`;`
  else if (v > A_PERCENT && v < A_LESS_THAN)
    true
  // `?`..`Z`
  else if (v > A_GREATER_THAN && v < A_SQUARE_BRACKET_OPEN)
    true
  // # $ = _ ~
  else
    v == A_POUND
      || v == A_DOLLAR
      || v == A_EQUALS
      || v == A_UNDERSCORE
      || v == A_TILDE
}

// endregion Unsafe Category Checks

// endregion Category Checks

// region Single Byte Checks

// region Safe Single Byte Checks

internal inline fun UByteBuffer.isExclaim(offset: Int = 0) = check(A_EXCLAIM, offset)
internal inline fun UByteBuffer.isSpace(offset: Int = 0) = check(A_SPACE, offset)

// endregion Safe Single Byte Checks

// region Unsafe Single Byte Checks

internal inline fun UByteBuffer.uIsAmpersand(offset: Int = 0) = uCheck(A_AMPERSAND, offset)
internal inline fun UByteBuffer.uIsApostrophe(offset: Int = 0) = uCheck(A_APOSTROPHE, offset)
internal inline fun UByteBuffer.uIsAt(offset: Int = 0) = uCheck(A_AT, offset)
internal inline fun UByteBuffer.uIsAsterisk(offset: Int = 0) = uCheck(A_ASTERISK, offset)
internal inline fun UByteBuffer.uIsBackslash(offset: Int = 0) = uCheck(A_BACKSLASH, offset)
internal inline fun UByteBuffer.uIsCarriageReturn(offset: Int = 0) = uCheck(A_CARRIAGE_RETURN, offset)
internal inline fun UByteBuffer.uIsColon(offset: Int = 0) = uCheck(A_COLON, offset)
internal inline fun UByteBuffer.uIsComma(offset: Int = 0) = uCheck(A_COMMA, offset)
internal inline fun UByteBuffer.uIsCurlyBracketClose(offset: Int = 0) = uCheck(A_CURLY_BRACKET_CLOSE, offset)
internal inline fun UByteBuffer.uIsCurlyBracketOpen(offset: Int = 0) = uCheck(A_CURLY_BRACKET_OPEN, offset)
internal inline fun UByteBuffer.uIsDash(offset: Int = 0) = uCheck(A_DASH, offset)
internal inline fun UByteBuffer.uIsDollar(offset: Int = 0) = uCheck(A_DOLLAR, offset)
internal inline fun UByteBuffer.uIsDoubleQuote(offset: Int = 0) = uCheck(A_DOUBLE_QUOTE, offset)
internal inline fun UByteBuffer.uIsExclaim(offset: Int = 0) = uCheck(A_EXCLAIM, offset)
internal inline fun UByteBuffer.uIsGrave(offset: Int = 0) = uCheck(A_GRAVE, offset)
internal inline fun UByteBuffer.uIsGreaterThan(offset: Int = 0) = uCheck(A_GREATER_THAN, offset)
internal inline fun UByteBuffer.uIsLineFeed(offset: Int = 0) = uCheck(A_LINE_FEED, offset)
internal inline fun UByteBuffer.uIsPercent(offset: Int = 0) = uCheck(A_PERCENT, offset)
internal inline fun UByteBuffer.uIsPeriod(offset: Int = 0) = uCheck(A_PERIOD, offset)
internal inline fun UByteBuffer.uIsPipe(offset: Int = 0) = uCheck(A_PIPE, offset)
internal inline fun UByteBuffer.uIsPlus(offset: Int = 0) = uCheck(A_PLUS, offset)
internal inline fun UByteBuffer.uIsPound(offset: Int = 0) = uCheck(A_POUND, offset)
internal inline fun UByteBuffer.uIsQuestion(offset: Int = 0) = uCheck(A_QUESTION, offset)
internal inline fun UByteBuffer.uIsSpace(offset: Int = 0) = uCheck(A_SPACE, offset)
internal inline fun UByteBuffer.uIsSquareBracketClose(offset: Int = 0) = uCheck(A_SQUARE_BRACKET_CLOSE, offset)
internal inline fun UByteBuffer.uIsSquareBracketOpen(offset: Int = 0) = uCheck(A_SQUARE_BRACKET_OPEN, offset)
internal inline fun UByteBuffer.uIsTab(offset: Int = 0) = uCheck(A_TAB, offset)
internal inline fun UByteBuffer.uIsTilde(offset: Int = 0) = uCheck(A_TILDE, offset)
internal inline fun UByteBuffer.uIsUnderscore(offset: Int = 0) = uCheck(A_UNDERSCORE, offset)

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