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
  YAMLToken(YAMLTokenType.VersionDirective, YAMLTokenDataVersionDirective(major, minor), start, end, getWarnings())

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
  YAMLToken(YAMLTokenType.TagDirective, YAMLTokenDataTagDirective(handle, prefix), start, end, getWarnings())

/**
 * [STREAM-START][YAMLTokenType.StreamStart]
 */
internal inline fun YAMLScanner.newStreamStartToken(
  encoding: YAMLEncoding,
  start:    SourcePosition,
  end:      SourcePosition,
): YAMLToken {
  return YAMLToken(YAMLTokenType.StreamStart, YAMLTokenDataStreamStart(encoding), start, end, getWarnings())
}

/**
 * [STREAM-END][YAMLTokenType.StreamEnd]
 */
internal inline fun YAMLScanner.newStreamEndToken(start: SourcePosition, end: SourcePosition) =
  YAMLToken(YAMLTokenType.StreamEnd, null, start, end, getWarnings())

/**
 * [DOCUMENT-START][YAMLTokenType.DocumentStart]
 */
internal inline fun YAMLScanner.newDocumentStartToken(start: SourcePosition, end: SourcePosition) =
  YAMLToken(YAMLTokenType.DocumentStart, null, start, end, getWarnings())

/**
 * [DOCUMENT-END][YAMLTokenType.DocumentEnd]
 */
internal inline fun YAMLScanner.newDocumentEndToken(start: SourcePosition, end: SourcePosition) =
  YAMLToken(YAMLTokenType.DocumentEnd, null, start, end, getWarnings())

/**
 * [SCALAR][YAMLTokenType.Scalar]
 */
@OptIn(ExperimentalUnsignedTypes::class)
internal inline fun YAMLScanner.newPlainScalarToken(value: UByteArray, start: SourcePosition, end: SourcePosition) =
  YAMLToken(YAMLTokenType.Scalar, YAMLTokenDataScalar(value, YAMLScalarStyle.Plain), start, end, getWarnings())

/**
 * [MAPPING-KEY][YAMLTokenType.MappingKey]
 */
internal inline fun YAMLScanner.newMappingKeyIndicatorToken(start: SourcePosition, end: SourcePosition) =
  YAMLToken(YAMLTokenType.MappingKey, null, start, end, getWarnings())

/**
 * [MAPPING-VALUE][YAMLTokenType.MappingValue]
 */
internal inline fun YAMLScanner.newMappingValueIndicatorToken(start: SourcePosition, end: SourcePosition) =
  YAMLToken(YAMLTokenType.MappingValue, null, start, end, getWarnings())

internal inline fun YAMLScanner.newSequenceEntryIndicatorToken(start: SourcePosition, end: SourcePosition) =
  YAMLToken(YAMLTokenType.BlockEntry, null, start, end, getWarnings())

/**
 * [FLOW-MAPPING-START][YAMLTokenType.FlowMappingStart]
 */
internal inline fun YAMLScanner.newFlowMappingStartToken(start: SourcePosition, end: SourcePosition) =
  YAMLToken(YAMLTokenType.FlowMappingStart, null, start, end, getWarnings())

/**
 * [FLOW-MAPPING-END][YAMLTokenType.FlowMappingEnd]
 */
internal inline fun YAMLScanner.newFlowMappingEndToken(start: SourcePosition, end: SourcePosition) =
  YAMLToken(YAMLTokenType.FlowMappingEnd, null, start, end, getWarnings())

/**
 * [FLOW-SEQUENCE-START][YAMLTokenType.FlowSequenceStart]
 */
internal inline fun YAMLScanner.newFlowSequenceStartToken(start: SourcePosition, end: SourcePosition) =
  YAMLToken(YAMLTokenType.FlowSequenceStart, null, start, end, getWarnings())

/**
 * [FLOW-SEQUENCE-END][YAMLTokenType.FlowSequenceEnd]
 */
internal inline fun YAMLScanner.newFlowSequenceEndToken(start: SourcePosition, end: SourcePosition) =
  YAMLToken(YAMLTokenType.FlowSequenceEnd, null, start, end, getWarnings())

internal inline fun YAMLScanner.newInvalidToken(start: SourcePosition, end: SourcePosition) =
  YAMLToken(YAMLTokenType.Invalid, null, start, end, getWarnings())
