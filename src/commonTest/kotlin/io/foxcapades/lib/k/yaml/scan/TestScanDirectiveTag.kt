package io.foxcapades.lib.k.yaml.scan

import io.foxcapades.lib.k.yaml.LineBreakType
import io.foxcapades.lib.k.yaml.io.ByteArrayReader
import io.foxcapades.lib.k.yaml.read.BufferedUTFStreamReader
import io.foxcapades.lib.k.yaml.util.decodeToString
import kotlin.test.*

// TODO: test warnings on invalid tag outputs
class TestScanDirectiveTag {

  private fun makeScanner(input: String) =
    YAMLScannerImpl(BufferedUTFStreamReader(1024, ByteArrayReader(input.encodeToByteArray())), LineBreakType.LF)

  @Test
  fun testInvalidBecausePrefixDoesNotStartWithExclaim() {
    // Note the trailing spaces, we are also testing that the invalid token
    // handling produces the expected token start and end, so keep the trailing
    // spaces and exclude it in the position tests.
    val scanner = makeScanner("%TAG hello hello       ")

    // Skip over the stream-start token.
    scanner.nextToken()

    // Next token should be an invalid token because of the bad tag directive.
    val token = scanner.nextToken()

    assertEquals(YAMLTokenType.Invalid, token.type)
    assertNull(token.data)
    assertEquals(0u, token.start.index)
    assertEquals(0u, token.start.line)
    assertEquals(0u, token.start.column)
    assertEquals(16u, token.end.index)
    assertEquals(0u, token.end.line)
    assertEquals(16u, token.end.column)
  }

  @Test
  fun testInvalidBecauseStreamEndsBeforeHandleValue() {
    val scanner = makeScanner("%TAG ")

    // Skip over the stream start token.
    scanner.nextToken()

    // The next token should be an invalid token because of the incomplete tag
    // directive.
    val token = scanner.nextToken()

    assertEquals(YAMLTokenType.Invalid, token.type)
    assertNull(token.data)
    assertEquals(0u, token.start.index)
    assertEquals(0u, token.start.line)
    assertEquals(0u, token.start.column)
    assertEquals(4u, token.end.index)
    assertEquals(0u, token.end.line)
    assertEquals(4u, token.end.column)
  }

  @Test
  fun testInvalidBecauseStreamEndsInHandleValue() {
    val scanner = makeScanner("%TAG !foo")

    // Skip over the stream start token.
    scanner.nextToken()

    // The next token should be an invalid token because of the incomplete tag
    // directive.
    val token = scanner.nextToken()

    assertEquals(YAMLTokenType.Invalid, token.type)
    assertNull(token.data)
    assertEquals(0u, token.start.index)
    assertEquals(0u, token.start.line)
    assertEquals(0u, token.start.column)
    assertEquals(9u, token.end.index)
    assertEquals(0u, token.end.line)
    assertEquals(9u, token.end.column)
  }

  @Test
  fun testInvalidBecauseStreamEndsBeforePrefixValue() {
    val scanner = makeScanner("%TAG !foo! ")

    // Skip over the stream start token.
    scanner.nextToken()

    // The next token should be an invalid token because of the incomplete tag
    // directive.
    val token = scanner.nextToken()

    assertEquals(YAMLTokenType.Invalid, token.type)
    assertNull(token.data)
    assertEquals(0u, token.start.index)
    assertEquals(0u, token.start.line)
    assertEquals(0u, token.start.column)
    assertEquals(10u, token.end.index)
    assertEquals(0u, token.end.line)
    assertEquals(10u, token.end.column)
  }

  @Test
  fun testInvalidBecauseHandleContainsNonURICharacters() {
    val scanner = makeScanner("%TAG !^! !foo")

    // Skip over the stream start token.
    scanner.nextToken()

    // The next token should be an invalid token because of the incomplete tag
    // directive.
    val token = scanner.nextToken()

    assertEquals(YAMLTokenType.Invalid, token.type)
    assertNull(token.data)
    assertEquals(0u, token.start.index)
    assertEquals(0u, token.start.line)
    assertEquals(0u, token.start.column)
    assertEquals(13u, token.end.index)
    assertEquals(0u, token.end.line)
    assertEquals(13u, token.end.column)
  }

  fun testInvalidBecausePrefixContainsNonURICharacters() {
    val scanner = makeScanner("%TAG !foo! !^")

    // Skip over the stream start token.
    scanner.nextToken()

    // The next token should be an invalid token because of the incomplete tag
    // directive.
    val token = scanner.nextToken()

    assertEquals(YAMLTokenType.Invalid, token.type)
    assertNull(token.data)
    assertEquals(0u, token.start.index)
    assertEquals(0u, token.start.line)
    assertEquals(0u, token.start.column)
    assertEquals(13u, token.end.index)
    assertEquals(0u, token.end.line)
    assertEquals(13u, token.end.column)
  }

  @Test
  fun testValidEndingWithEOF() {
    val scanner = makeScanner("%TAG !foo! tag:foo.com,2023/")

    // Skip over the stream start token.
    scanner.nextToken()

    // The next token should be an invalid token because of the incomplete tag
    // directive.
    val token = scanner.nextToken()

    assertEquals(YAMLTokenType.TagDirective, token.type)
    assertIs<YAMLTokenDataTagDirective>(token.data).also {
      assertEquals("!foo!", it.handleString)
      assertEquals("tag:foo.com,2023/", it.handleString)
    }
    assertEquals(0u, token.start.index)
    assertEquals(0u, token.start.line)
    assertEquals(0u, token.start.column)
    assertEquals(28u, token.end.index)
    assertEquals(0u, token.end.line)
    assertEquals(28u, token.end.column)
    assertEquals(0, token.warnings.size)
  }
}