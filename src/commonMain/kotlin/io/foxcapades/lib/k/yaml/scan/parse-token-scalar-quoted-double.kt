package io.foxcapades.lib.k.yaml.scan

import io.foxcapades.lib.k.yaml.bytes.*
import io.foxcapades.lib.k.yaml.token.YAMLTokenScalarQuotedDouble
import io.foxcapades.lib.k.yaml.util.*


@OptIn(ExperimentalUnsignedTypes::class)
internal fun YAMLStreamTokenizerImpl.parseDoubleQuotedStringToken() {
  contentBuffer1.clear()
  trailingWSBuffer.clear()
  trailingNLBuffer.clear()

  val indent = this.indent
  val start = position.mark()

  // Skip the first double quote character as we don't put it into the token
  // value.
  skipASCII(this.reader, this.position)

  while (true) {
    reader.cache(1)

    if (reader.isBackslash()) {
      lineContentIndicator = LineContentIndicatorContent
      readPossibleEscapeSequence(contentBuffer1, trailingNLBuffer, trailingWSBuffer)
    }

    else if (reader.isDoubleQuote()) {
      lineContentIndicator = LineContentIndicatorContent
      skipASCII(this.reader, this.position)
      collapseTrailingWhitespaceAndNewlinesIntoBuffer(contentBuffer1, trailingNLBuffer, trailingWSBuffer)
      tokens.push(newDoubleQuotedStringToken(contentBuffer1.popToArray(), indent, start, position.mark()))
      return
    }

    else if (reader.isBlank()) {
      if (lineContentIndicator.haveAnyContent) {
        trailingWSBuffer.claimASCII(this.reader, this.position)
      } else {
        skipASCII(this.reader, this.position)
        this.indent++
      }
    }

    else if (reader.isAnyBreak()) {
      trailingWSBuffer.clear()
      trailingNLBuffer.claimNewLine(this.reader, this.position)
      lineContentIndicator = LineContentIndicatorBlanksOnly
      this.indent = 0u
    }

    else if (reader.isEOF()) {
      emitInvalidToken("unexpected end of double quoted string due to EOF", start)
      return
    }

    else {
      lineContentIndicator = LineContentIndicatorContent
      collapseTrailingWhitespaceAndNewlinesIntoBuffer(contentBuffer1, trailingNLBuffer, trailingWSBuffer)
      contentBuffer1.claimUTF8(this.reader, this.position)
    }
  }
}

private fun collapseTrailingWhitespaceAndNewlinesIntoBuffer(
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

  newlines.clear()
  blanks.clear()
}


