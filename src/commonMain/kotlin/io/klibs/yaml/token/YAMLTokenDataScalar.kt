package io.klibs.yaml.token

import io.klibs.yaml.util.UByteString

interface YAMLTokenDataScalar : YAMLTokenData {
  val value: UByteString
  val style: YAMLScalarStyle
}