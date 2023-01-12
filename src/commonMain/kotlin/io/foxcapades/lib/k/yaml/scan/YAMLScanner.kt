package io.foxcapades.lib.k.yaml.scan

import io.foxcapades.lib.k.yaml.YAMLVersion
import io.foxcapades.lib.k.yaml.bytes.*
import io.foxcapades.lib.k.yaml.bytes.A_DASH
import io.foxcapades.lib.k.yaml.bytes.A_LF
import io.foxcapades.lib.k.yaml.bytes.A_PERCENT
import io.foxcapades.lib.k.yaml.bytes.A_PERIOD
import io.foxcapades.lib.k.yaml.bytes.A_SQ_OP
import io.foxcapades.lib.k.yaml.err.YAMLScannerException
import io.foxcapades.lib.k.yaml.read.*
import io.foxcapades.lib.k.yaml.util.SourcePositionTracker
import io.foxcapades.lib.k.yaml.util.UByteBuffer
import io.foxcapades.lib.k.yaml.util.takeCodepointFrom

class YAMLScanner {
  private var streamStarted = false
  private var streamEnded = false

  private val simpleKeys = SimpleKeyStack()

  private val tokens = TokenQueue()

  private val position = SourcePositionTracker()

  private val reader: YAMLReader

  private var tokensParsed = 0

  private var version = YAMLVersion.VERSION_1_2

  private var flowLevel = 0u

  private var indent = -1

  private var simpleKeyAllowed = false

  private inline val inFlow
    get() = flowLevel > 0u

  fun hasNextToken() = !streamEnded

  fun nextToken(): YAMLToken {
    if (streamEnded)
      throw IllegalStateException("called nextToken on a YAMLScanner that has already consumed its entire input stream")

    if (tokens.isEmpty)
      fetchMoreTokens()

    val out = tokens.pop()

    if (out.type == YAMLTokenType.StreamEnd)
      streamEnded = true

    return out
  }

  private fun _skip() {
    reader.skipCodepoint()
    position.incPosition()
  }

  private fun _skipLine_1_1() {
    if (reader.isCRLF()) {
      reader.skipCodepoints(2)
      position.incLine(2u)
    } else if (reader.isBreak_1_1()) {
      reader.skipCodepoint()
      position.incLine()
    } else {
      throw YAMLScannerException("called _skipLine_1_1 when not on a line break character", position.toSourcePosition())
    }
  }

  private fun _skipLine_1_2() {
    if (reader.isCRLF()) {
      reader.skipCodepoints(2)
      position.incLine(2u)
    } else if (reader.isBreak_1_2()) {
      reader.skipCodepoint()
      position.incLine()
    } else {
      throw YAMLScannerException("called _skipLine_1_2 when not on a line break character", position.toSourcePosition())
    }
  }

  private fun _read(string: UByteBuffer) {
    if (!string.takeCodepointFrom(reader.utf8Buffer)) {
      throw YAMLScannerException("invalid utf-8 codepoint", position.toSourcePosition())
    }
    position.incPosition()
  }

  private fun _readLine_1_1(string: UByteBuffer) {
    if (reader.isCRLF()) {
      string.push(A_LF)
      reader.skipCodepoints(2)
      position.incLine(2u)
    } else if (reader.isCROrLF() || reader.isNEL()) {
      string.push(A_LF)
      reader.skipCodepoint()
      position.incLine()
    } else if (reader.isLSOrPS()) {
      string.takeCodepointFrom(reader.utf8Buffer)
      position.incLine()
    } else {
      throw YAMLScannerException("called _readLine_1_1 when not on a valid YAML 1.1 line break character", position.toSourcePosition())
    }
  }

  private fun _readLine_1_2(string: UByteBuffer) {
    if (reader.isCRLF()) {
      string.push(A_LF)
      reader.skipCodepoints(2)
      position.incLine(2u)
    } else if (reader.isCR() || reader.isLF()) {
      string.push(A_LF)
      reader.skipCodepoint()
      position.incLine()
    } else {
      throw YAMLScannerException("called _readLine_1_2 when not on a valid YAML 1.2 line break character", position.toSourcePosition())
    }
  }

