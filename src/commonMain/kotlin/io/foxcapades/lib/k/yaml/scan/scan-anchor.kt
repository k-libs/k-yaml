package io.foxcapades.lib.k.yaml.scan

import io.foxcapades.lib.k.yaml.token.YAMLToken
import io.foxcapades.lib.k.yaml.token.YAMLTokenDataAnchor
import io.foxcapades.lib.k.yaml.token.YAMLTokenTypeAnchor
import io.foxcapades.lib.k.yaml.util.SourcePosition
import io.foxcapades.lib.k.yaml.util.isNsAnchorChar
import io.foxcapades.lib.k.yaml.util.takeCodepointFrom


@OptIn(ExperimentalUnsignedTypes::class)
internal fun YAMLScannerImpl.fetchAnchorToken() {
  haveContentOnThisLine = true

  contentBuffer1.clear()

  val start = position.mark()

  skipASCII(reader, position)
  reader.cache(1)

  if (reader.isBlankAnyBreakOrEOF()) {
    val end = position.mark()
    warn("incomplete anchor token", start, end)
    tokens.push(newInvalidToken(start, end))
    return
  }

  while (true) {
    if (reader.isBlankAnyBreakOrEOF()) {
      break
    } else if (reader.isNsAnchorChar()) {
      contentBuffer1.takeCodepointFrom(reader)
      position.incPosition()
    } else {
      val end = position.mark()
      warn("invalid or unexpected character while parsing an anchor token", start, end)
      tokens.push(newInvalidToken(start, end))
      return
    }

    reader.cache(1)
  }

  tokens.push(newAnchorToken(contentBuffer1.popToArray(), start, position.mark()))
}

@OptIn(ExperimentalUnsignedTypes::class)
private fun YAMLScannerImpl.newAnchorToken(anchor: UByteArray, start: SourcePosition, end: SourcePosition) =
  YAMLToken(YAMLTokenTypeAnchor, YAMLTokenDataAnchor(anchor), start, end, getWarnings())
