@file:Suppress("NOTHING_TO_INLINE")

package io.foxcapades.lib.k.yaml.scan

import io.foxcapades.lib.k.yaml.util.isAnyBreak
import io.foxcapades.lib.k.yaml.util.isBlankOrAnyBreak

// region Safe Tests

internal inline fun YAMLScannerImpl.haveEOF(offset: Int = 0) =
  reader.size <= offset && reader.atEOF

internal inline fun YAMLScannerImpl.haveAnyBreakOrEOF(offset: Int = 0) =
  reader.isAnyBreak(offset) || haveEOF(offset)

internal inline fun YAMLScannerImpl.haveBlankAnyBreakOrEOF(offset: Int = 0) =
  reader.isBlankOrAnyBreak(offset) || haveEOF(offset)

// endregion Safe Tests
