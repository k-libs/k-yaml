package io.foxcapades.lib.k.yaml.scan

import io.foxcapades.lib.k.yaml.token.YAMLTokenDocumentStart

internal fun YAMLScannerImpl.fetchDocumentStartToken() {
  val start = position.mark()
  skipASCII(this.reader, this.position, 3)
  this.lineContentIndicator = LineContentIndicatorContent
  tokens.push(YAMLTokenDocumentStart(start, position.mark(), popWarnings()))
}
