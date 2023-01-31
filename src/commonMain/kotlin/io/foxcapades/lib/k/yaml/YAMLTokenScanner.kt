package io.foxcapades.lib.k.yaml

import io.foxcapades.lib.k.yaml.scan.tokens.token.YAMLToken

interface YAMLStreamTokenizer {
  val hasNextToken: Boolean

  fun nextToken(): io.foxcapades.lib.k.yaml.scan.tokens.token.YAMLToken
}

interface YAMLTokenScanner {
  val hasNextToken: Boolean

  fun nextToken(): io.foxcapades.lib.k.yaml.scan.tokens.token.YAMLToken
}