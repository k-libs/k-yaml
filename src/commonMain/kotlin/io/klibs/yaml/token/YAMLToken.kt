package io.klibs.yaml.token

import io.klibs.yaml.util.SourcePosition

sealed interface YAMLToken {
  val type: YAMLTokenType
  val data: YAMLTokenData?
  val start: SourcePosition
  val end: SourcePosition

  fun clear()
  fun close()
}

