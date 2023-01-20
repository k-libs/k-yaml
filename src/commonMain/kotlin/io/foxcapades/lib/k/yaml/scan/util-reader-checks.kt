@file:Suppress("NOTHING_TO_INLINE")

package io.foxcapades.lib.k.yaml.scan

import io.foxcapades.lib.k.yaml.bytes.*


internal inline fun YAMLScannerImpl.bufferHasOffset(offset: Int) = reader.size > offset

// region Safe Octet Checking

/**
 * Tests the `UByte` at the given [offset] in the [YAMLScannerImpl.reader] against
 * the given [octet] value to see if they are equal.
 *
 * If reader buffer does not contain enough characters to contain [offset],
 * this method returns `false`.
 *
 * **Examples**
 * ```
 * // Given the following reader buffer:
 * // YAMLReader('A', 'B', 'C')
 *
 * // The following will return true
 * testReaderOctet('A')
 * testReaderOctet('B', 1)
 * testReaderOctet('C', 2)
 *
 * // And the following will return false
 * testReader('D', 3)  // false because the reader buffer does not contain
 *                     // offset 3 (would require 4 characters)
 * testReader('B')     // false because reader[0] != 'B'
 * ```
 *
 * @param octet Octet to compare the [YAMLScannerImpl.reader] value to.
 *
 * @param offset Offset in the reader buffer of the `UByte` value to test.
 *
 * @return `true` if the reader buffer contains enough characters to have a
 * value at [offset] and the value at `offset` is equal to the given [octet]
 * value.  `false` if the reader buffer does not contain enough characters to
 * contain `offset` or if the value at `offset` in the reader buffer is not
 * equal to the given `octet` value.
 */
private inline fun YAMLScannerImpl.testReaderOctet(octet: UByte, offset: Int = 0) =
  bufferHasOffset(offset) && unsafeTestReaderOctet(octet, offset)

/**
 * Tests the `UByte` values at offsets `0` and `1` in the reader buffer
 * against the given octet values to see if they are equal.
 */
private inline fun YAMLScannerImpl.testReaderOctets(octet1: UByte, octet2: UByte, offset: Int = 0) =
  bufferHasOffset(offset + 1)
    && unsafeTestReaderOctet(octet1, offset)
    && unsafeTestReaderOctet(octet2, offset + 1)

/**
 * Tests the `UByte` values at offsets `0`, `1`, and `2` in the reader buffer
 * against the given octet values to see if they are equal.
 */
internal inline fun YAMLScannerImpl.testReaderOctets(octet1: UByte, octet2: UByte, octet3: UByte, offset: Int = 0) =
  bufferHasOffset(offset + 2)
    && unsafeTestReaderOctet(octet1, offset)
    && unsafeTestReaderOctet(octet2, offset + 1)
    && unsafeTestReaderOctet(octet3, offset + 2)

/**
 * Tests the `UByte` values at offsets `0`, `1`, `2`, and `3` in the reader
 * buffer against the given octet values to see if they are equal.
 */
internal inline fun YAMLScannerImpl.testReaderOctets(octet1: UByte, octet2: UByte, octet3: UByte, octet4: UByte, offset: Int = 0) =
  bufferHasOffset(offset + 3)
    && unsafeTestReaderOctet(octet1, offset)
    && unsafeTestReaderOctet(octet2, offset + 1)
    && unsafeTestReaderOctet(octet3, offset + 2)
    && unsafeTestReaderOctet(octet4, offset + 3)

// endregion Safe Octet Checking

// region Unsafe Octet Checking

internal inline fun YAMLScannerImpl.unsafeTestReaderOctet(octet: UByte, offset: Int = 0) =
  reader[offset] == octet

internal inline fun YAMLScannerImpl.unsafeTestReaderOctets(octet1: UByte, octet2: UByte, offset: Int = 0) =
  reader[offset] == octet1
    && reader[offset + 1] == octet2

internal inline fun YAMLScannerImpl.unsafeTestReaderOctets(octet1: UByte, octet2: UByte, octet3: UByte, offset: Int = 0) =
  reader[offset] == octet1
    && reader[offset + 1] == octet2
    && reader[offset + 2] == octet3

// endregion Unsafe Octet Checking

// region Safe Tests

