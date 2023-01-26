package io.foxcapades.lib.k.yaml.scan

import io.foxcapades.lib.k.yaml.token.YAMLTokenMappingKey


internal fun YAMLStreamTokenizerImpl.fetchMappingKeyIndicatorToken() {
  val start = this.position.mark()

  skipASCII(this.buffer, this.position)

  this.tokens.push(YAMLTokenMappingKey(start, this.position.mark(), this.indent, this.popWarnings()))

  if (!lineContentIndicator.haveHardContent) {
    lineContentIndicator = LineContentIndicatorBlanksAndIndicators
    indent++
  }
}
