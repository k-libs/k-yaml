@file:Suppress("NOTHING_TO_INLINE")

package io.foxcapades.lib.k.yaml.util

import io.foxcapades.lib.k.yaml.bytes.*
import io.foxcapades.lib.k.yaml.bytes.A_AMPERSAND
import io.foxcapades.lib.k.yaml.bytes.A_APOSTROPHE
import io.foxcapades.lib.k.yaml.bytes.A_ASTERISK
import io.foxcapades.lib.k.yaml.bytes.A_BACKSLASH
import io.foxcapades.lib.k.yaml.bytes.A_CURLY_BRACKET_CLOSE
import io.foxcapades.lib.k.yaml.scan.*

internal interface UByteSource {

  val size: Int

  operator fun get(offset: Int): UByte

  fun pop(): UByte

  fun peek(): UByte

  fun skip(count: Int)
}

// region Content Tests

internal inline fun UByteSource.test(octet: UByte, offset: Int = 0) =
  size > offset && get(offset) == octet

internal inline fun UByteSource.uTest(octet: UByte, offset: Int = 0) =
  get(offset) == octet


// region Single Byte Tests

// region Safe Single Byte Tests
//
// These tests are ones that verify the length of the container before
// attempting to compare the contents against the target value.

internal inline fun UByteSource.isAmpersand(offset: Int = 0) = test(A_AMPERSAND, offset)
internal inline fun UByteSource.isApostrophe(offset: Int = 0) = test(A_APOSTROPHE, offset)
internal inline fun UByteSource.isAsterisk(offset: Int = 0) = test(A_ASTERISK, offset)
internal inline fun UByteSource.isAt(offset: Int = 0) = test(A_AMPERSAND, offset)
internal inline fun UByteSource.isBackslash(offset: Int = 0) = test(A_BACKSLASH, offset)
internal inline fun UByteSource.isCarriageReturn(offset: Int = 0) = test(A_CARRIAGE_RETURN, offset)
internal inline fun UByteSource.isColon(offset: Int = 0) = test(A_COLON, offset)
internal inline fun UByteSource.isComma(offset: Int = 0) = test(A_COMMA, offset)
internal inline fun UByteSource.isCurlyClose(offset: Int = 0) = test(A_CURLY_BRACKET_CLOSE, offset)
internal inline fun UByteSource.isCurlyOpen(offset: Int = 0) = test(A_CURLY_BRACKET_OPEN, offset)
internal inline fun UByteSource.isDash(offset: Int = 0) = test(A_DASH, offset)
internal inline fun UByteSource.isDollar(offset: Int = 0) = test(A_DOLLAR, offset)
internal inline fun UByteSource.isDoubleQuote(offset: Int = 0) = test(A_DOUBLE_QUOTE, offset)
internal inline fun UByteSource.isEquals(offset: Int = 0) = test(A_EQUALS, offset)
internal inline fun UByteSource.isExclamation(offset: Int = 0) = test(A_EXCLAIM, offset)
internal inline fun UByteSource.isGrave(offset: Int = 0) = test(A_GRAVE, offset)
internal inline fun UByteSource.isGreaterThan(offset: Int = 0) = test(A_GREATER_THAN, offset)
internal inline fun UByteSource.isLessThan(offset: Int = 0) = test(A_LESS_THAN, offset)
internal inline fun UByteSource.isLineFeed(offset: Int = 0) = test(A_LINE_FEED, offset)
internal inline fun UByteSource.isPeriod(offset: Int = 0) = test(A_PERIOD, offset)
internal inline fun UByteSource.isPercent(offset: Int = 0) = test(A_PERCENT, offset)
internal inline fun UByteSource.isPipe(offset: Int = 0) = test(A_PIPE, offset)
internal inline fun UByteSource.isPlus(offset: Int = 0) = test(A_PLUS, offset)
internal inline fun UByteSource.isPound(offset: Int = 0) = test(A_POUND, offset)
internal inline fun UByteSource.isQuestion(offset: Int = 0) = test(A_QUESTION, offset)
internal inline fun UByteSource.isSlash(offset: Int = 0) = test(A_SLASH, offset)
internal inline fun UByteSource.isSpace(offset: Int = 0) = test(A_SPACE, offset)
internal inline fun UByteSource.isSquareClose(offset: Int = 0) = test(A_SQUARE_BRACKET_CLOSE, offset)
internal inline fun UByteSource.isSquareOpen(offset: Int = 0) = test(A_SQUARE_BRACKET_OPEN, offset)
internal inline fun UByteSource.isTab(offset: Int = 0) = test(A_TAB, offset)
internal inline fun UByteSource.isTilde(offset: Int = 0) = test(A_TILDE, offset)
internal inline fun UByteSource.isUnderscore(offset: Int = 0) = test(A_UNDERSCORE, offset)

