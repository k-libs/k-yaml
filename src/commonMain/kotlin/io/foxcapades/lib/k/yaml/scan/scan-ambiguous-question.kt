package io.foxcapades.lib.k.yaml.scan

internal fun YAMLScannerImpl.fetchAmbiguousQuestionToken() {
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
