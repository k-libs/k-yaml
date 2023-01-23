package io.foxcapades.lib.k.yaml.scan

import io.foxcapades.lib.k.yaml.util.*


@Suppress("NOTHING_TO_INLINE")
internal inline fun UByteBuffer.skipNewLine() {
  if (size > 2) {
    when {
      uIsCRLF()          || uIsNextLine()           -> return skip(2)
      uIsLineFeed()      || uIsCarriageReturn()     -> return skip(1)
      uIsLineSeparator() || uIsParagraphSeparator() -> return skip(3)
    }
  } else if (size > 1) {
    when {
      uIsCRLF()     || uIsNextLine()       -> return skip(2)
      uIsLineFeed() || uIsCarriageReturn() -> return skip(1)
    }
  } else if (size > 0) {
    when {
      uIsLineFeed() || uIsCarriageReturn() -> return skip(1)
    }
  }

  throw IllegalStateException(
    "called UByteBuffer.skipNewLine() on a UByteBuffer whose next character is not a line break."
  )
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun UByteBuffer.skipNewLine(position: SourcePositionTracker) {
  if (size > 2) {
    when {
      uIsCRLF() -> {
        skip(2)
        position.incLine(2u)
        return
      }
      uIsLineFeed() || uIsCarriageReturn() -> {
        skip(1)
        position.incLine()
        return
      }

      uIsNextLine() -> {
        skip(2)
        position.incLine()
        return
      }

      uIsLineSeparator() -> {
        skip(3)
        position.incLine()
        return
      }

      uIsParagraphSeparator() -> {
        skip(3)
        position.incLine()
        return
      }
    }
  } else if (size > 1) {
    when {
      uIsCRLF() -> {
        skip(2)
        position.incLine(2u)
        return
      }
      uIsLineFeed() || uIsCarriageReturn() -> {
        skip(1)
        position.incLine()
        return
      }

      uIsNextLine() -> {
        skip(2)
        position.incLine()
        return
      }
    }
  } else if (size > 0) {
    when {
      uIsLineFeed() || uIsCarriageReturn() -> {
        skip(1)
        return
      }
    }
  }

  throw IllegalStateException(
    "called UByteBuffer.skipNewLine(SourcePositionTracker) on a UByteBuffer whose next character is not a line break."
  )
}