  private fun fetchMoreTokens() {
    var needMoreTokens: Boolean

    while (true) {
      needMoreTokens = false

      if (tokens.isEmpty) {
        needMoreTokens = true
      } else {
        staleSimpleKeys()

        // Check if any potential simple key may occupy the head position
        var i = 0
        while (i < simpleKeys.size) {
          val sk = simpleKeys[i++]

          if (sk.possible && sk.tokenNumber == tokensParsed) {
            needMoreTokens = true
            break
          }
        }
      }

      if (!needMoreTokens)
        break

      fetchNextToken()
    }
  }

  private fun fetchNextToken() {
    // Ensure that the reader is initialized (this will cause it to determine
    // the stream encoding if it hasn't already done so)
    reader.cache(1)

    // Check if the stream just started.  If so return a STREAM-START token.
    if (!streamStarted)
      return fetchStreamStart()

    // Eat whitespaces until we reach the next token.
    scanToNextToken()

    // Remove obsolete potential simple mapping keys
    staleSimpleKeys()

    // Check the indentation level against the current column
    unrollIndent(position.column)

    // Ensure that the reader buffer contains at least 4 bytes.
    // 4 bytes is the length of the longest indicators (`---<WS>` or `...<WS>`)
    reader.cache(4)

    // Have we reached the end of the stream?
    if (reader.atEOF && reader.isEmpty)
      return fetchStreamEnd()

    // If the buffer is empty for some reason, bail here, this will get called
    // again in the loop in [fetchMoreTokens].
    if (reader.isEmpty)
      return

    // Are we at a directive marker?
    if (reader.check(A_PERCENT))
      return fetchDirective()

    // Is it a comment?
    if (reader.uCheck(A_POUND))
      return fetchComment()

    // Are we at a document start indicator?
    if (
      // Stream ends on a valid document start indicator
      (
        reader.buffered == 3
        && reader.atEOF
        && reader.uCheck(A_DASH, 0)
        && reader.uCheck(A_DASH, 1)
        && reader.uCheck(A_DASH, 2)
      )
      // Stream does not end but contains a valid document start indicator
      || (
        reader.buffered > 3
        && reader.uCheck(A_DASH, 0)
        && reader.uCheck(A_DASH, 1)
        && reader.uCheck(A_DASH, 2)
        && reader.uIsBlankOrBreak(version, 3)
      )
    ) {
      return fetchDocumentIndicator(YAMLTokenType.DocumentStart)
    }

    // Are we at a document end indicator?
    if (
      // Stream ends on a valid document end indicator
      (
        reader.buffered == 3
        && reader.atEOF
        && reader.uCheck(A_PERIOD, 0)
        && reader.uCheck(A_PERIOD, 1)
        && reader.uCheck(A_PERIOD, 2)
      )
      // Stream does not end but contains a valid document end indicator
      || (
        reader.buffered > 3
        && reader.uCheck(A_PERIOD, 0)
        && reader.uCheck(A_PERIOD, 1)
        && reader.uCheck(A_PERIOD, 2)
        && reader.uIsBlankOrBreak(version, 3)
      )
    ) {
      return fetchDocumentIndicator(YAMLTokenType.DocumentEnd)
    }

    // uCheck is used in the following block because we know at this point that
    // the reader buffer contains at least one byte in it.
    when {
      // Are we at a flow sequence start indicator?
      reader.uCheck(A_SQ_OP) -> return fetchFlowCollectionStart(YAMLTokenType.FlowSequenceStart)
      // Are we at a flow mapping start indicator?
      reader.uCheck(A_CU_OP) -> return fetchFlowCollectionStart(YAMLTokenType.FlowMappingStart)
      // Are we at a flow sequence end indicator?
      reader.uCheck(A_SQ_CL) -> return fetchFlowCollectionEnd(YAMLTokenType.FlowSequenceEnd)
      // Are we at a flow mapping end indicator?
      reader.uCheck(A_CU_CL) -> return fetchFlowCollectionEnd(YAMLTokenType.FlowMappingEnd)
      // Are we at a flow entry separator indicator?
      reader.uCheck(A_COMMA) -> return fetchFlowEntry()
    }

    // Are we at a block entry indicator?
    if (reader.uCheck(A_DASH) && reader.isBlankBreakOrEOF(version, 1))
      return fetchBlockEntry()

    // Are we at a complex mapping key indicator?
    if (reader.uCheck(A_QUESTION) && (inFlow || reader.isBlankBreakOrEOF(version, 1)))
      return fetchMappingKey()

    // Are we at a mapping value indicator?
    if (reader.uCheck(A_COLON) && (inFlow || reader.isBlankBreakOrEOF(version, 1)))
      return fetchMappingValue()

    // Are we at an alias?
    if (reader.uCheck(A_ASTERISK))
      return fetchAnchor(YAMLTokenType.Alias)

    // Are we at an anchor?
    if (reader.uCheck(A_AMP))
      return fetchAnchor(YAMLTokenType.Anchor)

    // Are we at a tag?
    if (reader.uCheck(A_EXCLAIM))
      return fetchTag()

    // Is it a literal scalar?
    if (reader.uCheck(A_PIPE) && !inFlow)
      return fetchBlockScalar(true)

    // Is it a folded string?
    if (reader.uCheck(A_GREATER) && !inFlow)
      return fetchBlockScalar(false)

    // Is it a single quoted scalar?
    if (reader.uCheck(A_APOS))
      return fetchFlowScalar(true)

    // Is it a double quoted scalar?
    if (reader.uCheck(A_DBL_QUOTE))
      return fetchFlowScalar(false)

    // PLAIN SCALAR CHECK
    //
    // A plain scalar may generally begin with any non-blank character except
    // the following:
    //
    //   - ? : , [ ] { } # & * ! | > ' " % @ `
    //
    // However, we have already completely ruled out the following characters
    // with the checks above:
    //
    //   , [ ] { } # % & * ! ' "
    //
    // This means we only need to test for:
    //
    //   - ? : | > @ `
    //
    // The rules for these, however, are a bit sticky because in a block
    // context, the following _are_ permitted to being a scalar, provided that
    // they are not followed immediately by a blank character:
    //
    //   - ? :
    //
    // Additionally, in a flow context, the following is allowed to begin a
    // scalar (again provided that it is not followed immediately by a blank
    // character):
    //
    //   -
    if (
      !(
        reader.isBlankBreakOrEOF(version) // TODO: How would this happen
        || reader.uCheck(A_PIPE)          // We have already ruled out | in a block context
        || reader.uCheck(A_GREATER)       // We have already ruled out > in a block context
        || reader.uCheck(A_AT)
        || reader.uCheck(A_GRAVE)
      )
      || (!inFlow && (reader.uCheck(A_QUESTION) || reader.uCheck(A_COLON)) && !reader.isBlankBreakOrEOF(version, 1))
    ) {
      return fetchPlainScalar()
    }

    throw YAMLScannerException("found a character that cannot start any token while scanning for the next token in the stream", position.toSourcePosition())
  }

