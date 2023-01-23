package io.foxcapades.lib.k.yaml

import io.foxcapades.lib.k.yaml.token.YAMLToken

interface YAMLTokenScanner {
  val hasNextToken: Boolean

  fun nextToken(): YAMLToken
}

interface YAMLStreamTokenizer : YAMLTokenScanner

interface YAMLTokenSource : YAMLTokenScanner
