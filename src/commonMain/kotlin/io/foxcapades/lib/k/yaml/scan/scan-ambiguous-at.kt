package io.foxcapades.lib.k.yaml.scan

import io.foxcapades.lib.k.yaml.err.YAMLScannerException

internal fun YAMLScannerImpl.fetchAmbiguousAtToken() {
  val start = this.position.mark()
  skipASCII(this.reader, this.position)
  throw YAMLScannerException(
    "illegal token: no token may start with the reserved \"at\" (`@`) indicator character",
    start
  )
}
