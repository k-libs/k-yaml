package io.foxcapades.lib.k.yaml

import io.foxcapades.lib.k.yaml.token.YAMLToken
import io.foxcapades.lib.k.yaml.warn.SourceWarning

interface YAMLScanner {
  val hasNextToken: Boolean

  fun nextToken(): YAMLToken
}
