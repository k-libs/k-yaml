package io.foxcapades.lib.k.yaml.scan

internal fun YAMLScannerImpl.fetchAmbiguousAtToken() {
  // TODO: should the bad token be the whole line or just until the next blank?
  //       if this is an "error" case, maybe an exception should be thrown?
  tokens.push(newInvalidToken(position.mark(), skipUntilCommentBreakOrEOF()))
}