  /**
   * Check the list of potential simple keys and remove the positions that
   * cannot contain simple keys anymore.
   */
  private fun staleSimpleKeys() {
    var i = 0

    // Check for a simple key for each flow level
    while (i < simpleKeys.size) {
      val sk = simpleKeys[i++]

      // The spec requires that a simple key:
      //
      // - is limited to a single line
      // - is shorter than 1024 characters

      if (sk.possible && (sk.mark.line < position.line || sk.mark.index + 1024u < position.index)) {
        // Check if the potential simple key to be removed is required
        if (sk.required)
          throw YAMLScannerException("while scanning a simple key could not find expected ':' character", sk.mark)

        sk.possible = false
      }
    }
  }

  /**
   * Check if a simple key may start at the current position and add it if
   * needed.
   */
  private fun saveSimpleKey() {
    // A simple key is required at the current position if the scanner is in a
    // block context and the current column coincides with the indentation
    // level.
    val required = !inFlow && indent.toUInt() == position.column

    // If the current position may start a simple key, save it.
    if (simpleKeyAllowed) {
      val sk = SimpleKey(true, required, tokensParsed + tokens.size, position.toSourcePosition())

      removeSimpleKey()

      simpleKeys[0] = sk
    }
  }

  /**
   * Remove a potential simple key at the current flow level.
   *
   * TODO: This doesn't remove shit, it just marks it as not possible
   */
  private fun removeSimpleKey() {
    val sk = simpleKeys.peek()

    if (sk.possible && sk.required) {
      throw YAMLScannerException("while scanning a simple key could not find expected ':' character", sk.mark)
    }

    sk.possible = false
  }

  private fun increaseFlowLevel() {
    simpleKeys.push(SimpleKey())
    flowLevel++
  }

  private fun decreaseFlowLevel() {
    if (inFlow) {
      flowLevel--
      simpleKeys.pop()
    }
  }
}