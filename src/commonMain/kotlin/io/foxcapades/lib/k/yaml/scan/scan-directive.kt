package io.foxcapades.lib.k.yaml.scan

import io.foxcapades.lib.k.yaml.bytes.*
import io.foxcapades.lib.k.yaml.err.YAMLScannerException

/**
 * Attempts to parse the rest of the current line as a directive.
 *
 * At the time this function is called, all we know is that current reader
 * buffer character is `%`.  This could be the start of a YAML directive, a
 * tag directive, or just junk.
 */
internal fun YAMLScannerImpl.fetchDirectiveToken() {
  // Record the start position
  val startMark = position.mark()

  if (!atStartOfLine) {
    tokens.push(newInvalidToken(startMark, skipUntilCommentBreakOrEOF()))
    return
  }

  // Skip the `%` character.
  skipASCII()

  // Attempt to load 5 codepoints into the reader buffer so we can do the
  // following tests.
  cache(5)

  // Nothing more in the buffer?  That means the stream ended on a `%`
  // character which means an invalid token directive.
  if (!haveMoreCharactersAvailable)
    throw YAMLScannerException("stream ended on an incomplete or invalid directive", startMark)

  // See if the next 5 characters are "YAML<WS>"
  if (testReaderOctets(A_UPPER_Y, A_UPPER_A, A_UPPER_M, A_UPPER_L) && haveBlank(4)) {
    skipASCII(5)
    return fetchYAMLDirectiveToken(startMark)
  }

  // See if the next 4 characters are "TAG<WS>"
  if (testReaderOctets(A_UPPER_T, A_UPPER_A, A_UPPER_G) && haveBlank(3)) {
    skipASCII(4)
    return fetchTagDirectiveToken(startMark)
  }

  // If it's not YAML or TAG then it's invalid
  tokens.push(newInvalidToken(startMark, skipUntilCommentBreakOrEOF()))
}
