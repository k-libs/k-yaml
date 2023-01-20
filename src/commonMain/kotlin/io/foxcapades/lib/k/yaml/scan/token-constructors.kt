@file:Suppress("NOTHING_TO_INLINE")

package io.foxcapades.lib.k.yaml.scan

import io.foxcapades.lib.k.yaml.YAMLEncoding
import io.foxcapades.lib.k.yaml.util.SourcePosition

/**
 * [VERSION-DIRECTIVE][YAMLTokenType.VersionDirective]
 */
internal inline fun YAMLScannerImpl.newYAMLDirectiveToken(
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
internal inline fun YAMLScannerImpl.newTagDirectiveToken(
  handle: UByteArray,
  prefix: UByteArray,
  start:  SourcePosition,
  end:    SourcePosition,
) =
  YAMLToken(YAMLTokenType.TagDirective, YAMLTokenDataTagDirective(handle, prefix), start, end, getWarnings())

/**
 * [STREAM-START][YAMLTokenType.StreamStart]
 */
internal inline fun YAMLScannerImpl.newStreamStartToken(
  encoding: YAMLEncoding,
  start:    SourcePosition,
  end:      SourcePosition,
): YAMLToken {
  return YAMLToken(YAMLTokenType.StreamStart, YAMLTokenDataStreamStart(encoding), start, end, getWarnings())
}

/**
 * [STREAM-END][YAMLTokenType.StreamEnd]
 */
internal inline fun YAMLScannerImpl.newStreamEndToken(start: SourcePosition, end: SourcePosition) =
  YAMLToken(YAMLTokenType.StreamEnd, null, start, end, getWarnings())

/**
 * [DOCUMENT-START][YAMLTokenType.DocumentStart]
 */
internal inline fun YAMLScannerImpl.newDocumentStartToken(start: SourcePosition, end: SourcePosition) =
  YAMLToken(YAMLTokenType.DocumentStart, null, start, end, getWarnings())

/**
 * [DOCUMENT-END][YAMLTokenType.DocumentEnd]
 */
internal inline fun YAMLScannerImpl.newDocumentEndToken(start: SourcePosition, end: SourcePosition) =
  YAMLToken(YAMLTokenType.DocumentEnd, null, start, end, getWarnings())

/**
 * [SCALAR][YAMLTokenType.Scalar]
 */
@OptIn(ExperimentalUnsignedTypes::class)
internal inline fun YAMLScannerImpl.newPlainScalarToken(value: UByteArray, start: SourcePosition, end: SourcePosition) =
  YAMLToken(YAMLTokenType.Scalar, YAMLTokenDataScalar(value, YAMLScalarStyle.Plain), start, end, getWarnings())

/**
 * [MAPPING-KEY][YAMLTokenType.MappingKey]
 */
internal inline fun YAMLScannerImpl.newMappingKeyIndicatorToken(start: SourcePosition, end: SourcePosition) =
  YAMLToken(YAMLTokenType.MappingKey, null, start, end, getWarnings())

/**
 * [MAPPING-VALUE][YAMLTokenType.MappingValue]
 */
internal inline fun YAMLScannerImpl.newMappingValueIndicatorToken(start: SourcePosition, end: SourcePosition) =
  YAMLToken(YAMLTokenType.MappingValue, null, start, end, getWarnings())

internal inline fun YAMLScannerImpl.newSequenceEntryIndicatorToken(start: SourcePosition, end: SourcePosition) =
  YAMLToken(YAMLTokenType.BlockEntry, null, start, end, getWarnings())

/**
 * [FLOW-MAPPING-START][YAMLTokenType.FlowMappingStart]
 */
internal inline fun YAMLScannerImpl.newFlowMappingStartToken(start: SourcePosition, end: SourcePosition) =
  YAMLToken(YAMLTokenType.FlowMappingStart, null, start, end, getWarnings())

/**
 * [FLOW-MAPPING-END][YAMLTokenType.FlowMappingEnd]
 */
internal inline fun YAMLScannerImpl.newFlowMappingEndToken(start: SourcePosition, end: SourcePosition) =
  YAMLToken(YAMLTokenType.FlowMappingEnd, null, start, end, getWarnings())

/**
 * [FLOW-SEQUENCE-START][YAMLTokenType.FlowSequenceStart]
 */
internal inline fun YAMLScannerImpl.newFlowSequenceStartToken(start: SourcePosition, end: SourcePosition) =
  YAMLToken(YAMLTokenType.FlowSequenceStart, null, start, end, getWarnings())

/**
 * [FLOW-SEQUENCE-END][YAMLTokenType.FlowSequenceEnd]
 */
internal inline fun YAMLScannerImpl.newFlowSequenceEndToken(start: SourcePosition, end: SourcePosition) =
  YAMLToken(YAMLTokenType.FlowSequenceEnd, null, start, end, getWarnings())

@OptIn(ExperimentalUnsignedTypes::class)
internal inline fun YAMLScannerImpl.newCommentToken(value: UByteArray, start: SourcePosition, end: SourcePosition) =
  YAMLToken(YAMLTokenType.Comment, YAMLTokenDataComment(value), start, end, getWarnings())

internal inline fun YAMLScannerImpl.newInvalidToken(start: SourcePosition, end: SourcePosition) =
  YAMLToken(YAMLTokenType.Invalid, null, start, end, getWarnings())
