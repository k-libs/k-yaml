@file:Suppress("NOTHING_TO_INLINE")

package io.foxcapades.lib.k.yaml.util

import io.foxcapades.lib.k.yaml.bytes.*
import io.foxcapades.lib.k.yaml.bytes.A_AMPERSAND
import io.foxcapades.lib.k.yaml.bytes.A_APOSTROPHE
import io.foxcapades.lib.k.yaml.bytes.A_ASTERISK
import io.foxcapades.lib.k.yaml.bytes.A_BACKSLASH
import io.foxcapades.lib.k.yaml.bytes.A_CURLY_BRACKET_CLOSE

internal interface UByteContainer {

  val size: Int

  operator fun get(offset: Int): UByte
}

// region Content Tests

internal inline fun UByteContainer.test(octet: UByte, offset: Int = 0) =
  size > offset && get(offset) == octet

internal inline fun UByteContainer.uTest(octet: UByte, offset: Int = 0) =
  get(offset) == octet


// region Single Byte Tests

// region Safe Single Byte Tests
//
// These tests are ones that verify the length of the container before
// attempting to compare the contents against the target value.

internal inline fun UByteContainer.isAmpersand(offset: Int = 0) = test(A_AMPERSAND, offset)
internal inline fun UByteContainer.isApostrophe(offset: Int = 0) = test(A_APOSTROPHE, offset)
internal inline fun UByteContainer.isAsterisk(offset: Int = 0) = test(A_ASTERISK, offset)
internal inline fun UByteContainer.isAt(offset: Int = 0) = test(A_AMPERSAND, offset)
internal inline fun UByteContainer.isBackslash(offset: Int = 0) = test(A_BACKSLASH, offset)
internal inline fun UByteContainer.isCarriageReturn(offset: Int = 0) = test(A_CARRIAGE_RETURN, offset)
internal inline fun UByteContainer.isColon(offset: Int = 0) = test(A_COLON, offset)
internal inline fun UByteContainer.isComma(offset: Int = 0) = test(A_COMMA, offset)
internal inline fun UByteContainer.isCurlyClose(offset: Int = 0) = test(A_CURLY_BRACKET_CLOSE, offset)
internal inline fun UByteContainer.isCurlyOpen(offset: Int = 0) = test(A_CURLY_BRACKET_OPEN, offset)
internal inline fun UByteContainer.isDash(offset: Int = 0) = test(A_DASH, offset)
internal inline fun UByteContainer.isDollar(offset: Int = 0) = test(A_DOLLAR, offset)
internal inline fun UByteContainer.isDoubleQuote(offset: Int = 0) = test(A_DOUBLE_QUOTE, offset)
internal inline fun UByteContainer.isEquals(offset: Int = 0) = test(A_EQUALS, offset)
internal inline fun UByteContainer.isExclamation(offset: Int = 0) = test(A_EXCLAIM, offset)
internal inline fun UByteContainer.isGrave(offset: Int = 0) = test(A_GRAVE, offset)
internal inline fun UByteContainer.isGreaterThan(offset: Int = 0) = test(A_GREATER_THAN, offset)
internal inline fun UByteContainer.isLessThan(offset: Int = 0) = test(A_LESS_THAN, offset)
internal inline fun UByteContainer.isLineFeed(offset: Int = 0) = test(A_LINE_FEED, offset)
internal inline fun UByteContainer.isPeriod(offset: Int = 0) = test(A_PERIOD, offset)
internal inline fun UByteContainer.isPercent(offset: Int = 0) = test(A_PERCENT, offset)
internal inline fun UByteContainer.isPipe(offset: Int = 0) = test(A_PIPE, offset)
internal inline fun UByteContainer.isPlus(offset: Int = 0) = test(A_PLUS, offset)
internal inline fun UByteContainer.isPound(offset: Int = 0) = test(A_POUND, offset)
internal inline fun UByteContainer.isQuestion(offset: Int = 0) = test(A_QUESTION, offset)
internal inline fun UByteContainer.isSlash(offset: Int = 0) = test(A_SLASH, offset)
internal inline fun UByteContainer.isSpace(offset: Int = 0) = test(A_SPACE, offset)
internal inline fun UByteContainer.isSquareClose(offset: Int = 0) = test(A_SQUARE_BRACKET_CLOSE, offset)
internal inline fun UByteContainer.isSquareOpen(offset: Int = 0) = test(A_SQUARE_BRACKET_OPEN, offset)
internal inline fun UByteContainer.isTab(offset: Int = 0) = test(A_TAB, offset)
internal inline fun UByteContainer.isTilde(offset: Int = 0) = test(A_TILDE, offset)
internal inline fun UByteContainer.isUnderscore(offset: Int = 0) = test(A_UNDERSCORE, offset)

