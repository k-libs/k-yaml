package io.foxcapades.lib.k.yaml.scan.tokens

import io.foxcapades.lib.k.yaml.token.YAMLTokenMappingValue

internal fun YAMLStreamTokenizerImpl.fetchMappingValueIndicatorToken() {
  val start = this.position.mark()

  skipASCII(this.buffer, this.position)

  this.tokens.push(YAMLTokenMappingValue(start, this.position.mark(), this.indent, this.popWarnings()))

  if (!lineContentIndicator.haveHardContent) {
    lineContentIndicator = LineContentIndicatorBlanksAndIndicators
    indent++
  }
}
