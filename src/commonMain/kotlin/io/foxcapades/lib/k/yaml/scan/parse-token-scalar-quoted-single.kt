package io.foxcapades.lib.k.yaml.scan

import io.foxcapades.lib.k.yaml.bytes.A_SPACE
import io.foxcapades.lib.k.yaml.token.YAMLTokenScalarQuotedSingle
import io.foxcapades.lib.k.yaml.util.*

@OptIn(ExperimentalUnsignedTypes::class)
internal fun YAMLStreamTokenizerImpl.parseSingleQuotedStringToken() {
  contentBuffer1.clear()
  trailingWSBuffer.clear()
  trailingNLBuffer.clear()

  val indent = this.indent
  val start = position.mark()

  lineContentIndicator = LineContentIndicatorContent

  // Skip the leading `'` character.
  skipASCII(this.reader, this.position)

  while (true) {
    reader.cache(1)

    when {
      reader.isApostrophe() -> {
        skipASCII(this.reader, this.position)

        collapseTrailingWhitespaceAndNewlinesIntoBuffer(contentBuffer1, trailingNLBuffer, trailingWSBuffer)

        reader.cache(1)

        // If we have 2 apostrophe characters in a row, then we have an escape
        // sequence, and we should not stop reading the string here.
        //
        // Instead, append a single apostrophe to the content buffer and move
        // on.
        if (reader.isApostrophe()) {
          contentBuffer1.claimASCII(this.reader, this.position)
          continue
        }

        tokens.push(newSingleQuotedStringToken(contentBuffer1.popToArray(), indent, start))
        return
      }
      reader.isBlank()      -> trailingWSBuffer.claimASCII(this.reader, this.position)
      reader.isAnyBreak()   -> {
        trailingWSBuffer.clear()
        trailingNLBuffer.claimNewLine(this.reader, this.position)
      }
      reader.isEOF()        -> {
        emitInvalidToken("incomplete string token; unexpected stream end", start)
        return
      }
      else                  -> {
        collapseTrailingWhitespaceAndNewlinesIntoBuffer(contentBuffer1, trailingNLBuffer, trailingWSBuffer)
        contentBuffer1.claimUTF8(this.reader, this.position)
      }
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
}

@Suppress("NOTHING_TO_INLINE")
@OptIn(ExperimentalUnsignedTypes::class)
private inline fun YAMLStreamTokenizerImpl.newSingleQuotedStringToken(
  value:  UByteArray,
  indent: UInt,
  start:  SourcePosition,
  end:    SourcePosition = position.mark()
) =
  YAMLTokenScalarQuotedSingle(UByteString(value), start, end, indent, popWarnings())