package io.foxcapades.lib.k.yaml.scan

import io.foxcapades.lib.k.yaml.err.UIntOverflowException
import io.foxcapades.lib.k.yaml.util.asDecimalDigit
import io.foxcapades.lib.k.yaml.util.isDecimalDigit

internal fun YAMLScannerImpl.parseUByte(isNewLine: Boolean = false): UByte {
  reader.cache(1)

  if (isNewLine)
    position.incLine()
  else
    position.incPosition()

  return reader.pop()
}

internal fun YAMLScannerImpl.parseUInt(): UInt {
  val intStart = position.mark()
  var intValue = 0u
  var addValue: UInt

  while (true) {
    reader.cache(1)

    if (reader.isDecimalDigit()) {
      if (intValue > UInt.MAX_VALUE / 10u)
        throw UIntOverflowException(intStart)

      intValue *= 10u
      addValue = reader.asDecimalDigit()

      if (intValue > UInt.MAX_VALUE - addValue)
        throw UIntOverflowException(intStart)

      intValue += addValue

      skipASCII()
    } else {
      break
    }
  }

  return intValue
}
