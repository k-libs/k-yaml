package io.foxcapades.lib.k.yaml.scan

import io.foxcapades.lib.k.yaml.bytes.A_LF
import io.foxcapades.lib.k.yaml.err.YAMLScannerException
import io.foxcapades.lib.k.yaml.read.*
import io.foxcapades.lib.k.yaml.util.SourcePositionTracker
import io.foxcapades.lib.k.yaml.util.UByteBuffer
import io.foxcapades.lib.k.yaml.util.takeCodepointFrom

class YAMLScanner {
  private var streamStarted = false
  private var streamEnded = false

  private val tokens: TokenQueue

  private val reader: YAMLReader

  private val position: SourcePositionTracker

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
    
  }

}