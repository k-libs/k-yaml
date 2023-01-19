package io.foxcapades.lib.k.yaml

import io.foxcapades.lib.k.yaml.io.ByteArrayReader
import io.foxcapades.lib.k.yaml.read.YAMLReader
import io.foxcapades.lib.k.yaml.scan.LineBreakType
import io.foxcapades.lib.k.yaml.scan.YAMLScanner

val input1 = """
{hello for
this:butt context}
"""


fun main() {
  val scanner = YAMLScanner(YAMLReader(2048, ByteArrayReader(input1.toByteArray(Charsets.UTF_8))), LineBreakType.LF)

  while (scanner.hasMoreTokens)
    println(scanner.nextToken())
}

// 10000000101000
// 10000000000000