package io.foxcapades.lib.k.yaml.scan

import io.foxcapades.lib.k.yaml.util.isBlank
import io.foxcapades.lib.k.yaml.util.takeCodepointFrom


@OptIn(ExperimentalUnsignedTypes::class)
internal fun YAMLScannerImpl.fetchCommentToken() {
  contentBuffer1.clear()
  trailingWSBuffer.clear()

  val startMark = position.mark()

  skipASCII()
  eatBlanks()

  // The comment line may be empty
  if (reader.isAnyBreakOrEOF()) {
    tokens.push(newCommentToken(UByteArray(0), startMark, startMark.copy(1, 0, 1)))
    return
  }

  while (true) {
    reader.cache(1)

    if (reader.isBlank()) {
      trailingWSBuffer.takeFrom(reader, 1)
      position.incPosition()
    } else if (reader.isAnyBreakOrEOF()) {
      break
    } else {
      while (trailingWSBuffer.isNotEmpty)
        contentBuffer1.push(trailingWSBuffer.pop())

      contentBuffer1.takeCodepointFrom(reader)
      position.incPosition()
    }
  }

  tokens.push(newCommentToken(
    contentBuffer1.popToArray(),
    startMark,
    position.mark(modIndex = -trailingWSBuffer.size, modColumn = -trailingWSBuffer.size)
  ))
}
