package io.foxcapades.lib.k.yaml.token

sealed interface YAMLTokenNodeProperty : YAMLToken {

  val indent: UInt
}