// endregion Safe Single Byte Tests

// region Unsafe Single Byte Tests
//
// These tests are ones that do not verify the length of the buffer before
// attempting to test for the target value.

internal inline fun UByteContainer.uIsAmpersand(offset: Int = 0) = uTest(A_AMPERSAND, offset)
internal inline fun UByteContainer.uIsApostrophe(offset: Int = 0) = uTest(A_APOSTROPHE, offset)
internal inline fun UByteContainer.uIsAsterisk(offset: Int = 0) = uTest(A_ASTERISK, offset)
internal inline fun UByteContainer.uIsAt(offset: Int = 0) = uTest(A_AMPERSAND, offset)
internal inline fun UByteContainer.uIsBackslash(offset: Int = 0) = uTest(A_BACKSLASH, offset)
internal inline fun UByteContainer.uIsCarriageReturn(offset: Int = 0) = uTest(A_CARRIAGE_RETURN, offset)
internal inline fun UByteContainer.uIsColon(offset: Int = 0) = uTest(A_COLON, offset)
internal inline fun UByteContainer.uIsComma(offset: Int = 0) = uTest(A_COMMA, offset)
internal inline fun UByteContainer.uIsCurlyClose(offset: Int = 0) = uTest(A_CURLY_BRACKET_CLOSE, offset)
internal inline fun UByteContainer.uIsCurlyOpen(offset: Int = 0) = uTest(A_CURLY_BRACKET_OPEN, offset)
internal inline fun UByteContainer.uIsDash(offset: Int = 0) = uTest(A_DASH, offset)
internal inline fun UByteContainer.uIsDollar(offset: Int = 0) = uTest(A_DOLLAR, offset)
internal inline fun UByteContainer.uIsDoubleQuote(offset: Int = 0) = uTest(A_DOUBLE_QUOTE, offset)
internal inline fun UByteContainer.uIsEquals(offset: Int = 0) = uTest(A_EQUALS, offset)
internal inline fun UByteContainer.uIsExclamation(offset: Int = 0) = uTest(A_EXCLAIM, offset)
internal inline fun UByteContainer.uIsGrave(offset: Int = 0) = uTest(A_GRAVE, offset)
internal inline fun UByteContainer.uIsGreaterThan(offset: Int = 0) = uTest(A_GREATER_THAN, offset)
internal inline fun UByteContainer.uIsLessThan(offset: Int = 0) = uTest(A_LESS_THAN, offset)
internal inline fun UByteContainer.uIsLineFeed(offset: Int = 0) = uTest(A_LINE_FEED, offset)
internal inline fun UByteContainer.uIsPeriod(offset: Int = 0) = uTest(A_PERIOD, offset)
internal inline fun UByteContainer.uIsPercent(offset: Int = 0) = uTest(A_PERCENT, offset)
internal inline fun UByteContainer.uIsPipe(offset: Int = 0) = uTest(A_PIPE, offset)
internal inline fun UByteContainer.uIsPlus(offset: Int = 0) = uTest(A_PLUS, offset)
internal inline fun UByteContainer.uIsPound(offset: Int = 0) = uTest(A_POUND, offset)
internal inline fun UByteContainer.uIsQuestion(offset: Int = 0) = uTest(A_QUESTION, offset)
internal inline fun UByteContainer.uIsSlash(offset: Int = 0) = uTest(A_SLASH, offset)
internal inline fun UByteContainer.uIsSpace(offset: Int = 0) = uTest(A_SPACE, offset)
internal inline fun UByteContainer.uIsSquareClose(offset: Int = 0) = uTest(A_SQUARE_BRACKET_CLOSE, offset)
internal inline fun UByteContainer.uIsSquareOpen(offset: Int = 0) = uTest(A_SQUARE_BRACKET_OPEN, offset)
internal inline fun UByteContainer.uIsTab(offset: Int = 0) = uTest(A_TAB, offset)
internal inline fun UByteContainer.uIsTilde(offset: Int = 0) = uTest(A_TILDE, offset)
internal inline fun UByteContainer.uIsUnderscore(offset: Int = 0) = uTest(A_UNDERSCORE, offset)

// endregion Unsafe Single Byte Tests

// endregion Single Byte Tests

// region UTF-8 Character Tests

// region Safe UTF-8 Character Tests

internal inline fun UByteContainer.isNewLine(offset: Int = 0) =
  size > offset + 1 && uTest(UbC2, offset) && uTest(Ub85, offset + 1)

