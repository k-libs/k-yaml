package io.klibs.yaml.token

import io.klibs.yaml.YAMLEncoding
import io.klibs.yaml.util.UByteString

internal class YAMLTokenDataImpl
: YAMLTokenDataStreamStart
, YAMLTokenDataAlias
, YAMLTokenDataAnchor
, YAMLTokenDataTag
, YAMLTokenDataScalar
, YAMLTokenDataVersionDirective
, YAMLTokenDataTagDirective
{
  private val pool: YAMLTokenPool

  private var ptr1: Any? = null
  private var ptr2: Any? = null
  private var int1: Int = 0
  private var int2: Int = 0

  override val encoding: YAMLEncoding
    get() = ptr1 as YAMLEncoding

  override val value: UByteString
    get() = ptr1 as UByteString

  override val handle: UByteString
    get() = ptr1 as UByteString

  override val suffix: UByteString
    get() = ptr2 as UByteString

  override val prefix: UByteString
    get() = ptr2 as UByteString

  override val style: YAMLScalarStyle
    get() = ptr2 as YAMLScalarStyle

  override val major: Int
    get() = int1

  override val minor: Int
    get() = int2

  constructor(pool: YAMLTokenPool) {
    this.pool = pool
  }

  override fun clear() {
    ptr1 = null
    ptr2 = null
  }

  override fun close() {
    pool.returnTokenData(this)
  }

  fun asStreamStart(encoding: YAMLEncoding): YAMLTokenDataStreamStart {
    ptr1 = encoding
    ptr2 = null
    return this
  }

  fun asAlias(value: UByteString): YAMLTokenDataAlias {
    ptr1 = value
    ptr2 = null
    return this
  }

  fun asAnchor(value: UByteString): YAMLTokenDataAnchor {
    ptr1 = value
    ptr2 = null
    return this
  }

  fun asTag(handle: UByteString, suffix: UByteString): YAMLTokenDataTag {
    ptr1 = handle
    ptr2 = suffix
    return this
  }

  fun asScalar(value: UByteString, style: YAMLScalarStyle): YAMLTokenDataScalar {
    ptr1 = value
    ptr2 = style
    return this
  }

  fun asVersionDirective(major: Int, minor: Int): YAMLTokenDataVersionDirective {
    int1 = major
    int2 = minor
    return this
  }

  fun asTagDirective(handle: UByteString, prefix: UByteString): YAMLTokenDataTagDirective {
    ptr1 = handle
    ptr2 = prefix
    return this
  }
}
