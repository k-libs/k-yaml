package io.foxcapades.lib.k.yaml.scan

import io.foxcapades.lib.k.yaml.util.*
import io.foxcapades.lib.k.yaml.util.UByteBuffer
import io.foxcapades.lib.k.yaml.util.uIsNsWordChar


internal fun YAMLScanner.fetchTagDirectiveToken(startMark: SourcePosition) {
  // At this point we've already skipped over `%TAG<WS>`.
  //
  // Skip over any additional blank spaces which will hopefully leave us at the
  // start of our tag handle.
  var infixSpace = eatBlanks()

  // If, after skipping over the empty spaces, we hit a `#`, line break, or the
  // EOF, then we have an incomplete token.
  if (havePound() || haveAnyBreakOrEOF())
    return fetchIncompleteTagDirectiveToken(startMark, position.mark(modIndex = -infixSpace, modColumn = -infixSpace))

  // If the next character is not an exclamation mark, then we have a malformed
  // Tag Directive.
  if (!haveExclaim())
    return fetchMalformedTagDirectiveToken(startMark)

  // So at this point, we have seen `%TAG !`.  Now we have to determine whether
  // this is a primary tag handle (`!`), a secondary tag handle (`!!`), or a
  // named tag handle (`!<ns-word-char>!`).

  // Create a buffer to store our handle value in.
  val handleBuffer = UByteBuffer(16)

  // Claim the first `!` character from the reader into our buffer.
  handleBuffer.claimASCII(1, reader.utf8Buffer, position)

  while (true) {
    cache(1)

    if (haveExclaim())
      break

    if (havePercent()) {
      cache(3)

      if (reader.utf8Buffer.isDecimalDigit(1) && reader.utf8Buffer.isDecimalDigit(2)) {
        handleBuffer.claimASCII(3, reader.utf8Buffer, position)
        continue
      }

      // So it was a `%` character followed by something other than 2 decimal
      // digits.
      return fetchInvalidTagDirectiveToken(startMark)
    }

    if (haveBlankAnyBreakOrEOF())
      return fetchIncompleteTagDirectiveToken(startMark, position.mark())

    // TODO: should we recover by converting the invalid character into a hex
    //       escape sequence and tossing up a warning?
    if (!reader.utf8Buffer.uIsNsWordChar())
      return fetchInvalidTagDirectiveToken(startMark)

    // If it _is_ an `ns-word-char` then it is in the ASCII range and is a
    // single byte.
    handleBuffer.claimASCII(1, reader.utf8Buffer, position)
  }

  // Okay, so if we've made it this far, we've seen at least `%TAG !` and at
  // at most `%TAG !<ns-word-char>!`.  At this point we are expecting to see one
  // or more blank characters followed by the prefix value.

  // If we _don't_ have a blank character then the directive is junk.
  if (!reader.utf8Buffer.isBlank())
    return fetchInvalidTagDirectiveToken(startMark)

  // Skip the whitespaces until we encounter something else.
  infixSpace = eatBlanks()

  // If the next thing after the blanks was a linebreak or EOF then we have an
  // incomplete directive.
  if (haveAnyBreakOrEOF())
    return fetchIncompleteTagDirectiveToken(startMark, position.mark(modIndex =  -infixSpace, modColumn = -infixSpace))

  // Okay so we have another character in the buffer.  It _should_ be either an
  // exclamation mark (for a local tag prefix) followed by zero or more
  // `<ns-uri-char>` characters, or any `<ns-tag-char>` followed by zero or
  // `<ns-uri-char>` characters.

  // If we hit something else, other than an exclamation mark or an
  // `<ns-tag-char>` character, then we have an invalid tag directive.
  if (!(reader.utf8Buffer.isExclaim() || reader.utf8Buffer.isNsTagChar()))
    return fetchInvalidTagDirectiveToken(startMark)

  // So we have a valid starting character for our prefix, lets create a buffer
  // and read any remaining characters in the prefix into it.
  val prefixBuffer = UByteBuffer(16)

  // Claim the starting character that we already inspected.
  prefixBuffer.claimASCII(1, reader.utf8Buffer, position)

  while (true) {
    cache(1)

    if (haveBlankAnyBreakOrEOF())
      break

    // If we encounter a non-URI character than we have an invalid tag
    // directive.
    //
    // Unsafe call because we know based on the previous check that there is at
    // least one byte in the buffer.
    if (!reader.utf8Buffer.uIsNsURIChar())
      return fetchInvalidTagDirectiveToken(startMark)
  }

  // Okay so we've successfully read our tag handle and our tag prefix.  Trouble
  // is, the line isn't over yet.  There could be a heap of junk waiting for us,
  // causing this directive line to be invalid.
  if (reader.utf8Buffer.isBlank()) {
    // We have more spaces after the prefix value.  This could be valid if the
    // spaces are followed by a line break (useless trailing spaces) or if the
    // spaces are followed by a comment line.
    //
    // Skip the spaces and see what's next.  If it is something other than a
    // comment or a newline, then we have an invalid tag directive.
    eatBlanks()

    if (!(haveAnyBreakOrEOF() || havePound()))
      return fetchInvalidTagDirectiveToken(startMark)
  }

  // If we've made it this far, then yay!  We did it!  We found and successfully
  // parsed a valid tag token, now we just have to assemble it and queue it up.

  TODO("""
    . figure out what the tag handle looks like
    . skip over whitespaces
    . figure out what the tag prefix looks like
  """.trimIndent())

}

private fun YAMLScanner.fetchInvalidTagDirectiveToken(startMark: SourcePosition) {
  warn("malformed %TAG token", startMark)
  TODO("finish off the token")
}

private fun YAMLScanner.fetchIncompleteTagDirectiveToken(
  start: SourcePosition,
  end:   SourcePosition,
) {

}

private fun UByteBuffer.claimASCII(bytes: Int, other: UByteBuffer, position: SourcePositionTracker) {
  var i = 0
  while (i++ < bytes)
    push(other.pop())

  position.incPosition(bytes.toUInt())
}