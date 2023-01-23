package io.foxcapades.lib.k.yaml.scan

import io.foxcapades.lib.k.yaml.bytes.A_SPACE
import io.foxcapades.lib.k.yaml.token.YAMLTokenScalarPlain
import io.foxcapades.lib.k.yaml.util.*

internal fun YAMLScannerImpl.fetchPlainScalar() {
  when {
    this.inFlowMapping  -> this.fetchPlainScalarInFlowMapping()
    this.inFlowSequence -> this.fetchPlainScalarInFlowSequence()
    else                -> this.fetchPlainScalarInBlock()
  }
}

private fun YAMLScannerImpl.fetchPlainScalarInFlowMapping() {
  val start       = this.position.mark()
  val indent      = this.indent
  val bContent    = this.contentBuffer1
  val bTailWS     = this.trailingWSBuffer
  val bTailNL     = this.trailingNLBuffer
  val endPosition = this.position.copy()

  bContent.clear()
  bTailWS.clear()
  bTailNL.clear()

  this.haveContentOnThisLine = true

  while (true) {
    this.reader.cache(1)

    when {
      this.reader.isBlank() -> {
        // If we already have content on this line
        if (this.haveContentOnThisLine) {
          // Then we want to keep it in case we encounter another content
          // character, at which point we will need to insert the whitespaces
          // in between the content characters.
          bTailWS.claimASCII(this.reader, this.position)
        }

        // else, if we don't have any content on this line yet,
        else {
          // Discard the whitespace because we won't use it for anything whether
          // this line is empty or not.
          skipASCII(this.reader, this.position)
        }
      }

      this.reader.isAnyBreak() -> {
        // We can go ahead and clear out the trailing space buffer because we
        // won't be using it.
        bTailWS.clear()

        // Eat the newline and append it to our trailing newline buffer.
        bTailNL.claimNewLine(this.reader, this.position)

        // Now we are starting a new line, which has no content yet.
        this.haveContentOnThisLine = false
      }

         this.reader.isColon()
      || this.reader.isComma()
      || this.reader.isCurlyClose()
      || this.reader.isEOF()
      -> {
        emitPlainScalar(bContent, indent, start, endPosition.mark())
        return
      }

      else -> {
        while (bTailWS.isNotEmpty)
          bContent.claimASCII(bTailWS)

        collapseNewLinesInto(bContent, bTailNL, endPosition)

        bContent.claimUTF8(this.reader, this.position)
        endPosition.become(this.position)
      }
    }
  }
}

private fun YAMLScannerImpl.fetchPlainScalarInFlowSequence() {
  val start       = this.position.mark()
  val indent      = this.indent
  val bContent    = this.contentBuffer1
  val bTailWS     = this.trailingWSBuffer
  val bTailNL     = this.trailingNLBuffer
  val endPosition = this.position.copy()

  bContent.clear()
  bTailWS.clear()
  bTailNL.clear()

  this.haveContentOnThisLine = true

  while (true) {
    this.reader.cache(1)

    when {
      this.reader.isBlank() -> {
        if (this.haveContentOnThisLine)
          bTailWS.claimASCII(this.reader, this.position)
        else
          skipASCII(this.reader, this.position)
      }

      this.reader.isAnyBreak() -> {
        bTailWS.clear()
        bTailNL.claimNewLine(this.reader)
        this.haveContentOnThisLine = false
      }

      this.reader.isComma()
      || this.reader.isSquareClose()
      || this.reader.isEOF()
      -> {
        emitPlainScalar(bContent, indent, start, endPosition.mark())
        return
      }

      else -> {
        while (bTailWS.isNotEmpty)
          bContent.claimASCII(bTailWS)

        collapseNewLinesInto(bContent, bTailNL, endPosition)

        bContent.claimUTF8(this.reader, this.position)
        endPosition.become(this.position)
      }
    }
  }
}



