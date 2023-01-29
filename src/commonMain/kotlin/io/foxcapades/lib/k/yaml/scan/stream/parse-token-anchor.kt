package io.foxcapades.lib.k.yaml.scan.stream

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
internal fun YAMLStreamTokenizerImpl.parseAnchorToken() {
  val anchorName = this.contentBuffer1
  val start    = this.position.mark()

  anchorName.clear()

  skipASCII(this.buffer, this.position)
  this.lineContentIndicator = LineContentIndicatorContent
  this.buffer.cache(1)

  if (this.buffer.isBlankAnyBreakOrEOF()) {
    emitInvalidToken("incomplete anchor token", start)
    return
  }

  while (true) {
    if (this.buffer.isBlankAnyBreakOrEOF()) {
      break
    }

    else if (this.buffer.isNsAnchorChar()) {
      anchorName.claimUTF8(this.buffer, this.position)
    }

    else {
      this.skipUntilBlankBreakOrEOF()
      emitInvalidToken("invalid or unexpected character while parsing an anchor token", start)
      return
    }

    this.buffer.cache(1)
  }

  this.tokens.push(YAMLTokenAnchor(UByteString(anchorName.popToArray()), start, this.position.mark(), this.indent, this.popWarnings()))
}
