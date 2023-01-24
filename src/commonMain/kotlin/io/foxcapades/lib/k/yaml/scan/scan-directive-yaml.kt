package io.foxcapades.lib.k.yaml.scan

import io.foxcapades.lib.k.yaml.DefaultYAMLVersion
import io.foxcapades.lib.k.yaml.YAMLVersion
import io.foxcapades.lib.k.yaml.err.UIntOverflowException
import io.foxcapades.lib.k.yaml.token.YAMLTokenDirectiveYAML
import io.foxcapades.lib.k.yaml.token.YAMLTokenInvalid
import io.foxcapades.lib.k.yaml.util.*

/**
 * # Fetch `YAML-DIRECTIVE` Token
 *
 * Attempts to parse the rest of the current line as a `YAML-DIRECTIVE` token
 * assuming the start of the line is `%YAML<WS>`.
 *
 * The next step is to try and parse the version value which _should_ be one
 * of `1.1` or `1.2` but it could be anything because input be weird
 * sometimes.
 *
 * @param startMark Mark of the start position of the token.
 */
internal fun YAMLScannerImpl.fetchYAMLDirectiveToken(startMark: SourcePosition) {
  // We have content on this line.
  this.lineContentIndicator = LineContentIndicatorContent

  // We have already skipped over `%YAML<WS>`.  Eat any extra whitespaces
  // until we encounter something else, which will hopefully be a decimal
  // digit.
  var trailingSpaceCount = skipBlanks() + 1

  // If after skipping over the blank space characters after `%YAML` we hit
  // the EOF, a line break, or a `#` character (the start of a comment), then
  // we have an incomplete token because there can be no version number
  // following on this line.
  if (reader.isPound() || reader.isAnyBreakOrEOF())
    return fetchIncompleteYAMLDirectiveToken(
      startMark,
      position.mark(modIndex = -trailingSpaceCount, modColumn = -trailingSpaceCount)
    )

  // If the next character we see is not a decimal digit, then we've got some
  // junk characters instead of a version number.
  if (!reader.isDecimalDigit())
    return fetchMalformedYAMLDirectiveToken(startMark)

  // Okay, so we are on a decimal digit, that is _hopefully_ the start of our
  // major version.

  // Try and parse the major version as a UInt from the stream.
  val major = try {
    parseUInt()
  } catch (e: UIntOverflowException) {
    version = DefaultYAMLVersion
    return fetchOverflowYAMLDirectiveToken(startMark, e.startMark, true)
  }

  // Now we have to ensure that the value right after the major version int
  // value is a period character.

  reader.cache(1)
  if (reader.isAnyBreakOrEOF())
    return fetchIncompleteYAMLDirectiveToken(startMark, position.mark())
  if (!reader.isPeriod())
    return fetchMalformedYAMLDirectiveToken(startMark)

  // Skip the `.` character.
  skipASCII(this.reader, this.position)

  reader.cache(1)
  if (reader.isAnyBreakOrEOF())
    return fetchIncompleteYAMLDirectiveToken(startMark, position.mark())
  if (!reader.isDecimalDigit())
    return fetchMalformedYAMLDirectiveToken(startMark)

  val minor = try {
    parseUInt()
  } catch (e: UIntOverflowException) {
    version = DefaultYAMLVersion
    return fetchOverflowYAMLDirectiveToken(startMark, e.startMark, false)
  }

  // Okay, so at this point we have parsed the following:
  //
  //   ^%YAML[ \t]+\d+\.\d+
  //
  // Now we need to make sure that there is nothing else on this line except
  // for maybe trailing whitespace characters and possibly a comment.

  reader.cache(1)
  trailingSpaceCount = 0

  // If the next character after the minor version int is a whitespace
  // character:
  if (reader.isBlank()) {
    // Eat the whitespace(s) until we hit something else.
    trailingSpaceCount = skipBlanks()

    // Attempt to cache a character in our reader buffer
    reader.cache(1)

    // If the next character after the whitespace(s) is NOT a `#`, is NOT a
    // line break, and is NOT the EOF, then we have some extra junk at the
    // end of our token line and the token is considered malformed.
    if (!(reader.isPound() || reader.isAnyBreakOrEOF()))
      return fetchMalformedYAMLDirectiveToken(startMark)
  }

  // Else (meaning we're not at a whitespace), if the next thing in the reader
  // buffer is NOT a line break and is NOT the EOF, then we have extra junk
  // right after our minor version number, meaning the token is malformed.
  else if (!reader.isAnyBreakOrEOF()) {
    return fetchMalformedYAMLDirectiveToken(startMark)
  }

  // If we've made it this far then we have parsed the following:
  //
  //   ^%YAML[ \t]+\d+\.\d+[ \t]*$
  //
  // This means that we have a valid _looking_ YAML directive.  We now need to
  // verify that the major and minor version numbers that the input gave us
  // actually amount to a valid YAML version.

  val endMark = position.mark(modIndex = -trailingSpaceCount, modColumn = -trailingSpaceCount)

  // If the major version is not `1`, then we should emit a warning for an
  // unsupported version and attempt to parse it as the default YAML version.
  if (major != 1u)
    return fetchUnsupportedYAMLDirectiveToken(major, minor, startMark, endMark)

  version = when (minor) {
    1u   -> YAMLVersion.VERSION_1_1
    2u   -> YAMLVersion.VERSION_1_2
    else -> return fetchUnsupportedYAMLDirectiveToken(major, minor, startMark, endMark)
  }

  tokens.push(newYAMLDirectiveToken(major, minor, startMark, endMark))
}