private fun YAMLScannerImpl.fetchPlainScalarInBlock() {
  val start       = this.position.mark()
  val tokenIndent = this.indent
  val minIndent   = if (this.atStartOfLine) 0u else this.indent + 1u
  val bConfirmed  = this.contentBuffer1
  val bAmbiguous  = this.contentBuffer2
  val bTailWS     = this.trailingWSBuffer
  val bTailNL     = this.trailingNLBuffer
  val endPosition = this.position.copy()

  bConfirmed.clear()
  bAmbiguous.clear()
  bTailWS.clear()
  bTailNL.clear()

  while (true) {
    this.reader.cache(1)

    if (this.reader.isSpace()) {
      if (this.haveContentOnThisLine)
        bTailWS.claimASCII(this.reader, this.position)
      else {
        skipASCII(this.reader, this.position)
        this.indent++
      }
    }

    else if (this.reader.isTab()) {
      if (this.haveContentOnThisLine)
        bTailWS.claimASCII(this.reader, this.position)
      else
        skipASCII(this.reader, this.position)
    }

    else if (this.reader.isAnyBreak()) {
      // Clear out any trailing blanks since we don't need them
      bTailWS.clear()

      // If we have content on this line
      if (this.haveContentOnThisLine) {
        // If this is the first new line character after the start of the plain
        // scalar, then both the bTailNL buffer and the bConfirmed buffer will
        // be empty.  This is fine.

        // Then collapse any new lines into the confirmed buffer
        collapseBuffers(bConfirmed, bTailNL, bAmbiguous, endPosition)
      }

      // Consume the newline character
      bTailNL.claimNewLine(this.reader, this.position)

      // We don't have any content on this new line yet
      this.haveContentOnThisLine = false
      this.indent = 0u
    }

    // If we've made it here, then we are on a character that is NOT a space, a
    // tab, or any newline character.  That means it's a content character.  If
    // a content character appears before the minimum required indent, then we
    // will consider it the start of a separate token.
    if (this.indent < minIndent) {
      collapseBuffers(bConfirmed, bTailNL, bAmbiguous, endPosition)
      emitPlainScalar(bConfirmed, tokenIndent, start, endPosition.mark())
    }

    if (this.reader.isColon()) {
      this.reader.cache(2)

      if (this.reader.isBlankAnyBreakOrEOF(1)) {
        // If the confirmed buffer is not empty, then we are not on the same
        // line we were when we started parsing this plain scalar.  There is a
        // special case here, where if the indent level for this line is 0, then
        // we are to consider the content on this, separate line to be its own
        // separate plain scalar.
        //
        // Example:
        // ```
        // foo
        // bar
        // fizz:
        // ```
        // Here, "foo bar" will be the first token, "fizz" will be the second,
        // and the next index of this token iterator will emit a mapping value
        // indicator token.
        if (bConfirmed.isNotEmpty) {
          emitPlainScalar(bConfirmed, tokenIndent, start, endPosition.mark())

          val subStart = -(bAmbiguous.size + bTailWS.size)
          val subEnd   = -bTailWS.size
          emitPlainScalar(
            bAmbiguous,
            tokenIndent,
            this.position.mark(modIndex = subStart, modColumn = subStart),
            this.position.mark(modIndex = subEnd, modColumn = subEnd)
          )
          return
        } else {
          collapseBuffers(bConfirmed, bTailNL, bAmbiguous, endPosition)
          emitPlainScalar(bConfirmed, tokenIndent, start, endPosition.mark())
          return
        }
      } else {
        bAmbiguous.claimASCII(this.reader, this.position)
      }
    }

    else if (this.reader.isDash() && this.atStartOfLine) {
      this.reader.cache(4)

      if (
        (this.reader.isDash(1) && this.reader.isDash(2) && this.reader.isBlankAnyBreakOrEOF(3))
        || this.reader.isBlankAnyBreakOrEOF(1)
      ) {
        collapseBuffers(bConfirmed, bTailNL, bAmbiguous, endPosition)
        emitPlainScalar(bConfirmed, tokenIndent, start, endPosition.mark())
        return
      }

      bAmbiguous.claimASCII(this.reader, this.position)
    }

    // If it's a period, rule out end of document
    else if (this.reader.isPeriod() && this.atStartOfLine) {
      this.reader.cache(4)

      if (this.reader.isPeriod(1) && this.reader.isPeriod(2) && this.reader.isBlankAnyBreakOrEOF(3)) {
        collapseBuffers(bConfirmed, bTailNL, bAmbiguous, endPosition)
        emitPlainScalar(bConfirmed, tokenIndent, start, endPosition.mark())
        return
      }

      bAmbiguous.claimASCII(this.reader, this.position)
    }

    else if (
      this.reader.isPound()
      || (
        this.atStartOfLine
        && (this.reader.isSquareOpen() || this.reader.isCurlyOpen())
      )
      || this.reader.isEOF()
    ) {
      collapseBuffers(bConfirmed, bTailNL, bAmbiguous, endPosition)
      emitPlainScalar(bConfirmed, tokenIndent, start, endPosition.mark())
      return
    }

    else {
      bAmbiguous.claimUTF8(this.reader, this.position)
    }
  }
}

private fun collapseBuffers(
  into:     UByteBuffer,
  newLines: UByteBuffer,
  from:     UByteSource,
  pos:      SourcePositionTracker,
) {
  if (newLines.size == 1) {
    into.push(A_SPACE)
    pos.incPosition()
  } else if (newLines.size > 1) {
    newLines.skipNewLine(pos)
    while (newLines.isNotEmpty)
      into.claimNewLine(newLines, pos)
  }

  var i: Int
  var w: Int
  while (from.size > 0) {
    i = 0
    w = from.utf8Width()
    while (i++ < w)
      into.push(from.pop())

    pos.incPosition()
  }
}

@Suppress("NOTHING_TO_INLINE")
private fun collapseNewLinesInto(into: UByteBuffer, newLines: UByteBuffer, pos: SourcePositionTracker) {
  if (newLines.isCRLF() || newLines.isNextLine()) {
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
private inline fun YAMLScannerImpl.emitPlainScalar(
  value:  UByteBuffer,
  indent: UInt,
  start:  SourcePosition,
  end:    SourcePosition = this.position.mark()
) {
  this.tokens.push(YAMLTokenScalarPlain(UByteString(value.popToArray()), indent, start, end, getWarnings()))
}
