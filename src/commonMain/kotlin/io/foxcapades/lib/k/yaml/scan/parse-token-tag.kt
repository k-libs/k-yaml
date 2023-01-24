package io.foxcapades.lib.k.yaml.scan

import io.foxcapades.lib.k.yaml.bytes.A_EXCLAIM
import io.foxcapades.lib.k.yaml.bytes.StrEmpty
import io.foxcapades.lib.k.yaml.bytes.StrPrimaryTagPrefix
import io.foxcapades.lib.k.yaml.bytes.StrSecondaryTagPrefix
import io.foxcapades.lib.k.yaml.token.YAMLTokenTag
import io.foxcapades.lib.k.yaml.util.*


internal fun YAMLStreamTokenizerImpl.parseTagToken() {
  val startMark = position.mark()

  // Skip the first `!`
  skipASCII(this.reader, this.position)

  lineContentIndicator = LineContentIndicatorContent

  // Queue up the next character to read
  reader.cache(1)

  if (reader.isBlankAnyBreakOrEOF())
    return fetchNonSpecificTagToken(startMark)

  if (reader.isLessThan())
    return fetchVerbatimTagToken(startMark)

  if (reader.isExclamation())
    return fetchSecondaryTagToken(startMark)

  if (reader.isNsTagChar())
    return fetchHandleOrPrimaryTagToken(startMark)


  warn("unexpected or invalid character while parsing a tag", position.mark(), position.mark(1, 0, 1))
  skipUntilBlankBreakOrEOF()
  emitInvalidToken(startMark, position.mark())
  return
}

@OptIn(ExperimentalUnsignedTypes::class)
private fun YAMLStreamTokenizerImpl.fetchNonSpecificTagToken(startMark: SourcePosition) {
  emitTagToken(StrPrimaryTagPrefix, StrEmpty, startMark)
}

@OptIn(ExperimentalUnsignedTypes::class)
private fun YAMLStreamTokenizerImpl.fetchHandleOrPrimaryTagToken(startMark: SourcePosition) {
  // Clear our reusable buffer just in case some goober left something in it.
  contentBuffer1.clear()

  // Push our prefix `!` character.
  contentBuffer1.push(A_EXCLAIM)

  // Claim the URI character the cursor is currently on.
  contentBuffer1.claimUTF8(this.reader, this.position)

  while (true) {
    reader.cache(1)

    // If we hit a blank, new line, or the EOF
    if (reader.isBlankAnyBreakOrEOF()) {
      // Pop the exclamation mark off because what we have is a suffix on the
      // primary tag handle and a suffix should not have a leading exclamation
      // mark.
      contentBuffer1.pop()

      // Generate the primary tag token.
      return fetchPrimaryTagToken(startMark, contentBuffer1.popToArray())
    }

    // If we hit an exclamation mark
    else if (reader.isExclamation()) {
      // Then what we have is the handle for a local tag.

      // Eat the exclamation mark to finish off our handle
      contentBuffer1.claimASCII(this.reader, this.position)

      // Generate a local tag token
      return fetchLocalTagToken(startMark, contentBuffer1.popToArray())
    }

    // If we hit a URI character
    else if (reader.isNsURIChar()) {
      // append it to the buffer
      contentBuffer1.claimUTF8(this.reader, this.position)
    }

    // If we hit any other character
    else {
      warn("unexpected or invalid character while parsing a tag", position.mark(), position.mark(1, 0, 1))
      skipUntilBlankBreakOrEOF()
      emitInvalidToken(startMark, position.mark())
      return
    }
  }
}

@OptIn(ExperimentalUnsignedTypes::class)
private fun YAMLStreamTokenizerImpl.fetchPrimaryTagToken(startMark: SourcePosition, suffix: UByteArray) {
  emitTagToken(StrPrimaryTagPrefix, suffix, startMark)
}

@OptIn(ExperimentalUnsignedTypes::class)
private fun YAMLStreamTokenizerImpl.fetchLocalTagToken(startMark: SourcePosition, handle: UByteArray) {
  // !foo!bar
  // we've seen `!foo!`

  contentBuffer1.clear()

  reader.cache(1)

  if (reader.isBlankAnyBreakOrEOF()) {
    emitInvalidToken("incomplete tag; tag had no suffix", startMark, position.mark())
    return
  }

  while (true) {
    if (reader.isNsURIChar()) {
      contentBuffer1.claimUTF8(this.reader, this.position)
    }

    else if (reader.isBlankAnyBreakOrEOF()) {
      break
    }

    else {
      warn("unexpected or invalid character while parsing a tag", position.mark(), position.mark(1, 0, 1))
      skipUntilBlankBreakOrEOF()
      emitInvalidToken(startMark, position.mark())
      return
    }

    reader.cache(1)
  }

  emitTagToken(handle, contentBuffer1.popToArray(), startMark)
}

@OptIn(ExperimentalUnsignedTypes::class)
private fun YAMLStreamTokenizerImpl.fetchSecondaryTagToken(startMark: SourcePosition) {
  contentBuffer1.clear()

  skipASCII(this.reader, this.position)
  reader.cache(1)

  if (reader.isBlankAnyBreakOrEOF())
    TODO("secondary token with no suffix")

  while (true) {
    if (reader.isNsTagChar()) {
      contentBuffer1.claimUTF8(this.reader, this.position)
    } else if (reader.isBlankAnyBreakOrEOF()) {
      break
    } else {
      warn("invalid character in secondary tag token", position.mark(), position.mark(1, 0, 1))
      skipUntilBlankBreakOrEOF()
      emitInvalidToken(startMark, position.mark())
      return
    }

    reader.cache(1)
  }

  emitTagToken(StrSecondaryTagPrefix, contentBuffer1.popToArray(), startMark)
}

@OptIn(ExperimentalUnsignedTypes::class)
private fun YAMLStreamTokenizerImpl.fetchVerbatimTagToken(startMark: SourcePosition) {
  contentBuffer1.clear()

  contentBuffer1.push(A_EXCLAIM)
  contentBuffer1.claimASCII(this.reader, this.position)

  while (true) {
    reader.cache(1)

    if (reader.isGreaterThan()) {
      contentBuffer1.claimASCII(this.reader, this.position)
      break
    } else if (reader.isNsURIChar()) {
      contentBuffer1.claimASCII(this.reader, this.position)
    } else if (reader.isBlankAnyBreakOrEOF()) {
      TODO("incomplete verbatim tag")
    } else {
      TODO("unexpected character in verbatim tag")
    }
  }

  emitTagToken(contentBuffer1.popToArray(), StrEmpty, startMark)
}

@Suppress("NOTHING_TO_INLINE")
@OptIn(ExperimentalUnsignedTypes::class)
private inline fun YAMLStreamTokenizerImpl.emitTagToken(
  handle: UByteArray,
  suffix: UByteArray,
  start:  SourcePosition,
  end:    SourcePosition = this.position.mark()
) =
  this.tokens.push(YAMLTokenTag(UByteString(handle), UByteString(suffix), this.indent, start, end, this.popWarnings()))
