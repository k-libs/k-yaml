package io.foxcapades.lib.k.yaml.scan

import io.foxcapades.lib.k.yaml.token.*
import io.foxcapades.lib.k.yaml.util.SourcePosition
import io.foxcapades.lib.k.yaml.util.UByteString
import io.foxcapades.lib.k.yaml.util.isNsAnchorChar


/**
 * # Fetch Alias Token
 *
 * Parses and queues up a token for a YAML alias.
 *
 * @since 0.1.0
 * @author Elizabeth Paige Harper - https://github.com/foxcapades
 */
@OptIn(ExperimentalUnsignedTypes::class)
internal fun YAMLScannerImpl.fetchAliasToken() {
  val anchorName = this.contentBuffer1;
  val start      = this.position.mark()

  anchorName.clear()

  skipASCII(this.reader, this.position)
  this.reader.cache(1)
  this.lineContentIndicator = LineContentIndicatorContent

  if (reader.isBlankAnyBreakOrEOF()) {
    return this.emitInvalidToken(start, this.warn("incomplete alias token", start))
  }

  while (true) {

    if (this.reader.isBlankAnyBreakOrEOF()) {
      break
    }

    else if (this.reader.isNsAnchorChar()) {
      anchorName.claimUTF8(this.reader, this.position)
    }

    else {
      this.skipUntilBlankBreakOrEOF()
      return this.emitInvalidToken(start, this.warn("invalid or unexpected character while parsing an alias token", start))
    }

    this.reader.cache(1)
  }

  return emitAlias(anchorName.popToArray(), start, this.position.mark())
}

@Suppress("NOTHING_TO_INLINE")
@OptIn(ExperimentalUnsignedTypes::class)
private inline fun YAMLScannerImpl.emitAlias(alias: UByteArray, start: SourcePosition, end: SourcePosition) {
  this.tokens.push(YAMLTokenAlias(UByteString(alias), start, end, this.indent, getWarnings()))
}
