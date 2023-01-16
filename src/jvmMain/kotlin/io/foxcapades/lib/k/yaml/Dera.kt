package io.foxcapades.lib.k.yaml

import io.foxcapades.lib.k.yaml.io.ByteReader
import io.foxcapades.lib.k.yaml.read.YAMLReader
import io.foxcapades.lib.k.yaml.scan.LineBreakType
import io.foxcapades.lib.k.yaml.scan.YAMLScanner
import kotlin.math.min

val input1 = """
%YAML 1.2
---
asdfasdf



asd: hello
...
"""

class ByteArrayReader(val input: ByteArray) : ByteReader {
  private var position = 0

  override fun read(buffer: ByteArray, offset: Int, maxLen: Int): Int {
    if (position >= input.size)
      return -1

    val availableLength = input.size - position
    val fillableLength = buffer.size - offset

    val targetLength = min(min(availableLength, fillableLength), maxLen)

    input.copyInto(buffer, offset, position, position + targetLength)
    position += targetLength

    return targetLength
  }

}


fun main() {
  val scanner = YAMLScanner(YAMLReader(2048, ByteArrayReader(input1.toByteArray(Charsets.UTF_8))), LineBreakType.LF)

  while (scanner.hasMoreTokens)
    println(scanner.nextToken())
}

// 10000000101000
// 10000000000000