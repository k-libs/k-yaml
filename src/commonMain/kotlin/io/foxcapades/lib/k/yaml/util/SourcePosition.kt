package io.foxcapades.lib.k.yaml.util

class SourcePosition {

  val index: UInt
  val line: UInt
  val column: UInt

  constructor(index: UInt, line: UInt, column: UInt) {
    this.index  = index
    this.line   = line
    this.column = column
  }

  override fun equals(other: Any?) =
    this === other || (other is SourcePosition && index == other.index && line == other.line && column == other.column)

  override fun hashCode(): Int {
    var result = index.hashCode()
    result = 31 * result + line.hashCode()
    result = 31 * result + column.hashCode()
    return result
  }

  override fun toString() =
    "SourcePosition(index=$index, line=$line, column=$column)"

}
