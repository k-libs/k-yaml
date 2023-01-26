package io.foxcapades.lib.k.yaml.scan

import io.foxcapades.lib.k.yaml.token.YAMLTokenDocumentEnd

internal fun YAMLStreamTokenizerImpl.fetchDocumentEndToken() {
  val start = this.position.mark()
  skipASCII(this.buffer, this.position, 3)
  this.lineContentIndicator = LineContentIndicatorContent
  tokens.push(YAMLTokenDocumentEnd(start, this.position.mark(), this.popWarnings()))
}
