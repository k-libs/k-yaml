package io.foxcapades.lib.k.yaml.scan

import io.foxcapades.lib.k.yaml.YAMLEncoding
import io.foxcapades.lib.k.yaml.YAMLStreamTokenizer
import io.foxcapades.lib.k.yaml.io.ByteArrayReader
import io.foxcapades.lib.k.yaml.read.BufferedUTFStreamReader
import io.foxcapades.lib.k.yaml.token.*
import io.foxcapades.lib.k.yaml.util.SourcePosition
import io.foxcapades.lib.k.yaml.warn.SourceWarning
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

typealias WarningChecker = (warnings: Array<SourceWarning>) -> Unit

open class ScannerTest {

  @Suppress("NOTHING_TO_INLINE")
  protected inline fun p(i: Int = 0, l: Int = 0, c: Int = 0) =
    SourcePosition(i.toUInt(), l.toUInt(), c.toUInt())

  protected fun makeScanner(input: ByteArray): YAMLStreamTokenizer =
    YAMLStreamTokenizerImpl(BufferedUTFStreamReader(1024, ByteArrayReader(input)))

  protected fun makeScanner(input: String): YAMLStreamTokenizer =
    YAMLStreamTokenizerImpl(BufferedUTFStreamReader(1024, ByteArrayReader(input.encodeToByteArray())))

  protected fun YAMLStreamTokenizer.expectStreamStart(
    expectedEncoding: YAMLEncoding   = YAMLEncoding.UTF8,
    expectedStart:    SourcePosition = SourcePosition(0u, 0u, 0u),
    expectedEnd:      SourcePosition = expectedStart,
    warningChecker:   WarningChecker = this@ScannerTest::defaultWarningChecker,
  ): SourcePosition {
    assertTrue(this.hasNextToken)
    assertIs<YAMLTokenStreamStart>(this.nextToken()).also {
      assertEquals(expectedEncoding, it.encoding)
      assertEquals(expectedStart, it.start)
      assertEquals(expectedEnd, it.end)
      warningChecker(it.warnings)
    }

    return expectedEnd
  }

  protected fun YAMLStreamTokenizer.expectStreamEnd(
    expectedStart:    SourcePosition,
    warningChecker:   WarningChecker = this@ScannerTest::defaultWarningChecker,
  ): SourcePosition {
    assertTrue(this.hasNextToken)
    assertIs<YAMLTokenStreamEnd>(this.nextToken()).also {
      assertEquals(expectedStart, it.start)
      assertEquals(expectedStart, it.end)
      warningChecker(it.warnings)
    }

    return expectedStart
  }

  protected fun YAMLStreamTokenizer.expectInvalid(
    expectedIndent: UInt,
    expectedStart: SourcePosition,
    expectedEnd: SourcePosition,
    warningChecker:   WarningChecker = this@ScannerTest::defaultWarningChecker,
  ): SourcePosition {
    assertTrue(this.hasNextToken)
    assertIs<YAMLTokenInvalid>(this.nextToken()).also {
      assertEquals(expectedIndent, it.indent)
      assertEquals(expectedStart, it.start)
      assertEquals(expectedEnd, it.end)
      warningChecker(it.warnings)
    }

    return expectedEnd
  }

  protected fun YAMLStreamTokenizer.expectMappingKey(
    expectedStart: SourcePosition,
    expectedIndent: UInt = 0u,
    warningChecker: WarningChecker = this@ScannerTest::defaultWarningChecker,
  ): SourcePosition {
    val expectedEnd = expectedStart.resolve(1, 0, 1)

    assertTrue(this.hasNextToken)
    assertIs<YAMLTokenMappingKey>(this.nextToken()).also {
      assertEquals(expectedIndent, it.indent)
      assertEquals(expectedStart, it.start)
      assertEquals(expectedEnd, it.end)
      warningChecker(it.warnings)
    }

    return expectedEnd
  }

