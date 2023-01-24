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
  val scalarContent     = contentBuffer1
  val keepIndentAfter   = indent - indentHint
  val endPosition       = position.copy()

  var lastLineHadLeadingWhitespace = false

  scalarContent.clear()

  while (true) {
    reader.cache(1)

    if (reader.isSpace()) {
      // If the space character appears after any form of content in the line,
      // then consider it trailing whitespace and claim it into the scalar
      // content buffer.
      if (lineContentIndicator.haveAnyContent) {
        scalarContent.claimASCII(reader, position)
        endPosition.become(position)
      }

      // Else, if the space character appears _before_ any other content:
      else {
        // If the leading blank character count is equal to the mark where we
        // want to start keeping the indent characters
        if (indent == keepIndentAfter) {
          // Then eat all the trailing newlines into the content buffer
          while (trailingNewLines.isNotEmpty)
            scalarContent.claimNewLine(trailingNewLines)

          // And claim the space as part of the content as well
          scalarContent.claimASCII(reader, position)
          endPosition.become(position)
        }

        // else, if the leading blank character count is greater than the mark
        // where we want to start keeping the indent characters
        else if (indent > keepIndentAfter) {
          // then claim the space as part of the content
          scalarContent.claimASCII(reader, position)
          endPosition.become(position)
        }

        // else if the leading space character count is less than the mark where
        // we want to start keeping the indent characters
        else {
          // then just skip the space character
          skipASCII(reader, position)
        }

        // Increment our leading blank character count.
        indent++
      }
    }

    else if (reader.isTab()) {
      // If the tab character appears after any form of non-blank content in the
      // line, then consider it trailing whitespace and claim it into the scalar
      // content buffer.
      if (lineContentIndicator.haveAnyContent) {
        scalarContent.claimASCII(reader, position)
        endPosition.become(position)
      }

      // else, if the tab character appears before any form of non-blank content
      // in the line, then consider it leading whitespace and...
      else {
        if (indent < minimumIndent) {
          TODO("emit a warning for a tab character in the indentation")
        }

        else if (indent == keepIndentAfter) {
          while (trailingNewLines.isNotEmpty)
            scalarContent.claimNewLine(trailingNewLines)

          scalarContent.claimASCII(reader, position)
          endPosition.become(position)
        }

        else if (indent > keepIndentAfter) {
          scalarContent.claimASCII(reader, position)
          endPosition.become(position)
        }

        else {
          skipASCII(reader, position)
        }

        indent++
      }
    }

    else if (reader.isAnyBreak()) {
      trailingNewLines.claimNewLine(reader, position)
      lineContentIndicator = LineContentIndicatorBlanksOnly
      lastLineHadLeadingWhitespace = lastLineHadLeadingWhitespace || indent > keepIndentAfter
      indent = 0u
    }

    else if (reader.isEOF()) {
      applyChomping(scalarContent, trailingNewLines, chompMode, endPosition)
      finishFoldingScalar(scalarContent, actualIndent, start, endPosition)

      if (tailComment != null)
        this.tokens.push(tailComment)

      return
    }

    else {
      if (indent < minimumIndent) {
        applyChomping(scalarContent, trailingNewLines, chompMode, endPosition)
        finishFoldingScalar(scalarContent, actualIndent, start, endPosition)

        if (tailComment != null)
          tokens.push(tailComment)

        return
      }

      println(scalarContent.toArray().decodeToString())
      println(lastLineHadLeadingWhitespace)

      if (lastLineHadLeadingWhitespace) {
        while (trailingNewLines.isNotEmpty)
          scalarContent.claimNewLine(trailingNewLines)

        lastLineHadLeadingWhitespace = false
      } else {
        if (trailingNewLines.isNotEmpty) {
          val width = trailingNewLines.utf8Width()

          if (trailingNewLines.size == width) {
            scalarContent.push(A_SPACE)
            trailingNewLines.clear()
          } else if (trailingNewLines.size > width) {
            trailingNewLines.skipNewLine()
            while (trailingNewLines.isNotEmpty)
              scalarContent.claimNewLine(trailingNewLines)
          } else {
            throw IllegalStateException("trailingNewLines had fewer bytes than the UTF-8 width of the next character")
          }
        }
      }

      lineContentIndicator = LineContentIndicatorContent

      scalarContent.claimUTF8(reader, position)
      endPosition.become(position)
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
