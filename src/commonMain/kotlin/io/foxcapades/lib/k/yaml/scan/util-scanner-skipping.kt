package io.foxcapades.lib.k.yaml.scan

import io.foxcapades.lib.k.yaml.util.*
import io.foxcapades.lib.k.yaml.util.isBlank
import io.foxcapades.lib.k.yaml.util.isPound

@Deprecated("use the other one.")
internal fun YAMLScannerImpl.skipASCII(count: Int = 1) {
  reader.skip(count)
  position.incPosition(count.toUInt())
}

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
internal fun YAMLScannerImpl.eatBlanks(): Int {
  var out = 0

  reader.cache(1)
  while (reader.isBlank()) {
    skipASCII()
    reader.cache(1)
    out++
  }

  return out
}

internal fun YAMLScannerImpl.skipBlanks() {
  this.reader.cache(1)
  while (this.reader.isBlank()) {
    this.skipASCII()
    this.reader.cache(1)
  }
}

internal fun YAMLScannerImpl.skipLine() {
  reader.cache(4)

  if (reader.isCRLF()) {
    skipLine(NL.CRLF)
  } else if (reader.isCarriageReturn()) {
    skipLine(NL.CR)
  } else if (reader.isLineFeed()) {
    skipLine(NL.LF)
  } else if (reader.isNextLine()) {
    skipLine(NL.NEL)
  } else if (reader.isLineSeparator()) {
    skipLine(NL.LS)
  } else if (reader.isParagraphSeparator()) {
    skipLine(NL.PS)
  } else {
    throw IllegalStateException("called #skipLine() when the reader was not on a newline character")
  }
}

internal fun YAMLScannerImpl.skipToNextToken() {
  // TODO:
  //   | This method needs to differentiate between tabs and spaces when
  //   | slurping up those delicious, delicious bytes.
  //   |
  //   | This is because TAB characters are not permitted as part of
  //   | indentation.
  //   |
  //   | If we choose to warn about tab characters rather than throwing an
  //   | error, we need to determine the width of the tab character so as to
  //   | keep the column index correct...

  while (true) {
    reader.cache(1)

    when {
      // We found the end of the stream.
      reader.isSpace()    -> {
        skipASCII()
      }

      reader.isAnyBreak() -> {
        skipLine()
        haveContentOnThisLine = false
      }

      reader.isEOF()      -> {
        break
      }

      else                -> {
        haveContentOnThisLine = true
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
        skipASCII()
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