package io.foxcapades.lib.k.yaml

import io.foxcapades.lib.k.yaml.util.*

fun main() {
  val tmp = UByteBuffer()
  tmp.push(0x01u)
  tmp.push(0xD8u)
  tmp.push(0x37u)
  tmp.push(0xDCu)

  val buffer = UByteBuffer(4)

  tmp.popUTF16LE().toUTF8(buffer)

  buffer.toArray().forEach { println(it.toString(16)) }

  println(buffer.toArray().asByteArray().toString(Charsets.UTF_8))
}

// 10000000101000
// 10000000000000