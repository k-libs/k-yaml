package io.foxcapades.lib.k.yaml

import io.foxcapades.lib.k.yaml.io.ByteArrayReader
import io.foxcapades.lib.k.yaml.read.YAMLReaderBuffer
import io.foxcapades.lib.k.yaml.scan.LineBreakType
import io.foxcapades.lib.k.yaml.scan.YAMLScannerImpl

val input1 = """
foo:
  bar fizz
  buzz
"""


fun main() {
  val scanner = YAMLScannerImpl(YAMLReaderBuffer(2048, ByteArrayReader(input1.toByteArray(Charsets.UTF_8))), LineBreakType.LF)

  while (scanner.hasNextToken)
    println(scanner.nextToken())
}

// 10000000101000
// 10000000000000