internal inline fun YAMLScannerImpl.haveAmp        (offset: Int = 0) = testReaderOctet(A_AMPERSAND, offset)
internal inline fun YAMLScannerImpl.haveAsterisk   (offset: Int = 0) = testReaderOctet(A_ASTERISK, offset)
internal inline fun YAMLScannerImpl.haveBackslash  (offset: Int = 0) = testReaderOctet(A_BACKSLASH, offset)
internal inline fun YAMLScannerImpl.haveColon      (offset: Int = 0) = testReaderOctet(A_COLON, offset)
internal inline fun YAMLScannerImpl.haveComma      (offset: Int = 0) = testReaderOctet(A_COMMA, offset)
internal inline fun YAMLScannerImpl.haveCR         (offset: Int = 0) = testReaderOctet(A_CARRIAGE_RETURN, offset)
internal inline fun YAMLScannerImpl.haveCurlyClose (offset: Int = 0) = testReaderOctet(A_CURLY_BRACKET_CLOSE, offset)
internal inline fun YAMLScannerImpl.haveCurlyOpen  (offset: Int = 0) = testReaderOctet(A_CURLY_BRACKET_OPEN, offset)
internal inline fun YAMLScannerImpl.haveDash       (offset: Int = 0) = testReaderOctet(A_DASH, offset)
internal inline fun YAMLScannerImpl.haveExclaim    (offset: Int = 0) = testReaderOctet(A_EXCLAIM, offset)
internal inline fun YAMLScannerImpl.haveLF         (offset: Int = 0) = testReaderOctet(A_LINE_FEED, offset)
internal inline fun YAMLScannerImpl.haveLS         (offset: Int = 0) = testReaderOctets(UbE2, Ub80, UbA8, offset)
internal inline fun YAMLScannerImpl.haveNEL        (offset: Int = 0) = testReaderOctets(UbC2, Ub85, offset)
internal inline fun YAMLScannerImpl.havePercent    (offset: Int = 0) = testReaderOctet(A_PERCENT, offset)
internal inline fun YAMLScannerImpl.havePeriod     (offset: Int = 0) = testReaderOctet(A_PERIOD, offset)
internal inline fun YAMLScannerImpl.havePound      (offset: Int = 0) = testReaderOctet(A_POUND, offset)
internal inline fun YAMLScannerImpl.havePS         (offset: Int = 0) = testReaderOctets(UbE2, Ub80, UbA9, offset)
internal inline fun YAMLScannerImpl.haveQuestion   (offset: Int = 0) = testReaderOctet(A_QUESTION, offset)
internal inline fun YAMLScannerImpl.haveSpace      (offset: Int = 0) = testReaderOctet(A_SPACE, offset)
internal inline fun YAMLScannerImpl.haveSquareClose(offset: Int = 0) = testReaderOctet(A_SQUARE_BRACKET_CLOSE, offset)
internal inline fun YAMLScannerImpl.haveSquareOpen (offset: Int = 0) = testReaderOctet(A_SQUARE_BRACKET_OPEN, offset)
internal inline fun YAMLScannerImpl.haveTab        (offset: Int = 0) = testReaderOctet(A_TAB, offset)

internal inline fun YAMLScannerImpl.haveDecimalDigit(offset: Int = 0) =
  bufferHasOffset(offset)
    && unsafeHaveDecimalDigit(offset)

internal inline fun YAMLScannerImpl.haveHexDigit(offset: Int = 0) =
  bufferHasOffset(offset)
    && unsafeHaveHexDigit(offset)

internal inline fun YAMLScannerImpl.haveBlank(offset: Int = 0) =
  bufferHasOffset(offset)
    && unsafeHaveBlank(offset)

internal inline fun YAMLScannerImpl.haveCROrLF(offset: Int = 0) =
  bufferHasOffset(offset)
    && unsafeHaveCROrLF(offset)

internal inline fun YAMLScannerImpl.haveCRLF(offset: Int) =
  bufferHasOffset(offset + 1)
    && unsafeHaveCR(offset)
    && unsafeHaveLineFeed(offset + 1)

internal inline fun YAMLScannerImpl.haveAnyBreak(offset: Int = 0) =
  when {
    bufferHasOffset(offset + 2) -> unsafeHaveCROrLF(offset) || unsafeHaveNextLine(offset) || unsafeHaveLSOrPS(offset)
    bufferHasOffset(offset + 1) -> unsafeHaveCROrLF(offset) || unsafeHaveNextLine(offset)
    bufferHasOffset(offset)     -> unsafeHaveCROrLF(offset)
    else                        -> false
  }

