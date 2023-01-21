package io.foxcapades.lib.k.yaml.scan

import io.foxcapades.lib.k.yaml.util.*

internal fun UByteBuffer.skipNewLine() {
  if (size > 2) {
    when {
      uIsCRLF()               -> return skip(NL.CRLF.width)
      uIsLineFeed()           -> return skip(NL.LF.width)
      uIsCarriageReturn()     -> return skip(NL.CR.width)
      uIsNextLine()           -> return skip(NL.NEL.width)
      uIsLineSeparator()      -> return skip(NL.LS.width)
      uIsParagraphSeparator() -> return skip(NL.PS.width)
    }
  } else if (size > 1) {
    when {
      uIsCRLF()               -> return skip(NL.CRLF.width)
      uIsLineFeed()           -> return skip(NL.LF.width)
      uIsCarriageReturn()     -> return skip(NL.CR.width)
      uIsNextLine()           -> return skip(NL.NEL.width)
    }
  } else if (size > 0) {
    when {
      uIsCRLF()               -> return skip(NL.CRLF.width)
      uIsLineFeed()           -> return skip(NL.LF.width)
      uIsCarriageReturn()     -> return skip(NL.CR.width)
    }
  }

  throw IllegalStateException(
    "called UByteBuffer.skipNewLine() on a UByteBuffer whose next character is not a line break."
  )
}