  protected fun YAMLStreamTokenizer.expectMappingValue(
    expectedStart:  SourcePosition,
    expectedIndent: UInt           = 0u,
    warningChecker: WarningChecker = this@ScannerTest::defaultWarningChecker,
  ): SourcePosition {
    val expectedEnd = expectedStart.resolve(modIndex = 1, modColumn = 1)

    assertTrue(this.hasNextToken)

    val token = nextToken()
    println(token)

    assertIs<YAMLTokenMappingValue>(token).also {
      assertEquals(expectedIndent, it.indent)
      assertEquals(expectedStart, it.start)
      assertEquals(expectedEnd, it.end)
      warningChecker(it.warnings)
    }

    return expectedEnd
  }

  protected fun YAMLStreamTokenizer.expectSequenceEntry(
    expectedStart:  SourcePosition,
    expectedIndent: UInt           = 0u,
    warningChecker: WarningChecker = this@ScannerTest::defaultWarningChecker,
  ): SourcePosition {
    val expectedEnd = expectedStart.resolve(1, 0, 1)

    assertTrue(this.hasNextToken)
    assertIs<YAMLTokenSequenceEntry>(this.nextToken()).also {
      assertEquals(expectedIndent, it.indent)
      assertEquals(expectedStart, it.start)
      assertEquals(expectedEnd, it.end)
      warningChecker(it.warnings)
    }

    return expectedEnd
  }

  protected fun YAMLStreamTokenizer.expectFlowSequenceStart(
    expectedStart: SourcePosition,
    expectedIndent: UInt = 0u,
    warningChecker: WarningChecker = this@ScannerTest::defaultWarningChecker,
  ): SourcePosition {
    val expectedEnd = expectedStart.resolve(1, 0, 1)
    assertTrue(this.hasNextToken)
    assertIs<YAMLTokenFlowSequenceStart>(this.nextToken()).also {
      assertEquals(expectedIndent, it.indent)
      assertEquals(expectedStart, it.start)
      assertEquals(expectedEnd, it.end)
      warningChecker(it.warnings)
    }

    return expectedEnd
  }

  protected fun YAMLStreamTokenizer.expectFlowSequenceEnd(
    expectedStart:  SourcePosition,
    warningChecker: WarningChecker = this@ScannerTest::defaultWarningChecker,
  ): SourcePosition {
    val expectedEnd = expectedStart.resolve(1, 0, 1)

    assertTrue(this.hasNextToken)
    assertIs<YAMLTokenFlowSequenceEnd>(this.nextToken()).also {
      assertEquals(expectedStart, it.start)
      assertEquals(expectedEnd, it.end)
      warningChecker(it.warnings)
    }

    return expectedEnd
  }

  protected fun YAMLStreamTokenizer.expectFlowMappingStart(
    expectedStart:  SourcePosition,
    expectedIndent: UInt = 0u,
    warningChecker: WarningChecker = this@ScannerTest::defaultWarningChecker,
  ): SourcePosition {
    val expectedEnd = expectedStart.resolve(1, 0, 1)

    assertTrue(this.hasNextToken)
    assertIs<YAMLTokenFlowMappingStart>(this.nextToken()).also {
      assertEquals(expectedIndent, it.indent)
      assertEquals(expectedStart, it.start)
      assertEquals(expectedEnd, it.end)
      warningChecker(it.warnings)
    }

    return expectedEnd
  }

  protected fun YAMLStreamTokenizer.expectFlowMappingEnd(
    expectedStart: SourcePosition,
    warningChecker: WarningChecker = this@ScannerTest::defaultWarningChecker,
  ): SourcePosition {
    val expectedEnd = expectedStart.resolve(1, 0, 1)

    assertTrue(this.hasNextToken)
    assertIs<YAMLTokenFlowMappingEnd>(this.nextToken()).also {
      assertEquals(expectedStart, it.start)
      assertEquals(expectedEnd, it.end)
      warningChecker(it.warnings)
    }

    return expectedEnd
  }

