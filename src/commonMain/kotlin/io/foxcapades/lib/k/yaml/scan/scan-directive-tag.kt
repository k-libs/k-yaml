package io.foxcapades.lib.k.yaml.scan

import io.foxcapades.lib.k.yaml.util.SourcePosition


internal fun YAMLScanner.fetchTagDirectiveToken(startMark: SourcePosition) {
  TODO("fetch tag directive token")
}

private fun YAMLScanner.fetchInvalidTagDirectiveToken(startMark: SourcePosition) {
  warn("malformed %TAG token", startMark)
  TODO("finish off the token")
}

