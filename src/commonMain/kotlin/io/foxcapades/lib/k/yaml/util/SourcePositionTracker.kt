package io.foxcapades.lib.k.yaml.util

data class SourcePositionTracker(
  var index:  UInt = 0u,
  var line:   UInt = 0u,
  var column: UInt = 0u,
) {
  fun mark() = SourcePosition(index, line, column)

  fun become(other: SourcePositionTracker) {
    index  = other.index
    line   = other.line
    column = other.column
  }

  fun incPosition(chars: UInt = 1u) {
    index += chars
    column += chars
  }

  fun incLine(chars: UInt = 1u) {
    index += chars
    line++
    column = 0u
  }
}