internal inline fun YAMLScannerImpl.haveEOF(offset: Int = 0) =
  !bufferHasOffset(offset) && reader.atEOF

internal inline fun YAMLScannerImpl.haveAnyBreakOrEOF(offset: Int = 0) =
  when {
    bufferHasOffset(offset + 2) -> unsafeHaveCROrLF(offset) || unsafeHaveNextLine(offset) || unsafeHaveLSOrPS(offset)
    bufferHasOffset(offset + 1) -> unsafeHaveCROrLF(offset) || unsafeHaveNextLine(offset)
    bufferHasOffset(offset)     -> unsafeHaveCROrLF(offset)
    else                        -> reader.atEOF
  }

internal inline fun YAMLScannerImpl.haveBlankOrAnyBreak(offset: Int = 0) =
  when {
    bufferHasOffset(offset + 2) -> unsafeHaveBlank(offset) || unsafeHaveCROrLF(offset) || unsafeHaveNextLine(offset) || unsafeHaveLSOrPS(offset)
    bufferHasOffset(offset + 1) -> unsafeHaveBlank(offset) || unsafeHaveCROrLF(offset) || unsafeHaveNextLine(offset)
    bufferHasOffset(offset)     -> unsafeHaveBlank(offset) || unsafeHaveCROrLF(offset)
    else                        -> false
  }

internal inline fun YAMLScannerImpl.haveBlankAnyBreakOrEOF(offset: Int = 0) =
  when {
    bufferHasOffset(offset + 2) -> unsafeHaveBlank(offset) || unsafeHaveCROrLF(offset) || unsafeHaveNextLine(offset) || unsafeHaveLSOrPS(offset)
    bufferHasOffset(offset + 1) -> unsafeHaveBlank(offset) || unsafeHaveCROrLF(offset) || unsafeHaveNextLine(offset)
    bufferHasOffset(offset)     -> unsafeHaveBlank(offset) || unsafeHaveCROrLF(offset)
    else                        -> reader.atEOF
  }

// region YAML Character Classes

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
internal inline fun YAMLScannerImpl.haveCPrintable(offset: Int = 0) =
  when {
    // If we could possibly test up to 4 bytes for our next character
    bufferHasOffset(offset + 3) ->
      unsafeHavePrintSafeASCII(offset)
        || unsafeHaveNextLine(offset)
        || unsafeHavePrintSafe2ByteUTF8(offset)
        || unsafeHavePrintSafe3ByteUTF8(offset)
        || unsafeHavePrintSafe4ByteUTF8(offset)

    // If we could possibly test up to 3 bytes for our next character
    bufferHasOffset(offset + 2) ->
      unsafeHavePrintSafeASCII(offset)
        || unsafeHaveNextLine(offset)
        || unsafeHavePrintSafe2ByteUTF8(offset)
        || unsafeHavePrintSafe3ByteUTF8(offset)

    // If we could possibly test up to 2 bytes for our next character
    bufferHasOffset(offset + 1) ->
      unsafeHavePrintSafeASCII(offset)
        || unsafeHaveNextLine(offset)
        || unsafeHavePrintSafe2ByteUTF8(offset)

    bufferHasOffset(offset) ->
      unsafeHavePrintSafeASCII(offset)

    else ->
      false
  }

// endregion YAML Character Classes

// endregion Safe Tests

// region Unsafe Tests
// =====================================================================================================================
//
// The following are reader buffer content tests that are "unsafe" in that
// they do not verify the length of the buffer is great enough to contain the
// target offset before attempting to access that offset in the buffer.
//
// This means that the caller must perform the buffer size test ahead of the
// call to the octet test function to ensure that we don't get an error for
// attempting to access an out-of-bounds index.
//
// For example, if the reader buffer is currently empty (`reader.size == 0`),
// and we call `unsafeHaveAmp(2)`, an IndexOutOfBounds exception will be
// thrown for attempting to access index `2` in a buffer of size `0`.

// region Single Character Tests
// =============================================================================
//
// The following collection of unsafe buffer content tests are to test for the
// existence of a single UTF-8 character in the buffer.

/**
 * Tests if the character at the given offset in the reader buffer is an ASCII
 * Ampersand character (`&`).
 *
 * @param offset Offset in the reader buffer to test.
 *
 * Default = `0`
 *
 * @return `true` if the character at the given `offset` in the reader buffer
 * is an ASCII Ampersand character.  `false` if the character at the given
 * `offset` in the reader buffer is _not_ an ASCII Ampersand character.
 *
 * @throws IndexOutOfBoundsException If the reader buffer size is less than or
 * equal to the given [offset] value.
 */
