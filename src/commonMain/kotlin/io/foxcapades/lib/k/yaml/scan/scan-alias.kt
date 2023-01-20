package io.foxcapades.lib.k.yaml.scan

/**
 * # Fetch Alias Token
 *
 * Right now the reader is on an ASCII Asterisk character (`*`) in a token start
 * position.
 */
internal fun YAMLScannerImpl.fetchAliasToken() {
  // Record the start position of our token.
  val tokenStart = position.mark()

  // Skip over the `*` character
  skipASCII()

  // Try and cache the next character in the buffer
  cache(1)

  // If the asterisk was immediately followed by a blank, a linebreak or the
  // EOF then we have an incomplete ALIAS token.
  if (haveBlankAnyBreakOrEOF())
    TODO("handle incomplete alias token")


  TODO("Fetch an alias token")
}