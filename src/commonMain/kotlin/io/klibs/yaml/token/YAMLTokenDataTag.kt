package io.klibs.yaml.token

import io.klibs.yaml.util.UByteString

interface YAMLTokenDataTag : YAMLTokenData {
  val handle: UByteString
  val suffix: UByteString
}