  protected fun YAMLStreamTokenizer.expectFlowItemSeparator(
    expectedStart:  SourcePosition,
    warningChecker: WarningChecker = this@ScannerTest::defaultWarningChecker,
  ): SourcePosition {
    val expectedEnd = expectedStart.resolve(1, 0, 1)

    assertTrue(this.hasNextToken)
    assertIs<YAMLTokenFlowItemSeparator>(this.nextToken()).also {
      assertEquals(expectedStart, it.start)
      assertEquals(expectedEnd, it.end)
      warningChecker(it.warnings)
    }

    return expectedEnd
  }

  protected fun YAMLStreamTokenizer.expectAnchor(
    expectedAnchor: String,
    expectedStart:  SourcePosition,
    expectedIndent: UInt           = 0u,
    expectedEnd:    SourcePosition = expectedStart.resolve(
      modIndex = expectedAnchor.length + 1,
      modColumn = expectedAnchor.length + 1
    ),
    warningChecker: WarningChecker = this@ScannerTest::defaultWarningChecker,
  ): SourcePosition {
    assertTrue(this.hasNextToken)
    assertIs<YAMLTokenAnchor>(this.nextToken()).also {
      assertEquals(expectedAnchor, it.anchor.toString())
      assertEquals(expectedIndent, it.indent)
      assertEquals(expectedStart, it.start)
      assertEquals(expectedEnd, it.end)
      warningChecker(it.warnings)
    }

    return expectedEnd
  }

  protected fun YAMLStreamTokenizer.expectAlias(
    expectedAlias:  String,
    expectedStart:  SourcePosition,
    expectedIndent: UInt           = 0u,
    expectedEnd:    SourcePosition = expectedStart.resolve(
      modIndex = expectedAlias.length + 1,
      modColumn = expectedAlias.length + 1,
    ),
    warningChecker: WarningChecker = this@ScannerTest::defaultWarningChecker,
  ): SourcePosition {
    assertTrue(this.hasNextToken)
    assertIs<YAMLTokenAlias>(this.nextToken()).also {
      assertEquals(expectedAlias, it.alias.toString())
      assertEquals(expectedIndent, it.indent)
      assertEquals(expectedStart, it.start)
      assertEquals(expectedEnd, it.end)
      warningChecker(it.warnings)
    }

    return expectedEnd
  }

  protected fun YAMLStreamTokenizer.expectTag(
    expectedHandle: String,
    expectedSuffix: String,
    expectedStart:  SourcePosition,
    expectedIndent: UInt = 0u,
    tokenLength:    Int = expectedHandle.length + expectedSuffix.length,
    warningChecker: WarningChecker = this@ScannerTest::defaultWarningChecker,
  ): SourcePosition {
    val expectedEnd = expectedStart.resolve(tokenLength, 0, tokenLength)

    assertTrue(this.hasNextToken)
    assertIs<YAMLTokenTag>(this.nextToken()).also {
      assertEquals(expectedHandle, it.handle.toString())
      assertEquals(expectedSuffix, it.suffix.toString())
      assertEquals(expectedIndent, it.indent)
      assertEquals(expectedStart, it.start)
      assertEquals(expectedEnd, it.end)
      warningChecker(it.warnings)
    }

    return expectedEnd
  }

  protected fun YAMLStreamTokenizer.expectPlainScalar(
    expectedValue:  String,
    expectedStart:  SourcePosition,
    expectedIndent: UInt           = 0u,
    expectedEnd:    SourcePosition = expectedStart.resolve(expectedValue.length, 0, expectedValue.length),
    warningChecker: WarningChecker = this@ScannerTest::defaultWarningChecker,
  ): SourcePosition {
    assertTrue(this.hasNextToken)
    val token = nextToken()
    assertIs<YAMLTokenScalarPlain>(token).also {
      assertEquals(expectedValue, it.value.toString())
      assertEquals(expectedIndent, it.indent)
      assertEquals(expectedStart, it.start)
      assertEquals(expectedEnd, it.end)
      warningChecker(it.warnings)
    }

    return expectedEnd
  }

