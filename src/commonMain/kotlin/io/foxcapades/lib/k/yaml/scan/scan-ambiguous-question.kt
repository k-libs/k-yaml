package io.foxcapades.lib.k.yaml.scan

internal fun YAMLScannerImpl.fetchAmbiguousQuestionToken() {
  // If:      we are in a block context
  //   If:      the question mark is followed by a space, newline, or EOF, it is
  //            a mapping key indicator
  //   Else If: the question mark is followed by anything else, it is a plain
  //            scalar
  // Else If: we are in a flow context
  //   ????????

  // TODO:
  //   | This behavior does not take into account whether we are in a flow
  //   | context or not when making the following determinations.  Determine if
  //   | handling needs to be different for flow contexts and update if
  //   | necessary

  reader.cache(2)

  return if (reader.isBlankAnyBreakOrEOF(1))
    fetchMappingKeyIndicatorToken()
  else
    fetchPlainScalar()
}