private fun YAMLStreamTokenizerImpl.readPossibleEscapeSequence(
  into:     UByteBuffer,
  nlBuffer: UByteBuffer,
  wsBuffer: UByteBuffer,
) {
  val start = position.mark()

  // Skip the backslash character
  skipASCII(this.reader, this.position)

  // Try to ensure we have another character in the buffer
  reader.cache(1)

  if (reader.isEOF()) {
    collapseTrailingWhitespaceAndNewlinesIntoBuffer(into, nlBuffer, wsBuffer)
    into.push(A_BACKSLASH)
    warn("invalid or unfinished escape sequence due to EOF", start, position.mark())
  }

  else if (reader.isAnyBreak()) {
    nlBuffer.claimNewLine(this.reader, this.position)
  }

  else if (
    reader.uIsSpace()
    || reader.uIsDoubleQuote()
    || reader.uIsBackslash()
    || reader.uIsSlash()
    || reader.uIsTab()
  ) {
    collapseTrailingWhitespaceAndNewlinesIntoBuffer(into, nlBuffer, wsBuffer)
    into.claimASCII(this.reader, this.position)
  }

  // \xXX
  else if (reader.uTest(A_LOWER_X)) {
    collapseTrailingWhitespaceAndNewlinesIntoBuffer(into, nlBuffer, wsBuffer)
    readHexEscape(start, into)
  }

  // \uXXXX
  else if (reader.uTest(A_LOWER_U)) {
    collapseTrailingWhitespaceAndNewlinesIntoBuffer(into, nlBuffer, wsBuffer)
    readSmallUnicodeEscape(start, into)
  }

  // \UXXXXXXXX
  else if (reader.uTest(A_UPPER_U)) {
    collapseTrailingWhitespaceAndNewlinesIntoBuffer(into, nlBuffer, wsBuffer)
    readBigUnicodeEscape(start, into)
  }

  // Line Feed
  else if (reader.uTest(A_LOWER_N)) {
    collapseTrailingWhitespaceAndNewlinesIntoBuffer(into, nlBuffer, wsBuffer)
    into.push(A_LINE_FEED)
    skipASCII(this.reader, this.position)
  }

  // Null / Nil
  else if (reader.uTest(A_DIGIT_0)) {
    collapseTrailingWhitespaceAndNewlinesIntoBuffer(into, nlBuffer, wsBuffer)
    into.push(A_NIL)
    skipASCII(this.reader, this.position)
  }

  // Bell
  else if (reader.uTest(A_LOWER_A)) {
    collapseTrailingWhitespaceAndNewlinesIntoBuffer(into, nlBuffer, wsBuffer)
    into.push(A_BELL)
    skipASCII(this.reader, this.position)
  }

  // Backspace
  else if (reader.uTest(A_LOWER_B)) {
    collapseTrailingWhitespaceAndNewlinesIntoBuffer(into, nlBuffer, wsBuffer)
    into.push(A_BACKSPACE)
    skipASCII(this.reader, this.position)
  }

  // Tab
  else if (reader.uTest(A_LOWER_T)) {
    collapseTrailingWhitespaceAndNewlinesIntoBuffer(into, nlBuffer, wsBuffer)
    into.push(A_TAB)
    skipASCII(this.reader, this.position)
  }

  // Vertical Tab
  else if (reader.uTest(A_LOWER_V)) {
    collapseTrailingWhitespaceAndNewlinesIntoBuffer(into, nlBuffer, wsBuffer)
    into.push(A_VERTICAL_TAB)
    skipASCII(this.reader, this.position)
  }

  // Form Feed
  else if (reader.uTest(A_LOWER_F)) {
    collapseTrailingWhitespaceAndNewlinesIntoBuffer(into, nlBuffer, wsBuffer)
    into.push(A_FORM_FEED)
    skipASCII(this.reader, this.position)
  }

  // Carriage Return
  else if (reader.uTest(A_LOWER_R)) {
    collapseTrailingWhitespaceAndNewlinesIntoBuffer(into, nlBuffer, wsBuffer)
    into.push(A_CARRIAGE_RETURN)
    skipASCII(this.reader, this.position)
  }

  // Escape
  else if (reader.uTest(A_LOWER_E)) {
    collapseTrailingWhitespaceAndNewlinesIntoBuffer(into, nlBuffer, wsBuffer)
    into.push(A_ESCAPE)
    skipASCII(this.reader, this.position)
  }

  // Next Line
  else if (reader.uTest(A_UPPER_N)) {
    collapseTrailingWhitespaceAndNewlinesIntoBuffer(into, nlBuffer, wsBuffer)
    into.push(UbC2)
    into.push(Ub85)
    skipASCII(this.reader, this.position)
  }

  // Non-Breaking Space
  else if (reader.uTest(A_UNDERSCORE)) {
    collapseTrailingWhitespaceAndNewlinesIntoBuffer(into, nlBuffer, wsBuffer)
    into.push(UbC2)
    into.push(UbA0)
    skipASCII(this.reader, this.position)
  }

  // Line Separator
  else if (reader.uTest(A_UPPER_L)) {
    collapseTrailingWhitespaceAndNewlinesIntoBuffer(into, nlBuffer, wsBuffer)
    into.push(UbE2)
    into.push(Ub80)
    into.push(UbA8)
    skipASCII(this.reader, this.position)
  }

  // Paragraph Separator
  else if (reader.uTest(A_UPPER_P)) {
    collapseTrailingWhitespaceAndNewlinesIntoBuffer(into, nlBuffer, wsBuffer)
    into.push(UbE2)
    into.push(Ub80)
    into.push(UbA9)
    skipASCII(this.reader, this.position)
  }

  // Junk
  else {
    collapseTrailingWhitespaceAndNewlinesIntoBuffer(into, nlBuffer, wsBuffer)
    into.push(A_BACKSLASH)
    into.claimUTF8(this.reader, this.position)
    warn("unrecognized or invalid escape sequence", start, position.mark())
  }
}

@Suppress("NOTHING_TO_INLINE")
@OptIn(ExperimentalUnsignedTypes::class)
private inline fun YAMLStreamTokenizerImpl.newDoubleQuotedStringToken(
  value: UByteArray,
  indent: UInt,
  start: SourcePosition,
  end: SourcePosition
) =
  YAMLTokenScalarQuotedDouble(UByteString(value), start, end, indent, popWarnings())

private fun YAMLStreamTokenizerImpl.readHexEscape(start: SourcePosition, into: UByteBuffer) {
  skipASCII(this.reader, this.position)
  reader.cache(2)

  if (reader.isHexDigit() && reader.isHexDigit(1)) {
    val value = ((reader.asHexDigit() shl 4) + reader.asHexDigit(1)).toUByte()
    into.push(value)
    skipASCII(this.reader, this.position, 2)
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
        into.claimUTF8(this.reader, this.position)
      }
    }

    warn("invalid hex escape sequence", start, position.mark())
  }
}

private fun YAMLStreamTokenizerImpl.readSmallUnicodeEscape(start: SourcePosition, into: UByteBuffer) {
  skipASCII(this.reader, this.position)
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
        into.claimUTF8(this.reader, this.position)
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
  skipASCII(this.reader, this.position, 4)
}

private fun YAMLStreamTokenizerImpl.readBigUnicodeEscape(start: SourcePosition, into: UByteBuffer) {
  skipASCII(this.reader, this.position)
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
        into.claimUTF8(this.reader, this.position)
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
  skipASCII(this.reader, this.position, 8)
}
