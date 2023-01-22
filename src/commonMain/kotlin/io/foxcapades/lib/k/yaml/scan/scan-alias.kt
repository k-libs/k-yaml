package io.foxcapades.lib.k.yaml.scan

import io.foxcapades.lib.k.yaml.token.YAMLToken
import io.foxcapades.lib.k.yaml.token.YAMLTokenDataAlias
import io.foxcapades.lib.k.yaml.token.YAMLTokenTypeAlias
import io.foxcapades.lib.k.yaml.util.SourcePosition
import io.foxcapades.lib.k.yaml.util.isNsAnchorChar
import io.foxcapades.lib.k.yaml.util.takeCodepointFrom


@OptIn(ExperimentalUnsignedTypes::class)
internal fun YAMLScannerImpl.fetchAliasToken() {
  contentBuffer1.clear()

  val start = position.mark()

  skipASCII()
  reader.cache(1)

  if (reader.isBlankAnyBreakOrEOF()) {
    val end = position.mark()
    warn("incomplete alias token", start, end)
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
      warn("invalid or unexpected character while parsing an alias token", start, end)
      tokens.push(newInvalidToken(start, end))
      return
    }

    reader.cache(1)
  }

  tokens.push(newAliasToken(contentBuffer1.popToArray(), start, position.mark()))
}

@OptIn(ExperimentalUnsignedTypes::class)
private fun YAMLScannerImpl.newAliasToken(alias: UByteArray, start: SourcePosition, end: SourcePosition) =
  YAMLToken(YAMLTokenTypeAlias, YAMLTokenDataAlias(alias), start, end, getWarnings())
