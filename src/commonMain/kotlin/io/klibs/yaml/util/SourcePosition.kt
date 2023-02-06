package io.klibs.yaml.util

class SourcePosition {

  val index: UInt
  val line: UInt
  val column: UInt

  constructor(index: UInt, line: UInt, column: UInt) {
    this.index  = index
    this.line   = line
    this.column = column
  }

  fun resolve(modIndex: Int = 0, modLine: Int = 0, modColumn: Int = 0) =
    SourcePosition(
      if (modIndex  < 0) index  - (-modIndex).toUInt()  else index  + modIndex.toUInt(),
      if (modLine   < 0) line   - (-modLine).toUInt()   else line   + modLine.toUInt(),
      if (modColumn < 0) column - (-modColumn).toUInt() else column + modColumn.toUInt(),
    )


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
