package io.foxcapades.lib.k.yaml.scan

import io.foxcapades.lib.k.yaml.token.YAMLTokenDocumentStart

internal fun YAMLStreamTokenizerImpl.fetchDocumentStartToken() {
  val start = position.mark()
  skipASCII(this.buffer, this.position, 3)
  this.lineContentIndicator = LineContentIndicatorContent
  tokens.push(YAMLTokenDocumentStart(start, position.mark(), popWarnings()))
}
