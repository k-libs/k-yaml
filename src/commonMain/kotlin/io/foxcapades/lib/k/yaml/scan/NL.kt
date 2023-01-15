package io.foxcapades.lib.k.yaml.scan

import io.foxcapades.lib.k.yaml.bytes.*
import io.foxcapades.lib.k.yaml.util.UByteBuffer

internal enum class NL(val width: Int, val characters: Int) {
  CRLF(2, 2),
  CR(1, 1),
  LF(1, 1),
  NEL(2, 1),
  LS(3, 1),
  PS(3, 1),
  ;

  fun writeUTF8(buffer: UByteBuffer) {
    when (this) {
      CRLF -> {
        buffer.push(A_CARRIAGE_RETURN)
        buffer.push(A_LINE_FEED)
      }
      CR   -> {
        buffer.push(A_CARRIAGE_RETURN)
      }
      LF   -> {
        buffer.push(A_LINE_FEED)
      }
      NEL  -> {
        buffer.push(UbC2)
        buffer.push(Ub85)
      }
      LS   -> {
        buffer.push(UbE2)
        buffer.push(Ub80)
        buffer.push(UbA8)
      }
      PS   -> {
        buffer.push(UbE2)
        buffer.push(Ub80)
        buffer.push(UbA9)
      }
    }
  }
}
