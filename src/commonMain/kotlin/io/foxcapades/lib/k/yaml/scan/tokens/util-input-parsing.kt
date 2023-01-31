package io.foxcapades.lib.k.yaml.scan.tokens

import io.foxcapades.lib.k.yaml.err.UIntOverflowException
import io.foxcapades.lib.k.yaml.util.asDecimalDigit
import io.foxcapades.lib.k.yaml.util.isDecimalDigit

internal fun YAMLStreamTokenizerImpl.parseUByte(isNewLine: Boolean = false): UByte {
  buffer.cache(1)

  if (isNewLine)
    position.incLine()
  else
    position.incPosition()

  return buffer.pop()
}

internal fun YAMLStreamTokenizerImpl.parseUInt(): UInt {
  val intStart = position.mark()
  var intValue = 0u
  var addValue: UInt

  while (true) {
    buffer.cache(1)

    if (buffer.isDecimalDigit()) {
      if (intValue > UInt.MAX_VALUE / 10u)
        throw UIntOverflowException(intStart)

      intValue *= 10u
      addValue = buffer.asDecimalDigit()

      if (intValue > UInt.MAX_VALUE - addValue)
        throw UIntOverflowException(intStart)

      intValue += addValue

      skipASCII(this.buffer, this.position)
    } else {
      break
    }
  }

  return intValue
}
