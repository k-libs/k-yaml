package io.klibs.yaml.token

import io.klibs.yaml.util.SourcePosition
import io.klibs.collections.Stack
import io.klibs.yaml.YAMLEncoding
import io.klibs.yaml.util.UByteString

internal class YAMLTokenPool {
  private val tokenPool = Stack<YAMLTokenImpl>(8)
  private val dataPool = Stack<YAMLTokenDataImpl>(4)

  private fun getTokenData() =
    if (dataPool.isEmpty)
      YAMLTokenDataImpl(this)
    else
      dataPool.pop()

  private fun getToken() =
    if (tokenPool.isEmpty)
      YAMLTokenImpl(this)
    else
      tokenPool.pop()

  fun returnToken(token: YAMLToken) {
    if (token is YAMLTokenImpl) {
      token.clear()
      if (token !in tokenPool)
        tokenPool.push(token)
    }
  }

  fun returnTokenData(data: YAMLTokenData) {
    if (data is YAMLTokenDataImpl) {
      data.clear()
      if (data !in dataPool)
        dataPool.push(data)
    }
  }

  fun getStreamStart(encoding: YAMLEncoding, start: SourcePosition, end: SourcePosition) =
    getToken().asStreamStart(getTokenData().asStreamStart(encoding), start, end)

  fun getStreamEnd(start: SourcePosition, end: SourcePosition) =
    getToken().asStreamEnd(start, end)

  fun getVersionDirective(major: Int, minor: Int, start: SourcePosition, end: SourcePosition) =
    getToken().asVersionDirective(getTokenData().asVersionDirective(major, minor), start, end)

  fun getTagDirective(handle: UByteString, prefix: UByteString, start: SourcePosition, end: SourcePosition) =
    getToken().asTagDirective(getTokenData().asTagDirective(handle, prefix), start, end)

  fun getDocumentStart(start: SourcePosition, end: SourcePosition) =
    getToken().asDocumentStart(start, end)

  fun getDocumentEnd(start: SourcePosition, end: SourcePosition) =
    getToken().asDocumentEnd(start, end)

  fun getBlockSequenceStart(start: SourcePosition, end: SourcePosition) =
    getToken().asBlockSequenceStart(start, end)

  fun getBlockMappingStart(start: SourcePosition, end: SourcePosition) =
    getToken().asBlockMappingStart(start, end)

  fun getBlockEnd(start: SourcePosition, end: SourcePosition) =
    getToken().asBlockEnd(start, end)

  fun getFlowSequenceStart(start: SourcePosition, end: SourcePosition) =
    getToken().asFlowSequenceStart(start, end)

  fun getFlowSequenceEnd(start: SourcePosition, end: SourcePosition) =
    getToken().asFlowSequenceEnd(start, end)

  fun getFlowMappingStart(start: SourcePosition, end: SourcePosition) =
    getToken().asFlowMappingStart(start, end)

  fun getFlowMappingEnd(start: SourcePosition, end: SourcePosition) =
    getToken().asFlowMappingEnd(start, end)

  fun getBlockSequenceEntry(start: SourcePosition, end: SourcePosition) =
    getToken().asBlockSequenceEntry(start, end)

  fun getFlowItemSeparator(start: SourcePosition, end: SourcePosition) =
    getToken().asFlowItemSeparator(start, end)

  fun getMappingKey(start: SourcePosition, end: SourcePosition) =
    getToken().asMappingKey(start, end)

  fun getMappingValue(start: SourcePosition, end: SourcePosition) =
    getToken().asMappingValue(start, end)

  fun getAlias(value: UByteString, start: SourcePosition, end: SourcePosition) =
    getToken().asAlias(getTokenData().asAlias(value), start, end)

  fun getAnchor(value: UByteString, start: SourcePosition, end: SourcePosition) =
    getToken().asAnchor(getTokenData().asAnchor(value), start, end)

  fun getTag(handle: UByteString, suffix: UByteString, start: SourcePosition, end: SourcePosition) =
    getToken().asTag(getTokenData().asTag(handle, suffix), start, end)

  fun getScalar(value: UByteString, style: YAMLScalarStyle, start: SourcePosition, end: SourcePosition) =
    getToken().asScalar(getTokenData().asScalar(value, style), start, end)
}