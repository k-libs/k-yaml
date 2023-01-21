package io.foxcapades.lib.k.yaml.scan

import io.foxcapades.lib.k.yaml.bytes.A_SPACE
import io.foxcapades.lib.k.yaml.err.UIntOverflowException
import io.foxcapades.lib.k.yaml.token.YAMLToken
import io.foxcapades.lib.k.yaml.token.YAMLTokenDataScalar
import io.foxcapades.lib.k.yaml.token.YAMLTokenType
import io.foxcapades.lib.k.yaml.util.*

internal fun YAMLScannerImpl.fetchFoldedStringToken() {
  contentBuffer1.clear()
  contentBuffer2.clear()
  trailingWSBuffer.clear()
  trailingNLBuffer.clear()

  val leadingWSBuffer = contentBuffer2

  val start = position.mark()

  skipASCII()

  var chompingMode = BlockScalarChompModeClip
  var indentHint   = 0u

  reader.cache(1)

  if (reader.isDecimalDigit()) {
    indentHint = try {
      parseUInt()
    } catch (e: UIntOverflowException) {
      TODO("uint overflow while attempting to parse the indent hint")
    }

    reader.cache(1)

    if (reader.isPlus()) {
      chompingMode = BlockScalarChompModeKeep
    } else if (reader.isDash()) {
      chompingMode = BlockScalarChompModeStrip
    }
  }

  else if (reader.isPlus()) {
    chompingMode = BlockScalarChompModeKeep
    skipASCII()

    if (reader.isDecimalDigit()) {
      indentHint = try {
        parseUInt()
      } catch (e: UIntOverflowException) {
        TODO("uint overflow while attempting to parse the indent hint")
      }
    }
  }

  else if (reader.isDash()) {
    chompingMode = BlockScalarChompModeStrip
    skipASCII()

    if (reader.isDecimalDigit()) {
      indentHint = try {
        parseUInt()
      } catch (e: UIntOverflowException) {
        TODO("uint overflow while attempting to parse the indent hint")
      }
    }
  }

  // If we've made it here then we've passed by and captured any chomping
  // indicator and/or indent hint that was provided.

  // Next we should expect to see one of the following:
  //
  // - the EOF
  // - a blank character
  // - a newline character

  reader.cache(1)
  var lastWasSpace = false

  while (true) {
    when {
      reader.isBlank() -> {
        skipASCII()
        lastWasSpace = true
      }

      reader.isEOF() -> TODO("wrap up empty scalar")

      reader.isAnyBreak() -> break

      reader.isPound() && lastWasSpace -> {
        TODO("handle trailing comment on folding scalar start indicator line")
      }

      else -> TODO("invalid character on folding scalar start indicator line")
    }

    reader.cache(1)
  }

  // Okay, so if we're here now, then we are at a newline character that may
  // or may not be the start of our scalar value.

  val indent: UInt

  // Skip over the waiting newline.
  skipLine()

  while (true) {
    reader.cache(1)

    when {
      reader.isSpace() -> {
        indent = eatSpaces()
        break
      }

      reader.isAnyBreak() -> {
        trailingNLBuffer.claimNewLine()
      }

      reader.isEOF() -> {
        TODO("wrap up empty scalar")
      }

      else -> {
        indent = 0u
        break
      }
    }
  }

  // We have now determined our indentation level for the block scalar.

  if (indentHint > indent)
    TODO("error: the indent hint was greater than the actual indentation level")

  val keepSpacesStartingAt = indent - indentHint
  val lastContentPosition  = position.copy()
  var contentOnThisLine    = false

  var i = indent - keepSpacesStartingAt
  while (i-- > 0u)
    leadingWSBuffer.push(A_SPACE)

  while (true) {
    reader.cache(1)

    when {
      reader.isSpace() -> {
        if (contentOnThisLine) {
          contentBuffer1.claimASCII()
        } else if (position.column >= keepSpacesStartingAt) {
          leadingWSBuffer.claimASCII()
        } else {
          skipASCII()
        }
      }

      reader.isAnyBreak() -> {
        // If there is no content on this line, then we don't care about the
        // leading spaces because they aren't actually "leading" to anything.
        if (!contentOnThisLine) {
          leadingWSBuffer.clear()
        }

        trailingNLBuffer.claimNewLine()
        contentOnThisLine = false
      }

      reader.isEOF() -> {
        return finishFoldedScalar(chompingMode, start, lastContentPosition.mark())
      }

      else -> {
        contentOnThisLine = true

        // If the position of this character is before the detected indent, then
        // we are going to bail and call that a separate token.
        if (position.column < indent)
          return finishFoldedScalar(chompingMode, start, lastContentPosition.mark())

        // If we have leading space characters then we are _not_ going to
        // collapse the ws and nl buffers, we are going to add them to the
        // content as is.
        if (leadingWSBuffer.isNotEmpty) {
          while (trailingNLBuffer.isNotEmpty)
            contentBuffer1.push(trailingNLBuffer.pop())
          while (leadingWSBuffer.isNotEmpty)
            contentBuffer1.push(leadingWSBuffer.pop())
        }

        // Otherwise, if we do not have any leading spaces...

        // AND we have newlines to take care of:
        else if (trailingNLBuffer.isNotEmpty) {
          // If there is only one newline to take care of
          if (trailingNLBuffer.size == 1) {
            contentBuffer1.push(A_SPACE)
            trailingNLBuffer.clear()
          } else {
            // Skip the first newline character we saw
            trailingNLBuffer.skipNewLine()
            // Append the following newlines
            while (trailingNLBuffer.isNotEmpty)
              contentBuffer1.claimNewLine(trailingNLBuffer)
          }
        }

        contentBuffer1.claimUTF8()
        lastContentPosition.become(position)
      }
    }
  }
}

@OptIn(ExperimentalUnsignedTypes::class)
private fun YAMLScannerImpl.finishFoldedScalar(
  chomp: BlockScalarChompMode,
  start: SourcePosition,
  end:   SourcePosition,
) {
  // Handle trailing newlines based on the chomp mode.
  when (chomp) {
    BlockScalarChompModeClip -> {
      if (trailingNLBuffer.isNotEmpty) {
        contentBuffer1.claimNewLine(trailingNLBuffer)
      }
    }

    BlockScalarChompModeStrip -> {
      // Do nothing?
    }

    BlockScalarChompModeKeep -> {
      while (trailingNLBuffer.isNotEmpty)
        contentBuffer1.claimNewLine(trailingNLBuffer)
    }
  }

  tokens.push(newFoldedScalarToken(contentBuffer1.popToArray(), start, end))
}

@OptIn(ExperimentalUnsignedTypes::class)
private fun YAMLScannerImpl.newFoldedScalarToken(
  value: UByteArray,
  start: SourcePosition,
  end: SourcePosition
) =
  YAMLToken(YAMLTokenType.Scalar, YAMLTokenDataScalar(value, YAMLScalarStyle.Folded), start, end, getWarnings())