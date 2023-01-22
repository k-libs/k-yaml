package io.foxcapades.lib.k.yaml.scan

import io.foxcapades.lib.k.yaml.err.YAMLScannerException

internal fun YAMLScannerImpl.fetchAmbiguousGraveToken() {
  val start = this.position.mark()
  skipASCII(this.reader, this.position)
  throw YAMLScannerException(
    "illegal token: no token may start with the reserved \"grave accent\" ('`') indicator character",
    start
  )
}
