package io.foxcapades.lib.k.yaml.scan.stream

import io.foxcapades.lib.k.yaml.token.YAMLTokenTag
import io.foxcapades.lib.k.yaml.util.*
import io.foxcapades.lib.k.yaml.util.UByteBuffer
import io.foxcapades.lib.k.yaml.util.uIsExclamation
import io.foxcapades.lib.k.yaml.util.uIsLessThan


internal fun YAMLStreamTokenizerImpl.parseTagToken() {
  val start  = position.mark()
  val handle = contentBuffer1

  lineContentIndicator = LineContentIndicatorContent

  handle.clear()

  /*
  At this point, the current character in the reader buffer is the leading `!`
  character.

  Eat it into our handle buffer.
  */
  handle.claimASCII(buffer, position)

  /*
  The next character in the buffer (or whether we have such a character)
  determines what kind of tag we are going to attempt to parse.

  Attempt to ensure another character is cached in the reader buffer for us to
  test.
  */
  buffer.cache(1)

  /*
  If there is no more characters in the stream, or if the next character in the
  stream is a blank or a line break, then all we have is the `!` character,
  which is the "non-specific" tag.
  */
  if (buffer.isBlankAnyBreakOrEOF() || (buffer.uIsComma() && inFlow)) {
    parseNonSpecificTag(start, handle)
    return
  }

  /*
  If the next character in the reader buffer is a `<` character, then we should
  have a verbatim tag.
  */
  if (buffer.uIsLessThan()) {
    parseVerbatimTag(start, handle)
    return
  }

  /*
  If the next character in the reader buffer is another `!` character, then we
  should have a local secondary tag.
  */
  if (buffer.uIsExclamation()) {
    parseLocalTag(start, handle)
    return
  }

  /*
  If the next character is not one of the above, then it should be a URI or
  percent escape character that is part of either a primary local tag suffix or
  a named tag handle.
  */
  parseAmbiguousTag(start, handle)
}

@OptIn(ExperimentalUnsignedTypes::class)
private fun YAMLStreamTokenizerImpl.parseNonSpecificTag(start: SourcePosition, handle: UByteBuffer) {
  tokens.push(YAMLTokenTag(
    UByteString(handle.toArray()),
    UByteString(ubyteArrayOf()),
    indent,
    start,
    position.mark(),
    popWarnings()
  ))
}

@OptIn(ExperimentalUnsignedTypes::class)
private fun YAMLStreamTokenizerImpl.parseVerbatimTag(start: SourcePosition, handle: UByteBuffer) {
  // eat the `<` character
  handle.claimASCII(buffer, position)

  // eat the rest of the tag until we see a `>` character
  while (true) {
    buffer.cache(1)

    if (buffer.isNsURIChar()) {
      handle.claimASCII(buffer, position)
    }

    else if (buffer.isPercent()) {
      eatPercentEscape(handle, true)
    }

    else if (buffer.isBlankAnyBreakOrEOF()) {
      emitInvalidToken("incomplete verbatim tag due to reaching the end of the input stream", start)
      return
    }

    else if (buffer.isGreaterThan()) {
      handle.claimASCII(buffer, position)
      break
    }

    else {
      warn("non URI safe character in verbatim tag", position.mark(), position.mark(1, 0, 1))
      handle.claimUTF8(buffer, position)
    }
  }

  /*
  If we made it here then we have eaten the whole verbatim tag from opening `!<`
  to closing `>`.

  Now make sure there was actually something in there.  If the handle size is
  equal to `3` then we know it just contains the verbatim tag leader and suffix
  characters and no actual value. ("!<>")
  */
  if (handle.size == 3) {
    warn("empty verbatim tag", start)
  }

  tokens.push(YAMLTokenTag(
    UByteString(handle.toArray()),
    UByteString(ubyteArrayOf()),
    indent,
    start,
    position.mark(),
    popWarnings()
  ))
}

@OptIn(ExperimentalUnsignedTypes::class)
private fun YAMLStreamTokenizerImpl.parseLocalTag(start: SourcePosition, handle: UByteBuffer) {
  handle.claimASCII(buffer, position)

  val suffix = contentBuffer2
  suffix.clear()

  while (true) {
    buffer.cache(1)

    if (buffer.isBlankAnyBreakOrEOF() || (buffer.uIsComma() && inFlow)) {
      break
    }

    else if (buffer.isNsTagChar()) {
      suffix.claimASCII(buffer, position)
    }

    else if (buffer.isPercent()) {
      eatPercentEscape(suffix, false)
    }

    else {
      warn("non URI safe character in local tag", position.mark(), position.mark(1, 0, 1))
      suffix.claimUTF8(buffer, position)
    }
  }

  if (suffix.isEmpty) {
    warn("local tag with no suffix value", start)
  }

  tokens.push(YAMLTokenTag(
    UByteString(handle.toArray()),
    UByteString(suffix.toArray()),
    indent,
    start,
    position.mark(),
    popWarnings()
  ))
}

@OptIn(ExperimentalUnsignedTypes::class)
private fun YAMLStreamTokenizerImpl.parseAmbiguousTag(start: SourcePosition, handle: UByteBuffer) {
  val suffix = contentBuffer2

  suffix.clear()

  // look for end of tag, or the ! character that marks the end of the handle
  // and start of the suffix
  while (true) {
    buffer.cache(1)

    if (buffer.isBlankAnyBreakOrEOF() || (buffer.uIsComma() && inFlow)) {
      tokens.push(YAMLTokenTag(
        UByteString(handle.toArray()),
        UByteString(suffix.toArray()),
        indent,
        start,
        position.mark(),
        popWarnings()
      ))
      return
    }

    else if (buffer.uIsNsTagChar()) {
      suffix.claimASCII(buffer, position)
    }

    else if (buffer.isPercent()) {
      eatPercentEscape(suffix, true)
    }

    else if (buffer.isExclamation()) {
      while (suffix.isNotEmpty)
        handle.push(suffix.pop())

      parseLocalTag(start, handle)
      return
    }

    else {
      warn("non URI safe character in local tag", position.mark(), position.mark(1, 0, 1))
      suffix.claimUTF8(buffer, position)
    }
  }
}

private fun YAMLStreamTokenizerImpl.eatPercentEscape(content: UByteBuffer, inVerbatim: Boolean) {
  val start = position.mark()

  // eat the percent character
  content.claimASCII(buffer, position)

  buffer.cache(2)

  // verify that the next 2 characters are hex digits
  if (buffer.isHexDigit(0) && buffer.isHexDigit(1)) {
    content.claimASCII(buffer, position, 2)
    return
  }

  var i = 0
  while (i++ < 2) {
    if (buffer.isBlankAnyBreakOrEOF()) {
      warn("incomplete URI escape sequence due to reaching the end of the input stream", start, position.mark())
      return
    }

    if (inVerbatim && buffer.isGreaterThan()) {
      warn("incomplete URI escape sequence due to reaching the end of the verbatim tag", start, position.mark())
      return
    }

    content.claimUTF8(buffer, position)
  }
}
