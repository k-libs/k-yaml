package io.foxcapades.lib.k.yaml.scan.tokens.token

sealed interface YAMLTokenNodeProperty : io.foxcapades.lib.k.yaml.scan.tokens.token.YAMLToken {

  val indent: UInt
}