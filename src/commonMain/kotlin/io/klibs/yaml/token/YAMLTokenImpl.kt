package io.klibs.yaml.token

import io.klibs.yaml.util.SourcePosition

internal class YAMLTokenImpl : YAMLToken {
  private val p: YAMLTokenPool

  private var t: YAMLTokenType? = null
  private var d: YAMLTokenData? = null
  private var s: SourcePosition? = null
  private var e: SourcePosition? = null

  override val type: YAMLTokenType
    get() = t!!

  override val data: YAMLTokenData?
    get() = d

  override val start: SourcePosition
    get() = s!!

  override val end: SourcePosition
    get() = e!!

  constructor(pool: YAMLTokenPool) {
    p = pool
  }

  override fun clear() {
    t = null
    d?.close()
    d = null
    s = null
    e = null
  }

  override fun close() {
    d?.close()
    p.returnToken(this)
  }

  fun asStreamStart(data: YAMLTokenDataStreamStart, start: SourcePosition, end: SourcePosition): YAMLToken {
    t = YAMLTokenType.StreamStart
    d = data
    s = start
    e = end
    return this
  }

  fun asStreamEnd(start: SourcePosition, end: SourcePosition): YAMLToken {
    t = YAMLTokenType.StreamEnd
    d = null
    s = start
    e = end
    return this
  }

  fun asVersionDirective(data: YAMLTokenDataVersionDirective, start: SourcePosition, end: SourcePosition): YAMLToken {
    t = YAMLTokenType.VersionDirective
    d = data
    s = start
    e = end
    return this
  }

  fun asTagDirective(data: YAMLTokenDataTagDirective, start: SourcePosition, end: SourcePosition): YAMLToken {
    t = YAMLTokenType.TagDirective
    d = data
    s = start
    e = end
    return this
  }

  fun asDocumentStart(start: SourcePosition, end: SourcePosition): YAMLToken {
    t = YAMLTokenType.DocumentStart
    d = null
    s = start
    e = end
    return this
  }

  fun asDocumentEnd(start: SourcePosition, end: SourcePosition): YAMLToken {
    t = YAMLTokenType.DocumentEnd
    d = null
    s = start
    e = end
    return this
  }

  fun asBlockSequenceStart(start: SourcePosition, end: SourcePosition): YAMLToken {
     t = YAMLTokenType.BlockSequenceStart
     d = null
     s = start
     e = end
     return this
   }

  fun asBlockMappingStart(start: SourcePosition, end: SourcePosition): YAMLToken {
    t = YAMLTokenType.BlockMappingStart
    d = null
    s = start
    e = end
    return this
  }

  fun asBlockEnd(start: SourcePosition, end: SourcePosition): YAMLToken {
    t = YAMLTokenType.BlockEnd
    d = null
    s = start
    e = end
    return this
  }

  fun asFlowSequenceStart(start: SourcePosition, end: SourcePosition): YAMLToken {
    t = YAMLTokenType.FlowSequenceStart
    d = null
    s = start
    e = end
    return this
  }

  fun asFlowSequenceEnd(start: SourcePosition, end: SourcePosition): YAMLToken {
    t = YAMLTokenType.FlowSequenceEnd
    d = null
    s = start
    e = end
    return this
  }

  fun asFlowMappingStart(start: SourcePosition, end: SourcePosition): YAMLToken {
    t = YAMLTokenType.FlowMappingStart
    d = null
    s = start
    e = end
    return this
  }

  fun asFlowMappingEnd(start: SourcePosition, end: SourcePosition): YAMLToken {
    t = YAMLTokenType.FlowMappingEnd
    d = null
    s = start
    e = end
    return this
  }

  fun asBlockSequenceEntry(start: SourcePosition, end: SourcePosition): YAMLToken {
    t = YAMLTokenType.BlockSequenceEntry
    d = null
    s = start
    e = end
    return this
  }

  fun asFlowItemSeparator(start: SourcePosition, end: SourcePosition): YAMLToken {
    t = YAMLTokenType.FlowItemSeparator
    d = null
    s = start
    e = end
    return this
  }

  fun asMappingKey(start: SourcePosition, end: SourcePosition): YAMLToken {
    t = YAMLTokenType.MappingKey
    d = null
    s = start
    e = end
    return this
  }

  fun asMappingValue(start: SourcePosition, end: SourcePosition): YAMLToken {
    t = YAMLTokenType.MappingValue
    d = null
    s = start
    e = end
    return this
  }

  fun asAlias(data: YAMLTokenDataAlias, start: SourcePosition, end: SourcePosition): YAMLToken {
    t = YAMLTokenType.Alias
    d = data
    s = start
    e = end
    return this
  }

  fun asAnchor(data: YAMLTokenDataAnchor, start: SourcePosition, end: SourcePosition): YAMLToken {
    t = YAMLTokenType.Anchor
    d = data
    s = start
    e = end
    return this
  }

  fun asTag(data: YAMLTokenDataTag, start: SourcePosition, end: SourcePosition): YAMLToken {
    t = YAMLTokenType.Tag
    d = data
    s = start
    e = end
    return this
  }

  fun asScalar(data: YAMLTokenDataScalar, start: SourcePosition, end: SourcePosition): YAMLToken {
    t = YAMLTokenType.Scalar
    d = data
    s = start
    e = end
    return this
  }
}