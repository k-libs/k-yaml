package io.klibs.yaml.token

import io.klibs.yaml.util.UByteString

interface YAMLTokenDataAnchor : YAMLTokenData {
  val value: UByteString
}