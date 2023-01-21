package io.foxcapades.lib.k.yaml.scan

internal fun YAMLScannerImpl.fetchAmbiguousGraveToken() {
  val start = position.mark()
  skipUntilBlankBreakOrEOF()
  val end = position.mark()
  warn("illegal token: no token may start with the backtick/grave ('`') character", start, end)
  tokens.push(newInvalidToken(start, end))
}
