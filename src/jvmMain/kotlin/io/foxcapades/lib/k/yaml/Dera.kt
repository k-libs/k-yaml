package io.foxcapades.lib.k.yaml

import io.foxcapades.lib.k.yaml.io.ByteArrayReader
import io.foxcapades.lib.k.yaml.read.BufferedUTFStreamReader
import io.foxcapades.lib.k.yaml.scan.YAMLScannerImpl

val input1 = """
>
 foo

  bar
"""


fun main() {
  val scanner = YAMLScannerImpl(BufferedUTFStreamReader(2048, ByteArrayReader(input1.toByteArray(Charsets.UTF_8))), LineBreakType.LF)

  while (scanner.hasNextToken)
    println(scanner.nextToken())
}

// 10000000101000
// 10000000000000