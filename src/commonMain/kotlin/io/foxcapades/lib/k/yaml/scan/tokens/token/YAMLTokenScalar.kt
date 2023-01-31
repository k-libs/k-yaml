package io.foxcapades.lib.k.yaml.scan.tokens.token

import io.foxcapades.lib.k.yaml.util.UByteString

sealed interface YAMLTokenScalar : io.foxcapades.lib.k.yaml.scan.tokens.token.YAMLToken {
  val indent: UInt

  val value: UByteString
}