package io.foxcapades.lib.k.yaml

import io.foxcapades.lib.k.yaml.io.ByteArrayReader
import io.foxcapades.lib.k.yaml.read.BufferedUTFStreamReader
import io.foxcapades.lib.k.yaml.scan.stream.YAMLStreamTokenizerImpl

val input1 = """
the problem is:
  what do i do with this: >
foo   
 bar

"""

// TODO: need to track and use the indent level of the previous block context
//       because to be "valid" the folded block must be base-indent + 1


fun main() {
  val scanner = YAMLStreamTokenizerImpl(BufferedUTFStreamReader(2048, ByteArrayReader(input1.toByteArray(Charsets.UTF_8))))

  while (scanner.hasNextToken)
    println(scanner.nextToken())
}