// endregion Safe Single Byte Tests

// region Unsafe Single Byte Tests
//
// These tests are ones that do not verify the length of the buffer before
// attempting to test for the target value.

internal inline fun UByteSource.uIsAmpersand(offset: Int = 0) = uTest(A_AMPERSAND, offset)
internal inline fun UByteSource.uIsApostrophe(offset: Int = 0) = uTest(A_APOSTROPHE, offset)
internal inline fun UByteSource.uIsAsterisk(offset: Int = 0) = uTest(A_ASTERISK, offset)
internal inline fun UByteSource.uIsAt(offset: Int = 0) = uTest(A_AMPERSAND, offset)
internal inline fun UByteSource.uIsBackslash(offset: Int = 0) = uTest(A_BACKSLASH, offset)
internal inline fun UByteSource.uIsCarriageReturn(offset: Int = 0) = uTest(A_CARRIAGE_RETURN, offset)
internal inline fun UByteSource.uIsColon(offset: Int = 0) = uTest(A_COLON, offset)
internal inline fun UByteSource.uIsComma(offset: Int = 0) = uTest(A_COMMA, offset)
internal inline fun UByteSource.uIsCurlyClose(offset: Int = 0) = uTest(A_CURLY_BRACKET_CLOSE, offset)
internal inline fun UByteSource.uIsCurlyOpen(offset: Int = 0) = uTest(A_CURLY_BRACKET_OPEN, offset)
internal inline fun UByteSource.uIsDash(offset: Int = 0) = uTest(A_DASH, offset)
internal inline fun UByteSource.uIsDollar(offset: Int = 0) = uTest(A_DOLLAR, offset)
internal inline fun UByteSource.uIsDoubleQuote(offset: Int = 0) = uTest(A_DOUBLE_QUOTE, offset)
internal inline fun UByteSource.uIsEquals(offset: Int = 0) = uTest(A_EQUALS, offset)
internal inline fun UByteSource.uIsExclamation(offset: Int = 0) = uTest(A_EXCLAIM, offset)
internal inline fun UByteSource.uIsGrave(offset: Int = 0) = uTest(A_GRAVE, offset)
internal inline fun UByteSource.uIsGreaterThan(offset: Int = 0) = uTest(A_GREATER_THAN, offset)
internal inline fun UByteSource.uIsLessThan(offset: Int = 0) = uTest(A_LESS_THAN, offset)
internal inline fun UByteSource.uIsLineFeed(offset: Int = 0) = uTest(A_LINE_FEED, offset)
internal inline fun UByteSource.uIsPeriod(offset: Int = 0) = uTest(A_PERIOD, offset)
internal inline fun UByteSource.uIsPercent(offset: Int = 0) = uTest(A_PERCENT, offset)
internal inline fun UByteSource.uIsPipe(offset: Int = 0) = uTest(A_PIPE, offset)
internal inline fun UByteSource.uIsPlus(offset: Int = 0) = uTest(A_PLUS, offset)
internal inline fun UByteSource.uIsPound(offset: Int = 0) = uTest(A_POUND, offset)
internal inline fun UByteSource.uIsQuestion(offset: Int = 0) = uTest(A_QUESTION, offset)
internal inline fun UByteSource.uIsSlash(offset: Int = 0) = uTest(A_SLASH, offset)
internal inline fun UByteSource.uIsSpace(offset: Int = 0) = uTest(A_SPACE, offset)
internal inline fun UByteSource.uIsSquareClose(offset: Int = 0) = uTest(A_SQUARE_BRACKET_CLOSE, offset)
internal inline fun UByteSource.uIsSquareOpen(offset: Int = 0) = uTest(A_SQUARE_BRACKET_OPEN, offset)
internal inline fun UByteSource.uIsTab(offset: Int = 0) = uTest(A_TAB, offset)
internal inline fun UByteSource.uIsTilde(offset: Int = 0) = uTest(A_TILDE, offset)
internal inline fun UByteSource.uIsUnderscore(offset: Int = 0) = uTest(A_UNDERSCORE, offset)

