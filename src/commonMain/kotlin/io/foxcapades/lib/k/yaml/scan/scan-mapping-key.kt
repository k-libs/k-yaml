package io.foxcapades.lib.k.yaml.scan

import io.foxcapades.lib.k.yaml.token.YAMLTokenMappingKey


internal fun YAMLScannerImpl.fetchMappingKeyIndicatorToken() {
  this.haveContentOnThisLine = true
  val start = this.position.mark()
  skipASCII(this.reader, this.position)
  this.tokens.push(YAMLTokenMappingKey(start, this.position.mark(), this.getWarnings()))
}
