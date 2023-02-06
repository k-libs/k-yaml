package io.klibs.yaml.token

import io.klibs.yaml.util.UByteString

interface YAMLTokenDataAlias : YAMLTokenData {
  val value: UByteString
}