  protected fun YAMLStreamTokenizer.expectLiteralScalar(
    expectedValue:  String,
    expectedIndent: UInt,
    expectedStart:  SourcePosition,
    expectedEnd:    SourcePosition,
    warningChecker: WarningChecker = this@ScannerTest::defaultWarningChecker,
  ): SourcePosition {
    assertTrue(this.hasNextToken)
    assertIs<YAMLTokenScalarLiteral>(this.nextToken()).also {
      assertEquals(expectedValue, it.value.toString())
      assertEquals(expectedIndent, it.indent)
      assertEquals(expectedStart, it.start)
      assertEquals(expectedEnd, it.end)
      warningChecker(it.warnings)
    }

    return expectedEnd
  }

  protected fun YAMLStreamTokenizer.expectFoldedScalar(
    expectedValue:  String,
    expectedIndent: UInt,
    expectedStart:  SourcePosition,
    expectedEnd:    SourcePosition,
    warningChecker: WarningChecker = this@ScannerTest::defaultWarningChecker,
  ): SourcePosition {
    assertTrue(this.hasNextToken)
    assertIs<YAMLTokenScalarFolded>(this.nextToken()).also {
      assertEquals(expectedValue, it.value.toString())
      assertEquals(expectedIndent, it.indent)
      assertEquals(expectedStart, it.start)
      assertEquals(expectedEnd, it.end)
      warningChecker(it.warnings)
    }

    return expectedEnd
  }

  protected fun YAMLStreamTokenizer.expectSingleQuotedScalar(
    expectedValue:  String,
    expectedStart:  SourcePosition,
    expectedIndent: UInt = 0u,
    expectedEnd:    SourcePosition = expectedStart.resolve(
      modIndex = expectedValue.length + 2,
      modColumn = expectedValue.length + 2,
    ),
    warningChecker: WarningChecker = this@ScannerTest::defaultWarningChecker,
  ): SourcePosition {
    assertTrue(this.hasNextToken)
    assertIs<YAMLTokenScalarQuotedSingle>(this.nextToken()).also {
      assertEquals(expectedValue, it.value.toString())
      assertEquals(expectedIndent, it.indent)
      assertEquals(expectedStart, it.start)
      assertEquals(expectedEnd, it.end)
      warningChecker(it.warnings)
    }

    return expectedEnd
  }

  protected fun YAMLStreamTokenizer.expectDoubleQuotedScalar(
    expectedValue:  String,
    expectedStart:  SourcePosition,
    expectedIndent: UInt           = 0u,
    expectedEnd:    SourcePosition = expectedStart.resolve(
      modIndex = expectedValue.length + 2,
      modColumn = expectedValue.length + 2,
    ),
    warningChecker: WarningChecker = this@ScannerTest::defaultWarningChecker,
  ): SourcePosition {
    assertTrue(this.hasNextToken)
    assertIs<YAMLTokenScalarQuotedDouble>(this.nextToken()).also {
      assertEquals(expectedValue, it.value.toString())
      assertEquals(expectedIndent, it.indent)
      assertEquals(expectedStart, it.start)
      assertEquals(expectedEnd, it.end)
      warningChecker(it.warnings)
    }

    return expectedEnd
  }

