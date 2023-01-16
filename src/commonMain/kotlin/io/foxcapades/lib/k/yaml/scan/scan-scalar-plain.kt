package io.foxcapades.lib.k.yaml.scan

import io.foxcapades.lib.k.yaml.bytes.A_SPACE
import io.foxcapades.lib.k.yaml.util.UByteBuffer
import io.foxcapades.lib.k.yaml.util.utf8Width


@OptIn(ExperimentalUnsignedTypes::class)
internal fun YAMLScanner.fetchPlainScalar() {
  // Record the position of the first character in the plain scalar.
  val startMark = position.mark()

  // Create a rolling tracker that will be used to keep track of the position
  // of the last non-blank, non-break character seen before the next token is
  // encountered.
  val endMark = position.copy()

  // Whitespaces at the end of a line
  val trailingWS = UByteBuffer()

  // Line breaks in between
  val lineBreaks = UByteBuffer()

  // Buffer for the token value
  val tokenBuffer = UByteBuffer(2048)

  while (true) {
    // Load the next codepoint into the reader buffer
    cache(1)

    // If the reader is empty, then whatever we currently have in our token
    // buffer
    if (!haveMoreCharactersAvailable)
      return tokens.push(newPlainScalarToken(tokenBuffer.popToArray(), startMark, endMark.mark()))

    // TODO:
    //   | fetchPlainScalar is too greedy, it should stop parsing when it hits the
    //   | start of another token on a new line.
    //   |
    //   | This means that when starting a new line, if we are at the first column,
    //   | we need to perform a check that detects whether we have a possible token
    //   | leader.

    // When we hit one of the following characters, then we pay attention to
    // what's going on because we may have hit the start of a new token:
    //
    //   `:` `,` `?` `#`
    //
    // When we hit a newline, or whitespace character, buffer it on the side
    // in case we need it.
    when {

      // If we hit a whitespace character
      haveBlank() -> {
        // And the last character was not a line break
        if (lineBreaks.isEmpty)
        // Pop it from the reader buffer and append it to our trailing
        // whitespace buffer for possible use if we encounter a non-space
        // character on this line.
          trailingWS.claimASCII()

        // Skip to the next character
        continue
      }

      // If we hit a newline character
      haveAnyBreak() -> {
        // Append it to our newline buffer in case we need it to collapse into
        // a space or shortened set of newlines as per the YAML specification.
        lineBreaks.claimNewLine()

        // Continue to the next character.
        continue
      }

      // If we hit a `:` character
      haveColon() -> {
        // Attempt to cache another codepoint in the buffer, we need to look
        // ahead to the next character to determine if we've reached the end
        // of this scalar.
        cache(2)

        // If the colon character is followed by any of this junk, then IT IS
        // THE END! (of the scalar we've been chewing on, but the beginning of
        // a mapping value indicator)
        if (haveBlankAnyBreakOrEOF(1))
          return tokens.push(newPlainScalarToken(tokenBuffer.popToArray(), startMark, endMark.mark()))
      }

      // If we hit a `,` character AND we are in a flow context
      haveComma() && inFlow -> {
        // Then we have reached the end of our scalar token (and the start of
        // a flow entry separator token
        return tokens.push(newPlainScalarToken(tokenBuffer.popToArray(), startMark, endMark.mark()))
      }

      // If we hit a `?` character AND we are at the start of a line AND we
      // are NOT in a flow context.
      haveQuestion() && !inFlow && atStartOfLine -> {
        // Attempt to cache another codepoint in the buffer, we need to look
        // ahead to the next character to determine whether we've reached the
        // end of this scalar
        cache(2)

        // If the question mark character is followed by any of this stuff,
        // then it is, in fact, the start of a complex mapping key indicator.
        if (haveBlankAnyBreakOrEOF(1)) {
          return tokens.push(newPlainScalarToken(tokenBuffer.popToArray(), startMark, endMark.mark()))
        }
      }

      // If we hit a `#` character AND we were preceded by a whitespace or
      // line breaks
      havePound() && (trailingWS.isNotEmpty || lineBreaks.isNotEmpty) -> {
        // Then we've found the start of a comment, so wrap up our scalar.
        return tokens.push(newPlainScalarToken(tokenBuffer.popToArray(), startMark, endMark.mark()))
      }

      lineBreaks.isNotEmpty && trailingWS.isEmpty -> {
        var breakNow = false

        if (havePercent()) {
          breakNow = true
        }

        else if (haveDash()) {
          cache(4)
          if (haveBlankAnyBreakOrEOF(1)) {
            breakNow = true
          } else if (haveDash(1) && haveDash(2) && haveBlankAnyBreakOrEOF(3)) {
            breakNow = true
          }
        }

        else if (havePeriod()) {
          cache(4)
          if (havePeriod(1) && havePeriod(2) && haveBlankAnyBreakOrEOF(3)) {
            breakNow = true
          }
        }

        if (breakNow)
          return tokens.push(newPlainScalarToken(tokenBuffer.popToArray(), startMark, endMark.mark()))
      }
    }

    // If we didn't hit `continue` or `return` above, then we can just append
    // it to the token value buffer.

    // If there were no line breaks in the pile of whitespaces, then we
    // only may have trailing whitespace characters:
    if (lineBreaks.isEmpty) {
      tokenBuffer.takeFrom(trailingWS)
    } else {
      // Ignore the first line break
      lineBreaks.skip(lineBreaks.utf8Width())

      // If there was only one line break, convert it to a single space
      if (lineBreaks.isEmpty) {
        tokenBuffer.push(A_SPACE)
      }

      // If there were additional line breaks then append them to the
      // token buffer
      else {
        while (lineBreaks.isNotEmpty) {
          tokenBuffer.claimNewLine(lineBreaks)
        }
      }
    }

    // TODO:
    //   | Before popping the next character from the reader to the token
    //   | buffer:
    //   |
    //   | What are the limitations or rules placed on what characters are
    //   | allowed in a plain scalar?  Do they have to be displayable?
    //   |
    //   | We're gonna take the codepoint no matter what, but we should emit
    //   | a warning about the invalid characters, and potentially escape
    //   | control characters?

    tokenBuffer.claimUTF8()
    endMark.become(position)

  }
}
