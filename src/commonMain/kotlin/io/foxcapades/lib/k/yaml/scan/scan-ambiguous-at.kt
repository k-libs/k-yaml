package io.foxcapades.lib.k.yaml.scan

internal fun YAMLScannerImpl.fetchAmbiguousAtToken() {
  val start = position.mark()
  skipUntilBlankBreakOrEOF()
  val end = position.mark()
  warn("illegal token: no token may start with the at ('@') character", start, end)
  tokens.push(newInvalidToken(start, end))
}