// endregion Unsafe Single Byte Tests

// endregion Single Byte Tests

// region UTF-8 Character Tests

// region Safe UTF-8 Character Tests

internal inline fun UByteSource.isNextLine(offset: Int = 0) =
  size > offset + 1 && uTest(UbC2, offset) && uTest(Ub85, offset + 1)

internal inline fun UByteSource.isLineSeparator(offset: Int = 0) =
  size > offset + 2 && uTest(UbE2, offset) && uTest(Ub80, offset + 1) && uTest(UbA8, offset + 2)

internal inline fun UByteSource.isParagraphSeparator(offset: Int = 0) =
  size > offset + 2 && uTest(UbE2, offset) && uTest(Ub80, offset + 1) && uTest(UbA9, offset + 2)

// endregion Safe UTF-8 Character Tests

// region Unsafe UTF-8 Character Tests

internal inline fun UByteSource.uIsNextLine(offset: Int = 0) =
  uTest(UbC2, offset) && uTest(Ub85, offset + 1)

internal inline fun UByteSource.uIsLineSeparator(offset: Int = 0) =
  uTest(UbE2, offset) && uTest(Ub80, offset + 1) && uTest(UbA8, offset + 2)

internal inline fun UByteSource.uIsParagraphSeparator(offset: Int = 0) =
  uTest(UbE2, offset) && uTest(Ub80, offset + 1) && uTest(UbA9, offset + 2)

// endregion Unsafe UTF-8 Character Tests

// endregion UTF-8 Character Tests

// region Byte Class Tests

// region Safe Byte Class Tests

internal inline fun UByteSource.isBlank(offset: Int = 0) =
  size > offset && (uIsSpace(offset) || uIsTab(offset))

internal inline fun UByteSource.isDecimalDigit(offset: Int = 0): Boolean {
  if (size > offset) {
    val v = get(offset)

    // `0`..`9`
    return v > A_SLASH && v < A_COLON
  }

  return false
}

