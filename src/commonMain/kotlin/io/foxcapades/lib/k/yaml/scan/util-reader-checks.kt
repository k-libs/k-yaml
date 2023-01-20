@file:Suppress("NOTHING_TO_INLINE")

package io.foxcapades.lib.k.yaml.scan

import io.foxcapades.lib.k.yaml.read.YAMLReaderBuffer
import io.foxcapades.lib.k.yaml.util.isAnyBreak
import io.foxcapades.lib.k.yaml.util.isBlankOrAnyBreak

// region Safe Tests

internal inline fun YAMLReaderBuffer.isEOF(offset: Int = 0) =
  size <= offset && atEOF

internal inline fun YAMLReaderBuffer.isAnyBreakOrEOF(offset: Int = 0) =
  isAnyBreak(offset) || isEOF(offset)

internal inline fun YAMLReaderBuffer.isBlankAnyBreakOrEOF(offset: Int = 0) =
  isBlankOrAnyBreak(offset) || isEOF(offset)

// endregion Safe Tests
