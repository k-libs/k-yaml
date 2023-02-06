package io.klibs.yaml.token

import io.klibs.yaml.YAMLEncoding

interface YAMLTokenDataStreamStart : YAMLTokenData {
  val encoding: YAMLEncoding
}