package io.foxcapades.lib.k.yaml.scan

import io.foxcapades.lib.k.yaml.bytes.A_SPACE
import io.foxcapades.lib.k.yaml.token.*
import io.foxcapades.lib.k.yaml.util.*

/**
 * # Fetch Folded String Token
 */
internal fun YAMLScannerImpl.fetchFoldedStringToken() {
  /*
  = Fetch Folded String Token

  Currently, we are looking at a `>` character.

  This character may legally only be followed by a handful of other characters
  matching the following pattern:

  [source, regexp]
  ----
  >([-+][0-9]*|[0-9]+[-+]?)?([ \t]+#.*|[ \t]*)?
  ----

  After that there may be an EOF, or a line break.

  So, first things first, lets get a start position for this token and eat our
  leader character (`>`).

  [source, kotlin]
  ----
  */
  val start = this.position.mark()

  this.haveContentOnThisLine = true

  skipASCII(this.reader, this.position)

  // Now lets grab (and clear) some buffers for us to use in this parsing
  // process.
  val bContent = this.contentBuffer1;   bContent.clear()
  val bLeadWS  = this.contentBuffer2;   bLeadWS.clear()
  val bTailWS  = this.trailingWSBuffer; bTailWS.clear()
  val bTailNL  = this.trailingNLBuffer; bTailNL.clear()

  // Define some variables that we will be using later in this process.
  val chompMode: BlockScalarChompMode
  val indentHint: UInt

  val blockIndent:       UInt
  val keepIndentAfter: UInt

  var keptLeadingSpaceCount = 0u

  detectBlockScalarIndicators(start) { cm, ih ->
    chompMode  = cm
    indentHint = ih
  }

  /*
  ----
  Now we have processed any indent hint and/or chomping indicator.  At this
  point we should expect to see a newline, spaces optionally followed by a
  comment, or the EOF.

  Attempt to eat whitespaces trailing after the scalar start indicator(s).

  If there are spaces, then it is valid for a comment to follow on this line,
  however, if there are not trailing spaces then it is not valid for a comment
  to appear.

  [source, kotlin]
  ----
  */
  if (this.skipBlanks() > 0) {

    this.reader.cache(1)

    when {
      this.reader.isPound()    -> TODO("handle comment trailing after folding scalar start indicator.  This comment should be kept and emitted _after_ the folding scalar")
      this.reader.isAnyBreak() -> {}
      this.reader.isEOF()      -> TODO("wrap up the scalar value (which is empty)")
      else                     -> TODO("handle invalid/unexpected character on the same line as the folding scalar start indicator")
    }
  } else {

    this.reader.cache(1)

    when {
      this.reader.isAnyBreak() -> {}
      this.reader.isEOF()      -> TODO("wrap up the scalar value (which is empty)")
      else                     -> TODO("handle invalid/unexpected character immediately following the folding scalar start indicator")
    }
  }
  /*
  ----
  If made it to this point, then we are at the end of the line containing the
  folding character start indicator.

  Our next step is to attempt to figure out the indentation level for our
  folding scalar content (which may be 0).  This will be determined by the
  amount of leading whitespace on the first non-empty line we encounter.

  [source, kotlin]
  ----
  */
  // Skip the newline that we are currently stopped at.
  skipNewLine(this.reader, this.position)

  // We are on a new line now, so we don't know if the line has any content on
  // it yet.
  this.haveContentOnThisLine = false

  while (true) {
    this.reader.cache(1)

    when {
      this.reader.isSpace() -> {
        bLeadWS.claimASCII(this.reader, this.position)
      }

      this.reader.isAnyBreak() -> {
        // We hit a newline, so ignore the "leading" whitespace because it was
        // a lie and wasn't actually leading to anything.
        bLeadWS.clear()

        // Record our newline because we keep these.
        bTailNL.claimNewLine(this.reader, this.position)
      }

      this.reader.isEOF() -> {
        TODO("we have an empty scalar that may have newlines that need to be collapsed")
      }

      else -> {
        // We found content on this line, remember that.
        this.haveContentOnThisLine = true
        this.indent = bLeadWS.size.toUInt()

        blockIndent = this.position.column
        break
      }
    }
  }

  // If we have a parent indent and the current indent is less than that, then
  // we are actually at the start of something else entirely.
  if (this.indents.isNotEmpty && this.indent <= this.indents.peek()) {
    TODO("we have an empty scalar that may have newlines that need to be collapsed")
  }

  // If we have a detected indent that is less than the indent required by the
  // provided indent hint.
  if (this.indent < indentHint) {
    TODO("we have an invalid indent value, decide if we should fail here or produce a warning and just change our indent hint from here on out")
  }

  keepIndentAfter = this.indent - indentHint
  /*
  So we've figured out our indent and indent hint, now lets start actually
  reading the content.

  In the process we are here (^):
  [source, yaml]
  ----
  >
    foo
    ^
  ----
  */
  while (true) {
    this.reader.cache(1)

    // If we hit a space
    if (this.reader.isSpace()) {
      // and we already have content on this line
      if (this.haveContentOnThisLine) {
        // we want to keep this whitespace as part of the content.
        bContent.claimASCII(this.reader, this.position)
      }

      // and we don't already have content on this line
      else {
        // then it is "leading" space
        this.indent++

        if (this.indent >= keepIndentAfter)
          keptLeadingSpaceCount++

        skipASCII(this.reader, this.position)
      }
    }

    // If we hit a line break
    else if (this.reader.isAnyBreak()) {
      // If we didn't have content on this line
      if (!this.haveContentOnThisLine) {
        // then zero out the leading space count because it turns out that the
        // leading space wasn't actually leading to anything.
        keptLeadingSpaceCount = 0u
      }

      bTailNL.claimNewLine(this.reader, this.position)
    }

    else if (this.reader.isEOF()) {
      return this.collapseFinishAndEmitFoldingScalarToken(
        bContent,
        bTailNL,
        blockIndent,
        chompMode,
        start
      )
    }

    else {
      this.haveContentOnThisLine = true

      if (keptLeadingSpaceCount > 0u) {
        // if we have a leading space count right now then we are on the first
        // character of the new line.  In this case we need to verify that the
        // indent of this line is not less than our required minimum indent
        // value.

        // If the indentation on this line was less than the required minimum
        // indent for this block scalar, then
        if (this.indent < blockIndent) {
          // If we have an indent less than the indent level detected for the
          // block scalar, then we will end our scalar here and call what we
          // are seeing now the start of a new value.
          return this.collapseFinishAndEmitFoldingScalarToken(
            bContent,
            bTailNL,
            blockIndent,
            chompMode,
            start
          )
        }

        // If we have leading whitespace on this line that is greater than any
        // spaces expected due to the indent hint, then we cannot collapse the
        // line breaks between this line and the last line with content.
        if (keptLeadingSpaceCount > indentHint) {
          while (bTailNL.isNotEmpty)
            bContent.claimNewLine(bTailNL)
        }

        // If we have more than one line break between this line and the last,
        else if (bTailNL.size > 1) {
          bTailNL.skipNewLine()
          while (bTailNL.isNotEmpty)
            bContent.claimNewLine(bTailNL)
        } else if (bTailNL.size == 1) {
          bContent.push(A_SPACE)
        }

        keptLeadingSpaceCount = 0u
      }

      bContent.claimUTF8(this.reader, this.position)
    }
  }
}

