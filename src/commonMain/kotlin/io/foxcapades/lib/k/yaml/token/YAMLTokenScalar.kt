package io.foxcapades.lib.k.yaml.token

import io.foxcapades.lib.k.yaml.util.UByteString

sealed interface YAMLTokenScalar : YAMLToken {
  val indent: UInt

  val value: UByteString
}