internal inline fun YAMLScannerImpl.unsafeHaveAmp(offset: Int = 0) = unsafeTestReaderOctet(A_AMPERSAND, offset)

/**
 * Tests if the character at the given offset in the reader buffer is an ASCII
 * Apostrophe character (`'`).
 *
 * @param offset Offset in the reader buffer to test.
 *
 * Default = `0`
 *
 * @return `true` if the character at the given `offset` in the reader buffer
 * is an ASCII Apostrophe character.  `false` if the character at the given
 * `offset` in the reader buffer is _not_ an ASCII Ampersand character.
 *
 * @throws IndexOutOfBoundsException If the reader buffer size is less than or
 * equal to the given [offset] value.
 */
internal inline fun YAMLScannerImpl.unsafeHaveApostrophe(offset: Int = 0) = unsafeTestReaderOctet(A_APOSTROPHE, offset)

/**
 * Tests if the character at the given offset in the reader buffer is an ASCII
 * Asterisk character (`*`).
 *
 * @param offset Offset in the reader buffer to test.
 *
 * Default = `0`
 *
 * @return `true` if the character at the given `offset` in the reader buffer
 * is an ASCII Asterisk character.  `false` if the character at the given
 * `offset` in the reader buffer is _not_ an ASCII Ampersand character.
 *
 * @throws IndexOutOfBoundsException If the reader buffer size is less than or
 * equal to the given [offset] value.
 */
internal inline fun YAMLScannerImpl.unsafeHaveAsterisk(offset: Int = 0) = unsafeTestReaderOctet(A_ASTERISK, offset)
internal inline fun YAMLScannerImpl.unsafeHaveAt(offset: Int = 0) = unsafeTestReaderOctet(A_AT, offset)
internal inline fun YAMLScannerImpl.unsafeHaveColon      (offset: Int = 0) = unsafeTestReaderOctet(A_COLON, offset)
internal inline fun YAMLScannerImpl.unsafeHaveComma      (offset: Int = 0) = unsafeTestReaderOctet(A_COMMA, offset)
internal inline fun YAMLScannerImpl.unsafeHaveCR         (offset: Int = 0) = unsafeTestReaderOctet(A_CARRIAGE_RETURN, offset)
internal inline fun YAMLScannerImpl.unsafeHaveCurlyClose (offset: Int = 0) = unsafeTestReaderOctet(A_CURLY_BRACKET_CLOSE, offset)
internal inline fun YAMLScannerImpl.unsafeHaveCurlyOpen  (offset: Int = 0) = unsafeTestReaderOctet(A_CURLY_BRACKET_OPEN, offset)
internal inline fun YAMLScannerImpl.unsafeHaveDash       (offset: Int = 0) = unsafeTestReaderOctet(A_DASH, offset)
internal inline fun YAMLScannerImpl.unsafeHaveDoubleQuote(offset: Int = 0) = unsafeTestReaderOctet(A_DOUBLE_QUOTE, offset)
internal inline fun YAMLScannerImpl.unsafeHaveGrave(offset: Int = 0) = unsafeTestReaderOctet(A_GRAVE, offset)
internal inline fun YAMLScannerImpl.unsafeHaveGreaterThan(offset: Int = 0) = unsafeTestReaderOctet(A_GREATER_THAN, offset)
internal inline fun YAMLScannerImpl.unsafeHaveLineFeed   (offset: Int = 0) = unsafeTestReaderOctet(A_LINE_FEED, offset)

/**
 * Tests if the character at the given [offset] in the reader buffer is a UTF
 * Line Separator character (`<LS>` a.k.a. `\u2028`).
 *
 * @param offset Offset in the reader buffer to test.
 *
 * Default = `0`
 *
 * @return `true` if the character at the given `offset` in the reader buffer
 * is a Line Separator character.  `false` if the character at the given
 * `offset` in the reader buffer is _not_ a Line Separator character.
 *
 * @throws IndexOutOfBoundsException If the reader buffer size is less than or
 * equal to `offset + 2`.
 */
internal inline fun YAMLScannerImpl.unsafeHaveLineSeparator(offset: Int = 0) = unsafeTestReaderOctets(UbE2, Ub80, UbA8, offset)

