@file:Suppress("NOTHING_TO_INLINE")

package io.foxcapades.lib.k.yaml.scan.stream

import io.foxcapades.lib.k.yaml.read.BufferedUTFStreamReader
import io.foxcapades.lib.k.yaml.util.isAnyBreak
import io.foxcapades.lib.k.yaml.util.isBlankOrAnyBreak

// region Safe Tests

internal inline fun BufferedUTFStreamReader.isEOF(offset: Int = 0) =
  size <= offset && atEOF

internal inline fun BufferedUTFStreamReader.isAnyBreakOrEOF(offset: Int = 0) =
  isAnyBreak(offset) || isEOF(offset)

internal inline fun BufferedUTFStreamReader.isBlankAnyBreakOrEOF(offset: Int = 0) =
  isBlankOrAnyBreak(offset) || isEOF(offset)

// endregion Safe Tests
