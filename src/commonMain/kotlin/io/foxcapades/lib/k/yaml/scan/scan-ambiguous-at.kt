package io.foxcapades.lib.k.yaml.scan

/**
 * # Fetch Ambiguous "At" Character
 *
 * Parses the invalid `@` character as the beginning of a plain scalar value and
 * emits a warning about the invalid character.
 *
 * @since 0.1.0
 * @author Elizabeth Paige Harper - https://github.com/foxcapades
 */
internal fun YAMLScannerImpl.fetchAmbiguousAtToken() {
  val start = this.position.mark()
  this.warn("illegal character: the \"at\" ('@') character is reserved in YAML and must not begin any token.", start)
  return this.fetchPlainScalar()
}
