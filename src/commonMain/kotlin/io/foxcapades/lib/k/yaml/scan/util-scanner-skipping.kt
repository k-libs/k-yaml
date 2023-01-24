package io.foxcapades.lib.k.yaml.scan

import io.foxcapades.lib.k.yaml.util.*

internal fun skipASCII(from: UByteSource, position: SourcePositionTracker, count: Int = 1) {
  from.skip(count)
  position.incPosition(count.toUInt())
}

internal fun YAMLScannerImpl.skipUTF8(count: Int = 1) {
  reader.skipCodepoints(count)
  position.incPosition(count.toUInt())
}

/**
 * Skips over `<SPACE>` and `<TAB>` characters in the reader buffer,
 * incrementing the position tracker as it goes.
 *
 * @return The number of blank characters that were skipped.
 */
internal fun YAMLScannerImpl.skipBlanks(): Int {
  var out = 0

  reader.cache(1)
  while (reader.isBlank()) {
    skipASCII(this.reader, this.position)
    reader.cache(1)
    out++
  }

  return out
}

internal fun skipNewLine(from: UByteSource, position: SourcePositionTracker) {
  when {
    from.isCRLF() -> {
      from.skip(2)
      position.incLine(2u)
    }

    from.isLineFeedOrCarriageReturn() -> {
      from.skip(1)
      position.incLine()
    }

    from.isNextLine() -> {
      from.skip(2)
      position.incLine()
    }

    from.isLineOrParagraphSeparator() -> {
      from.skip(3)
      position.incLine()
    }

    else -> {
      throw IllegalStateException(
        "called skipNewLine(UByteSource, SourcePositionTracker) and provided a UByteSource whose next character is " +
          "not a new line"
      )
    }
  }
}


internal fun YAMLScannerImpl.skipToNextToken() {
  while (true) {
    reader.cache(1)

    when {
      reader.isSpace() -> {
        skipASCII(reader, position)
        if (lineContentIndicator != LineContentIndicatorContent)
          indent++
      }

      reader.isTab() -> {
        if (lineContentIndicator.haveHardContent) {
          skipASCII(reader, position)
        } else if (inFlow || lineContentIndicator == LineContentIndicatorBlanksAndIndicators) {
          skipASCII(reader, position)
          indent++
        } else {
          break
        }
      }

      reader.isAnyBreak() -> {
        skipNewLine(this.reader, this.position)
        lineContentIndicator = LineContentIndicatorBlanksOnly
        indent = 0u
      }

      reader.isEOF()      -> {
        break
      }

      else                -> {
        break
      }
    }
  }
}

internal fun YAMLScannerImpl.skipUntilBlankBreakOrEOF() {
  while (true) {
    reader.cache(1)

    if (reader.isBlankAnyBreakOrEOF())
      return
    else
      skipUTF8()
  }
}

/**
 * # Skip Until Comment, Break, or EOF
 *
 * Skips over reader buffer contents until it reaches the start of a comment,
 * a line break, or the end of the input stream.
 *
 * This method will return a mark for the position 1 after the last "content"
 * character in the line, ignoring trailing blank characters.
 *
 * ## Comment
 *
 * For the case when skipping ends due to a comment being encountered:
 *
 * ```
 * ^ = Cursor Position
 * * = Returned Mark Position
 *
 * Before: asdf asdf asdfasdf     # foo
 *         ^
 *
 * After:  asdf asdf asdfasdf     # foo
 *                           *    ^
 * ```
 *
 * ## Line Break
 *
 * For the case when skipping ends due to a line break being encountered:
 *
 * ```
 * ^ = Cursor Position
 * * = Returned Mark Position
 *
 * Before: asdf asdf asdfasdf     <LINE-BREAK>
 *         ^
 *
 * After:  asdf asdf asdfasdf     <LINE-BREAK>
 *                           *    ^
 * ```
 *
 * ## EOF
 *
 * For the case when skipping ends due to the EOF being encountered:
 *
 * ```
 * ^ = Cursor Position
 * * = Returned Mark Position
 *
 * Before: asdf asdf asdfasdf     <EOF>
 *         ^
 *
 * After:  asdf asdf asdfasdf     <EOF>
 *                           *    ^
 * ```
 *
 * @return A source position mark that is one after the last "content" character
 * in the line, excluding any trailing whitespaces.
 */
internal fun YAMLScannerImpl.skipUntilCommentBreakOrEOF(): SourcePosition {
  var trailingWhitespaceCount = 0
  val endMark: SourcePosition

  while (true) {
    reader.cache(1)

    when {
      // If we've encountered `<WS>#` then we can end because we've found the
      // start of a comment.
      //
      // If we've encountered a line break or the EOF, then we can end because
      // it's the end of the junk token.
      reader.isPound() && trailingWhitespaceCount > 0 || reader.isAnyBreakOrEOF() -> {
        endMark = position.mark(modIndex = -trailingWhitespaceCount, modColumn = -trailingWhitespaceCount)
        break
      }

      // If we've encountered a whitespace character, just keep a counter of it
      // because it may be trailing whitespace (which we want to "ignore").
      reader.isBlank() -> {
        trailingWhitespaceCount++
        skipASCII(this.reader, this.position)
      }

      // Else, it's a junk "content" character
      else -> {
        // Reset our trailing whitespace counter because we hit something that
        // is not a whitespace, meaning the whitespace we've counted so far is
        // not "trailing" whitespace, but just some space in the middle of the
        // line.
        trailingWhitespaceCount = 0

        // Skip over the character (which is of an unknown UTF-8 width at this
        // point)
        skipUTF8()
      }
    }
  }

  return endMark
}