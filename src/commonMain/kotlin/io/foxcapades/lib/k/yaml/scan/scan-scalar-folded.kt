package io.foxcapades.lib.k.yaml.scan

import io.foxcapades.lib.k.yaml.err.UIntOverflowException
import io.foxcapades.lib.k.yaml.token.YAMLToken
import io.foxcapades.lib.k.yaml.token.YAMLTokenDataScalar
import io.foxcapades.lib.k.yaml.token.YAMLTokenType
import io.foxcapades.lib.k.yaml.util.*

internal fun YAMLScannerImpl.fetchFoldedStringToken() {
  contentBuffer1.clear()
  trailingWSBuffer.clear()
  trailingNLBuffer.clear()

  val start = position.mark()

  skipASCII()

  var chompingMode = ChompingModeClip
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
      chompingMode = ChompingModeKeep
    } else if (reader.isDash()) {
      chompingMode = ChompingModeStrip
    }
  }

  else if (reader.isPlus()) {
    chompingMode = ChompingModeKeep
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
    chompingMode = ChompingModeStrip
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
      reader.isBlank()                 -> { skipASCII(); lastWasSpace = true }
      reader.isEOF()                   -> TODO("wrap up empty scalar")
      reader.isAnyBreak()              -> break
      reader.isPound() && lastWasSpace -> {
        TODO("handle trailing comment on folding scalar start indicator line")
      }
      else                -> TODO("invalid character on folding scalar start indicator line")
    }

    reader.cache(1)
  }

  // Okay, so if we're here now, then we are at a newline character that may
  // or may not be the start of our scalar value.

  val indent: UInt

  while (true) {
    // Skip over the waiting newline.
    skipLine()

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
    }
  }

  // We have now determined our indentation level for the block scalar.

  if (indentHint > indent)
    TODO("error: the indent hint was greater than the actual indentation level")

  val keepSpacesStartingAt = indent - indentHint
  val lastContentPosition = position.copy()

  while (true) {
    reader.cache(1)

    when {
      reader.isSpace() -> {
        if (trailingNLBuffer.isNotEmpty && position.column > keepSpacesStartingAt) {
          contentBuffer1.claimASCII()
        } else {
          trailingWSBuffer.claimASCII()
        }
      }

      reader.isAnyBreak() -> {
        trailingWSBuffer.clear()
        trailingNLBuffer.claimNewLine()
      }

      reader.isEOF() -> {
        return finishFoldedScalar(chompingMode, start, lastContentPosition.mark())
      }

      else -> {
        if (position.column < indent) {
          return finishFoldedScalar(chompingMode, start, lastContentPosition.mark())
        } else {
          if (trailingWSBuffer.isNotEmpty && trailingNLBuffer.isEmpty) {
            while (trailingWSBuffer.isNotEmpty)
              contentBuffer1.push(trailingWSBuffer.pop())
          }

          contentBuffer1.claimUTF8()
          lastContentPosition.become(position)
        }
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