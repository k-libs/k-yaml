package io.foxcapades.lib.k.yaml.scan

import io.foxcapades.lib.k.yaml.io.ByteArrayReader
import io.foxcapades.lib.k.yaml.read.YAMLReader
import kotlin.test.*

class TestPlainScalarsInFlowMapping {

  @Test
  fun simpleFlowMapping() {
    val input = "{this is a key:this is a value}"
    val reader = YAMLReader(1024, ByteArrayReader(input.encodeToByteArray()))
    val scanner = YAMLScanner(reader, LineBreakType.LF)

    var token: YAMLToken

    // Ensure the first token should be a stream start token
    assertTrue(scanner.hasMoreTokens)
    token = scanner.nextToken()

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
    assertNull(token.data)
    assertEquals(0u, token.start.index)
    assertEquals(0u, token.start.line)
    assertEquals(0u, token.start.column)
    assertEquals(1u, token.end.index)
    assertEquals(0u, token.end.line)
    assertEquals(1u, token.end.column)

    // The third token ("this is a key")
    assertTrue(scanner.hasMoreTokens)
    token = scanner.nextToken()

    assertEquals(YAMLTokenType.Scalar, token.type)
    assertIs<YAMLTokenDataScalar>(token.data).also {
      assertEquals("this is a key", it.valueString())
      assertEquals(YAMLScalarStyle.Plain, it.style)
    }
    assertEquals(1u, token.start.index)
    assertEquals(0u, token.start.line)
    assertEquals(1u, token.start.column)
    assertEquals(14u, token.end.index)
    assertEquals(0u, token.end.line)
    assertEquals(14u, token.end.column)

    // The fourth token (mapping value indicator)
    assertTrue(scanner.hasMoreTokens)
    token = scanner.nextToken()

    assertEquals(YAMLTokenType.MappingValue, token.type)
    assertNull(token.data)
    assertEquals(14u, token.start.index)
    assertEquals(0u, token.start.line)
    assertEquals(14u, token.start.column)
    assertEquals(15u, token.end.index)
    assertEquals(0u, token.end.line)
    assertEquals(15u, token.end.column)

    // The fifth token ("this is a value")
    assertTrue(scanner.hasMoreTokens)
    token = scanner.nextToken()

    assertEquals(YAMLTokenType.Scalar, token.type)
    assertIs<YAMLTokenDataScalar>(token.data).also {
      assertEquals("this is a value", it.valueString())
      assertEquals(YAMLScalarStyle.Plain, it.style)
    }
    assertEquals(15u, token.start.index)
    assertEquals(0u, token.start.line)
    assertEquals(15u, token.start.column)
    assertEquals(30u, token.end.index)
    assertEquals(0u, token.end.line)
    assertEquals(30u, token.end.column)

    // The sixth token (flow mapping end)
    assertTrue(scanner.hasMoreTokens)
    token = scanner.nextToken()

    assertEquals(YAMLTokenType.FlowMappingEnd, token.type)
    assertNull(token.data)
    assertEquals(30u, token.start.index)
    assertEquals(0u, token.start.line)
    assertEquals(30u, token.start.column)
    assertEquals(31u, token.end.index)
    assertEquals(0u, token.end.line)
    assertEquals(31u, token.end.column)

    // The seventh token (stream end)
    assertTrue(scanner.hasMoreTokens)
    token = scanner.nextToken()

    assertEquals(YAMLTokenType.StreamEnd, token.type)
    assertNull(token.data)
    assertEquals(31u, token.start.index)
    assertEquals(0u, token.start.line)
    assertEquals(31u, token.start.column)
    assertEquals(31u, token.end.index)
    assertEquals(0u, token.end.line)
    assertEquals(31u, token.end.column)

    // No more tokens
    assertFalse(scanner.hasMoreTokens)
  }

  @Test
  fun simpleFlowMappingWithMultilineKey() {
    val input = "{this\nis\na\nkey:this is a value}"
    val reader = YAMLReader(1024, ByteArrayReader(input.encodeToByteArray()))
    val scanner = YAMLScanner(reader, LineBreakType.LF)

    var token: YAMLToken

    // Ensure the first token should be a stream start token
    assertTrue(scanner.hasMoreTokens)
    token = scanner.nextToken()

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
    assertNull(token.data)
    assertEquals(0u, token.start.index)
    assertEquals(0u, token.start.line)
    assertEquals(0u, token.start.column)
    assertEquals(1u, token.end.index)
    assertEquals(0u, token.end.line)
    assertEquals(1u, token.end.column)

    // The third token ("this is a key")
    assertTrue(scanner.hasMoreTokens)
    token = scanner.nextToken()

    assertEquals(YAMLTokenType.Scalar, token.type)
    assertIs<YAMLTokenDataScalar>(token.data).also {
      assertEquals("this is a key", it.valueString())
      assertEquals(YAMLScalarStyle.Plain, it.style)
    }
    assertEquals(1u, token.start.index)
    assertEquals(0u, token.start.line)
    assertEquals(1u, token.start.column)
    assertEquals(14u, token.end.index)
    assertEquals(3u, token.end.line)
    assertEquals(3u, token.end.column)

    // The fourth token (mapping value indicator)
    assertTrue(scanner.hasMoreTokens)
    token = scanner.nextToken()

    assertEquals(YAMLTokenType.MappingValue, token.type)
    assertNull(token.data)
    assertEquals(14u, token.start.index)
    assertEquals(3u, token.start.line)
    assertEquals(3u, token.start.column)
    assertEquals(15u, token.end.index)
    assertEquals(3u, token.end.line)
    assertEquals(4u, token.end.column)

    // The fifth token ("this is a value")
    assertTrue(scanner.hasMoreTokens)
    token = scanner.nextToken()

    assertEquals(YAMLTokenType.Scalar, token.type)
    assertIs<YAMLTokenDataScalar>(token.data).also {
      assertEquals("this is a value", it.valueString())
      assertEquals(YAMLScalarStyle.Plain, it.style)
    }
    assertEquals(15u, token.start.index)
    assertEquals(3u, token.start.line)
    assertEquals(4u, token.start.column)
    assertEquals(30u, token.end.index)
    assertEquals(3u, token.end.line)
    assertEquals(19u, token.end.column)

    // The sixth token (flow mapping end)
    assertTrue(scanner.hasMoreTokens)
    token = scanner.nextToken()

    assertEquals(YAMLTokenType.FlowMappingEnd, token.type)
    assertNull(token.data)
    assertEquals(30u, token.start.index)
    assertEquals(3u, token.start.line)
    assertEquals(19u, token.start.column)
    assertEquals(31u, token.end.index)
    assertEquals(3u, token.end.line)
    assertEquals(20u, token.end.column)

    // The seventh token (stream end)
    assertTrue(scanner.hasMoreTokens)
    token = scanner.nextToken()

    assertEquals(YAMLTokenType.StreamEnd, token.type)
    assertNull(token.data)
    assertEquals(31u, token.start.index)
    assertEquals(3u, token.start.line)
    assertEquals(20u, token.start.column)
    assertEquals(31u, token.end.index)
    assertEquals(3u, token.end.line)
    assertEquals(20u, token.end.column)

    // No more tokens
    assertFalse(scanner.hasMoreTokens)
  }
}