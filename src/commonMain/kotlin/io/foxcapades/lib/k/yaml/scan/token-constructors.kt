@file:Suppress("NOTHING_TO_INLINE")

package io.foxcapades.lib.k.yaml.scan

import io.foxcapades.lib.k.yaml.YAMLEncoding
import io.foxcapades.lib.k.yaml.token.*
import io.foxcapades.lib.k.yaml.util.SourcePosition

/**
 * [YAML-DIRECTIVE][YAMLTokenTypeDirectiveYAML]
 */
internal inline fun YAMLScannerImpl.newYAMLDirectiveToken(
  major: UInt,
  minor: UInt,
  start: SourcePosition,
  end:   SourcePosition,
) =
  YAMLToken(YAMLTokenTypeDirectiveYAML, YAMLTokenDataDirectiveYAML(major, minor), start, end, getWarnings())

/**
 * [TAG-DIRECTIVE][YAMLTokenTypeDirectiveTag]
 */
@OptIn(ExperimentalUnsignedTypes::class)
internal inline fun YAMLScannerImpl.newTagDirectiveToken(
  handle: UByteArray,
  prefix: UByteArray,
  start:  SourcePosition,
  end:    SourcePosition,
) =
  YAMLToken(YAMLTokenTypeDirectiveTag, YAMLTokenDataDirectiveTag(handle, prefix), start, end, getWarnings())

/**
 * [STREAM-START][YAMLTokenType.StreamStart]
 */
internal inline fun YAMLScannerImpl.newStreamStartToken(
  encoding: YAMLEncoding,
  start:    SourcePosition,
  end:      SourcePosition,
): YAMLToken {
  return YAMLToken(YAMLTokenTypeStreamStart, YAMLTokenDataStreamStart(encoding), start, end, getWarnings())
}

/**
 * [STREAM-END][YAMLTokenType.StreamEnd]
 */
internal inline fun YAMLScannerImpl.newStreamEndToken(start: SourcePosition, end: SourcePosition) =
  YAMLToken(YAMLTokenTypeStreamEnd, null, start, end, getWarnings())

/**
 * [DOCUMENT-START][YAMLTokenType.DocumentStart]
 */
internal inline fun YAMLScannerImpl.newDocumentStartToken(start: SourcePosition, end: SourcePosition) =
  YAMLToken(YAMLTokenTypeDocumentStart, null, start, end, getWarnings())

/**
 * [DOCUMENT-END][YAMLTokenType.DocumentEnd]
 */
internal inline fun YAMLScannerImpl.newDocumentEndToken(start: SourcePosition, end: SourcePosition) =
  YAMLToken(YAMLTokenTypeDocumentEnd, null, start, end, getWarnings())

/**
 * [SCALAR][YAMLTokenType.Scalar]
 */
@OptIn(ExperimentalUnsignedTypes::class)
internal inline fun YAMLScannerImpl.newPlainScalarToken(value: UByteArray, indent: UInt, start: SourcePosition, end: SourcePosition) =
  YAMLToken(YAMLTokenTypeScalarPlain, YAMLTokenDataPlainScalar(value, indent), start, end, getWarnings())

/**
 * [MAPPING-KEY][YAMLTokenType.MappingKey]
 */
internal inline fun YAMLScannerImpl.newMappingKeyIndicatorToken(start: SourcePosition, end: SourcePosition) =
  YAMLToken(YAMLTokenTypeMappingKey, null, start, end, getWarnings())


/**
 * [FLOW-MAPPING-START][YAMLTokenType.FlowMappingStart]
 */
internal inline fun YAMLScannerImpl.newFlowMappingStartToken(start: SourcePosition, end: SourcePosition) =
  YAMLToken(YAMLTokenTypeFlowMappingStart, null, start, end, getWarnings())

/**
 * [FLOW-MAPPING-END][YAMLTokenType.FlowMappingEnd]
 */
internal inline fun YAMLScannerImpl.newFlowMappingEndToken(start: SourcePosition, end: SourcePosition) =
  YAMLToken(YAMLTokenTypeFlowMappingEnd, null, start, end, getWarnings())

/**
 * [FLOW-SEQUENCE-START][YAMLTokenType.FlowSequenceStart]
 */
internal inline fun YAMLScannerImpl.newFlowSequenceStartToken(start: SourcePosition, end: SourcePosition) =
  YAMLToken(YAMLTokenTypeFlowSequenceStart, null, start, end, getWarnings())

/**
 * [FLOW-SEQUENCE-END][YAMLTokenType.FlowSequenceEnd]
 */
internal inline fun YAMLScannerImpl.newFlowSequenceEndToken(start: SourcePosition, end: SourcePosition) =
  YAMLToken(YAMLTokenTypeFlowSequenceEnd, null, start, end, getWarnings())

@OptIn(ExperimentalUnsignedTypes::class)
internal inline fun YAMLScannerImpl.newCommentToken(value: UByteArray, start: SourcePosition, end: SourcePosition) =
  YAMLToken(YAMLTokenTypeComment, YAMLTokenDataComment(value), start, end, getWarnings())

internal inline fun YAMLScannerImpl.newInvalidToken(start: SourcePosition, end: SourcePosition) =
  YAMLToken(YAMLTokenTypeInvalid, null, start, end, getWarnings())
