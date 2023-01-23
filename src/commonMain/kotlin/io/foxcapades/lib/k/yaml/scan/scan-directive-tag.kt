package io.foxcapades.lib.k.yaml.scan

import io.foxcapades.lib.k.yaml.token.YAMLTokenDirectiveTag
import io.foxcapades.lib.k.yaml.util.*

@OptIn(ExperimentalUnsignedTypes::class)
internal fun YAMLScannerImpl.fetchTagDirectiveToken(startMark: SourcePosition) {
  // We have content on this line.
  this.haveContentOnThisLine = true

  // At this point we've already skipped over `%TAG<WS>`.
  //
  // Skip over any additional blank spaces which will hopefully leave us at the
  // start of our tag handle.
  //
  // We add 1 to the space count to account for the one space we've already seen
  // before this function was called.
  var infixSpace = 1 + eatBlanks()

  // If, after skipping over the empty spaces, we hit a `#`, line break, or the
  // EOF, then we have an incomplete token.
  if (reader.isPound() || reader.isAnyBreakOrEOF())
    return fetchIncompleteTagDirectiveToken(startMark, position.mark(modIndex = -infixSpace, modColumn = -infixSpace))

  // If the next character is not an exclamation mark, then we have a malformed
  // Tag Directive.
  if (!reader.isExclamation())
    return fetchInvalidTagDirectiveToken("unexpected character that cannot start a tag handle", startMark)

  // So at this point, we have seen `%TAG !`.  Now we have to determine whether
  // this is a primary tag handle (`!`), a secondary tag handle (`!!`), or a
  // named tag handle (`!<ns-word-char>!`).

  // Create a buffer to store our handle value in.
  val handleBuffer = UByteBuffer(16)

  // Claim the first `!` character from the reader into our buffer.
  handleBuffer.takeASCIIFrom(1, reader, position)

  while (true) {
    reader.cache(1)

    // If we have our ending exclamation mark:
    if (reader.isExclamation()) {
      // eat it
      handleBuffer.takeASCIIFrom(1, reader, position)
      // break out of the loop because we are done with the handle
      break
    }

    if (reader.isPercent()) {
      reader.cache(3)

      if (reader.isHexDigit(1) && reader.isHexDigit(2)) {
        handleBuffer.takeASCIIFrom(3, reader, position)
        continue
      }

      // So it was a `%` character followed by something other than 2 decimal
      // digits.
      return fetchInvalidTagDirectiveToken(
        "invalid URI escape; '%' character not followed by 2 hex digits",
        startMark
      )
    }

    if (reader.isBlankAnyBreakOrEOF())
      return fetchIncompleteTagDirectiveToken(startMark, position.mark())

    // TODO: should we recover by converting the invalid character into a hex
    //       escape sequence and tossing up a warning?
    if (!reader.uIsNsWordChar())
      return fetchInvalidTagDirectiveToken("tag handle contained an invalid character", startMark)

    // If it _is_ an `ns-word-char` then it is in the ASCII range and is a
    // single byte.
    handleBuffer.takeASCIIFrom(1, reader, position)
  }

  // Okay, so if we've made it this far, we've seen at least `%TAG !` and at
  // at most `%TAG !<ns-word-char>!`.  At this point we are expecting to see one
  // or more blank characters followed by the prefix value.

  // If we _don't_ have a blank character then the directive is junk.
  if (!reader.isBlank())
    return fetchInvalidTagDirectiveToken("unexpected character after tag handle", startMark)

  // Skip the whitespaces until we encounter something else.
  infixSpace = eatBlanks()

  // If the next thing after the blanks was a linebreak or EOF then we have an
  // incomplete directive.
  if (reader.isAnyBreakOrEOF() || reader.isPound())
    return fetchIncompleteTagDirectiveToken(startMark, position.mark(modIndex =  -infixSpace, modColumn = -infixSpace))

  // Okay so we have another character in the buffer.  It _should_ be either an
  // exclamation mark (for a local tag prefix) followed by zero or more
  // `<ns-uri-char>` characters, or any `<ns-tag-char>` followed by zero or
  // `<ns-uri-char>` characters.

  // If we hit something else, other than an exclamation mark or an
  // `<ns-tag-char>` character, then we have an invalid tag directive.
  if (!(reader.isExclamation() || reader.isNsTagChar()))
    return fetchInvalidTagDirectiveToken("unexpected first character of tag prefix", startMark)

  // So we have a valid starting character for our prefix, lets create a buffer
  // and read any remaining characters in the prefix into it.
  val prefixBuffer = UByteBuffer(16)

  // Claim the starting character that we already inspected.
  prefixBuffer.takeASCIIFrom(1, reader, position)

  while (true) {
    reader.cache(1)

    if (reader.isBlankAnyBreakOrEOF())
      break

    // If we encounter a non-URI character than we have an invalid tag
    // directive.
    //
    // Unsafe call because we know based on the previous check that there is at
    // least one byte in the buffer.
    if (!reader.uIsNsURIChar())
      return fetchInvalidTagDirectiveToken("unexpected non-URI safe character in tag prefix", startMark)

    prefixBuffer.takeASCIIFrom(1, reader, position)
  }

  infixSpace = 0

  // Okay so we've successfully read our tag handle and our tag prefix.  Trouble
  // is, the line isn't over yet.  There could be a heap of junk waiting for us,
  // causing this directive line to be invalid.
  if (reader.isBlank()) {
    // We have more spaces after the prefix value.  This could be valid if the
    // spaces are followed by a line break (useless trailing spaces) or if the
    // spaces are followed by a comment line.
    //
    // Skip the spaces and see what's next.  If it is something other than a
    // comment or a newline, then we have an invalid tag directive.
    infixSpace = eatBlanks()

    if (!(reader.isAnyBreakOrEOF() || reader.isPound()))
      return fetchInvalidTagDirectiveToken("unexpected character after prefix value", startMark)
  }

  // If we've made it this far, then yay!  We did it!  We found and successfully
  // parsed a valid tag token, now we just have to assemble it and queue it up.

  tokens.push(newTagDirectiveToken(
    handleBuffer.popToArray(),
    prefixBuffer.popToArray(),
    startMark,
    position.mark(modIndex = -infixSpace, modColumn = -infixSpace)
  ))
}

@Suppress("NOTHING_TO_INLINE")
private inline fun YAMLScannerImpl.fetchInvalidTagDirectiveToken(reason: String, start: SourcePosition) {
  emitInvalidToken("malformed %TAG token: $reason", start, skipUntilCommentBreakOrEOF())
}

@Suppress("NOTHING_TO_INLINE")
private inline fun YAMLScannerImpl.fetchIncompleteTagDirectiveToken(start: SourcePosition, end: SourcePosition) {
  emitInvalidToken("incomplete %TAG directive", start, end)
}

@Suppress("NOTHING_TO_INLINE")
@OptIn(ExperimentalUnsignedTypes::class)
private inline fun YAMLScannerImpl.newTagDirectiveToken(
  handle: UByteArray,
  prefix: UByteArray,
  start:  SourcePosition,
  end:    SourcePosition,
) =
  YAMLTokenDirectiveTag(UByteString(handle), UByteString(prefix), start, end, getWarnings())