/**
 * # Fetch Invalid YAML Token for UInt Overflow
 *
 * Emits a warning, then fetches an `<INVALID>` token for the situation where
 * we were attempting to parse a part of the version number segment of a
 * `%YAML` token and either the major or minor version number overflows a the
 * `uint32` type.
 *
 * ## Generating the Warning
 *
 * The primary purpose of this function is to generate a helpful warning
 * before passing the invalid token up to the caller.  This warning is
 * generated by counting the number digits in the total major/minor version
 * number as it appears in the source YAML to provide a start and end mark for
 * the number value that would have overflowed our `uint32`.
 *
 * This allows consumers of these tokens to generate markup to render errors
 * with context hints such as underlining the invalid value.
 *
 * **Example Error Hint**
 * ```log
 * WARN: uint32 overflow while attempting to parse a %YAML directive major version
 * ----
 * %YAML 99999999999999999999.1 # this is an invalid YAML version
 *       ^^^^^^^^^^^^^^^^^^^^
 * ----
 * ```
 */
private fun YAMLScannerImpl.fetchOverflowYAMLDirectiveToken(
  tokenStartMark: SourcePosition,
  intStartMark: SourcePosition,
  isMajor:        Boolean,
) {
  // Ensure that we have a character in the buffer to test against.
  reader.cache(1)

  // Skip over all the decimal digit characters until we hit the end of this
  // absurdly long int value.
  while (reader.isDecimalDigit()) {
    // Skip it as ASCII because if it's a decimal digit then we know it's an
    // ASCII character
    skipASCII(this.reader, this.position)

    // Cache another character to test on the next pass of the loop
    reader.cache(1)
  }

  // Emit a warning about the overflow, passing in:
  //
  // - A warning message that is slightly customized based on whether it is a
  //   major or minor version number that was overflowed,
  // - A mark for the start of the absurdly long int value
  // - The current position (which will be the first non-digit character after
  //   the start of the long int, or the EOF
  warn(
    "uint32 overflow while attempting to parse a %YAML directive ${if (isMajor) "major" else "minor"} version",
    intStartMark,
    position.mark()
  )

  return emitInvalidToken(tokenStartMark, skipUntilCommentBreakOrEOF())
}

/**
 * Fetches an [invalid token][YAMLTokenInvalid] for the case where the
 * YAML directive token contains some junk characters.
 *
 * When called, the reader buffer cursor will be on the first junk character.
 *
 * The job of this method is to find the end of the junk on the line and
 * create a warning highlighting that junk before queueing up an `INVALID`
 * token and returning.
 */
private fun YAMLScannerImpl.fetchMalformedYAMLDirectiveToken(tokenStartMark: SourcePosition) {
  val junkStart = position.mark()
  val junkEnd   = skipUntilCommentBreakOrEOF()

  warn("malformed %YAML directive", junkStart, junkEnd)
  return emitInvalidToken(tokenStartMark, junkEnd)
}

private fun YAMLScannerImpl.fetchIncompleteYAMLDirectiveToken(start: SourcePosition, end: SourcePosition) {
  version = DefaultYAMLVersion
  warn("incomplete %YAML directive; assuming YAML version $DefaultYAMLVersion", start, end)
  return emitInvalidToken(start, end)
}

private fun YAMLScannerImpl.fetchUnsupportedYAMLDirectiveToken(
  major: UInt,
  minor: UInt,
  start: SourcePosition,
  end: SourcePosition
) {
  version = DefaultYAMLVersion
  warn("unsupported YAML version $major.$minor; attempting to scan input as YAML version $DefaultYAMLVersion")
  tokens.push(newYAMLDirectiveToken(major, minor, start, end))
}

@Suppress("NOTHING_TO_INLINE")
private inline fun YAMLScannerImpl.newYAMLDirectiveToken(
  major: UInt,
  minor: UInt,
  start: SourcePosition,
  end:   SourcePosition,
) =
  YAMLTokenDirectiveYAML(major, minor, start, end, getWarnings())
