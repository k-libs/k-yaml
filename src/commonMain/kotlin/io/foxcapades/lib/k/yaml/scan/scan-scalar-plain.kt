package io.foxcapades.lib.k.yaml.scan

import io.foxcapades.lib.k.yaml.bytes.A_SPACE
import io.foxcapades.lib.k.yaml.util.SourcePositionTracker
import io.foxcapades.lib.k.yaml.util.UByteBuffer
import io.foxcapades.lib.k.yaml.util.utf8Width


@OptIn(ExperimentalUnsignedTypes::class)
internal fun YAMLScanner.fetchPlainScalar() {
  val startMark = position.mark()

  // Rolling end position of this scalar value (don't create a mark every time
  // because that's a new class instantiation per stream character).
  val endPosition = position.copy()

  val startOfLinePosition = position.copy()

  // TODO:
  //  | these things should be class properties so as to be reusable rather
  //  | than creating 4 hecking buffer instances for every plain scalar token
  //  | we scan.
  val confirmedBuffer = UByteBuffer(1024)
  val ambiguousBuffer = UByteBuffer(1024)
  val trailingWS      = UByteBuffer(16)
  val trailingNL      = UByteBuffer(4)

  while (true) {
    reader.cache(1)

    if (haveEOF()) {
      collapseNewlinesAndMergeBuffers(endPosition, confirmedBuffer, ambiguousBuffer, trailingWS, trailingNL)
      tokens.push(newPlainScalarToken(confirmedBuffer.popToArray(), startMark, endPosition.mark()))
      return
    }

    if (haveBlank()) {
      trailingWS.push(reader.pop())
      position.incPosition()
      continue
    }

    if (haveAnyBreak()) {
      trailingWS.clear()

      if (ambiguousBuffer.isNotEmpty)
        collapseNewlinesAndMergeBuffers(endPosition, confirmedBuffer, ambiguousBuffer, trailingWS, trailingNL)

      trailingNL.claimNewLine(reader.utf8Buffer, position)
      continue
    }

    if (haveColon()) {
      reader.cache(2)

      if (haveBlankAnyBreakOrEOF(1)) {
        if (confirmedBuffer.isNotEmpty)
          tokens.push(newPlainScalarToken(confirmedBuffer.popToArray(), startMark, endPosition.mark()))

        tokens.push(newPlainScalarToken(ambiguousBuffer.popToArray(), startOfLinePosition.mark(), position.mark()))

        return
      }
    }

    if (haveDash() && trailingNL.isNotEmpty) {
      reader.cache(4)

      if (haveBlankAnyBreakOrEOF(1)) {
        tokens.push(newPlainScalarToken(confirmedBuffer.popToArray(), startMark, endPosition.mark()))
        return
      }
    }

    if (atStartOfLine) {
      startOfLinePosition.become(position)

      // If we are in this block, then the last character we saw was a newline
      // character.  This means that we don't need to worry about the contents
      // of the ambiguous buffer inside this if block as that buffer will always
      // be empty.

      if (haveDash()) {
        reader.cache(4)

        if (haveDash(1) && haveDash(2) && haveBlankAnyBreakOrEOF(3)) {
          tokens.push(newPlainScalarToken(confirmedBuffer.popToArray(), startMark, endPosition.mark()))
          return
        }
      }

      if (havePeriod()) {
        cache (4)

        if (havePeriod(1) && havePeriod(2) && haveBlankAnyBreakOrEOF(3)) {
          tokens.push(newPlainScalarToken(confirmedBuffer.popToArray(), startMark, endPosition.mark()))
          return
        }
      }

      if (haveQuestion()) {
        cache(2)

        if (haveBlankAnyBreakOrEOF(1)) {
          tokens.push(newPlainScalarToken(confirmedBuffer.popToArray(), startMark, endPosition.mark()))
          return
        }
      }

      if (
        havePercent()
        || haveSquareOpen()
        || haveCurlyOpen()
      ) {
        tokens.push(newPlainScalarToken(confirmedBuffer.popToArray(), startMark, endPosition.mark()))
        return
      }
    } // end if (atStartOfLine)

    // Catch-all: append it to the ambiguous buffer
    ambiguousBuffer.claimUTF8()
  }
}

private fun YAMLScanner.collapseNewlinesAndMergeBuffers(
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
      endPosition.incPosition()
    } else {
      newLines.skip(newLines.utf8Width())
      to.claimNewLine(newLines, endPosition)
    }

  } else {
    while (spaces.isNotEmpty) {
      to.push(spaces.pop())
      endPosition.incPosition()
    }
  }

  spaces.clear()
  newLines.clear()

  while (from.isNotEmpty) {
    to.push(from.pop())
    endPosition.incPosition()
  }
}
