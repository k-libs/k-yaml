package io.foxcapades.lib.k.yaml.scan

import io.foxcapades.lib.k.yaml.bytes.A_SPACE
import io.foxcapades.lib.k.yaml.token.*
import io.foxcapades.lib.k.yaml.util.*

internal fun YAMLScannerImpl.fetchFoldedScalar(
  start: SourcePosition,
  chompMode: BlockScalarChompMode,
  indentHint: UInt,
  minimumIndent: UInt,
  actualIndent: UInt,
  tailComment: YAMLTokenComment?,
  trailingNewLines: UByteBuffer,
) {
  val scalarContent     = this.contentBuffer1
  val trailingBlanks    = this.trailingWSBuffer
  val keepIndentAfter   = this.indent - indentHint
  val endPosition       = this.position.copy()
  var leadingSpaceCount = 0u

  scalarContent.clear()
  trailingBlanks.clear()

  while (true) {
    this.reader.cache(1)

    if (this.reader.isSpace()) {
      if (this.haveContentOnThisLine) {
        scalarContent.claimASCII(this.reader, this.position)
        endPosition.become(this.position)
      }

      else {
        if (this.indent >= keepIndentAfter)
          leadingSpaceCount++

        skipASCII(this.reader, this.position)

        this.indent++
      }
    }

    else if (this.reader.isAnyBreak()) {
      if (!this.haveContentOnThisLine) {
        leadingSpaceCount = 0u
      }

      trailingNewLines.claimNewLine(this.reader, this.position)
      this.haveContentOnThisLine = false
      this.indent = 0u
    }

    else if (this.reader.isEOF() || this.indent < minimumIndent) {
      applyChomping(scalarContent, trailingNewLines, chompMode, endPosition)
      this.finishFoldingScalar(scalarContent, actualIndent, start, endPosition)

      if (tailComment != null)
        this.tokens.push(tailComment)

      return
    }

    else {
      this.haveContentOnThisLine = true

      if (leadingSpaceCount > indentHint) {
        while (trailingNewLines.isNotEmpty)
          scalarContent.claimNewLine(trailingNewLines)
      }

      else if (trailingNewLines.size > 1) {
        trailingNewLines.skipNewLine()
        while (trailingNewLines.isNotEmpty)
          scalarContent.claimNewLine(trailingNewLines)
      }

      else if (trailingNewLines.size == 1) {
        scalarContent.push(A_SPACE)
        trailingNewLines.skipNewLine()
      }

      leadingSpaceCount = 0u

      scalarContent.claimUTF8(this.reader, this.position)
      endPosition.become(this.position)
    }
  }
}

@Suppress("NOTHING_TO_INLINE")
@OptIn(ExperimentalUnsignedTypes::class)
internal inline fun YAMLScannerImpl.emitEmptyFoldedScalar(indent: UInt, start: SourcePosition) {
  this.tokens.push(YAMLTokenScalarFolded(UByteString(UByteArray(0)), indent, start, start, this.getWarnings()))
}

@Suppress("NOTHING_TO_INLINE")
@OptIn(ExperimentalUnsignedTypes::class)
internal inline fun YAMLScannerImpl.finishFoldingScalar(
  scalarContent:    UByteBuffer,
  actualIndent:     UInt,
  start:            SourcePosition,
  endPosition:      SourcePositionTracker,
) {
  this.tokens.push(YAMLTokenScalarFolded(
    UByteString(scalarContent.toArray()),
    actualIndent,
    start,
    endPosition.mark(),
    this.getWarnings()
  ))
}
