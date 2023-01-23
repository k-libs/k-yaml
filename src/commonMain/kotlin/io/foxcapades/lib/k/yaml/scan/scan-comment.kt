package io.foxcapades.lib.k.yaml.scan

import io.foxcapades.lib.k.yaml.token.YAMLTokenComment
import io.foxcapades.lib.k.yaml.util.SourcePosition
import io.foxcapades.lib.k.yaml.util.UByteBuffer
import io.foxcapades.lib.k.yaml.util.UByteString
import io.foxcapades.lib.k.yaml.util.isBlank

/**
 * # Fetch Comment Token
 *
 * Parses the rest of the current line as the comment's content and emits a
 * [comment token][YAMLTokenComment].
 *
 * @since 0.1.0
 * @author Elizabeth Paige Harper - https://github.com/foxcapades
 */
internal fun YAMLScannerImpl.fetchCommentToken() {
  val trailing = this.haveContentOnThisLine
  val indent   = this.indent
  val bContent = this.contentBuffer1
  val bTailWS  = this.trailingWSBuffer
  val start    = this.position.mark()

  // We have content on this line.
  this.haveContentOnThisLine = true

  bContent.clear()
  bTailWS.clear()

  skipASCII(this.reader, this.position)
  skipBlanks()

  // The comment line may be empty
  if (this.reader.isAnyBreakOrEOF())
    return this.emitCommentToken(bContent, indent, trailing, start, start.copy(modIndex = 1, modColumn = 1))

  // Iterate through the characters in the stream.
  while (true) {
    // Try and make sure there is a character in our reader buffer for us to
    // test.
    this.reader.cache(1)

    // If the next character in the buffer is a blank character, then append it
    // to our trailing whitespace buffer because we may or may not need it later
    // depending on whether there are more non-blank characters on this line or
    // not.
    if (this.reader.isBlank()) {
      bTailWS.claimASCII(this.reader, this.position)
    }

    // If the next thing in the buffer is a line break, or the end of the input
    // stream, then break out of our loop because we are done parsing the
    // comment content.
    else if (reader.isAnyBreakOrEOF()) {
      // Note: we intentionally discard the trailing whitespace here.
      break
    }

    // If the next thing in the buffer is anything else, then we will append it
    // to our comment content.
    else {
      // If there were any trailing whitespaces before this non-blank character
      // then we need to put them into the buffer betweent the last non-blank
      // character and the next one.
      while (bTailWS.isNotEmpty)
        bContent.claimASCII(bTailWS)

      // Now claim the unknown character as part of the comment content.
      bContent.claimUTF8(this.reader, this.position)
    }
  }

  return this.emitCommentToken(
    bContent,
    indent,
    trailing,
    start,
    // Note: The modIndex and modColumn bits are to exclude the trailing
    // whitespace from the token ending position.
    this.position.mark(modIndex = -bTailWS.size, modColumn = -bTailWS.size)
  )
}

@Suppress("NOTHING_TO_INLINE")
@OptIn(ExperimentalUnsignedTypes::class)
private inline fun YAMLScannerImpl.emitCommentToken(
  content:  UByteBuffer,
  indent:   UInt,
  trailing: Boolean,
  start: SourcePosition,
  end:      SourcePosition = this.position.mark()
) {
  this.tokens.push(YAMLTokenComment(
    UByteString(content.popToArray()),
    indent,
    trailing,
    start,
    end,
    this.getWarnings(),
  ))
}
