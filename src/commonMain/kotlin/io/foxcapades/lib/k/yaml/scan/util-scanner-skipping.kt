package io.foxcapades.lib.k.yaml.scan

import io.foxcapades.lib.k.yaml.util.SourcePosition
import io.foxcapades.lib.k.yaml.util.isBlank
import io.foxcapades.lib.k.yaml.util.isPound

//
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