/**
 * Tests if the character at the given [offset] in the reader buffer is a UTF
 * Next Line indicator character (`<NEL>` a.k.a. `\u0085`).
 *
 * @param offset Offset in the reader buffer to test.
 *
 * Default = `0`
 *
 * @return `true` if the character at the given `offset` in the reader buffer
 * is a Next Line character.  `false` if the character at the given `offset`
 * in the reader buffer is _not_ a Next Line character.
 *
 * @throws IndexOutOfBoundsException If the reader buffer size is less than or
 * equal to `offset + 1`.
 */
internal inline fun YAMLScannerImpl.unsafeHaveNextLine(offset: Int = 0) = unsafeTestReaderOctets(UbC2, Ub85, offset)

/**
 * Tests if the character at the given offset in the reader buffer is an ASCII
 * percent sign character (`%`).
 *
 * @param offset Offset in the reader buffer to test.
 *
 * Default = `0`
 *
 * @return `true` if the character at the given `offset` in the reader buffer
 * is an ASCII percent sign character.  `false` if the character at the given
 * `offset` in the reader buffer is _not_ an ASCII percent sign character.
 *
 * @throws IndexOutOfBoundsException If the reader buffer size is less than or
 * equal to the given [offset] value.
 */
internal inline fun YAMLScannerImpl.unsafeHavePercent(offset: Int = 0) = unsafeTestReaderOctet(A_PERCENT, offset)

internal inline fun YAMLScannerImpl.unsafeHavePeriod     (offset: Int = 0) = unsafeTestReaderOctet(A_PERIOD, offset)
internal inline fun YAMLScannerImpl.unsafeHavePipe       (offset: Int = 0) = unsafeTestReaderOctet(A_PIPE, offset)
internal inline fun YAMLScannerImpl.unsafeHavePlus       (offset: Int = 0) = unsafeTestReaderOctet(A_PLUS, offset)
internal inline fun YAMLScannerImpl.unsafeHavePound      (offset: Int = 0) = unsafeTestReaderOctet(A_POUND, offset)
internal inline fun YAMLScannerImpl.unsafeHaveParagraphSeparator(offset: Int = 0) = unsafeTestReaderOctets(UbE2, Ub80, UbA9, offset)
internal inline fun YAMLScannerImpl.unsafeHaveQuestion(offset: Int = 0) = unsafeTestReaderOctet(A_QUESTION, offset)
internal inline fun YAMLScannerImpl.unsafeHaveSpace      (offset: Int = 0) = unsafeTestReaderOctet(A_SPACE, offset)
internal inline fun YAMLScannerImpl.unsafeHaveSquareClose(offset: Int = 0) = unsafeTestReaderOctet(A_SQUARE_BRACKET_CLOSE, offset)
internal inline fun YAMLScannerImpl.unsafeHaveSquareOpen (offset: Int = 0) = unsafeTestReaderOctet(A_SQUARE_BRACKET_OPEN, offset)
internal inline fun YAMLScannerImpl.unsafeHaveTab        (offset: Int = 0) = unsafeTestReaderOctet(A_TAB, offset)
internal inline fun YAMLScannerImpl.unsafeHaveTilde(offset: Int = 0) = unsafeTestReaderOctet(A_TILDE, offset)

// =============================================================================
// endregion Single Character Tests

// region Numeric Character Range Tests
// =============================================================================
//
// The following collection of unsafe buffer content tests are to test that
// the character at the given offset falls inside a valid range of characters
// to be a numeric digit representation.

internal inline fun YAMLScannerImpl.unsafeHaveDecimalDigit(offset: Int = 0) =
  reader[offset] >= A_DIGIT_0 && reader[offset] <= A_9

internal inline fun YAMLScannerImpl.unsafeHaveHexDigit(offset: Int = 0) =
  unsafeHaveDecimalDigit(offset)
    || (reader[offset] >= A_UPPER_A && reader[offset] <= A_UP_F)
    || (reader[offset] >= A_LOWER_A && reader[offset] <= A_LO_F)

// =============================================================================
// endregion Numeric Character Range Tests

/**
 * Unsafe test to check if the buffer contains either a `<SPACE>` or a `<TAB>`
 * character at the given offset.
 */
internal inline fun YAMLScannerImpl.unsafeHaveBlank(offset: Int = 0) =
  unsafeHaveSpace(offset)
    || unsafeHaveTab(offset)

