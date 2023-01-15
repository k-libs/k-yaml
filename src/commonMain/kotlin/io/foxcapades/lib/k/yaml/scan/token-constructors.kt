@file:Suppress("NOTHING_TO_INLINE")

package io.foxcapades.lib.k.yaml.scan

import io.foxcapades.lib.k.yaml.YAMLEncoding
import io.foxcapades.lib.k.yaml.util.SourcePosition

/**
 * [VERSION-DIRECTIVE][YAMLTokenType.VersionDirective]
 */
internal inline fun YAMLScanner.newYAMLDirectiveToken(
  major: UInt,
  minor: UInt,
  start: SourcePosition,
  end:   SourcePosition,
) =
  YAMLToken(
    YAMLTokenType.VersionDirective,
    YAMLTokenDataVersionDirective(major, minor),
    start,
    end,
    warnings.popToArray()
  )

/**
 * [TAG-DIRECTIVE][YAMLTokenType.TagDirective]
 */
@OptIn(ExperimentalUnsignedTypes::class)
internal inline fun YAMLScanner.newTagDirectiveToken(
  handle: UByteArray,
  prefix: UByteArray,
  start:  SourcePosition,
  end:    SourcePosition,
) =
  YAMLToken(YAMLTokenType.TagDirective, YAMLTokenDataTagDirective(handle, prefix), start, end, warnings.popToArray())

/**
 * [STREAM-START][YAMLTokenType.StreamStart]
 */
internal inline fun YAMLScanner.newStreamStartToken(
  encoding: YAMLEncoding,
  start:    SourcePosition,
  end:      SourcePosition,
) =
  YAMLToken(YAMLTokenType.StreamStart, YAMLTokenDataStreamStart(encoding), start, end, warnings.popToArray())

/**
 * [STREAM-END][YAMLTokenType.StreamEnd]
 */
internal inline fun YAMLScanner.newStreamEndToken(start: SourcePosition, end: SourcePosition) =
  YAMLToken(YAMLTokenType.StreamEnd, null, start, end, warnings.popToArray())

@OptIn(ExperimentalUnsignedTypes::class)
internal inline fun YAMLScanner.newPlainScalarToken(value: UByteArray, start: SourcePosition, end: SourcePosition) =
  YAMLToken(YAMLTokenType.Scalar, YAMLTokenDataScalar(value, YAMLScalarStyle.Plain), start, end, warnings.popToArray())

internal inline fun YAMLScanner.newInvalidToken(start: SourcePosition, end: SourcePosition) =
  YAMLToken(YAMLTokenType.Invalid, null, start, end, warnings.popToArray())
