package io.foxcapades.lib.k.yaml.scan

import io.foxcapades.lib.k.yaml.token.YAMLTokenDocumentEnd

internal fun YAMLScannerImpl.fetchDocumentEndToken() {
  // We have content on this line.
  this.haveContentOnThisLine = true

  val start = this.position.mark()
  skipASCII(this.reader, this.position, 3)
  tokens.push(YAMLTokenDocumentEnd(start, this.position.mark(), this.getWarnings()))
}
