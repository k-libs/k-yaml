package io.foxcapades.lib.k.yaml.scan

import io.foxcapades.lib.k.yaml.YAMLEncoding
import io.foxcapades.lib.k.yaml.YAMLTokenScanner
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
  protected fun makeScanner(input: ByteArray): YAMLTokenScanner =
    YAMLScannerImpl(BufferedUTFStreamReader(1024, ByteArrayReader(input)))

  protected fun makeScanner(input: String): YAMLTokenScanner =
    YAMLScannerImpl(BufferedUTFStreamReader(1024, ByteArrayReader(input.encodeToByteArray())))

  protected fun YAMLTokenScanner.expectStreamStart(
    expectedEncoding: YAMLEncoding   = YAMLEncoding.UTF8,
    expectedStart:    SourcePosition = SourcePosition(0u, 0u, 0u),
    expectedEnd:      SourcePosition = expectedStart,
    warningChecker:   WarningChecker = this@ScannerTest::defaultWarningChecker,
  ) {
    assertTrue(this.hasNextToken)
    assertIs<YAMLTokenStreamStart>(this.nextToken()).also {
      assertEquals(expectedEncoding, it.encoding)
      assertEquals(expectedStart, it.start)
      assertEquals(expectedEnd, it.end)
      warningChecker(it.warnings)
    }
  }

  protected fun YAMLTokenScanner.expectStreamEnd(
    expectedStart:    SourcePosition,
    expectedEnd:      SourcePosition = expectedStart,
    warningChecker:   WarningChecker = this@ScannerTest::defaultWarningChecker,
  ) {
    assertTrue(this.hasNextToken)
    assertIs<YAMLTokenStreamEnd>(this.nextToken()).also {
      assertEquals(expectedStart, it.start)
      assertEquals(expectedEnd, it.end)
      warningChecker(it.warnings)
    }
  }

  protected fun YAMLTokenScanner.expectMappingKey(
    expectedIndent: UInt,
    expectedStart:  SourcePosition,
    warningChecker: WarningChecker = this@ScannerTest::defaultWarningChecker,
  ) {
    assertTrue(this.hasNextToken)
    assertIs<YAMLTokenMappingKey>(this.nextToken()).also {
      assertEquals(expectedIndent, it.indent)
      assertEquals(expectedStart, it.start)
      assertEquals(expectedStart.copy(1, 0, 1), it.end)
      warningChecker(it.warnings)
    }
  }

  protected fun YAMLTokenScanner.expectMappingValue(
    expectedIndent: UInt,
    expectedStart:  SourcePosition,
    warningChecker: WarningChecker = this@ScannerTest::defaultWarningChecker,
  ) {
    assertTrue(this.hasNextToken)
    assertIs<YAMLTokenMappingValue>(this.nextToken()).also {
      assertEquals(expectedIndent, it.indent)
      assertEquals(expectedStart, it.start)
      assertEquals(expectedStart.copy(1, 0, 1), it.end)
      warningChecker(it.warnings)
    }
  }

  protected fun YAMLTokenScanner.expectSequenceEntry(
    expectedIndent: UInt,
    expectedStart:  SourcePosition,
    warningChecker: WarningChecker = this@ScannerTest::defaultWarningChecker,
  ) {
    assertTrue(this.hasNextToken)
    assertIs<YAMLTokenSequenceEntry>(this.nextToken()).also {
      assertEquals(expectedIndent, it.indent)
      assertEquals(expectedStart, it.start)
      assertEquals(expectedStart.copy(1, 0, 1), it.end)
      warningChecker(it.warnings)
    }
  }

  protected fun YAMLTokenScanner.expectFlowSequenceStart(
    expectedIndent: UInt,
    expectedStart:  SourcePosition,
    expectedEnd:    SourcePosition = expectedStart.copy(1, 0, 1),
    warningChecker: WarningChecker = this@ScannerTest::defaultWarningChecker,
  ) {
    assertTrue(this.hasNextToken)
    assertIs<YAMLTokenFlowSequenceStart>(this.nextToken()).also {
      assertEquals(expectedIndent, it.indent)
      assertEquals(expectedStart, it.start)
      assertEquals(expectedEnd, it.end)
      warningChecker(it.warnings)
    }
  }

  protected fun YAMLTokenScanner.expectFlowSequenceEnd(
    expectedStart:  SourcePosition,
    warningChecker: WarningChecker = this@ScannerTest::defaultWarningChecker,
  ) {
    assertTrue(this.hasNextToken)
    assertIs<YAMLTokenFlowSequenceEnd>(this.nextToken()).also {
      assertEquals(expectedStart, it.start)
      assertEquals(expectedStart.copy(1, 0, 1), it.end)
      warningChecker(it.warnings)
    }
  }

  protected fun YAMLTokenScanner.expectFlowMappingStart(
    expectedIndent: UInt,
    expectedStart:  SourcePosition,
    expectedEnd:    SourcePosition = expectedStart.copy(1, 0, 1),
    warningChecker: WarningChecker = this@ScannerTest::defaultWarningChecker,
  ) {
    assertTrue(this.hasNextToken)
    assertIs<YAMLTokenFlowMappingStart>(this.nextToken()).also {
      assertEquals(expectedIndent, it.indent)
      assertEquals(expectedStart, it.start)
      assertEquals(expectedEnd, it.end)
      warningChecker(it.warnings)
    }
  }

  protected fun YAMLTokenScanner.expectFlowMappingEnd(
    expectedStart:  SourcePosition,
    expectedEnd:    SourcePosition = expectedStart.copy(1, 0, 1),
    warningChecker: WarningChecker = this@ScannerTest::defaultWarningChecker,
  ) {
    assertTrue(this.hasNextToken)
    assertIs<YAMLTokenFlowMappingEnd>(this.nextToken()).also {
      assertEquals(expectedStart, it.start)
      assertEquals(expectedEnd, it.end)
      warningChecker(it.warnings)
    }
  }

  protected fun YAMLTokenScanner.expectFlowItemSeparator(
    expectedStart:  SourcePosition,
    warningChecker: WarningChecker = this@ScannerTest::defaultWarningChecker,
  ) {
    assertTrue(this.hasNextToken)
    assertIs<YAMLTokenFlowItemSeparator>(this.nextToken()).also {
      assertEquals(expectedStart, it.start)
      assertEquals(expectedStart.copy(1, 0, 1), it.end)
      warningChecker(it.warnings)
    }
  }

  protected fun YAMLTokenScanner.expectAnchor(
    expectedAnchor: String,
    expectedIndent: UInt,
    expectedStart:  SourcePosition,
    expectedEnd:    SourcePosition = expectedStart.copy(
      modIndex  = expectedAnchor.length + 1,
      modColumn = expectedAnchor.length + 1
    ),
    warningChecker: WarningChecker = this@ScannerTest::defaultWarningChecker,
  ) {
    assertTrue(this.hasNextToken)
    assertIs<YAMLTokenAnchor>(this.nextToken()).also {
      assertEquals(expectedAnchor, it.anchor.toString())
      assertEquals(expectedIndent, it.indent)
      assertEquals(expectedStart, it.start)
      assertEquals(expectedEnd, it.end)
      warningChecker(it.warnings)
    }
  }

  protected fun YAMLTokenScanner.testAlias(
    expectedAlias:  String,
    expectedIndent: UInt,
    expectedStart:  SourcePosition,
    expectedEnd:    SourcePosition = expectedStart.copy(
      modIndex  = expectedAlias.length + 1,
      modColumn = expectedAlias.length + 1,
    ),
    warningChecker: WarningChecker = this@ScannerTest::defaultWarningChecker,
  ) {
    assertTrue(this.hasNextToken)
    assertIs<YAMLTokenAlias>(this.nextToken()).also {
      assertEquals(expectedAlias, it.alias.toString())
      assertEquals(expectedIndent, it.indent)
      assertEquals(expectedStart, it.start)
      assertEquals(expectedEnd, it.end)
      warningChecker(it.warnings)
    }
  }

  protected fun YAMLTokenScanner.testTag(
    expectedHandle: String,
    expectedSuffix: String,
    expectedStart:  SourcePosition,
    expectedEnd:    SourcePosition = expectedStart.copy(
      modIndex  = expectedHandle.length + expectedSuffix.length,
      modColumn = expectedHandle.length + expectedSuffix.length,
    ),
    warningChecker: WarningChecker = this@ScannerTest::defaultWarningChecker,
  ) {
    assertTrue(this.hasNextToken)
    assertIs<YAMLTokenTag>(this.nextToken()).also {
      assertEquals(expectedHandle, it.handle.toString())
      assertEquals(expectedSuffix, it.suffix.toString())
      assertEquals(expectedStart, it.start)
      assertEquals(expectedEnd, it.end)
      warningChecker(it.warnings)
    }
  }

  protected fun YAMLTokenScanner.expectPlainScalar(
    expectedValue:  String,
    expectedIndent: UInt,
    expectedStart:  SourcePosition,
    expectedEnd:    SourcePosition = expectedStart.copy(expectedValue.length, 0, expectedValue.length),
    warningChecker: WarningChecker = this@ScannerTest::defaultWarningChecker,
  ) {
    assertTrue(this.hasNextToken)
    assertIs<YAMLTokenScalarPlain>(this.nextToken()).also {
      assertEquals(expectedValue, it.value.toString())
      assertEquals(expectedIndent, it.indent)
      assertEquals(expectedStart, it.start)
      assertEquals(expectedEnd, it.end)
      warningChecker(it.warnings)
    }
  }

  protected fun YAMLTokenScanner.expectLiteralScalar(
    expectedValue:  String,
    expectedIndent: UInt,
    expectedStart:  SourcePosition,
    expectedEnd:    SourcePosition,
    warningChecker: WarningChecker = this@ScannerTest::defaultWarningChecker,
  ) {
    assertTrue(this.hasNextToken)
    assertIs<YAMLTokenScalarLiteral>(this.nextToken()).also {
      assertEquals(expectedValue, it.value.toString())
      assertEquals(expectedIndent, it.indent)
      assertEquals(expectedStart, it.start)
      assertEquals(expectedEnd, it.end)
      warningChecker(it.warnings)
    }
  }

  protected fun YAMLTokenScanner.expectFoldedScalar(
    expectedValue:  String,
    expectedIndent: UInt,
    expectedStart:  SourcePosition,
    expectedEnd:    SourcePosition,
    warningChecker: WarningChecker = this@ScannerTest::defaultWarningChecker,
  ) {
    assertTrue(this.hasNextToken)
    assertIs<YAMLTokenScalarFolded>(this.nextToken()).also {
      assertEquals(expectedValue, it.value.toString())
      assertEquals(expectedIndent, it.indent)
      assertEquals(expectedStart, it.start)
      assertEquals(expectedEnd, it.end)
      warningChecker(it.warnings)
    }
  }

  protected fun YAMLTokenScanner.testSingleQuotedScalar(
    expectedValue:  String,
    expectedIndent: UInt,
    expectedStart:  SourcePosition,
    expectedEnd:    SourcePosition,
    warningChecker: WarningChecker = this@ScannerTest::defaultWarningChecker,
  ) {
    assertTrue(this.hasNextToken)
    assertIs<YAMLTokenScalarQuotedSingle>(this.nextToken()).also {
      assertEquals(expectedValue, it.value.toString())
      assertEquals(expectedIndent, it.indent)
      assertEquals(expectedStart, it.start)
      assertEquals(expectedEnd, it.end)
      warningChecker(it.warnings)
    }
  }

  protected fun YAMLTokenScanner.expectDoubleQuotedScalar(
    expectedValue:  String,
    expectedIndent: UInt,
    expectedStart:  SourcePosition,
    expectedEnd:    SourcePosition,
    warningChecker: WarningChecker = this@ScannerTest::defaultWarningChecker,
  ) {
    assertTrue(this.hasNextToken)
    assertIs<YAMLTokenScalarQuotedDouble>(this.nextToken()).also {
      assertEquals(expectedValue, it.value.toString())
      assertEquals(expectedIndent, it.indent)
      assertEquals(expectedStart, it.start)
      assertEquals(expectedEnd, it.end)
      warningChecker(it.warnings)
    }
  }

  protected fun YAMLTokenScanner.expectComment(
    expectedValue:    String,
    expectedIndent:   UInt,
    expectedTrailing: Boolean,
    expectedStart:    SourcePosition,
    expectedEnd:      SourcePosition,
    warningChecker:   WarningChecker = this@ScannerTest::defaultWarningChecker,
  ) {
    assertTrue(this.hasNextToken)
    assertIs<YAMLTokenComment>(this.nextToken()).also {
      assertEquals(expectedValue, it.value.toString())
      assertEquals(expectedIndent, it.indent)
      assertEquals(expectedTrailing, it.trailing)
      assertEquals(expectedStart, it.start)
      assertEquals(expectedEnd, it.end)
      warningChecker(it.warnings)
    }
  }

  protected fun YAMLTokenScanner.testYAMLDirective(
    expectedMajor:  UInt,
    expectedMinor:  UInt,
    expectedStart:  SourcePosition,
    expectedEnd:    SourcePosition,
    warningChecker: WarningChecker = this@ScannerTest::defaultWarningChecker,
  ) {
    assertTrue(this.hasNextToken)
    assertIs<YAMLTokenDirectiveYAML>(this.nextToken()).also {
      assertEquals(expectedMajor, it.majorVersion)
      assertEquals(expectedMinor, it.minorVersion)
      assertEquals(expectedStart, it.start)
      assertEquals(expectedEnd, it.end)
      warningChecker(it.warnings)
    }
  }


  protected fun YAMLTokenScanner.testDocumentStart(
    expectedStart:  SourcePosition,
    warningChecker: WarningChecker = this@ScannerTest::defaultWarningChecker,
  ) {
    assertTrue(this.hasNextToken)
    assertIs<YAMLTokenDocumentStart>(this.nextToken()).also {
      assertEquals(expectedStart, it.start)
      assertEquals(expectedStart.copy(3, 0, 3), it.end)
      warningChecker(it.warnings)
    }
  }

  protected fun YAMLTokenScanner.testDocumentEnd(
    expectedStart:  SourcePosition,
    warningChecker: WarningChecker = this@ScannerTest::defaultWarningChecker,
  ) {
    assertTrue(this.hasNextToken)
    assertIs<YAMLTokenDocumentEnd>(this.nextToken()).also {
      assertEquals(expectedStart, it.start)
      assertEquals(expectedStart.copy(3, 0, 3), it.end)
      warningChecker(it.warnings)
    }
  }

  protected fun defaultWarningChecker(warnings: Array<SourceWarning>) {
    assertTrue(warnings.isEmpty())
  }
}