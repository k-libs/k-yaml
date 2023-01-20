package io.foxcapades.lib.k.yaml

import io.foxcapades.lib.k.yaml.io.ByteArrayReader
import io.foxcapades.lib.k.yaml.read.BufferedUTFStreamReader
import io.foxcapades.lib.k.yaml.scan.YAMLScannerImpl

val input1 = """
%TAG !foo! bar/
foo: !foo!string bar
fizz: buzz
cats: dogs
this: &id
that: *id
cars:
- trucks
- vans
- motorcycles
ding: [
  bats,
  dong
]
dang: {
  kids: are,
  going: nuts
}
look: 'a single quoted string

that

spans multiple lines for some
stupid reason      '
"""


fun main() {
  val scanner = YAMLScannerImpl(BufferedUTFStreamReader(2048, ByteArrayReader(input1.toByteArray(Charsets.UTF_8))), LineBreakType.LF)

  while (scanner.hasNextToken)
    println(scanner.nextToken())
}

// 10000000101000
// 10000000000000