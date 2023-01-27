package io.foxcapades.lib.k.yaml.scan

import io.foxcapades.lib.k.yaml.bytes.A_SPACE
import io.foxcapades.lib.k.yaml.token.YAMLTokenScalarPlain
import io.foxcapades.lib.k.yaml.util.*

internal fun YAMLStreamTokenizerImpl.parsePlainScalar() {
  // TODO: catch the case where the first character in the plain scalar is a tab
  //       because that may be illegal
  when {
    this.inFlowMapping  -> this.fetchPlainScalarInFlowMapping()
    this.inFlowSequence -> this.fetchPlainScalarInFlowSequence()
    else                -> this.fetchPlainScalarInBlock()
  }
}

private fun YAMLStreamTokenizerImpl.fetchPlainScalarInFlowMapping() {
  val start       = this.position.mark()
  val indent      = this.indent
  val bContent    = this.contentBuffer1
  val bTailWS     = this.trailingWSBuffer
  val bTailNL     = this.trailingNLBuffer
  val endPosition = this.position.copy()

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
      }

      this.buffer.isAnyBreak() -> {
        // We can go ahead and clear out the trailing space buffer because we
        // won't be using it.
        bTailWS.clear()

        // Eat the newline and append it to our trailing newline buffer.
        bTailNL.claimNewLine(this.buffer, this.position)

        // Now we are starting a new line, which has no content yet.
        lineContentIndicator = LineContentIndicatorBlanksOnly
      }

         this.buffer.isColon()
      || this.buffer.isComma()
      || this.buffer.isCurlyClose()
      || this.buffer.isEOF()
                               -> {
        emitPlainScalar(bContent, indent, start, endPosition.mark())
        lineContentIndicator = LineContentIndicatorContent
        return
      }

      else                     -> {
        while (bTailWS.isNotEmpty)
          bContent.claimASCII(bTailWS)

        collapseNewLinesInto(bContent, bTailNL, endPosition)

        bContent.claimUTF8(this.buffer, this.position)
        endPosition.become(this.position)
        lineContentIndicator = LineContentIndicatorContent
      }
    }
  }
}

private fun YAMLStreamTokenizerImpl.fetchPlainScalarInFlowSequence() {
  val start       = this.position.mark()
  val indent      = this.indent
  val bContent    = this.contentBuffer1
  val bTailWS     = this.trailingWSBuffer
  val bTailNL     = this.trailingNLBuffer
  val endPosition = this.position.copy()

  var lastWasBlankOrNewLine = false

  bContent.clear()
  bTailWS.clear()
  bTailNL.clear()

  this.lineContentIndicator = LineContentIndicatorContent

  while (true) {
    this.buffer.cache(1)

    when {
      this.buffer.isBlank()                     -> {
        if (lineContentIndicator == LineContentIndicatorContent)
          bTailWS.claimASCII(this.buffer, this.position)
        else
          skipASCII(this.buffer, this.position)

        lastWasBlankOrNewLine = true
      }

      this.buffer.isAnyBreak()                  -> {
        bTailWS.clear()
        bTailNL.claimNewLine(this.buffer)
        lineContentIndicator = LineContentIndicatorBlanksOnly
        lastWasBlankOrNewLine = true
      }

         this.buffer.isComma()
      || this.buffer.isSquareClose()
      || this.buffer.isEOF()
                                                -> {
        emitPlainScalar(bContent, indent, start, endPosition.mark())
        lineContentIndicator = LineContentIndicatorContent
        lastWasBlankOrNewLine = false
        return
      }

      lastWasBlankOrNewLine && buffer.isPound() ->{
        emitPlainScalar(bContent, indent, start, endPosition.mark())
        lineContentIndicator = LineContentIndicatorContent
        lastWasBlankOrNewLine = false
        return
      }

      else                                      -> {
        while (bTailWS.isNotEmpty)
          bContent.claimASCII(bTailWS)

        collapseNewLinesInto(bContent, bTailNL, endPosition)

        bContent.claimUTF8(this.buffer, this.position)
        endPosition.become(this.position)
        this.lineContentIndicator = LineContentIndicatorContent
        lastWasBlankOrNewLine = false
      }
    }
  }
}



private fun YAMLStreamTokenizerImpl.fetchPlainScalarInBlock() {
  val start        = this.position.mark()
  val tokenIndent  = this.indent
  val bConfirmed   = this.contentBuffer1
  val bAmbiguous   = this.contentBuffer2
  val bTailWS      = this.trailingWSBuffer
  val bTailNL      = this.trailingNLBuffer
  val endPosition  = this.position.copy()
  var leadWSCount  = 0u
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
        leadWSCount++
      }
      lastWasBlank = true
    }

    else if (this.buffer.isTab()) {
      if (lineContentIndicator.haveAnyContent)
        bTailWS.claimASCII(this.buffer, this.position)
      else
        skipASCII(this.buffer, this.position)
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
        collapseBuffers(bConfirmed, bTailNL, bAmbiguous, endPosition)
        endPosition.incPosition(leadWSCount)
        leadWSCount = 0u
      }

      // Consume the newline character
      bTailNL.claimNewLine(this.buffer, this.position)

      // We don't have any content on this new line yet
      lineContentIndicator = LineContentIndicatorBlanksOnly
      this.indent = 0u
      lastWasBlank = true
    }

    else if (this.indent < tokenIndent) {
      collapseBuffers(bConfirmed, bTailNL, bAmbiguous, endPosition)
      emitPlainScalar(bConfirmed, tokenIndent, start, endPosition.mark())
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
          emitPlainScalar(bConfirmed, tokenIndent, start, endPosition.mark())

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
          collapseBuffers(bConfirmed, bTailNL, bAmbiguous, endPosition)
          emitPlainScalar(bConfirmed, tokenIndent, start, endPosition.mark())
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
        collapseBuffers(bConfirmed, bTailNL, bAmbiguous, endPosition)
        emitPlainScalar(bConfirmed, tokenIndent, start, endPosition.mark())
        return
      }

      bAmbiguous.claimASCII(this.buffer, this.position)
      lineContentIndicator = LineContentIndicatorContent
      lastWasBlank = false
    }

    else if (this.buffer.isPeriod() && this.atStartOfLine) {
      this.buffer.cache(4)

      if (this.buffer.isPeriod(1) && this.buffer.isPeriod(2) && this.buffer.isBlankAnyBreakOrEOF(3)) {
        collapseBuffers(bConfirmed, bTailNL, bAmbiguous, endPosition)
        emitPlainScalar(bConfirmed, tokenIndent, start, endPosition.mark())
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
      collapseBuffers(bConfirmed, bTailNL, bAmbiguous, endPosition)
      emitPlainScalar(bConfirmed, tokenIndent, start, endPosition.mark())
      return
    }

    else {
      while (bTailWS.isNotEmpty)
        bAmbiguous.claimASCII(bTailWS)

      bAmbiguous.claimUTF8(this.buffer, this.position)
      this.lineContentIndicator = LineContentIndicatorContent
      lastWasBlank = false
    }
  }
}

private fun collapseBuffers(
  into:     UByteBuffer,
  newLines: UByteBuffer,
  from:     UByteSource,
  pos:      SourcePositionTracker,
) {
  if (from.size > 0) {
    if (newLines.size == 1) {
      into.push(A_SPACE)
      pos.incLine()
    } else if (newLines.size > 1) {
      newLines.skipNewLine(pos)
      while (newLines.isNotEmpty)
        into.claimNewLine(newLines, pos)
    }

    while (from.size > 0)
      into.claimUTF8(from, pos)
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
