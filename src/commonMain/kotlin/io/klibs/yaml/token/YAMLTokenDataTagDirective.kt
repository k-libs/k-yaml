package io.klibs.yaml.token

import io.klibs.yaml.util.UByteString

interface YAMLTokenDataTagDirective : YAMLTokenData {
  val handle: UByteString
  val prefix: UByteString
}