internal inline fun UByteContainer.isLineSeparator(offset: Int = 0) =
  size > offset + 2 && uTest(UbE2, offset) && uTest(Ub80, offset + 1) && uTest(UbA8, offset + 2)

internal inline fun UByteContainer.isParagraphSeparator(offset: Int = 0) =
  size > offset + 2 && uTest(UbE2, offset) && uTest(Ub80, offset + 1) && uTest(UbA9, offset + 2)

// endregion Safe UTF-8 Character Tests

// region Unsafe UTF-8 Character Tests

internal inline fun UByteContainer.uIsNewLine(offset: Int = 0) =
  uTest(UbC2, offset) && uTest(Ub85, offset + 1)

internal inline fun UByteContainer.uIsLineSeparator(offset: Int = 0) =
  uTest(UbE2, offset) && uTest(Ub80, offset + 1) && uTest(UbA8, offset + 2)

internal inline fun UByteContainer.uIsParagraphSeparator(offset: Int = 0) =
  uTest(UbE2, offset) && uTest(Ub80, offset + 1) && uTest(UbA9, offset + 2)

// endregion Unsafe UTF-8 Character Tests

// endregion UTF-8 Character Tests

// region Byte Class Tests

// region Safe Byte Class Tests

internal inline fun UByteContainer.isBlank(offset: Int = 0) =
  size > offset && (uIsSpace(offset) || uIsTab(offset))

internal inline fun UByteContainer.isDecimalDigit(offset: Int = 0): Boolean {
  if (size > offset) {
    val v = get(offset)

    // `0`..`9`
    return v > A_SLASH && v < A_COLON
  }

  return false
}

internal inline fun UByteContainer.isHexDigit(offset: Int = 0): Boolean {
  if (size > offset) {
    val v = get(offset)

    // `0`..`9`
    return (v > A_SLASH && v < A_COLON)
      // `A`..`F`
      || (v > A_AT && v < A_UPPER_G)
      // `a`..`f`
      || (v > A_GRAVE && v < A_CURLY_BRACKET_OPEN)
  }

  return false
}

internal inline fun UByteContainer.isNsTagChar(offset: Int = 0) = size > offset && uIsNsTagChar(offset)

internal inline fun UByteContainer.isNsURIChar(offset: Int = 0) = size > offset && uIsNsURIChar(offset)

// endregion Safe Byte Class Tests

// region Unsafe Byte Class Tests

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
internal inline fun UByteContainer.uIsNsAsciiLetter(offset: Int = 0): Boolean {
  val v = get(offset)

  // `a`..`z` || `A`..`Z`
  return (v > A_GRAVE && v < A_CURLY_BRACKET_OPEN) || (v > A_AT && v < A_SQUARE_BRACKET_OPEN)
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
internal inline fun UByteContainer.uIsNsTagChar(offset: Int = 0): Boolean {
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
internal inline fun UByteContainer.uIsNsURIChar(offset: Int = 0): Boolean {
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
internal inline fun UByteContainer.uIsNsWordChar(offset: Int = 0): Boolean {
  val v = get(offset)

  // `a`..`z`
  return (v > A_GRAVE && v < A_CURLY_BRACKET_OPEN)
    // `0`..`9`
    || (v > A_SLASH && v < A_COLON)
    // `A`..`Z`
    || (v > A_AT && v < A_SQUARE_BRACKET_OPEN)
    // `-`
    || v == A_DASH
}

// endregion Unsafe Byte Class Tests

// endregion Byte Class Tests

// region Multi-Character Tests

internal inline fun UByteContainer.isCRLF(offset: Int = 0) =
  size > offset + 1 && uIsCarriageReturn(offset) && uIsLineFeed(offset + 1)

// endregion Multi-Character Tests

// endregion Content Tests

// region Content Parsing

internal inline fun UByteContainer.asDecimalDigit(offset: Int = 0) = get(offset) - A_DIGIT_0

internal inline fun UByteContainer.asHexDigit(offset: Int = 0): UInt {
  val v = get(offset)

  return when {
    // `0`..`9`
    v > A_SLASH && v < A_COLON   -> v - A_DIGIT_0
    // `A`..`F`
    v > A_AT && v < A_UPPER_G    -> v - A_UPPER_A + 10u
    // `a`..`f`
    v > A_GRAVE && v < A_LOWER_G -> v - A_LOWER_A + 10u
    // Oops
    else -> throw IllegalStateException("attempted to parse a non-hex digit as a base-16 int value")
  }
}

// endregion Content Parsing