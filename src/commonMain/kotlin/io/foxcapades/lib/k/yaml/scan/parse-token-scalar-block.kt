package io.foxcapades.lib.k.yaml.scan

import io.foxcapades.lib.k.yaml.bytes.A_LINE_FEED
import io.foxcapades.lib.k.yaml.err.UIntOverflowException
import io.foxcapades.lib.k.yaml.err.YAMLScannerException
import io.foxcapades.lib.k.yaml.token.YAMLTokenComment
import io.foxcapades.lib.k.yaml.util.*

@OptIn(ExperimentalUnsignedTypes::class)
internal fun YAMLStreamTokenizerImpl.parseBlockScalar(isLiteral: Boolean) {
  val startMark        = this.position.mark()
  val trailingNewLines = this.trailingNLBuffer
  val actualIndent     = this.indent

  var minIndent = when {
    this.atStartOfLine                                                   -> 0u
    this.lineContentIndicator == LineContentIndicatorBlanksAndIndicators -> this.indent - 1u
    else                                                                 -> this.indent + 1u
  }

  var tailComment: YAMLTokenComment? = null

  val chompMode:   BlockScalarChompMode
  val indentHint:  UInt

  trailingNewLines.clear()

  lineContentIndicator = LineContentIndicatorContent
  skipASCII(this.buffer, this.position)

  this.buffer.cache(1)

  if (this.buffer.isPlus() || this.buffer.isDash()) {
    chompMode = this.parseUByte()

    this.buffer.cache(1)

    indentHint = if (this.buffer.isDecimalDigit())
      try { this.parseUInt() }
      catch (e: UIntOverflowException) {
        throw YAMLScannerException("block scalar indent hint value overflows type uint32", startMark.resolve(2, 0, 2))
      }
    else
      0u
  }

  else if (this.buffer.isDecimalDigit()) {
    indentHint = try { this.parseUInt() }
    catch (e: UIntOverflowException) {
      throw YAMLScannerException("block scalar indent hint value overflows type uint32", startMark.resolve(2, 0, 2))
    }

    this.buffer.cache(1)

    chompMode = if (this.buffer.isPlus() || this.buffer.isDash())
      this.parseUByte()
    else
      BlockScalarChompModeClip
  }

  else {
    indentHint = 0u
    chompMode  = BlockScalarChompModeClip
  }

  val haveBlanks = this.skipBlanks() > 0
  this.buffer.cache(1)

  when {
    haveBlanks && this.buffer.isPound() -> {
      val commentContent = contentBuffer1
      val trailingBlanks = trailingWSBuffer
      val commentStart   = position.mark()

      commentContent.clear()
      trailingBlanks.clear()

      // Skip over the `#` character.
      skipASCII(this.buffer, this.position)
      // Skip over any blank characters
      skipBlanks()

      while (true) {
        this.buffer.cache(1)

        if (this.buffer.isBlank()) {
          trailingBlanks.claimASCII(this.buffer, this.position)
        }

        else if (this.buffer.isAnyBreakOrEOF()) {
          tailComment = YAMLTokenComment(
            UByteString(commentContent.toArray()),
            this.indent,
            true,
            commentStart,
            this.position.mark(modIndex = -trailingBlanks.size, modColumn = -trailingBlanks.size),
            this.popWarnings()
          )

          break
        }

        else {
          while (trailingBlanks.isNotEmpty)
            commentContent.push(trailingBlanks.pop())

          commentContent.claimUTF8(this.buffer, this.position)
        }
      }
    }

    this.buffer.isAnyBreak()            -> { /* Do nothing. */ }

    this.buffer.isEOF()                 -> {
      if (isLiteral)
        emitEmptyLiteralScalar(this.indent, startMark)
      else
        emitEmptyFoldedScalar(this.indent, startMark)

      return
    }

    else -> TODO("handle invalid/unexpected character on the same line as the folding scalar start indicator")
  }

  trailingNewLines.claimNewLine(this.buffer, this.position)
  lineContentIndicator = LineContentIndicatorBlanksOnly

  // Determine the scalar block's indent level
  while (true) {
    this.buffer.cache(1)

    if (this.buffer.isSpace()) {
      skipASCII(this.buffer, this.position)
      this.indent++
    }

    else if (this.buffer.isAnyBreak()) {
      trailingNewLines.claimNewLine(this.buffer, this.position)
      lineContentIndicator = LineContentIndicatorBlanksOnly
      this.indent = 0u
    }

    else if (this.buffer.isEOF()) {
      val content     = this.contentBuffer1
      val endPosition = this.position.copy()

      content.clear()

      applyChomping(content, trailingNewLines, chompMode, endPosition)

      if (isLiteral)
        finishLiteralScalar(content, actualIndent, startMark, endPosition)
      else
        finishFoldingScalar(content, actualIndent, startMark, endPosition)

      return
    }

    else {
      lineContentIndicator = LineContentIndicatorContent
      this.indent = this.position.column

      if (this.indent < minIndent) {
        println(minIndent)
        TODO("we have an empty scalar that may have newlines that need to be appended to the literal content")
      }

      if (this.indent < indentHint) {
        TODO("we have an invalid indent value")
      }

      minIndent = this.indent
      break
    }
  }

  if (trailingNewLines.size == 1)
    trailingNewLines.clear()

  if (isLiteral)
    fetchLiteralScalar(startMark, chompMode, indentHint, minIndent, actualIndent, tailComment, trailingNewLines)
  else
    fetchFoldedScalar(startMark, chompMode, indentHint, minIndent, actualIndent, tailComment, trailingNewLines)
}

internal fun applyChomping(
  scalarContent: UByteBuffer,
  trailingNewLines: UByteBuffer,
  chompingMode: BlockScalarChompMode,
  endPosition: SourcePositionTracker
) {
  if (chompingMode == BlockScalarChompModeStrip) {
    // Do nothing in this case.
  }

  else if (chompingMode == BlockScalarChompModeClip) {
    if (trailingNewLines.isEmpty)
      scalarContent.push(A_LINE_FEED)
    else
      scalarContent.claimNewLine(trailingNewLines, endPosition)
  }

  else if (chompingMode == BlockScalarChompModeKeep) {
    if (trailingNewLines.isEmpty)
      scalarContent.push(A_LINE_FEED)
    else
      while (trailingNewLines.isNotEmpty)
        scalarContent.claimNewLine(trailingNewLines, endPosition)
  }

  else {
    throw IllegalStateException("invalid BlockScalarChompMode value")
  }
}