internal inline fun YAMLScannerImpl.unsafeHaveCROrLF(offset: Int = 0) =
  unsafeHaveLineFeed(offset)
    || unsafeHaveCR(offset)

internal inline fun YAMLScannerImpl.unsafeHaveLSOrPS(offset: Int = 0) =
  unsafeTestReaderOctet(UbE2, offset)
    && unsafeTestReaderOctet(Ub80, offset + 1)
    && (
    unsafeTestReaderOctet(UbA8, offset + 2)
      || unsafeTestReaderOctet(UbA9, offset + 2)
    )

internal inline fun YAMLScannerImpl.unsafeHavePrintSafeASCII(offset: Int = 0) =
  // In the non-control range (letters, numbers, visible symbols, and space)
  (reader[offset] > Ub19 && reader[offset] < Ub7F)
    // safe control characters
    || reader[offset] == A_LINE_FEED
    || reader[offset] == A_TAB
    || reader[offset] == A_CARRIAGE_RETURN

// Characters in the unicode range `\u00A0 .. \u07FF`
//
// This encompasses all 2 byte combinations in the range `0xC2A0 .. 0xDFBF`
internal inline fun YAMLScannerImpl.unsafeHavePrintSafe2ByteUTF8(offset: Int = 0) =
  // 0xC2 + 0xA0 -> 0xDF + 0xBF
  (reader[offset] == UbC2 && reader[offset + 1] >= UbA0)
    || (reader[offset] > UbC2 && reader[offset] < UbDF)
    || (reader[offset] == UbDF && reader[offset + 1] <= UbBF)

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
internal inline fun YAMLScannerImpl.unsafeHavePrintSafe3ByteUTF8(offset: Int = 0) =
  when (val first = reader[offset]) {
    // 0xE0_A0_80 -> 0xE0_FF_FF
    UbE0 -> reader[offset + 1] > UbA0 || (reader[offset + 1] == UbA0 && reader[offset + 2] >= Ub80)
    // 0xED_00_00 -> 0xED_9F_BF
    UbED -> reader[offset + 1] < Ub9F || (reader[offset + 1] == Ub9F && reader[offset + 2] <= UbBF)
    // 0xEF_00_00 -> 0xEF_BF_BD
    UbEF -> reader[offset + 1] < UbBF || (reader[offset + 1] == UbBF && reader[offset + 2] <= UbBD)
    // 0xE1_00_00 -> 0xEC_FF_FF
    // 0xEE_00_00 -> 0xEE_FF_FF
    else -> (first > UbE0 && first < UbED) || first == UbEE
  }

// `U+10000 .. U+10FFFF`
// `0xF0908080 .. 0xF48FBFBF`
internal inline fun YAMLScannerImpl.unsafeHavePrintSafe4ByteUTF8(offset: Int = 0) =
  when (val first = reader[offset]) {
    // The first byte is 0xF0
    UbF0 ->
      // If the second byte is greater than 0x90, then it has to be a valid
      // codepoint.
      reader[offset + 1] > Ub90
      || (
        // If the second byte is equal to 0x90, then it will only be valid if
        // followed by a value that is greater than or equal to 0x8080.
        reader[offset + 1] == Ub90
        && (
          // If the third byte is greater than 0x80, then it must be a valid
          // codepoint.
          reader[offset + 2] > Ub80
          || (
            // If the third byte is greater equal to 0x80, then it will only be
            // valid if followed by a value that is greater than or equal to
            // 0x80
            reader[offset + 2] == Ub80
            && reader[offset + 3] >= Ub80
          )
        )
      )

    // The first byte is 0xF4
    UbF4 ->
      // If the second byte is less than 0x8F, then it has to be a valid
      // codepoint.
      reader[offset + 1] < Ub8F
      || (
        // If the second byte is equal to 0x8F, then it will only be valid if
        // followed by a value that is less than or equal to 0xBFBF.
        reader[offset + 1] == Ub8F
        && (
          // If the third byte is less than 0xBF, then it has to be a valid
          // codepoint.
          reader[offset + 2] < UbBF
          || (
            // If the third byte is equal to 0xBF, then it will only be valid if
            // followed by a value that is less than or equal to 0xBF
            reader[offset + 2] == UbBF
              && reader[offset + 3] <= UbBF
          )
        )
      )

    else ->
      first > UbF0 && first < UbF4
  }



// ===================================================================================================================
// endregion Unsafe Tests