internal inline fun UByteSource.isHexDigit(offset: Int = 0): Boolean {
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

// endregion Safe Byte Class Tests

// region Unsafe Byte Class Tests

internal inline fun UByteSource.uIsBlank(offset: Int = 0) =
  uTest(A_SPACE, offset) || uTest(A_TAB, offset)

internal inline fun UByteSource.uIsFlowIndicator(offset: Int) =
  uTest(A_SQUARE_BRACKET_OPEN, offset)
  || uTest(A_SQUARE_BRACKET_CLOSE, offset)
  || uTest(A_CURLY_BRACKET_OPEN, offset)
  || uTest(A_CURLY_BRACKET_CLOSE, offset)

// endregion Unsafe Byte Class Tests

// endregion Byte Class Tests

// region Character Class Tests

// region Safe Character Class Tests

internal inline fun UByteSource.isAnyBreak(offset: Int = 0) =
  if (size > offset + 2)
    uIsLineFeed(offset) || uIsCarriageReturn(offset) || uIsNextLine(offset) || uIsLineSeparator(offset) || uIsParagraphSeparator(offset)
  else if (size > offset + 1)
    uIsLineFeed(offset) || uIsCarriageReturn(offset) || uIsNextLine(offset)
  else if (size > offset)
    uIsLineFeed(offset) || uIsCarriageReturn(offset)
  else
    false

internal inline fun UByteSource.isBlankOrAnyBreak(offset: Int = 0) =
  (size > offset + 2 && (uIsBlank(offset) || uIsLineFeed(offset) || uIsCarriageReturn(offset) || uIsNextLine(offset) || uIsLineSeparator(offset) || uIsParagraphSeparator(offset)))
    || (size > offset + 1 && (uIsBlank(offset) || uIsLineFeed(offset) || uIsCarriageReturn(offset) || uIsNextLine(offset)))
    || (size > offset && (uIsBlank(offset) || uIsLineFeed(offset) || uIsCarriageReturn(offset)))

internal inline fun UByteSource.isBOM(offset: Int = 0) =
  size > offset + 1 && uTest(UbFE, offset) && uTest(UbFF, offset + 1)

// endregion Safe Character Class Tests

// region Unsafe Character Class Tests

internal inline fun UByteSource.uIsBOM(offset: Int = 0) =
  uTest(UbFE, offset) && uTest(UbFF, offset + 1)

// endregion Unsafe Character Class Tests

// endregion Character Class Tests

// region Multi-Character Tests

internal inline fun UByteSource.isCRLF(offset: Int = 0) =
  size > offset + 1 && uIsCarriageReturn(offset) && uIsLineFeed(offset + 1)

internal inline fun UByteSource.uIsCRLF(offset: Int = 0) =
  uIsCarriageReturn(offset) && uIsLineFeed(offset + 1)

// endregion Multi-Character Tests

// region YAML Character Class Tests

// region Safe YAML Character Class Tests

/**
 * ```
 * [1] c-printable ::=
 *                          # 8 bit
 *     x09                  # Tab (\t)
 *   | x0A                  # Line feed (LF \n)
 *   | x0D                  # Carriage Return (CR \r)
 *   | [x20-x7E]            # Printable ASCII
 *                          # 16 bit
 *   | x85                  # Next Line (NEL)
 *   | [xA0-xD7FF]          # Basic Multilingual Plane (BMP)
 *   | [xE000-xFFFD]        # Additional Unicode Areas
 *                          # 32 bit
 *   | [x010000-x10FFFF]
 * ```
 */
internal inline fun UByteSource.isCPrintable(offset: Int = 0) =
  when {
    size > offset + 3 -> uIsPrintSafeASCII(offset) || uIsNextLine(offset) || uIsPrintSafe2ByteUTF8(offset) || uIsPrintSafe3ByteUTF8(offset) || uIsPrintSafe4ByteUTF8(offset)
    size > offset + 2 -> uIsPrintSafeASCII(offset) || uIsNextLine(offset) || uIsPrintSafe2ByteUTF8(offset) || uIsPrintSafe3ByteUTF8(offset)
    size > offset + 1 -> uIsPrintSafeASCII(offset) || uIsNextLine(offset) || uIsPrintSafe2ByteUTF8(offset)
    size > offset     -> uIsPrintSafeASCII(offset)
    else              -> false
  }

internal inline fun UByteSource.isNbChar(offset: Int = 0) =
  isCPrintable(offset) && !isAnyBreak(offset)

internal inline fun UByteSource.isNsChar(offset: Int = 0) =
  isNbChar(offset) && !isBlank(offset)

internal inline fun UByteSource.isNsTagChar(offset: Int = 0) =
  size > offset && uIsNsTagChar(offset)

internal inline fun UByteSource.isNsURIChar(offset: Int = 0) =
  size > offset && uIsNsURIChar(offset)

internal inline fun UByteSource.isNsAnchorChar(offset: Int = 0) =
  isNsChar(offset) && !uIsFlowIndicator(offset)

// endregion Safe YAML Character Class Tests

// region Unsafe YAML Character Class Tests

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
internal inline fun UByteSource.uIsNsAsciiLetter(offset: Int = 0): Boolean {
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
internal inline fun UByteSource.uIsNsTagChar(offset: Int = 0): Boolean {
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
internal inline fun UByteSource.uIsNsURIChar(offset: Int = 0): Boolean {
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
internal inline fun UByteSource.uIsNsWordChar(offset: Int = 0): Boolean {
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

// endregion Unsafe YAML Character Class Tests

// region YAML Character Class Test Support

internal inline fun UByteSource.uIsPrintSafeASCII(offset: Int = 0) =
  // In the non-control range (letters, numbers, visible symbols, and space)
  (get(offset) > Ub19 && get(offset) < Ub7F)
    // safe control characters
    || get(offset) == A_LINE_FEED
    || get(offset) == A_TAB
    || get(offset) == A_CARRIAGE_RETURN

// Characters in the unicode range `\u00A0 .. \u07FF`
//
// This encompasses all 2 byte combinations in the range `0xC2A0 .. 0xDFBF`
internal inline fun UByteSource.uIsPrintSafe2ByteUTF8(offset: Int = 0) =
  // 0xC2 + 0xA0 -> 0xDF + 0xBF
  (get(offset) == UbC2 && get(offset + 1) >= UbA0)
    || (get(offset) > UbC2 && get(offset) < UbDF)
    || (get(offset) == UbDF && get(offset + 1) <= UbBF)

/**
 * # (Unsafe) Have Print-safe 3 Byte UTF-8?
 *
 * Tests whether the next 3 bytes in the reader are in the range of printable
 * UTF-8 characters that are stored as 3 bytes.
 *
 * The ranges of codepoints this includes are:
 *
 * - `U+0800 .. U+D7FF
 * - `U+E000 .. U+FFFD
 *
 * In byte speak, this encompasses all 3 byte combinations in the ranges:
 *
 * - `0xE0A080 .. 0xED9FBF`
 * - `0xEE8080 .. 0xEFBFBD`
 */
internal inline fun UByteSource.uIsPrintSafe3ByteUTF8(offset: Int = 0) =
  when (val first = get(offset)) {
    // 0xE0_A0_80 -> 0xE0_FF_FF
    UbE0 -> get(offset + 1) > UbA0 || (get(offset + 1) == UbA0 && get(offset + 2) >= Ub80)
    // 0xED_00_00 -> 0xED_9F_BF
    UbED -> get(offset + 1) < Ub9F || (get(offset + 1) == Ub9F && get(offset + 2) <= UbBF)
    // 0xEF_00_00 -> 0xEF_BF_BD
    UbEF -> get(offset + 1) < UbBF || (get(offset + 1) == UbBF && get(offset + 2) <= UbBD)
    // 0xE1_00_00 -> 0xEC_FF_FF
    // 0xEE_00_00 -> 0xEE_FF_FF
    else -> (first > UbE0 && first < UbED) || first == UbEE
  }

// `U+10000 .. U+10FFFF`
// `0xF0908080 .. 0xF48FBFBF`
internal inline fun UByteSource.uIsPrintSafe4ByteUTF8(offset: Int = 0) =
  when (val first = get(offset)) {
    // The first byte is 0xF0
    UbF0 ->
      // If the second byte is greater than 0x90, then it has to be a valid
      // codepoint.
      get(offset + 1) > Ub90
        || (
        // If the second byte is equal to 0x90, then it will only be valid if
        // followed by a value that is greater than or equal to 0x8080.
        get(offset + 1) == Ub90
          && (
          // If the third byte is greater than 0x80, then it must be a valid
          // codepoint.
          get(offset + 2) > Ub80
            || (
            // If the third byte is greater equal to 0x80, then it will only be
            // valid if followed by a value that is greater than or equal to
            // 0x80
            get(offset + 2) == Ub80
              && get(offset + 3) >= Ub80
            )
          )
        )

    // The first byte is 0xF4
    UbF4 ->
      // If the second byte is less than 0x8F, then it has to be a valid
      // codepoint.
      get(offset + 1) < Ub8F
        || (
        // If the second byte is equal to 0x8F, then it will only be valid if
        // followed by a value that is less than or equal to 0xBFBF.
        get(offset + 1) == Ub8F
          && (
          // If the third byte is less than 0xBF, then it has to be a valid
          // codepoint.
          get(offset + 2) < UbBF
            || (
            // If the third byte is equal to 0xBF, then it will only be valid if
            // followed by a value that is less than or equal to 0xBF
            get(offset + 2) == UbBF
              && get(offset + 3) <= UbBF
            )
          )
        )

    else ->
      first > UbF0 && first < UbF4
  }

// endregion YAML Character Class Test Support

// endregion YAML Character Class Tests

// endregion Content Tests

// region Content Parsing

internal inline fun UByteSource.asDecimalDigit(offset: Int = 0) = get(offset) - A_DIGIT_0

internal inline fun UByteSource.asHexDigit(offset: Int = 0): UInt {
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