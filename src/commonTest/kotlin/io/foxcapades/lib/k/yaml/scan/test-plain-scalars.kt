package io.foxcapades.lib.k.yaml.scan

import io.foxcapades.lib.k.yaml.io.ByteArrayReader
import io.foxcapades.lib.k.yaml.read.YAMLReader
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class TestPlainScalars1 {
  @Test
  fun testPlainScalars1() {
    val input = "{this is a key:this is a value}"
    val reader = YAMLReader(1024, ByteArrayReader(input.encodeToByteArray()))
    val scanner = YAMLScanner(reader, LineBreakType.LF)

    var token: YAMLToken

    assertTrue(scanner.hasMoreTokens)
    token = scanner.nextToken()

    // Ensure the first token is a stream start token
    assertEquals(YAMLTokenType.StreamStart, token.type)
    assertIs<YAMLTokenDataStreamStart>(token.data)
    assertEquals(0u, token.start.index)
    assertEquals(0u, token.start.line)
    assertEquals(0u, token.start.column)
    assertEquals(0u, token.end.index)
    assertEquals(0u, token.end.line)
    assertEquals(0u, token.end.column)

    // The second token (flow mapping start)
    assertTrue(scanner.hasMoreTokens)
    token = scanner.nextToken()

    assertEquals(YAMLTokenType.FlowMappingStart, token.type)

  }
}