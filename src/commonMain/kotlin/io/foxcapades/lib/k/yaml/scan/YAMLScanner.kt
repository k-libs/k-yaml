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

  private var indents = IndentStack()

  private var indent = -1

  private var simpleKeyAllowed = false

  private inline val inFlow
    get() = flowLevel > 0u

  fun hasNextToken() = !streamEnded

  fun nextToken(): YAMLToken {
  }

  private val tokenBuffer = UByteBuffer(2048)

  private fun handleStreamStart() {
    if (!streamStarted)
      return fetchStreamStart()

    scanToNextToken()

    reader.cache(4)

    when {
      reader.check(A_PERCENT)  -> TODO("handle directive")
      reader.check(A_DASH)     -> TODO("handle document start or block sequence entry or scalar start or simple mapping key start")
      reader.check(A_PERIOD)   -> TODO("handle document end or scalar start or simple mapping key start")
      reader.check(A_QUESTION) -> TODO("handle complex mapping key start")
      reader.check(A_AMP)      ->
    }

    if (reader.atEOF && reader.isEmpty)
      return fetchStreamEnd()
  }
}