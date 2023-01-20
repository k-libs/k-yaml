package io.foxcapades.lib.k.yaml.scan

import io.foxcapades.lib.k.yaml.LineBreakType
import io.foxcapades.lib.k.yaml.io.ByteArrayReader
import io.foxcapades.lib.k.yaml.read.BufferedUTFStreamReader
import io.foxcapades.lib.k.yaml.util.SourcePosition
import kotlin.test.*

class TestDoubleQuotedStringHexEscapeSequences {

  private fun makeScanner(input: String) =
    YAMLScannerImpl(BufferedUTFStreamReader(1024, ByteArrayReader(input.encodeToByteArray())), LineBreakType.LF)

  @Test
  fun invalidHexSequenceDueToEOF1() {
    val input = "\"\\x"
    val scanner = makeScanner(input)

    // Skip over the stream start
    scanner.nextToken()

    // The next token should be our string
    val token = scanner.nextToken()

    assertEquals(YAMLTokenType.Invalid, token.type)
    assertNull(token.data)
    assertEquals(SourcePosition(0u, 0u, 0u), token.start)
    assertEquals(SourcePosition(3u, 0u, 3u), token.end)
    assertEquals(2, token.warnings.size)
    assertEquals(SourcePosition(1u, 0u, 1u), token.warnings[0].start)
    assertEquals(SourcePosition(3u, 0u, 3u), token.warnings[0].end)
    assertEquals(SourcePosition(0u, 0u, 0u), token.warnings[1].start)
    assertEquals(SourcePosition(3u, 0u, 3u), token.warnings[1].end)
  }

  @Test
  fun invalidHexSequenceDueToEOF2() {
    val input = "\"\\x4"
    val scanner = makeScanner(input)

    // Skip over the stream start
    scanner.nextToken()

    // The next token should be our string
    val token = scanner.nextToken()

    assertEquals(YAMLTokenType.Invalid, token.type)
    assertNull(token.data)
    assertEquals(SourcePosition(0u, 0u, 0u), token.start)
    assertEquals(SourcePosition(4u, 0u, 4u), token.end)
    assertEquals(2, token.warnings.size)
    assertEquals(SourcePosition(1u, 0u, 1u), token.warnings[0].start)
    assertEquals(SourcePosition(4u, 0u, 4u), token.warnings[0].end)
    assertEquals(SourcePosition(0u, 0u, 0u), token.warnings[1].start)
    assertEquals(SourcePosition(4u, 0u, 4u), token.warnings[1].end)
  }

  @Test
  fun invalidHexSequenceDueToBadByte1() {
    val input = "\"\\x😀2\""
    val scanner = makeScanner(input)

    // Skip over the stream start
    scanner.nextToken()

    // The next token should be our string
    val token = scanner.nextToken()

    println(token)

    assertEquals(YAMLTokenType.Scalar, token.type)
    assertIs<YAMLTokenDataScalar>(token.data).also {
      assertEquals("\\x😀2", it.valueString())
      assertEquals(YAMLScalarStyle.DoubleQuoted, it.style)
    }
    assertEquals(SourcePosition(0u, 0u, 0u), token.start)
    assertEquals(SourcePosition(6u, 0u, 6u), token.end)
    assertEquals(1, token.warnings.size)
    assertEquals(SourcePosition(1u, 0u, 1u), token.warnings[0].start)
    assertEquals(SourcePosition(5u, 0u, 5u), token.warnings[0].end)
  }

  @Test
  fun invalidHexSequenceDueToBadByte2() {
    val input = "\"\\x2😀\""
    val scanner = makeScanner(input)

    // Skip over the stream start
    scanner.nextToken()

    // The next token should be our string
    val token = scanner.nextToken()

    println(token)

    assertEquals(YAMLTokenType.Scalar, token.type)
    assertIs<YAMLTokenDataScalar>(token.data).also {
      assertEquals("\\x2😀", it.valueString())
      assertEquals(YAMLScalarStyle.DoubleQuoted, it.style)
    }
    assertEquals(SourcePosition(0u, 0u, 0u), token.start)
    assertEquals(SourcePosition(6u, 0u, 6u), token.end)
    assertEquals(1, token.warnings.size)
    assertEquals(SourcePosition(1u, 0u, 1u), token.warnings[0].start)
    assertEquals(SourcePosition(5u, 0u, 5u), token.warnings[0].end)
  }
}