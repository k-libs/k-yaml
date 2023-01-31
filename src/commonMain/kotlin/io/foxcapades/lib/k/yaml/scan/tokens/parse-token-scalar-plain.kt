package io.foxcapades.lib.k.yaml.scan.tokens

import io.foxcapades.lib.k.yaml.bytes.A_SPACE
import io.foxcapades.lib.k.yaml.token.YAMLTokenScalarPlain
import io.foxcapades.lib.k.yaml.util.*

internal fun YAMLStreamTokenizerImpl.parsePlainScalar() {
  // TODO: catch the case where the first character in the plain scalar is a tab
  //       because that may be illegal
  when {
    this.inFlow  -> this.fetchPlainScalarInFlow()
    else         -> this.fetchPlainScalarInBlock()
  }
}

private fun YAMLStreamTokenizerImpl.fetchPlainScalarInFlow() {
  val start        = this.position.mark()
  val indent       = this.indent
  val bContent     = this.contentBuffer1
  val bTailWS      = this.trailingWSBuffer
  val bTailNL      = this.trailingNLBuffer
  val endPosition  = this.position.copy()
  var lastWasBlank = false

  bContent.clear()
  bTailWS.clear()
  bTailNL.clear()

  lineContentIndicator = LineContentIndicatorContent

  while (true) {
    this.buffer.cache(1)

    when {
      this.buffer.isBlank()    -> {
        // If we already have content on this line
        if (lineContentIndicator == LineContentIndicatorContent) {
          // Then we want to keep it in case we encounter another content
          // character, at which point we will need to insert the whitespaces
          // in between the content characters.
          bTailWS.claimASCII(this.buffer, this.position)
        }

        // else, if we don't have any content on this line yet,
        else {
          // Discard the whitespace because we won't use it for anything whether
          // this line is empty or not.
          skipASCII(this.buffer, this.position)
        }

        lastWasBlank = true
      }

      this.buffer.isAnyBreak() -> {
        // We can go ahead and clear out the trailing space buffer because we
        // won't be using it.
        bTailWS.clear()

        // Eat the newline and append it to our trailing newline buffer.
        bTailNL.claimNewLine(this.buffer, this.position)

        // Now we are starting a new line, which has no content yet.
        this.lineContentIndicator = LineContentIndicatorBlanksOnly
        this.indent = 0u

        lastWasBlank = true
      }

      this.buffer.isColon() -> {
        buffer.cache(2)

        if (
          buffer.isBlankAnyBreakOrEOF(1)
          || buffer.uIsComma(1)
          || buffer.uIsCurlyClose(1)
          || buffer.uIsCurlyOpen(1)
          || buffer.uIsSquareClose(1)
          || buffer.uIsSquareOpen(1)
        ) {
          emitPlainScalar(bContent, indent, start, endPosition.mark())
          lineContentIndicator = LineContentIndicatorContent
          return
        } else {
          claimFlowCharacter(bContent, bTailWS, bTailNL, endPosition)
        }

        lastWasBlank = false
      }

         this.buffer.isComma()
      || this.buffer.isCurlyClose()
      || this.buffer.isSquareClose()
      || this.buffer.isEOF()
      || (lastWasBlank && buffer.isPound())
      -> {
        emitPlainScalar(bContent, indent, start, endPosition.mark())
        lineContentIndicator = LineContentIndicatorContent
        return
      }

      else -> {
        claimFlowCharacter(bContent, bTailWS, bTailNL, endPosition)
        lastWasBlank = false
      }
    }
  }
}

private fun YAMLStreamTokenizerImpl.claimFlowCharacter(
  content: UByteBuffer,
  tailWS:  UByteBuffer,
  tailNL:  UByteBuffer,
  endPos:  SourcePositionTracker,
) {
  while (tailWS.isNotEmpty)
    content.claimASCII(tailWS)

  collapseNewLinesInto(content, tailNL, endPos)

  content.claimUTF8(this.buffer, this.position)
  endPos.become(this.position)
  lineContentIndicator = LineContentIndicatorContent
}

