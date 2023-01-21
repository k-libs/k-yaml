package io.foxcapades.lib.k.yaml.scan

import io.foxcapades.lib.k.yaml.util.isDash


internal fun YAMLScannerImpl.fetchAmbiguousDashToken() {
  // If we've hit a `-` character then we could be at the start of a block
  // sequence entry, a document start, a plain scalar, or junk

  // TODO:
  //   | if we are in a flow context and we encounter "- ", what the fudge do
  //   | we do with that?

  // Cache the next 3 characters in the buffer to accommodate the size of the
  // document start token `^---(?:\s|$)`
  reader.cache(4)

  // If we have `-(?:\s|$)`
  if (reader.isBlankAnyBreakOrEOF(1))
    return fetchBlockEntryIndicatorToken()

  // See if we are at the start of the line and next up is `--(?:\s|$)`
  if (atStartOfLine && reader.isDash(1) && reader.isDash(2) && reader.isBlankAnyBreakOrEOF(3)) {
    return fetchDocumentStartToken()
  }

  fetchPlainScalar()
}
