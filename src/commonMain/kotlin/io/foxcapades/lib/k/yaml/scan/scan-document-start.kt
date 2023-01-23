package io.foxcapades.lib.k.yaml.scan

import io.foxcapades.lib.k.yaml.token.YAMLTokenDocumentStart

internal fun YAMLScannerImpl.fetchDocumentStartToken() {
  // We have content on this line.
  this.haveContentOnThisLine = true

  val start = position.mark()
  skipASCII(this.reader, this.position, 3)
  tokens.push(YAMLTokenDocumentStart(start, position.mark(), getWarnings()))
}