private fun YAMLStreamTokenizerImpl.fetchPlainScalarInBlock() {
  val start        = this.position.mark()
  val tokenIndent  = this.indent
  val bConfirmed   = this.contentBuffer1
  val bAmbiguous   = this.contentBuffer2
  val bTailWS      = this.trailingWSBuffer
  val bTailNL      = this.trailingNLBuffer
  val confirmedEnd = this.position.copy()
  val ambiguousEnd = this.position.copy()
  var lastWasBlank = false

  bConfirmed.clear()
  bAmbiguous.clear()
  bTailWS.clear()
  bTailNL.clear()

  while (true) {
    this.buffer.cache(1)

    if (this.buffer.isSpace()) {
      if (lineContentIndicator.haveAnyContent) {
        bTailWS.claimASCII(this.buffer, this.position)
      } else {
        skipASCII(this.buffer, this.position)
        this.indent++
      }
      lastWasBlank = true
    }

    else if (this.buffer.isTab()) {
      if (lineContentIndicator.haveAnyContent) {
        bTailWS.claimASCII(this.buffer, this.position)
      } else {
        skipASCII(this.buffer, this.position)
      }
      lastWasBlank = true
    }

    else if (this.buffer.isAnyBreak()) {
      // Clear out any trailing blanks since we don't need them
      bTailWS.clear()

      // If we have content on this line
      if (lineContentIndicator == LineContentIndicatorContent) {
        // If this is the first new line character after the start of the plain
        // scalar, then both the bTailNL buffer and the bConfirmed buffer will
        // be empty.  This is fine.

        // Then collapse any new lines into the confirmed buffer
        collapseBuffers(bConfirmed, bTailNL, bAmbiguous)
        confirmedEnd.become(ambiguousEnd)
      }

      // Consume the newline character
      bTailNL.claimNewLine(this.buffer, this.position)

      // We don't have any content on this new line yet
      this.lineContentIndicator = LineContentIndicatorBlanksOnly
      this.indent = 0u
      lastWasBlank = true
    }

    else if (this.indent < tokenIndent) {
      collapseBuffers(bConfirmed, bTailNL, bAmbiguous)
      confirmedEnd.become(ambiguousEnd)
      emitPlainScalar(bConfirmed, tokenIndent, start, confirmedEnd.mark())
      return
    }

    else if (buffer.isColon()) {
      buffer.cache(2)

      if (buffer.isBlankAnyBreakOrEOF(1)) {
        // If the confirmed buffer is not empty, then we are not on the same
        // line we were when we started parsing this plain scalar.  There is a
        // special case here, where if the indent level for this line is 0, then
        // we are to consider the content on this, separate line to be its own
        // separate plain scalar.
        if (bConfirmed.isNotEmpty) {
          emitPlainScalar(bConfirmed, tokenIndent, start, confirmedEnd.mark())

          if (bAmbiguous.isNotEmpty) {
            val subStart = -(bAmbiguous.size + bTailWS.size)
            val subEnd = -bTailWS.size

            emitPlainScalar(
              bAmbiguous,
              tokenIndent,
              this.position.mark(modIndex = subStart, modColumn = subStart),
              this.position.mark(modIndex = subEnd, modColumn = subEnd)
            )
          }
          return
        } else {
          collapseBuffers(bConfirmed, bTailNL, bAmbiguous)
          confirmedEnd.become(ambiguousEnd)
          emitPlainScalar(bConfirmed, tokenIndent, start, confirmedEnd.mark())
          return
        }
      } else {
        bAmbiguous.claimASCII(this.buffer, this.position)
        lineContentIndicator = LineContentIndicatorContent
      }

      lastWasBlank = false
    }

    else if (this.buffer.isDash() && !lineContentIndicator.haveAnyContent) {
      this.buffer.cache(4)

      if (
        (atStartOfLine && buffer.isDash(1) && buffer.isDash(2) && buffer.isBlankAnyBreakOrEOF(3))
        || (this.indent <= tokenIndent && buffer.isBlankAnyBreakOrEOF(1))
      ) {
        collapseBuffers(bConfirmed, bTailNL, bAmbiguous)
        confirmedEnd.become(ambiguousEnd)
        emitPlainScalar(bConfirmed, tokenIndent, start, confirmedEnd.mark())
        return
      }

      bAmbiguous.claimASCII(this.buffer, this.position)
      lineContentIndicator = LineContentIndicatorContent
      lastWasBlank = false
    }

    else if (this.buffer.isPeriod() && this.atStartOfLine) {
      this.buffer.cache(4)

      if (this.buffer.isPeriod(1) && this.buffer.isPeriod(2) && this.buffer.isBlankAnyBreakOrEOF(3)) {
        collapseBuffers(bConfirmed, bTailNL, bAmbiguous)
        confirmedEnd.become(ambiguousEnd)
        emitPlainScalar(bConfirmed, tokenIndent, start, confirmedEnd.mark())
        return
      }

      bAmbiguous.claimASCII(this.buffer, this.position)
      this.lineContentIndicator = LineContentIndicatorContent
      lastWasBlank = false
    }

    else if (
      (this.buffer.isPound() && lastWasBlank)
      || (
        this.atStartOfLine
        && (this.buffer.isSquareOpen() || this.buffer.isCurlyOpen())
      )
      || this.buffer.isEOF()
    ) {
      collapseBuffers(bConfirmed, bTailNL, bAmbiguous)
      confirmedEnd.become(ambiguousEnd)
      emitPlainScalar(bConfirmed, tokenIndent, start, confirmedEnd.mark())
      return
    }

    else {
      while (bTailWS.isNotEmpty)
        bAmbiguous.claimASCII(bTailWS)

      bAmbiguous.claimUTF8(this.buffer, this.position)
      ambiguousEnd.become(this.position)

      this.lineContentIndicator = LineContentIndicatorContent
      lastWasBlank = false
    }
  }
}

