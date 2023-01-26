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
  skipASCII(this.buffer, this.position)

  while (true) {
    buffer.cache(1)

    if (buffer.isBackslash()) {
      lineContentIndicator = LineContentIndicatorContent
      readPossibleEscapeSequence(contentBuffer1, trailingNLBuffer, trailingWSBuffer)
    }

    else if (buffer.isDoubleQuote()) {
      lineContentIndicator = LineContentIndicatorContent
      skipASCII(this.buffer, this.position)
      collapseTrailingWhitespaceAndNewlinesIntoBuffer(contentBuffer1, trailingNLBuffer, trailingWSBuffer)
      tokens.push(newDoubleQuotedStringToken(contentBuffer1.popToArray(), indent, start, position.mark()))
      return
    }

    else if (buffer.isBlank()) {
      if (lineContentIndicator.haveAnyContent) {
        trailingWSBuffer.claimASCII(this.buffer, this.position)
      } else {
        skipASCII(this.buffer, this.position)
        this.indent++
      }
    }

    else if (buffer.isAnyBreak()) {
      trailingWSBuffer.clear()
      trailingNLBuffer.claimNewLine(this.buffer, this.position)
      lineContentIndicator = LineContentIndicatorBlanksOnly
      this.indent = 0u
    }

    else if (buffer.isEOF()) {
      emitInvalidToken("unexpected end of double quoted string due to EOF", start)
      return
    }

    else {
      lineContentIndicator = LineContentIndicatorContent
      collapseTrailingWhitespaceAndNewlinesIntoBuffer(contentBuffer1, trailingNLBuffer, trailingWSBuffer)
      contentBuffer1.claimUTF8(this.buffer, this.position)
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
  skipASCII(this.buffer, this.position)

  // Try to ensure we have another character in the buffer
  buffer.cache(1)

  if (buffer.isEOF()) {
    collapseTrailingWhitespaceAndNewlinesIntoBuffer(into, nlBuffer, wsBuffer)
    into.push(A_BACKSLASH)
    warn("invalid or unfinished escape sequence due to EOF", start, position.mark())
  }

  else if (buffer.isAnyBreak()) {
    nlBuffer.claimNewLine(this.buffer, this.position)
  }

  else if (
    buffer.uIsSpace()
    || buffer.uIsDoubleQuote()
    || buffer.uIsBackslash()
    || buffer.uIsSlash()
    || buffer.uIsTab()
  ) {
    collapseTrailingWhitespaceAndNewlinesIntoBuffer(into, nlBuffer, wsBuffer)
    into.claimASCII(this.buffer, this.position)
  }

  // \xXX
  else if (buffer.uTest(A_LOWER_X)) {
    collapseTrailingWhitespaceAndNewlinesIntoBuffer(into, nlBuffer, wsBuffer)
    readHexEscape(start, into)
  }

  // \uXXXX
  else if (buffer.uTest(A_LOWER_U)) {
    collapseTrailingWhitespaceAndNewlinesIntoBuffer(into, nlBuffer, wsBuffer)
    readSmallUnicodeEscape(start, into)
  }

  // \UXXXXXXXX
  else if (buffer.uTest(A_UPPER_U)) {
    collapseTrailingWhitespaceAndNewlinesIntoBuffer(into, nlBuffer, wsBuffer)
    readBigUnicodeEscape(start, into)
  }

  // Line Feed
  else if (buffer.uTest(A_LOWER_N)) {
    collapseTrailingWhitespaceAndNewlinesIntoBuffer(into, nlBuffer, wsBuffer)
    into.push(A_LINE_FEED)
    skipASCII(this.buffer, this.position)
  }

  // Null / Nil
  else if (buffer.uTest(A_DIGIT_0)) {
    collapseTrailingWhitespaceAndNewlinesIntoBuffer(into, nlBuffer, wsBuffer)
    into.push(A_NIL)
    skipASCII(this.buffer, this.position)
  }

  // Bell
  else if (buffer.uTest(A_LOWER_A)) {
    collapseTrailingWhitespaceAndNewlinesIntoBuffer(into, nlBuffer, wsBuffer)
    into.push(A_BELL)
    skipASCII(this.buffer, this.position)
  }

  // Backspace
  else if (buffer.uTest(A_LOWER_B)) {
    collapseTrailingWhitespaceAndNewlinesIntoBuffer(into, nlBuffer, wsBuffer)
    into.push(A_BACKSPACE)
    skipASCII(this.buffer, this.position)
  }

  // Tab
  else if (buffer.uTest(A_LOWER_T)) {
    collapseTrailingWhitespaceAndNewlinesIntoBuffer(into, nlBuffer, wsBuffer)
    into.push(A_TAB)
    skipASCII(this.buffer, this.position)
  }

  // Vertical Tab
  else if (buffer.uTest(A_LOWER_V)) {
    collapseTrailingWhitespaceAndNewlinesIntoBuffer(into, nlBuffer, wsBuffer)
    into.push(A_VERTICAL_TAB)
    skipASCII(this.buffer, this.position)
  }

  // Form Feed
  else if (buffer.uTest(A_LOWER_F)) {
    collapseTrailingWhitespaceAndNewlinesIntoBuffer(into, nlBuffer, wsBuffer)
    into.push(A_FORM_FEED)
    skipASCII(this.buffer, this.position)
  }

  // Carriage Return
  else if (buffer.uTest(A_LOWER_R)) {
    collapseTrailingWhitespaceAndNewlinesIntoBuffer(into, nlBuffer, wsBuffer)
    into.push(A_CARRIAGE_RETURN)
    skipASCII(this.buffer, this.position)
  }

  // Escape
  else if (buffer.uTest(A_LOWER_E)) {
    collapseTrailingWhitespaceAndNewlinesIntoBuffer(into, nlBuffer, wsBuffer)
    into.push(A_ESCAPE)
    skipASCII(this.buffer, this.position)
  }

  // Next Line
  else if (buffer.uTest(A_UPPER_N)) {
    collapseTrailingWhitespaceAndNewlinesIntoBuffer(into, nlBuffer, wsBuffer)
    into.push(UbC2)
    into.push(Ub85)
    skipASCII(this.buffer, this.position)
  }

  // Non-Breaking Space
  else if (buffer.uTest(A_UNDERSCORE)) {
    collapseTrailingWhitespaceAndNewlinesIntoBuffer(into, nlBuffer, wsBuffer)
    into.push(UbC2)
    into.push(UbA0)
    skipASCII(this.buffer, this.position)
  }

  // Line Separator
  else if (buffer.uTest(A_UPPER_L)) {
    collapseTrailingWhitespaceAndNewlinesIntoBuffer(into, nlBuffer, wsBuffer)
    into.push(UbE2)
    into.push(Ub80)
    into.push(UbA8)
    skipASCII(this.buffer, this.position)
  }

  // Paragraph Separator
  else if (buffer.uTest(A_UPPER_P)) {
    collapseTrailingWhitespaceAndNewlinesIntoBuffer(into, nlBuffer, wsBuffer)
    into.push(UbE2)
    into.push(Ub80)
    into.push(UbA9)
    skipASCII(this.buffer, this.position)
  }

  // Junk
  else {
    collapseTrailingWhitespaceAndNewlinesIntoBuffer(into, nlBuffer, wsBuffer)
    into.push(A_BACKSLASH)
    into.claimUTF8(this.buffer, this.position)
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
  skipASCII(this.buffer, this.position)
  buffer.cache(2)

  if (buffer.isHexDigit() && buffer.isHexDigit(1)) {
    val value = ((buffer.asHexDigit() shl 4) + buffer.asHexDigit(1)).toUByte()
    into.push(value)
    skipASCII(this.buffer, this.position, 2)
  } else {
    into.push(A_BACKSLASH)
    into.push(A_LOWER_X)

    var i = 0
    while (i++ < 2) {
      buffer.cache(1)
      if (buffer.isBlankAnyBreakOrEOF()) {
        warn("incomplete hex escape sequence", start, position.mark())
        return
      } else {
        into.claimUTF8(this.buffer, this.position)
      }
    }

    warn("invalid hex escape sequence", start, position.mark())
  }
}

private fun YAMLStreamTokenizerImpl.readSmallUnicodeEscape(start: SourcePosition, into: UByteBuffer) {
  skipASCII(this.buffer, this.position)
  buffer.cache(4)

  // If one or more of the next 4 bytes are NOT hex digits
  if (!(
      buffer.isHexDigit(0)
        || buffer.isHexDigit(1)
        || buffer.isHexDigit(2)
        || buffer.isHexDigit(3)
      )) {
    // iterate through the next 4 CHARACTERS and call those the invalid escape
    // sequence.  If we hit a blank, line break, or the EOF in the middle,
    // then only claim up to that point as part of the warning / token
    var i = 0
    while (i++ < 4) {
      buffer.cache(1)
      if (buffer.isBlankAnyBreakOrEOF()) {
        warn("incomplete unicode escape sequence", start, position.mark())
        return
      } else {
        into.claimUTF8(this.buffer, this.position)
      }
    }

    warn("invalid unicode escape sequence", start, position.mark())
    return
  }

  // If we're here then we have 4 hex digits to read from the buffer to form
  // our character which we will need to convert to UTF-8 to append to the
  // [into] buffer.
  val value =
    (buffer.asHexDigit(0) shl 12) +
      (buffer.asHexDigit(1) shl 8) +
      (buffer.asHexDigit(2) shl 4) +
      (buffer.asHexDigit(3))

  value.toUTF8(into)
  skipASCII(this.buffer, this.position, 4)
}

private fun YAMLStreamTokenizerImpl.readBigUnicodeEscape(start: SourcePosition, into: UByteBuffer) {
  skipASCII(this.buffer, this.position)
  buffer.cache(8)

  // If one or more of the next 4 bytes are NOT hex digits
  if (!(
      buffer.isHexDigit(0)
        || buffer.isHexDigit(1)
        || buffer.isHexDigit(2)
        || buffer.isHexDigit(3)
        || buffer.isHexDigit(4)
        || buffer.isHexDigit(5)
        || buffer.isHexDigit(6)
        || buffer.isHexDigit(7)
      )) {
    // iterate through the next 4 CHARACTERS and call those the invalid escape
    // sequence.  If we hit a blank, line break, or the EOF in the middle,
    // then only claim up to that point as part of the warning / token
    var i = 0
    while (i++ < 8) {
      buffer.cache(1)
      if (buffer.isBlankAnyBreakOrEOF()) {
        warn("incomplete unicode escape sequence", start, position.mark())
        return
      } else {
        into.claimUTF8(this.buffer, this.position)
      }
    }

    warn("invalid unicode escape sequence", start, position.mark())
    return
  }

  // If we're here then we have 4 hex digits to read from the buffer to form
  // our character which we will need to convert to UTF-8 to append to the
  // [into] buffer.
  val value =
    (buffer.asHexDigit(0) shl 28) +
      (buffer.asHexDigit(1) shl 24) +
      (buffer.asHexDigit(2) shl 20) +
      (buffer.asHexDigit(3) shl 16) +
      (buffer.asHexDigit(4) shl 12) +
      (buffer.asHexDigit(5) shl 8) +
      (buffer.asHexDigit(6) shl 4) +
      (buffer.asHexDigit(7))

  value.toUTF8(into)
  skipASCII(this.buffer, this.position, 8)
}
