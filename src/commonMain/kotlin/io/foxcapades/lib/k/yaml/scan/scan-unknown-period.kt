package io.foxcapades.lib.k.yaml.scan

internal fun YAMLScanner.fetchAmbiguousPeriodToken() {
  cache(4)

  if (atStartOfLine && havePeriod(1) && havePeriod(2) && haveBlankAnyBreakOrEOF(3))
    fetchDocumentEndToken()
  else
    fetchPlainScalar()
}

private fun YAMLScanner.fetchDocumentEndToken() {
  val start = position.mark()
  skipASCII(3)
  tokens.push(newDocumentEndToken(start, position.mark()))
}