private fun collapseBuffers(
  into:     UByteBuffer,
  newLines: UByteBuffer,
  from:     UByteSource,
) {
  if (from.size > 0) {
    if (newLines.size == 1) {
      into.push(A_SPACE)
    } else if (newLines.size > 1) {
      newLines.skipNewLine()
      while (newLines.isNotEmpty)
        into.claimNewLine(newLines)
    }

    while (from.size > 0)
      into.claimUTF8(from)
  }
}

@Suppress("NOTHING_TO_INLINE")
private fun collapseNewLinesInto(into: UByteBuffer, newLines: UByteBuffer, pos: SourcePositionTracker) {
  if (newLines.isCRLF()) {
    if (newLines.size == 2) {
      into.push(A_SPACE)
      newLines.clear()
      pos.incLine(2u)
    } else {
      newLines.skipNewLine(pos)
      while (newLines.size > 0)
        into.claimNewLine(newLines, pos)
    }
  }

  else if (newLines.isLineFeedOrCarriageReturn()) {
    if (newLines.size == 1) {
      into.push(A_SPACE)
      newLines.clear()
      pos.incLine()
    } else {
      newLines.skipNewLine(pos)
      while (newLines.size > 0)
        into.claimNewLine(newLines, pos)
    }
  }

  else if (newLines.isNextLine()) {
    if (newLines.size == 2) {
      into.push(A_SPACE)
      newLines.clear()
      pos.incLine()
    } else {
      newLines.skipNewLine(pos)
      while (newLines.size > 0)
        into.claimNewLine(newLines, pos)
    }
  }

  else if (newLines.isLineOrParagraphSeparator()) {
    if (newLines.size == 3) {
      into.push(A_SPACE)
      newLines.clear()
      pos.incLine()
    } else {
      newLines.skipNewLine(pos)
      while (newLines.size > 0)
        into.claimNewLine(newLines, pos)
    }
  }

  else if (newLines.isNotEmpty) {
    throw IllegalStateException(
      "collapseNewLinesInto(UByteBuffer, UByteBuffer, SourcePositionTracker) called on a newLines buffer containing " +
        "one or more non-newline characters"
    )
  }
}

@Suppress("NOTHING_TO_INLINE")
@OptIn(ExperimentalUnsignedTypes::class)
private inline fun YAMLStreamTokenizerImpl.emitPlainScalar(
  value:  UByteBuffer,
  indent: UInt,
  start:  SourcePosition,
  end:    SourcePosition = this.position.mark()
) {
  this.tokens.push(YAMLTokenScalarPlain(UByteString(value.popToArray()), indent, start, end, popWarnings()))
}
