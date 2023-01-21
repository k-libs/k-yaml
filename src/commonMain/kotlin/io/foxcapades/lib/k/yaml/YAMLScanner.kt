package io.foxcapades.lib.k.yaml

import io.foxcapades.lib.k.yaml.token.YAMLToken

interface YAMLScanner {
  val hasNextToken: Boolean

  fun nextToken(): YAMLToken
}
