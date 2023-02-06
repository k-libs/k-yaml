package io.klibs.yaml.util

/**
 * # Source Position Tracker
 *
 * This type is used to keep a moving 'cursor position' in a source YAML
 * stream.
 *
 * @author Elizabeth Paige Harper - https://github.com/foxcapades
 * @since 0.1.0
 */
class SourcePositionTracker {
  var index: UInt
    private set

  var line: UInt
    private set

  var column: UInt
    private set

  constructor() {
    index  = 0u
    line   = 0u
    column = 0u
  }

  private constructor(index: UInt, line: UInt, column: UInt) {
    this.index  = index
    this.line   = line
    this.column = column
  }

  /**
   * # Create Mark
   *
   * Creates an immutable [mark][SourcePosition] from the current position of
   * this tracker, or optionally from a new position calculated from the current
   * values of this position tracker modified by any given `mod*` parameter
   * values.
   *
   * ## Examples
   *
   * ### 1. Creating a mark at the current position
   *
   * ```
   * val here = this.mark()
   * ```
   *
   * ### 2. Creating a mark one non-newline character forwards
   *
   * ```
   * val there = this.mark(modIndex = 1, modColumn = 1)
   * ```
   *
   * ### 3. Creating a mark one CRLF backwards
   *
   * Perhaps you have to create a mark because you now know the CRLF you just
   * passed is in the wrong spot, and you want to make a warning.  In a
   * situation like this, you need to create a position reversed or rewound from
   * the current position.
   *
   * For this particular situation, assuming we know that the last position
   * update was caused by a `CRLF` combo in the input stream, we can create a
   * mark for the source position before that `CRLF` by doing the following:
   *
   * ```
   * val beforeHere = this.mark(modIndex = -2, modLine = -1)
   * ```
   *
   * In the above example, we passed the [mark]
   *
   *
   * ## Mod Values
   *
   * The mod values are applied by adding the parameter value to the value of
   * the matching class field.  The pairing is, and math is performed as
   * follows:
   *
   * ```
   * out.index  = this.index  + modIndex
   * out.line   = this.line   + modLine
   * out.column = this.column + modColumn
   * ```
   *
   * If the desired mark is _before_ the current position in some manner, the
   * given `mod` values would be negative to reflect this.
   *
   * ### Determining Mod Values
   *
   * The mod values are related the same way the class fields are related in
   * that the [modIndex] field is a sum of all characters making up the
   * [modLine] and [modColumn] values.
   *
   * While in most situations, `modIndex` will be equal to the sum of `modLine`
   * and `modColumn`, it is tracking a related, but different thing entirely,
   * which is the number of UTF-8 characters that were read to _reach_ the
   * current `modLine` and `modColumn`.
   *
   * This means that a correct `modIndex` value will always be _at minimum_ the
   * sum of `modLine` and `modColumn`, but will be greater than that sum in
   * situations where more than one UTF-8 character comprised a single
   * line/column cursor index; the most common example of this being `\r\n`
   * where we eat 2 characters to form the single position forward of moving to
   * the next line:
   *
   * ```
   * line++
   * column = 0
   * ```
   *
   * Callers must track or otherwise know the correct `modIndex` value in
   * relation to the actual source characters that would fall within that raw
   * character range of the current position.
   *
   * This method has no mechanism for validating that the `mod*` inputs given to
   * it are in any way sane.
   *
   * @param modIndex The value that will be added to this position instance's
   * [index] value to produce the [SourcePosition.index] value of the returned
   * [SourcePosition] instance.
   *
   * Default = `0`
   *
   * @param modLine The value that will be added to this position instance's
   * [line] value to produce the [SourcePosition.line] value of the returned
   * [SourcePosition] instance.
   *
   * Default = `0`
   *
   * @param modColumn The value that will be added to this position instance's
   * [column] value to produce the [SourcePosition.column] value of the returned
   * [SourcePosition] instance.
   *
   * @return A new [SourcePosition] instance containing the current values of
   * this [SourcePositionTracker] instance, optionally modified by any provided
   * `mod*` arguments.
   */
  fun mark(
    modIndex:  Int = 0,
    modLine:   Int = 0,
    modColumn: Int = 0,
  ) = SourcePosition(
    if (modIndex < 0)  index  - (-modIndex).toUInt()  else index  + modIndex.toUInt(),
    if (modLine < 0)   line   - (-modLine).toUInt()   else line   + modLine.toUInt(),
    if (modColumn < 0) column - (-modColumn).toUInt() else column + modColumn.toUInt()
  )

  fun become(other: SourcePositionTracker) {
    index  = other.index
    line   = other.line
    column = other.column
  }

  fun reset() {
    index  = 0u
    line   = 0u
    column = 0u
  }

  fun copy() = SourcePositionTracker(index, line, column)

  fun incPosition(chars: UInt = 1u) {
    index += chars
    column += chars
  }

  fun incLine(chars: UInt = 1u) {
    index += chars
    line++
    column = 0u
  }

  override fun toString() =
    "SourcePositionTracker(index=$index, line=$line, column=$column)"

  override fun equals(other: Any?) =
    this === other || (other is SourcePosition && index == other.index && line == other.line  && column == other.column)

  override fun hashCode(): Int {
    var result = index.hashCode()
    result = 31 * result + line.hashCode()
    result = 31 * result + column.hashCode()
    return result
  }
}
