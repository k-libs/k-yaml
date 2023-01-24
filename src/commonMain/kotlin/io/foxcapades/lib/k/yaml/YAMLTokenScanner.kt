package io.foxcapades.lib.k.yaml

import io.foxcapades.lib.k.yaml.token.YAMLToken

interface YAMLStreamTokenizer {
  val hasNextToken: Boolean

  fun nextToken(): YAMLToken
}

interface YAMLTokenScanner {
  val hasNextToken: Boolean

  fun nextToken(): YAMLToken
}