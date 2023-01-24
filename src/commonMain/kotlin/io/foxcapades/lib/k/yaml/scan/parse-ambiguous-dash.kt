package io.foxcapades.lib.k.yaml.scan

import io.foxcapades.lib.k.yaml.token.YAMLTokenDocumentStart
import io.foxcapades.lib.k.yaml.token.YAMLTokenScalarPlain
import io.foxcapades.lib.k.yaml.token.YAMLTokenSequenceEntry
import io.foxcapades.lib.k.yaml.util.isDash


/**
 * # Fetch Ambiguous Dash Token
 *
 * Determines the meaning of an encountered dash (`-`) character and emits the
 * correct token based on the context.
 *
 * If the dash character is followed immediately by 2 additional dash characters
 * then a blank space, line break, or the EOF, this method will cause a
 * [document start token][YAMLTokenDocumentStart] to be emitted.
 *
 * If the dash character is followed immediately by a blank space, a line break,
 * or the end of the input stream, this method will cause a
 * [sequence entry token][YAMLTokenSequenceEntry] to be emitted.
 *
 * Else, if the dash character is followed immediately by anything else, this
 * method will cause a [plain scalar token][YAMLTokenScalarPlain] to be emitted.
 *
 * @since 0.1.0
 * @author Elizabeth Paige Harper - https://github.com/foxcapades
 */
internal fun YAMLStreamTokenizerImpl.parseAmbiguousDashToken() {
  // TODO:
  //   | if we are in a flow context and we encounter "- ", what the fudge do
  //   | we do with that?

  // Cache the next 3 characters in the buffer to accommodate the size of the
  // document start token `^---(?:\s|$)`
  this.reader.cache(4)

  // If we have `-(?:\s|$)`
  if (this.reader.isBlankAnyBreakOrEOF(1))
    return this.fetchSequenceEntryIndicator()

  // See if we are at the start of the line and next up is `---(?:\s|$)`
  if (
    this.atStartOfLine
    && this.reader.isDash(1)
    && this.reader.isDash(2)
    && this.reader.isBlankAnyBreakOrEOF(3)
  )
    return this.fetchDocumentStartToken()

  return this.parsePlainScalar()
}
