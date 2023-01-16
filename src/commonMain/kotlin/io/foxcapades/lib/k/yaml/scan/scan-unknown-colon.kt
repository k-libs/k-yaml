package io.foxcapades.lib.k.yaml.scan

internal fun YAMLScanner.fetchAmbiguousColonToken() {
  // So we've hit a colon character.  If it is followed by a space, linebreak
  // or EOF then it is a mapping value indicator token.  If it is followed by
  // anything else then it is the start of a plain scalar token.

  // Cache the character after the colon in the buffer.
  cache(2)

  // If we are in a flow, then a colon automatically means value separator.
  //
  // If we are not in a flow, then the colon is only a value separator if it is
  // followed by a blank, a line break, or the EOF
  if (!(inFlow || haveBlankAnyBreakOrEOF(1)))
    return fetchPlainScalar()

  // Record the start position for our token (the position of the colon
  // character)
  val start = position.mark()

  // Skip over the colon character in the stream
  skipASCII()

  // Record the end position for our token (the position immediately after the
  // colon character)
  val end = position.mark()

  // Generate and queue up the token
  tokens.push(newMappingValueIndicatorToken(start, end))
}
