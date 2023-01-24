package io.foxcapades.lib.k.yaml.scan

import io.foxcapades.lib.k.yaml.token.YAMLTokenComment
import io.foxcapades.lib.k.yaml.token.YAMLTokenScalarLiteral
import io.foxcapades.lib.k.yaml.util.*

internal fun YAMLScannerImpl.fetchLiteralScalar(
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

  while (true) {
    this.reader.cache(1)

    if (this.reader.isSpace()) {
      if (lineContentIndicator == LineContentIndicatorContent) {
        scalarContent.claimASCII(this.reader, this.position)
        endPosition.become(this.position)
      }

      else {
        if (this.indent >= keepIndentAfter) {
          while (trailingNewLines.isNotEmpty)
            scalarContent.claimNewLine(trailingNewLines)

          scalarContent.claimASCII(this.reader, this.position)
          endPosition.become(this.position)
        } else {
          skipASCII(this.reader, this.position)
        }

        this.indent++
      }
    }

    else if (this.reader.isAnyBreak()) {
      trailingNewLines.claimNewLine(this.reader, this.position)
      lineContentIndicator = LineContentIndicatorBlanksOnly
      this.indent = 0u
    }

    else if (this.reader.isEOF()) {
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

      scalarContent.claimUTF8(this.reader, this.position)
      endPosition.become(this.position)
    }
  }
}

@Suppress("NOTHING_TO_INLINE")
@OptIn(ExperimentalUnsignedTypes::class)
internal inline fun YAMLScannerImpl.emitEmptyLiteralScalar(indent: UInt, start: SourcePosition) {
  this.tokens.push(YAMLTokenScalarLiteral(UByteString(UByteArray(0)), indent, start, start, this.popWarnings()))
}

@OptIn(ExperimentalUnsignedTypes::class)
internal fun YAMLScannerImpl.finishLiteralScalar(
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
