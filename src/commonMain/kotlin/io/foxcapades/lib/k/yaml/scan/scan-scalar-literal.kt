package io.foxcapades.lib.k.yaml.scan

import io.foxcapades.lib.k.yaml.err.UIntOverflowException
import io.foxcapades.lib.k.yaml.err.YAMLScannerException
import io.foxcapades.lib.k.yaml.util.*
import io.foxcapades.lib.k.yaml.util.isDash
import io.foxcapades.lib.k.yaml.util.isDecimalDigit
import io.foxcapades.lib.k.yaml.util.isPlus
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

internal fun YAMLScannerImpl.fetchLiteralStringToken() {
  val start     = this.position.mark()
  val indent    = this.indent
  val minIndent = if (this.atStartOfLine) 0u else this.indent + 1u
  val content   = contentBuffer1
  val leadWS    = contentBuffer2
  val tailWS    = trailingWSBuffer
  val tailNL    = trailingNLBuffer

  val chompMode:  BlockScalarChompMode
  val indentHint: UInt

  this.haveContentOnThisLine = true

  content.clear()
  leadWS.clear()
  tailWS.clear()
  tailNL.clear()

  skipASCII(this.reader, this.position)

  detectBlockScalarIndicators(start) { cm, ih ->
    chompMode  = cm
    indentHint = ih
  }

  if (this.skipBlanks() > 0) {
    this.reader.cache(1)

    when {
      this.reader.isPound()    -> TODO("handle trailing comment after a literal scalar start indicator.  This comment should be kept and emitted _after_ the scalar token")
      this.reader.isAnyBreak() -> {}
      this.reader.isEOF()      -> TODO("wrap up the scalar value (which is empty)")
      else                     -> TODO("handle invalid/unexpected character on the same line as the literal scalar start indicator")
    }
  } else {
    this.reader.cache(1)

    when {
      this.reader.isAnyBreak() -> {}
      this.reader.isEOF()      -> TODO("wrap up the scalar value (which is empty)")
      else                     -> TODO("handle invalid/unexpected character immediately following the literal scalar start indicator")
    }
  }

  skipNewLine(this.reader, this.position)
  this.haveContentOnThisLine = false

  // Determine the scalar block's indent level
  while (true) {
    this.reader.cache(1)

    if (this.reader.isSpace()) {
      leadWS.claimASCII(this.reader, this.position)
      this.indent++
    }

    else if (this.reader.isAnyBreak()) {
      leadWS.clear()
      tailNL.claimNewLine(this.reader, this.position)
      this.haveContentOnThisLine = false
      this.indent = 0u
    }

    else if (this.reader.isEOF()) {
      TODO("we have an empty scalar that may have newlines that need to be appended to the literal content")
    }
  }

  TODO("fetch pipe string")
}

@OptIn(ExperimentalContracts::class)
internal inline fun YAMLScannerImpl.detectBlockScalarIndicators(
  start: SourcePosition,
  fn: (cm: BlockScalarChompMode, ih: UInt) -> Unit
) {
  contract {
    callsInPlace(fn, InvocationKind.EXACTLY_ONCE)
  }

  val chompMode:  BlockScalarChompMode
  val indentHint: UInt

  this.reader.cache(1)

  if (this.reader.isPlus() || this.reader.isDash()) {
    chompMode = this.parseUByte()

    this.reader.cache(1)

    indentHint = if (this.reader.isDecimalDigit())
      try { this.parseUInt() }
      catch (e: UIntOverflowException) {
        throw YAMLScannerException("block scalar indent hint value overflows type uint32", start.copy(2, 0, 2))
      }
    else
      0u
  } else if (this.reader.isDecimalDigit()) {
    indentHint = try { this.parseUInt() }
      catch (e: UIntOverflowException) {
        throw YAMLScannerException("block scalar indent hint value overflows type uint32", start.copy(2, 0, 2))
      }

    this.reader.cache(1)

    chompMode = if (this.reader.isPlus() || this.reader.isDash())
      this.parseUByte()
    else
      BlockScalarChompModeClip
  } else {
    indentHint = 0u
    chompMode  = BlockScalarChompModeClip
  }

  fn(chompMode, indentHint)
}