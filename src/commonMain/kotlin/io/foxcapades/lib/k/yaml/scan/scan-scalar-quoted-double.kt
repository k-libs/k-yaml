package io.foxcapades.lib.k.yaml.scan

import io.foxcapades.lib.k.yaml.bytes.*
import io.foxcapades.lib.k.yaml.token.YAMLToken
import io.foxcapades.lib.k.yaml.token.YAMLTokenDataScalar
import io.foxcapades.lib.k.yaml.token.YAMLTokenType
import io.foxcapades.lib.k.yaml.util.*


@OptIn(ExperimentalUnsignedTypes::class)
internal fun YAMLScannerImpl.fetchDoubleQuotedStringToken() {
  contentBuffer1.clear()
  trailingWSBuffer.clear()
  trailingNLBuffer.clear()

  val start = position.mark()

  // Skip the first double quote character as we don't put it into the token
  // value.
  skipASCII()

  while (true) {
    reader.cache(1)

    if (reader.isBackslash()) {
      readPossibleEscapeSequence(contentBuffer1, trailingNLBuffer)
    }

    else if (reader.isDoubleQuote()) {
      skipASCII()
      collapseTrailingWhitespaceAndNewlinesIntoBuffer(contentBuffer1, trailingNLBuffer, trailingWSBuffer)
      tokens.push(newDoubleQuotedStringToken(contentBuffer1.popToArray(), start, position.mark()))
      return
    }

    else if (reader.isBlank()) {
      trailingWSBuffer.claimASCII()
    }

    else if (reader.isAnyBreak()) {
      trailingWSBuffer.clear()
      trailingNLBuffer.claimNewLine()
    }

    else if (reader.isEOF()) {
      val end = position.mark()
      warn("unexpected end of double quoted string due to EOF", start, end)
      tokens.push(newInvalidToken(start, end))
      return
    }

    else {
      collapseTrailingWhitespaceAndNewlinesIntoBuffer(contentBuffer1, trailingNLBuffer, trailingWSBuffer)
      contentBuffer1.claimUTF8()
    }
  }
}

private fun YAMLScannerImpl.collapseTrailingWhitespaceAndNewlinesIntoBuffer(
  target:   UByteBuffer,
  newlines: UByteBuffer,
  blanks:   UByteBuffer,
) {
  if (newlines.isNotEmpty) {
    if (newlines.size == 1) {
      target.push(A_SPACE)
      newlines.clear()
    } else {
      newlines.pop()
      while (newlines.isNotEmpty)
        target.claimNewLine(newlines)
    }
  } else if (blanks.isNotEmpty) {
    while (blanks.isNotEmpty)
      target.push(blanks.pop())
  }
}


private fun YAMLScannerImpl.readPossibleEscapeSequence(into: UByteBuffer, nlBuffer: UByteBuffer) {
  val start = position.mark()

  // Skip the backslash character
  skipASCII()

  // Try to ensure we have another character in the buffer
  reader.cache(1)

  if (reader.isEOF()) {
    into.push(A_BACKSLASH)
    warn("invalid or unfinished escape sequence due to EOF", start, position.mark())
  }

  else if (reader.isAnyBreak()) {
    nlBuffer.claimNewLine()
  }

  else if (
    reader.uIsSpace()
    || reader.uIsDoubleQuote()
    || reader.uIsBackslash()
    || reader.uIsSlash()
    || reader.uIsTab()
  ) {
    into.claimASCII()
  }

  // \xXX
  else if (reader.uTest(A_LOWER_X)) {
    readHexEscape(start, into)
  }

  // \uXXXX
  else if (reader.uTest(A_LOWER_U)) {
    readSmallUnicodeEscape(start, into)
  }

  // \UXXXXXXXX
  else if (reader.uTest(A_UPPER_U)) {
    readBigUnicodeEscape(start, into)
  }

  // Null / Nil
  else if (reader.uTest(A_DIGIT_0)) {
    into.push(A_NIL)
    skipASCII()
  }

  // Bell
  else if (reader.uTest(A_LOWER_A)) {
    into.push(A_BELL)
    skipASCII()
  }

  // Backspace
  else if (reader.uTest(A_LOWER_B)) {
    into.push(A_BACKSPACE)
    skipASCII()
  }

  // Tab
  else if (reader.uTest(A_LOWER_T)) {
    into.push(A_TAB)
    skipASCII()
  }

  // Line Feed
  else if (reader.uTest(A_LOWER_N)) {
    into.push(A_LINE_FEED)
    skipASCII()
  }

  // Vertical Tab
  else if (reader.uTest(A_LOWER_V)) {
    into.push(A_VERTICAL_TAB)
    skipASCII()
  }

  // Form Feed
  else if (reader.uTest(A_LOWER_F)) {
    into.push(A_FORM_FEED)
    skipASCII()
  }

  // Carriage Return
  else if (reader.uTest(A_LOWER_R)) {
    into.push(A_CARRIAGE_RETURN)
    skipASCII()
  }

  // Escape
  else if (reader.uTest(A_LOWER_E)) {
    into.push(A_ESCAPE)
    skipASCII()
  }

  // Next Line
  else if (reader.uTest(A_UPPER_N)) {
    into.push(UbC2)
    into.push(Ub85)
    skipASCII()
  }

  // Non-Breaking Space
  else if (reader.uTest(A_UNDERSCORE)) {
    into.push(UbC2)
    into.push(UbA0)
    skipASCII()
  }

  // Line Separator
  else if (reader.uTest(A_UPPER_L)) {
    into.push(UbE2)
    into.push(Ub80)
    into.push(UbA8)
    skipASCII()
  }

  // Paragraph Separator
  else if (reader.uTest(A_UPPER_P)) {
    into.push(UbE2)
    into.push(Ub80)
    into.push(UbA9)
    skipASCII()
  }

  // Junk
  else {
    into.push(A_BACKSLASH)
    into.claimUTF8()
    warn("unrecognized or invalid escape sequence", start, position.mark())
  }
}

