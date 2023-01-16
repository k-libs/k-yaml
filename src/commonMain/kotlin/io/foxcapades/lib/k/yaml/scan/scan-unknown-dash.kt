package io.foxcapades.lib.k.yaml.scan

internal fun YAMLScanner.fetchAmbiguousDashToken() {
  // If we've hit a `-` character then we could be at the start of a block
  // sequence entry, a document start, a plain scalar, or junk

  // TODO:
  //   | if we are in a flow context and we encounter "- ", what the fudge do
  //   | we do with that?

  // Cache the next 3 characters in the buffer to accommodate the size of the
  // document start token `^---(?:\s|$)`
  cache(4)

  // If we have `-(?:\s|$)`
  if (haveBlankAnyBreakOrEOF(1))
    return fetchBlockEntryIndicatorToken()

  // See if we are at the start of the line and next up is `--(?:\s|$)`
  if (atStartOfLine && haveDash(1) && haveDash(2) && haveBlankAnyBreakOrEOF(3)) {
    return fetchDocumentStartToken()
  }

  fetchPlainScalar()
}

private fun YAMLScanner.fetchBlockEntryIndicatorToken() {
  val start = position.mark()
  skipASCII()
  tokens.push(newSequenceEntryIndicatorToken(start, position.mark()))
}

private fun YAMLScanner.fetchDocumentStartToken() {
  val start = position.mark()
  skipASCII(3)
  tokens.push(newDocumentStartToken(start, position.mark()))
}