  protected fun YAMLStreamTokenizer.expectComment(
    expectedValue:    String,
    expectedIndent:   UInt,
    expectedTrailing: Boolean,
    expectedStart:    SourcePosition,
    expectedEnd:      SourcePosition = expectedStart.resolve(
      modIndex  = expectedValue.length + 2,
      modColumn = expectedValue.length + 2,
    ),
    warningChecker:   WarningChecker = this@ScannerTest::defaultWarningChecker,
  ): SourcePosition {
    assertTrue(this.hasNextToken)
    assertIs<YAMLTokenComment>(this.nextToken()).also {
      assertEquals(expectedValue, it.value.toString())
      assertEquals(expectedIndent, it.indent)
      assertEquals(expectedTrailing, it.trailing)
      assertEquals(expectedStart, it.start)
      assertEquals(expectedEnd, it.end)
      warningChecker(it.warnings)
    }

    return expectedEnd
  }

  protected fun YAMLStreamTokenizer.expectYAMLDirective(
    expectedMajor:  UInt,
    expectedMinor:  UInt,
    expectedStart:  SourcePosition,
    expectedEnd:    SourcePosition = expectedStart.resolve(9, 0, 9),
    warningChecker: WarningChecker = this@ScannerTest::defaultWarningChecker,
  ): SourcePosition {
    assertTrue(this.hasNextToken)
    assertIs<YAMLTokenDirectiveYAML>(this.nextToken()).also {
      assertEquals(expectedMajor, it.majorVersion)
      assertEquals(expectedMinor, it.minorVersion)
      assertEquals(expectedStart, it.start)
      assertEquals(expectedEnd, it.end)
      warningChecker(it.warnings)
    }

    return expectedEnd
  }

  protected fun YAMLStreamTokenizer.expectTagDirective(
    expectedHandle: String,
    expectedPrefix: String,
    expectedStart:  SourcePosition,
    tokenLength: Int = 6 + expectedHandle.length + expectedPrefix.length,
    warningChecker: WarningChecker = this@ScannerTest::defaultWarningChecker,
  ): SourcePosition {
    val expectedEnd = expectedStart.resolve(tokenLength, 0, tokenLength)

    assertTrue(hasNextToken)
    assertIs<YAMLTokenDirectiveTag>(nextToken()).also {
      assertEquals(expectedHandle, it.handle.toString())
      assertEquals(expectedPrefix, it.prefix.toString())
      assertEquals(expectedStart, it.start)
      assertEquals(expectedEnd, it.end)
      warningChecker(it.warnings)
    }

    return expectedEnd
  }

  protected fun YAMLStreamTokenizer.expectDocumentStart(
    expectedStart:  SourcePosition,
    warningChecker: WarningChecker = this@ScannerTest::defaultWarningChecker,
  ): SourcePosition {
    val expectedEnd = expectedStart.resolve(3, 0, 3)

    assertTrue(this.hasNextToken)
    assertIs<YAMLTokenDocumentStart>(this.nextToken()).also {
      assertEquals(expectedStart, it.start)
      assertEquals(expectedEnd, it.end)
      warningChecker(it.warnings)
    }

    return expectedEnd
  }

  protected fun YAMLStreamTokenizer.expectDocumentEnd(
    expectedStart:  SourcePosition,
    warningChecker: WarningChecker = this@ScannerTest::defaultWarningChecker,
  ): SourcePosition {
    val expectedEnd = expectedStart.resolve(3, 0, 3)

    assertTrue(this.hasNextToken)
    assertIs<YAMLTokenDocumentEnd>(this.nextToken()).also {
      assertEquals(expectedStart, it.start)
      assertEquals(expectedEnd, it.end)
      warningChecker(it.warnings)
    }

    return expectedEnd
  }

  protected fun SourcePosition.skipSpace(count: Int = 1): SourcePosition =
     resolve(count, 0, count)

  protected fun SourcePosition.skipLine(indentNext: Int = 0): SourcePosition =
    SourcePosition(index + indentNext.toUInt() + 1u, line + 1u, indentNext.toUInt())

  protected fun defaultWarningChecker(warnings: Array<SourceWarning>) {
    assertTrue(warnings.isEmpty())
  }
}