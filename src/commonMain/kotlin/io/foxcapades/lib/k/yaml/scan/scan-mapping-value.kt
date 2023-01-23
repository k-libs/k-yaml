package io.foxcapades.lib.k.yaml.scan

import io.foxcapades.lib.k.yaml.token.YAMLTokenMappingValue

internal fun YAMLScannerImpl.fetchMappingValueIndicatorToken() {
  this.haveContentOnThisLine = true
  val start = this.position.mark()
  skipASCII(this.reader, this.position)
  this.tokens.push(YAMLTokenMappingValue(start, this.position.mark(), this.getWarnings()))
}
