package io.foxcapades.lib.k.yaml.scan

import io.foxcapades.lib.k.yaml.bytes.A_SPACE
import io.foxcapades.lib.k.yaml.util.*

@OptIn(ExperimentalUnsignedTypes::class)
internal fun YAMLScannerImpl.fetchPlainScalar() {
  contentBuffer1.clear()
  contentBuffer2.clear()
  trailingWSBuffer.clear()
  trailingNLBuffer.clear()

  val startMark = position.mark()

  // Rolling end position of this scalar value (don't create a mark every time
  // because that's a new class instantiation per stream character).
  val endPosition = position.copy()

  val startOfLinePosition = position.copy()

  while (true) {
    reader.cache(1)

    if (reader.isEOF()) {
      if (contentBuffer2.size == 1) {
        if (contentBuffer2.uIsSquareClose()) {
          if (contentBuffer1.isNotEmpty)
            tokens.push(newPlainScalarToken(contentBuffer1.popToArray(), startMark, endPosition.mark()))

          emitFlowSequenceEndToken(startOfLinePosition.mark(), startOfLinePosition.mark(1, 0, 1))
          return
        } else if (contentBuffer2.uIsCurlyClose()) {
          if (contentBuffer1.isNotEmpty)
            tokens.push(newPlainScalarToken(contentBuffer1.popToArray(), startMark, endPosition.mark()))

          emitFlowMappingEndToken(startOfLinePosition.mark(), startOfLinePosition.mark(1, 0, 1))
          return
        }
      }

      collapseNewlinesAndMergeBuffers(endPosition, contentBuffer1, contentBuffer2, trailingWSBuffer, trailingNLBuffer)
      tokens.push(newPlainScalarToken(contentBuffer1.popToArray(), startMark, endPosition.mark()))
      return
    }

    if (reader.isBlank()) {
      trailingWSBuffer.push(reader.pop())
      position.incPosition()
      continue
    }

    if (reader.isAnyBreak()) {
      // Here we examine if the entire line last line we just ate (not counting
      // whitespaces) was a closing square or curly bracket character.
      //
      // This is handled as a special case just for trying to make sense of an
      // invalid, multiline plain scalar value.  In this special case,
      // regardless of whether we are in a flow, we will consider the closing
      // bracket a flow end token.
      //
      // In addition to this, because the closing brace will have already been
      // "consumed" from the reader, we will need to also emit the appropriate
      // token.
      //
      // This logic and comment appear twice in this file and nowhere else.
      if (contentBuffer2.size == 1) {
        if (contentBuffer2.uIsSquareClose()) {
          if (contentBuffer1.isNotEmpty)
            tokens.push(newPlainScalarToken(contentBuffer1.popToArray(), startMark, endPosition.mark()))

          emitFlowSequenceEndToken(startOfLinePosition.mark(), startOfLinePosition.mark(1, 0, 1))
          return
        } else if (contentBuffer2.uIsCurlyClose()) {
          if (contentBuffer1.isNotEmpty)
            tokens.push(newPlainScalarToken(contentBuffer1.popToArray(), startMark, endPosition.mark()))

          emitFlowMappingEndToken(startOfLinePosition.mark(), startOfLinePosition.mark(1, 0, 1))
          return
        }
      }

      trailingWSBuffer.clear()
      collapseNewlinesAndMergeBuffers(endPosition, contentBuffer1, contentBuffer2, trailingWSBuffer, trailingNLBuffer)
      trailingNLBuffer.claimNewLine(reader, position)
      haveContentOnThisLine = false
      continue
    }

    if (reader.isColon()) {
      reader.cache(2)

      // If we are not in a flow mapping, and the colon is followed by a
      // whitespace, then split this plain scalar on the last newline to make
      // the previous plain scalar value, and the new mapping key value.
      //
      // For example, the following:
      // ```
      // hello
      // goodbye: taco
      // ```
      //
      // would become:
      // 1. Plain scalar: "hello"
      // 2. Plain scalar: "goodbye"
      // 3. Mapping value indicator
      // 4. Plain scalar: "taco"
      if (!inFlowMapping && reader.isBlankAnyBreakOrEOF(1)) {
        if (contentBuffer1.isNotEmpty)
          tokens.push(newPlainScalarToken(contentBuffer1.popToArray(), startMark, endPosition.mark()))

        tokens.push(newPlainScalarToken(contentBuffer2.popToArray(), startOfLinePosition.mark(), position.mark()))

        return
      }

      // If we are in a flow mapping, then unlike the block mapping, we A) don't
      // care if there is a space following the colon character, and B) don't
      // want to split the plain scalar on newlines.
      else if (inFlowMapping) {
        trailingWSBuffer.clear()

        collapseNewlinesAndMergeBuffers(endPosition, contentBuffer1, contentBuffer2, trailingWSBuffer, trailingNLBuffer)

        tokens.push(newPlainScalarToken(contentBuffer1.popToArray(), startMark, endPosition.mark()))

        return
      }
    }

    if (reader.isDash() && trailingNLBuffer.isNotEmpty) {
      reader.cache(4)

      if (reader.isBlankAnyBreakOrEOF(1)) {
        tokens.push(newPlainScalarToken(contentBuffer1.popToArray(), startMark, endPosition.mark()))
        return
      }
    }

    if (inFlow && reader.isComma()) {
      collapseNewlinesAndMergeBuffers(endPosition, contentBuffer1, contentBuffer2, trailingWSBuffer, trailingNLBuffer)
      tokens.push(newPlainScalarToken(contentBuffer1.popToArray(), startMark, endPosition.mark()))
      return
    }

    if (inFlowMapping && reader.isCurlyClose()) {
      collapseNewlinesAndMergeBuffers(endPosition, contentBuffer1, contentBuffer2, trailingWSBuffer, trailingNLBuffer)
      tokens.push(newPlainScalarToken(contentBuffer1.popToArray(), startMark, endPosition.mark()))
      return
    }

    if (inFlowSequence && reader.isSquareClose()) {
      collapseNewlinesAndMergeBuffers(endPosition, contentBuffer1, contentBuffer2, trailingWSBuffer, trailingNLBuffer)
      tokens.push(newPlainScalarToken(contentBuffer1.popToArray(), startMark, endPosition.mark()))
      return
    }

    if (atStartOfLine) {
      startOfLinePosition.become(position)

      // If we are in this block, then the last character we saw was a newline
      // character.  This means that we don't need to worry about the contents
      // of the ambiguous buffer inside this if block as that buffer will always
      // be empty.

      if (reader.isDash()) {
        reader.cache(4)

        if (reader.isDash(1) && reader.isDash(2) && reader.isBlankAnyBreakOrEOF(3)) {
          tokens.push(newPlainScalarToken(contentBuffer1.popToArray(), startMark, endPosition.mark()))
          return
        }
      }

      if (reader.isPeriod()) {
        reader.cache (4)

        if (reader.isPeriod(1) && reader.isPeriod(2) && reader.isBlankAnyBreakOrEOF(3)) {
          tokens.push(newPlainScalarToken(contentBuffer1.popToArray(), startMark, endPosition.mark()))
          return
        }
      }

      if (reader.isQuestion()) {
        reader.cache(2)

        if (reader.isBlankAnyBreakOrEOF(1)) {
          tokens.push(newPlainScalarToken(contentBuffer1.popToArray(), startMark, endPosition.mark()))
          return
        }
      }

      if (
        reader.isPercent()
        || reader.isSquareOpen()
        || reader.isCurlyOpen()
      ) {
        tokens.push(newPlainScalarToken(contentBuffer1.popToArray(), startMark, endPosition.mark()))
        return
      }
    } // end if (atStartOfLine)

    // Catch-all: append it to the ambiguous buffer

    // If we have any trailing whitespaces, then append them to the ambiguous
    // buffer because we just hit a non-blank character
    if (trailingNLBuffer.isEmpty)
      while (trailingWSBuffer.isNotEmpty)
        contentBuffer2.push(trailingWSBuffer.pop())

    contentBuffer2.claimUTF8()
  }
}

private fun YAMLScannerImpl.collapseNewlinesAndMergeBuffers(
  endPosition: SourcePositionTracker,
  to: UByteBuffer,
  from: UByteBuffer,
  spaces: UByteBuffer,
  newLines: UByteBuffer,
) {
  if (from.isEmpty)
    return

  if (newLines.isNotEmpty) {

    if (newLines.size == 1) {
      to.push(A_SPACE)
      newLines.skipNewLine(endPosition)
    } else {
      newLines.skipNewLine(endPosition)
      while (newLines.isNotEmpty)
        to.claimNewLine(newLines, endPosition)
    }

  } else {
    while (spaces.isNotEmpty) {
      to.push(spaces.pop())
      endPosition.incPosition()
    }
  }

  while (from.isNotEmpty) {
    to.push(from.pop())
    endPosition.incPosition()
  }
}