@OptIn(ExperimentalUnsignedTypes::class)
private fun YAMLScannerImpl.collapseFinishAndEmitFoldingScalarToken(
  scalarContent:    UByteBuffer,
  trailingNewLines: UByteBuffer,
  actualIndent:     UInt,
  chompingMode:     BlockScalarChompMode,
  start:            SourcePosition,
) {
  if (trailingNewLines.isNotEmpty) {
    if (chompingMode == BlockScalarChompModeStrip) {
      // Do not write trailing line breaks to buffer.
    }

    else if (chompingMode == BlockScalarChompModeClip) {
      scalarContent.claimNewLine(trailingNewLines)
    }

    else if (chompingMode == BlockScalarChompModeKeep) {
      while (trailingNewLines.isNotEmpty)
        scalarContent.claimNewLine(trailingNewLines)
    }

    else {
      throw IllegalStateException("invalid BlockScalarChompMode value")
    }
  }

  this.tokens.push(this.newFoldedScalarToken(scalarContent.popToArray(), actualIndent, start))
}

@OptIn(ExperimentalUnsignedTypes::class)
private fun YAMLScannerImpl.newFoldedScalarToken(
  value:    UByteArray,
  indent:   UInt,
  start:    SourcePosition,
  end:      SourcePosition = this.position.mark(),
) =
  YAMLTokenScalarFolded(
    UByteString(value),
    indent,
    start,
    end,
    getWarnings()
  )