package io.foxcapades.lib.k.yaml.scan

import io.foxcapades.lib.k.yaml.io.ByteArrayReader
import io.foxcapades.lib.k.yaml.read.BufferedUTFStreamReader
import io.foxcapades.lib.k.yaml.token.*
import io.foxcapades.lib.k.yaml.util.SourcePosition
import kotlin.test.*

class TestPlainScalarsInFlowMapping {

  @Test
  fun simpleFlowMapping() {
    val input = "{this is a key:this is a value}"
    val reader = BufferedUTFStreamReader(1024, ByteArrayReader(input.encodeToByteArray()))
    val scanner = YAMLScannerImpl(reader)

    var token: YAMLToken

    // Ensure the first token should be a stream start token
    assertTrue(scanner.hasNextToken)
    token = scanner.nextToken()

    assertIs<YAMLTokenStreamStart>(token)
    assertEquals(0u, token.start.index)
    assertEquals(0u, token.start.line)
    assertEquals(0u, token.start.column)
    assertEquals(0u, token.end.index)
    assertEquals(0u, token.end.line)
    assertEquals(0u, token.end.column)

    // The second token (flow mapping start)
    assertTrue(scanner.hasNextToken)
    token = scanner.nextToken()

    assertIs<YAMLTokenFlowMappingStart>(token)
    assertEquals(0u, token.start.index)
    assertEquals(0u, token.start.line)
    assertEquals(0u, token.start.column)
    assertEquals(1u, token.end.index)
    assertEquals(0u, token.end.line)
    assertEquals(1u, token.end.column)

    // The third token ("this is a key")
    assertTrue(scanner.hasNextToken)
    token = scanner.nextToken()

    assertIs<YAMLTokenScalarPlain>(token)
    assertEquals("this is a key", token.value.toString())
    assertEquals(1u, token.start.index)
    assertEquals(0u, token.start.line)
    assertEquals(1u, token.start.column)
    assertEquals(14u, token.end.index)
    assertEquals(0u, token.end.line)
    assertEquals(14u, token.end.column)

    // The fourth token (mapping value indicator)
    assertTrue(scanner.hasNextToken)
    token = scanner.nextToken()

    assertIs<YAMLTokenMappingValue>(token)
    assertEquals(14u, token.start.index)
    assertEquals(0u, token.start.line)
    assertEquals(14u, token.start.column)
    assertEquals(15u, token.end.index)
    assertEquals(0u, token.end.line)
    assertEquals(15u, token.end.column)

    // The fifth token ("this is a value")
    assertTrue(scanner.hasNextToken)
    token = scanner.nextToken()

    assertIs<YAMLTokenScalarPlain>(token)
    assertEquals("this is a value", token.value.toString())
    assertEquals(15u, token.start.index)
    assertEquals(0u, token.start.line)
    assertEquals(15u, token.start.column)
    assertEquals(30u, token.end.index)
    assertEquals(0u, token.end.line)
    assertEquals(30u, token.end.column)

    // The sixth token (flow mapping end)
    assertTrue(scanner.hasNextToken)
    token = scanner.nextToken()

    assertIs<YAMLTokenFlowMappingEnd>(token)
    assertEquals(30u, token.start.index)
    assertEquals(0u, token.start.line)
    assertEquals(30u, token.start.column)
    assertEquals(31u, token.end.index)
    assertEquals(0u, token.end.line)
    assertEquals(31u, token.end.column)

    // The seventh token (stream end)
    assertTrue(scanner.hasNextToken)
    token = scanner.nextToken()

    assertIs<YAMLTokenStreamEnd>(token)
    assertEquals(31u, token.start.index)
    assertEquals(0u, token.start.line)
    assertEquals(31u, token.start.column)
    assertEquals(31u, token.end.index)
    assertEquals(0u, token.end.line)
    assertEquals(31u, token.end.column)

    // No more tokens
    assertFalse(scanner.hasNextToken)
  }

  @Test
  fun simpleFlowMappingWithMultilineKey() {
    val input = "{this\nis\n\na\n\n\nkey:this is a value}"
    val reader = BufferedUTFStreamReader(1024, ByteArrayReader(input.encodeToByteArray()))
    val scanner = YAMLScannerImpl(reader)

    val expectedKey      = "this is\na\n\nkey"
    val expectedKeyStart = SourcePosition(1u, 0u, 1u)
    val expectedKeyEnd   = SourcePosition(17u, 6u, 3u)

    val expectedVal = "this is a value"

    var token: YAMLToken

    // Ensure the first token should be a stream start token
    assertTrue(scanner.hasNextToken)
    assertIs<YAMLTokenStreamStart>(scanner.nextToken()).also {
      assertEquals(0u, it.start.index)
      assertEquals(0u, it.start.line)
      assertEquals(0u, it.start.column)
      assertEquals(0u, it.end.index)
      assertEquals(0u, it.end.line)
      assertEquals(0u, it.end.column)
    }

    // The second token (flow mapping start)
    assertTrue(scanner.hasNextToken)
    assertIs<YAMLTokenFlowMappingStart>(scanner.nextToken()).also {
      assertEquals(0u, it.start.index)
      assertEquals(0u, it.start.line)
      assertEquals(0u, it.start.column)
      assertEquals(1u, it.end.index)
      assertEquals(0u, it.end.line)
      assertEquals(1u, it.end.column)
    }

    // The third token ("this is a key")
    assertTrue(scanner.hasNextToken)
    token = scanner.nextToken()

    assertIs<YAMLTokenScalarPlain>(token)
    assertEquals(expectedKey, token.value.toString())
    assertEquals(expectedKeyStart, token.start)
    assertEquals(expectedKeyEnd, token.end)

    // The fourth token (mapping value indicator)
    assertTrue(scanner.hasNextToken)
    token = scanner.nextToken()

    assertIs<YAMLTokenMappingValue>(token)
    assertEquals(17u, token.start.index)
    assertEquals(6u, token.start.line)
    assertEquals(3u, token.start.column)
    assertEquals(18u, token.end.index)
    assertEquals(6u, token.end.line)
    assertEquals(4u, token.end.column)

    // The fifth token ("this is a value")
    assertTrue(scanner.hasNextToken)
    token = scanner.nextToken()

    assertIs<YAMLTokenScalarPlain>(token)
    assertEquals("this is a value", token.value.toString())
    assertEquals(18u, token.start.index)
    assertEquals(6u, token.start.line)
    assertEquals(4u, token.start.column)
    assertEquals(33u, token.end.index)
    assertEquals(6u, token.end.line)
    assertEquals(19u, token.end.column)

    // The sixth token (flow mapping end)
    assertTrue(scanner.hasNextToken)
    token = scanner.nextToken()

    assertIs<YAMLTokenFlowMappingEnd>(token)
    assertEquals(33u, token.start.index)
    assertEquals(6u, token.start.line)
    assertEquals(19u, token.start.column)
    assertEquals(34u, token.end.index)
    assertEquals(6u, token.end.line)
    assertEquals(20u, token.end.column)

    // The seventh token (stream end)
    assertTrue(scanner.hasNextToken)
    token = scanner.nextToken()

    assertIs<YAMLTokenStreamEnd>(token)
    assertEquals(34u, token.start.index)
    assertEquals(6u, token.start.line)
    assertEquals(20u, token.start.column)
    assertEquals(34u, token.end.index)
    assertEquals(6u, token.end.line)
    assertEquals(20u, token.end.column)

    // No more tokens
    assertFalse(scanner.hasNextToken)
  }
}