@OptIn(ExperimentalUnsignedTypes::class)
private fun YAMLScannerImpl.newDoubleQuotedStringToken(value: UByteArray, start: SourcePosition, end: SourcePosition) =
  YAMLToken(YAMLTokenType.Scalar, YAMLTokenDataScalar(value, YAMLScalarStyle.DoubleQuoted), start, end, getWarnings())

private fun YAMLScannerImpl.readHexEscape(start: SourcePosition, into: UByteBuffer) {
  skipASCII()
  reader.cache(2)

  if (reader.isHexDigit() && reader.isHexDigit(1)) {
    val value = ((reader.asHexDigit() shl 4) + reader.asHexDigit(1)).toUByte()
    into.push(value)
    skipASCII(2)
  } else {
    into.push(A_BACKSLASH)
    into.push(A_LOWER_X)

    var i = 0
    while (i++ < 2) {
      reader.cache(1)
      if (reader.isBlankAnyBreakOrEOF()) {
        warn("incomplete hex escape sequence", start, position.mark())
        return
      } else {
        into.claimUTF8()
      }
    }

    warn("invalid hex escape sequence", start, position.mark())
  }
}

private fun YAMLScannerImpl.readSmallUnicodeEscape(start: SourcePosition, into: UByteBuffer) {
  skipASCII()
  reader.cache(4)

  // If one or more of the next 4 bytes are NOT hex digits
  if (!(
      reader.isHexDigit(0)
        || reader.isHexDigit(1)
        || reader.isHexDigit(2)
        || reader.isHexDigit(3)
      )) {
    // iterate through the next 4 CHARACTERS and call those the invalid escape
    // sequence.  If we hit a blank, line break, or the EOF in the middle,
    // then only claim up to that point as part of the warning / token
    var i = 0
    while (i++ < 4) {
      reader.cache(1)
      if (reader.isBlankAnyBreakOrEOF()) {
        warn("incomplete unicode escape sequence", start, position.mark())
        return
      } else {
        into.claimUTF8()
      }
    }

    warn("invalid unicode escape sequence", start, position.mark())
    return
  }

  // If we're here then we have 4 hex digits to read from the buffer to form
  // our character which we will need to convert to UTF-8 to append to the
  // [into] buffer.
  val value =
    (reader.asHexDigit(0) shl 12) +
      (reader.asHexDigit(1) shl 8) +
      (reader.asHexDigit(2) shl 4) +
      (reader.asHexDigit(3))

  value.toUTF8(into)
  skipASCII(4)
}

private fun YAMLScannerImpl.readBigUnicodeEscape(start: SourcePosition, into: UByteBuffer) {
  skipASCII()
  reader.cache(8)

  // If one or more of the next 4 bytes are NOT hex digits
  if (!(
      reader.isHexDigit(0)
        || reader.isHexDigit(1)
        || reader.isHexDigit(2)
        || reader.isHexDigit(3)
        || reader.isHexDigit(4)
        || reader.isHexDigit(5)
        || reader.isHexDigit(6)
        || reader.isHexDigit(7)
      )) {
    // iterate through the next 4 CHARACTERS and call those the invalid escape
    // sequence.  If we hit a blank, line break, or the EOF in the middle,
    // then only claim up to that point as part of the warning / token
    var i = 0
    while (i++ < 8) {
      reader.cache(1)
      if (reader.isBlankAnyBreakOrEOF()) {
        warn("incomplete unicode escape sequence", start, position.mark())
        return
      } else {
        into.claimUTF8()
      }
    }

    warn("invalid unicode escape sequence", start, position.mark())
    return
  }

  // If we're here then we have 4 hex digits to read from the buffer to form
  // our character which we will need to convert to UTF-8 to append to the
  // [into] buffer.
  val value =
    (reader.asHexDigit(0) shl 28) +
      (reader.asHexDigit(1) shl 24) +
      (reader.asHexDigit(2) shl 20) +
      (reader.asHexDigit(3) shl 16) +
      (reader.asHexDigit(4) shl 12) +
      (reader.asHexDigit(5) shl 8) +
      (reader.asHexDigit(6) shl 4) +
      (reader.asHexDigit(7))

  value.toUTF8(into)
  skipASCII(8)
}
