package io.foxcapades.lib.k.yaml.scan

import io.foxcapades.lib.k.yaml.token.YAMLTokenAnchor
import io.foxcapades.lib.k.yaml.util.UByteString
import io.foxcapades.lib.k.yaml.util.isNsAnchorChar

/**
 * # Fetch Anchor Token
 *
 * Parses and emits a token for a YAML anchor.
 *
 * @since 0.1.0
 * @author Elizabeth Paige Harper - https://github.com/foxcapades
 */
@OptIn(ExperimentalUnsignedTypes::class)
internal fun YAMLScannerImpl.fetchAnchorToken() {
  val anchorName = this.contentBuffer1
  val start    = this.position.mark()

  anchorName.clear()

  skipASCII(this.reader, this.position)
  this.lineContentIndicator = LineContentIndicatorContent
  this.reader.cache(1)

  if (this.reader.isBlankAnyBreakOrEOF()) {
    emitInvalidToken("incomplete anchor token", start)
    return
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
      emitInvalidToken("invalid or unexpected character while parsing an anchor token", start)
      return
    }

    this.reader.cache(1)
  }

  this.tokens.push(YAMLTokenAnchor(UByteString(anchorName.popToArray()), start, this.position.mark(), this.indent, this.popWarnings()))
}
