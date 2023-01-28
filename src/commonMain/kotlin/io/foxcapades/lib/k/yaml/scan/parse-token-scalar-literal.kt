package io.foxcapades.lib.k.yaml.scan

import io.foxcapades.lib.k.yaml.bytes.A_SPACE
import io.foxcapades.lib.k.yaml.token.YAMLTokenComment
import io.foxcapades.lib.k.yaml.token.YAMLTokenScalarLiteral
import io.foxcapades.lib.k.yaml.util.*

internal fun YAMLStreamTokenizerImpl.fetchLiteralScalar(
  start: SourcePosition,
  chompMode: BlockScalarChompMode,
  indentHint: UInt,
  minIndent: UInt,
  actualIndent: UInt,
  tailComment: YAMLTokenComment?,
  trailingNewLines: UByteBuffer,
) {
  val scalarContent    = contentBuffer1
  val endPosition      = this.position.copy()
  val keepIndentAfter  = this.indent - indentHint

  scalarContent.clear()

  // Prefill the buffer with any leading spaces that were supposed to be present
  // due to the indent hint.
  if (indent > keepIndentAfter) {
    var i = indent
    while (i-- > keepIndentAfter)
      scalarContent.push(A_SPACE)
  }

  while (true) {
    this.buffer.cache(1)

    if (this.buffer.isSpace()) {
      if (lineContentIndicator == LineContentIndicatorContent) {
        scalarContent.claimASCII(this.buffer, this.position)
        endPosition.become(this.position)
      }

      else {
        if (this.indent >= keepIndentAfter) {
          while (trailingNewLines.isNotEmpty)
            scalarContent.claimNewLine(trailingNewLines)

          scalarContent.claimASCII(this.buffer, this.position)
          endPosition.become(this.position)
        } else {
          skipASCII(this.buffer, this.position)
        }

        this.indent++
      }
    }

    else if (this.buffer.isAnyBreak()) {
      trailingNewLines.claimNewLine(this.buffer, this.position)
      lineContentIndicator = LineContentIndicatorBlanksOnly
      this.indent = 0u
    }

    else if (this.buffer.isEOF()) {
      applyChomping(scalarContent, trailingNewLines, chompMode, endPosition)
      finishLiteralScalar(scalarContent, actualIndent, start, endPosition)

      if (tailComment != null)
        this.tokens.push(tailComment)

      return
    }

    else {
      if (this.indent < minIndent) {
        applyChomping(scalarContent, trailingNewLines, chompMode, endPosition)
        finishLiteralScalar(scalarContent, actualIndent, start, endPosition)

        if (tailComment != null)
          this.tokens.push(tailComment)

        return
      }

      while (trailingNewLines.isNotEmpty)
        scalarContent.claimNewLine(trailingNewLines)

      lineContentIndicator = LineContentIndicatorContent

      scalarContent.claimUTF8(this.buffer, this.position)
      endPosition.become(this.position)
    }
  }
}

@Suppress("NOTHING_TO_INLINE")
@OptIn(ExperimentalUnsignedTypes::class)
internal inline fun YAMLStreamTokenizerImpl.emitEmptyLiteralScalar(indent: UInt, start: SourcePosition) {
  this.tokens.push(YAMLTokenScalarLiteral(UByteString(UByteArray(0)), indent, start, start, this.popWarnings()))
}

@OptIn(ExperimentalUnsignedTypes::class)
internal fun YAMLStreamTokenizerImpl.finishLiteralScalar(
  scalarContent: UByteBuffer,
  actualIndent:  UInt,
  start:         SourcePosition,
  endPosition:   SourcePositionTracker,
) {
  this.tokens.push(YAMLTokenScalarLiteral(
    UByteString(scalarContent.toArray()),
    actualIndent,
    start,
    endPosition.mark(),
    this.popWarnings()
  ))
}
