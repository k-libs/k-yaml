package io.foxcapades.lib.k.yaml.scan

import io.foxcapades.lib.k.yaml.bytes.A_LINE_FEED
import io.foxcapades.lib.k.yaml.util.*

internal fun UByteBuffer.claimNewLine(from: UByteSource, position: SourcePositionTracker) {
  when {
    from.isCRLF() -> {
      this.push(A_LINE_FEED)
      from.skip(2)
      position.incLine(2u)
    }

    from.isLineFeedOrCarriageReturn() -> {
      this.push(A_LINE_FEED)
      from.skip(1)
      position.incLine()
    }

    from.isNextLine() -> {
      this.push(from.pop())
      this.push(from.pop())
      position.incLine()
    }

    from.isLineOrParagraphSeparator() -> {
      this.push(from.pop())
      this.push(from.pop())
      this.push(from.pop())
      position.incLine()
    }

    else -> {
      throw IllegalStateException(
        "called UByteBuffer.claimNewLine(UByteSource, SourcePositionTracker) and provided a UByteSource whose next " +
          "character is not a new line"
      )
    }
  }
}

internal fun UByteBuffer.claimNewLine(from: UByteSource) {
  when {
    from.isCRLF() -> {
      this.push(A_LINE_FEED)
      from.skip(2)
    }

    from.isLineFeedOrCarriageReturn() -> {
      this.push(A_LINE_FEED)
      from.skip(1)
    }

    from.isNextLine() -> {
      this.push(from.pop())
      this.push(from.pop())
    }

    from.isLineOrParagraphSeparator() -> {
      this.push(from.pop())
      this.push(from.pop())
      this.push(from.pop())
    }

    else -> {
      throw IllegalStateException(
        "called UByteBuffer.claimNewLine(UByteSource) and provided a UByteSource whose next character is not a new line"
      )
    }
  }
}

internal fun UByteBuffer.claimASCII(from: UByteSource, position: SourcePositionTracker, count: Int) {
  var i = 0
  while (i++ < count)
    this.push(from.pop())

  position.incPosition(count.toUInt())
}

internal fun UByteBuffer.claimASCII(from: UByteSource, position: SourcePositionTracker) {
  this.push(from.pop())
  position.incPosition()
}

internal fun UByteBuffer.claimASCII(from: UByteSource, count: Int) {
  var i = 0
  while (i++ < count)
    this.push(from.pop())
}

internal fun UByteBuffer.claimASCII(from: UByteSource) {
  this.push(from.pop())
}

internal fun UByteBuffer.claimUTF8(from: UByteSource, position: SourcePositionTracker, count: Int = 1) {
  var i = 0
  while (i++ < count) {
    var width = from.peek().utf8Width()
    while (width-- > 0)
      this.push(from.pop())
  }

  position.incPosition(count.toUInt())
}

internal fun UByteBuffer.claimUTF8(from: UByteSource, position: SourcePositionTracker) {
  var width = from.peek().utf8Width()
  while (width-- > 0)
    this.push(from.pop())

  position.incPosition()
}

internal fun UByteBuffer.claimUTF8(from: UByteSource) {
  var width = from.peek().utf8Width()
  while (width-- > 0)
    this.push(from.pop())
}