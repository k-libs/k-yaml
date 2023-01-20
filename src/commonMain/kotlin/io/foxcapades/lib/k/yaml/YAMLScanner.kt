package io.foxcapades.lib.k.yaml

import io.foxcapades.lib.k.yaml.scan.YAMLToken

interface YAMLScanner {
  val hasNextToken: Boolean

  fun nextToken(): YAMLToken
}
