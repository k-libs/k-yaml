package io.foxcapades.lib.k.yaml.scan

import io.foxcapades.lib.k.yaml.token.YAMLTokenDocumentEnd

internal fun YAMLScannerImpl.fetchDocumentEndToken() {
  val start = this.position.mark()
  skipASCII(this.reader, this.position, 3)
  this.lineContentIndicator = LineContentIndicatorContent
  tokens.push(YAMLTokenDocumentEnd(start, this.position.mark(), this.getWarnings()))
}
