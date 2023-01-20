package io.foxcapades.lib.k.yaml.scan

sealed interface YAMLScanner {
  val hasNextToken: Boolean

  fun nextToken(): YAMLToken
}
