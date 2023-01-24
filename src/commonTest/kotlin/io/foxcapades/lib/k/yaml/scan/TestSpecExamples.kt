package io.foxcapades.lib.k.yaml.scan

import io.foxcapades.lib.k.yaml.util.SourcePosition
import kotlin.test.Test
import kotlin.test.assertEquals


/**
 * # Literal Scalar Tests: From the Spec
 */
class TestSpecExamples : ScannerTest() {

  @Test
  fun example_5_3_block_structure_indicators() {
    //language=yaml
    val input = "sequence:\n" +
      "- one\n" +
      "- two\n" +
      "mapping:\n" +
      "  ? sky\n" +
      "  : blue\n" +
      "  sea : green"

    val scanner = makeScanner(input)

    scanner.expectStreamStart()

    // sequence:
    scanner.expectPlainScalar("sequence", 0u, SourcePosition(0u, 0u, 0u))
    scanner.expectMappingValue(0u, SourcePosition(8u, 0u, 8u))

    // - one
    scanner.expectSequenceEntry(0u, SourcePosition(10u, 1u, 0u))
    scanner.expectPlainScalar("one", 2u, SourcePosition(12u, 1u, 2u))

    // - two
    scanner.expectSequenceEntry(0u, SourcePosition(16u, 2u, 0u))
    scanner.expectPlainScalar("two", 2u, SourcePosition(18u, 2u, 2u))

    // mapping:
    scanner.expectPlainScalar("mapping", 0u, SourcePosition(22u, 3u, 0u))
    scanner.expectMappingValue(0u, SourcePosition(29u, 3u, 7u))

    //   ? sky
    //   : blue
    scanner.expectMappingKey(2u, SourcePosition(33u, 4u, 2u))
    scanner.expectPlainScalar("sky", 4u, SourcePosition(35u, 4u, 4u))
    scanner.expectMappingValue(2u, SourcePosition(41u, 5u, 2u))
    scanner.expectPlainScalar("blue", 4u, SourcePosition(43u, 5u, 4u))

    //   sea : green
    scanner.expectPlainScalar("sea", 2u, SourcePosition(50u, 6u, 2u))
    scanner.expectMappingValue(2u, SourcePosition(54u, 6u, 6u))
    scanner.expectPlainScalar("green", 2u, SourcePosition(56u, 6u, 8u))

    scanner.expectStreamEnd(SourcePosition(61u, 6u, 13u))
  }

  @Test
  fun example_5_4_flow_collection_indicators() {
    val input = "sequence: [ one, two, ]\n" +
      "mapping: { sky: blue, sea: green }"

    val scanner = makeScanner(input)

    scanner.expectStreamStart()
    scanner.expectPlainScalar("sequence", 0u, SourcePosition(0u, 0u, 0u))
    scanner.expectMappingValue(0u, SourcePosition(8u, 0u, 8u))
    scanner.expectFlowSequenceStart(0u, SourcePosition(10u, 0u, 10u))
    scanner.expectPlainScalar("one", 0u, SourcePosition(12u, 0u, 12u))
    scanner.expectFlowItemSeparator(SourcePosition(15u, 0u, 15u))
    scanner.expectPlainScalar("two", 0u, SourcePosition(17u, 0u, 17u))
    scanner.expectFlowItemSeparator(SourcePosition(20u, 0u, 20u))
    scanner.expectFlowSequenceEnd(SourcePosition(22u, 0u, 22u))
    scanner.expectPlainScalar("mapping", 0u, SourcePosition(24u, 1u, 0u))
    scanner.expectMappingValue(0u, SourcePosition(31u, 1u, 7u))
    scanner.expectFlowMappingStart(0u, SourcePosition(33u, 1u, 9u))
    scanner.expectPlainScalar("sky", 0u, SourcePosition(35u, 1u, 11u))
    scanner.expectMappingValue(0u, SourcePosition(38u, 1u, 14u))
    scanner.expectPlainScalar("blue", 0u, SourcePosition(40u, 1u, 16u))
    scanner.expectFlowItemSeparator(SourcePosition(44u, 1u, 20u))
    scanner.expectPlainScalar("sea", 0u, SourcePosition(46u, 1u, 22u))
    scanner.expectMappingValue(0u, SourcePosition(49u, 1u, 25u))
    scanner.expectPlainScalar("green", 0u, SourcePosition(51u, 1u, 27u))
    scanner.expectFlowMappingEnd(SourcePosition(57u, 1u, 33u))
    scanner.expectStreamEnd(SourcePosition(58u, 1u, 34u))
  }

  @Test
  fun example_5_5_comment_indicator() {
    val input   = "# Comment only."
    val scanner = makeScanner(input)

    scanner.expectStreamStart()
    scanner.expectComment("Comment only.", 0u, false, SourcePosition(0u, 0u, 0u), SourcePosition(15u, 0u, 15u))
    scanner.expectStreamEnd(SourcePosition(15u, 0u, 15u))

  }

  @Test
  fun example_5_6_node_property_indicators() {
    val input = "anchored: !local &anchor value\n" +
      "alias: *anchor"
    val scanner = makeScanner(input)

    scanner.expectStreamStart()
    scanner.expectPlainScalar("anchored", 0u, SourcePosition(0u, 0u, 0u))
    scanner.expectMappingValue(0u, SourcePosition(8u, 0u, 8u))
    scanner.testTag("!", "local", SourcePosition(10u, 0u, 10u))
    scanner.expectAnchor("anchor", 0u, SourcePosition(17u, 0u, 17u))
    scanner.expectPlainScalar("value", 0u, SourcePosition(25u, 0u, 25u))
    scanner.expectPlainScalar("alias", 0u, SourcePosition(31u, 1u, 0u))
    scanner.expectMappingValue(0u, SourcePosition(36u, 1u, 5u))
    scanner.testAlias("anchor", 0u, SourcePosition(38u, 1u, 7u))
    scanner.expectStreamEnd(SourcePosition(45u, 1u, 14u))
  }

  @Test
  fun example_5_7_block_scalar_indicators() {
    val input = "literal: |\n" +
      "  some\n" +
      "  text\n" +
      "folded: >\n" +
      "  some\n" +
      "  text"
    val scanner = makeScanner(input)

    scanner.expectStreamStart()

    // literal:
    scanner.expectPlainScalar("literal", 0u, SourcePosition(0u, 0u, 0u))
    scanner.expectMappingValue(0u, SourcePosition(7u, 0u, 7u))

    // |
    //   some
    //   text
    scanner.expectLiteralScalar("some\ntext\n", 0u, SourcePosition(9u, 0u, 9u), SourcePosition(25u, 3u, 0u))

    // folded:
    scanner.expectPlainScalar("folded", 0u, SourcePosition(25u, 3u, 0u))
    scanner.expectMappingValue(0u, SourcePosition(31u, 3u, 6u))

    // >
    //   some
    //   text
    scanner.expectFoldedScalar("some text\n", 0u, SourcePosition(33u, 3u, 8u), SourcePosition(48u, 5u, 6u))

    scanner.expectStreamEnd(SourcePosition(48u, 5u, 6u))
  }

  @Test
  fun example_5_8_quoted_scalar_indicators() {
    val input = "single: 'text'\n" +
      "double: \"text\""
    val scanner = makeScanner(input)

    scanner.expectStreamStart()
    scanner.expectPlainScalar("single", 0u, SourcePosition(0u, 0u, 0u))
    scanner.expectMappingValue(0u, SourcePosition(6u, 0u, 6u))
    scanner.testSingleQuotedScalar("text", 0u, SourcePosition(8u, 0u, 8u), SourcePosition(14u, 0u, 14u))
    scanner.expectPlainScalar("double", 0u, SourcePosition(15u, 1u, 0u), SourcePosition(21u, 1u, 6u))
    scanner.expectMappingValue(0u, SourcePosition(21u, 1u, 6u))
    scanner.expectDoubleQuotedScalar("text", 0u, SourcePosition(23u, 1u, 8u), SourcePosition(29u, 1u, 14u))
    scanner.expectStreamEnd(SourcePosition(29u, 1u, 14u))
  }

  @Test
  fun example_5_9_directive_indicator() {
    val input   = "%YAML 1.2\n--- text"
    val scanner = makeScanner(input)

    scanner.expectStreamStart()
    scanner.testYAMLDirective(1u, 2u, SourcePosition(0u, 0u, 0u), SourcePosition(9u, 0u, 9u))
    scanner.expectDocumentStart(SourcePosition(10u, 1u, 0u))
    scanner.expectPlainScalar("text", 0u, SourcePosition(14u, 1u, 4u), SourcePosition(18u, 1u, 8u))
    scanner.expectStreamEnd(SourcePosition(18u, 1u, 8u))
  }

  @Test
  fun example_5_10_invalid_use_of_reserved_indicators() {
    val input   = "commercial-at: @text\ngrave-accent: `text"
    val scanner = makeScanner(input)

    scanner.expectStreamStart()

    scanner.expectPlainScalar("commercial-at", 0u, SourcePosition(0u, 0u, 0u), SourcePosition(13u, 0u, 13u))
    scanner.expectMappingValue(0u, SourcePosition(13u, 0u, 13u))
    scanner.expectPlainScalar("@text", 0u, SourcePosition(15u, 0u, 15u), SourcePosition(20u, 0u, 20u)) {
      assertEquals(1, it.size)
    }

    scanner.expectPlainScalar("grave-accent", 0u, SourcePosition(21u, 1u, 0u))
    scanner.expectMappingValue(0u, SourcePosition(33u, 1u, 12u))
    scanner.expectPlainScalar("`text", 0u, SourcePosition(35u, 1u, 14u)) {
      assertEquals(1, it.size)
    }

    scanner.expectStreamEnd(SourcePosition(40u, 1u, 19u))
  }

  @Test
  fun example_5_11_line_break_characters() {
    val input = "|\n" +
      "  Line break (no glyph)\n" +
      "  Line break (glyphed)\n"

    val scanner = makeScanner(input)

    scanner.expectStreamStart()
    scanner.expectLiteralScalar("Line break (no glyph)\nLine break (glyphed)\n", 0u, SourcePosition(0u, 0u, 0u), SourcePosition(49u, 3u, 0u))
    scanner.expectStreamEnd(SourcePosition(49u, 3u, 0u))
  }

  @Test
  fun example_5_12_tabs_and_spaces() {
    val input = "# Tabs and spaces\n" +
      "quoted: \"Quoted \t\"\n" +
      "block:\t|\n" +
      "  void main() {\n" +
      "  \tprintf(\"Hello, world!\\n\");\n" +
      "  }\n"

    val scanner = makeScanner(input)

    scanner.expectStreamStart()
    scanner.expectComment("Tabs and spaces", 0u, false, SourcePosition(0u, 0u, 0u), SourcePosition(17u, 0u, 17u))
    scanner.expectPlainScalar("quoted", 0u, SourcePosition(18u, 1u, 0u), SourcePosition(24u, 1u, 6u))
    scanner.expectMappingValue(0u, SourcePosition(24u, 1u, 6u))
    scanner.expectDoubleQuotedScalar("Quoted \t", 0u, SourcePosition(26u, 1u, 8u), SourcePosition(36u, 1u, 18u))
    scanner.expectPlainScalar("block", 0u, SourcePosition(37u, 2u, 0u), SourcePosition(42u, 2u, 5u))
    scanner.expectMappingValue(0u, SourcePosition(42u, 2u, 5u))
    scanner.expectLiteralScalar("void main() {\n\tprintf(\"Hello, world!\\n\");\n}\n", 0u, SourcePosition(44u, 2u, 7u), SourcePosition(96u, 6u, 0u))
    scanner.expectStreamEnd(SourcePosition(96u, 6u, 0u))
  }

  @Test
  fun example_5_13_escaped_characters() {
    val input = """- "Fun with \\"
- "\" \a \b \e \f"
- "\n \r \t \v \0"
- "\  \_ \N \L \P \
  \x41 \u0041 \U00000041""""

    val scanner = makeScanner(input)

    scanner.expectStreamStart()
    scanner.expectSequenceEntry(0u, SourcePosition(0u, 0u, 0u))
    scanner.expectDoubleQuotedScalar("Fun with \\", 2u, SourcePosition(2u, 0u, 2u), SourcePosition(15u, 0u, 15u))
    scanner.expectSequenceEntry(0u, SourcePosition(16u, 1u, 0u))
    scanner.expectDoubleQuotedScalar("\" \u0007 \u0008 \u001b \u000c", 2u, SourcePosition(18u, 1u, 2u), SourcePosition(34u, 1u, 18u))
    scanner.expectSequenceEntry(0u, SourcePosition(35u, 2u, 0u))
    scanner.expectDoubleQuotedScalar("\n \r \t \u000b \u0000", 2u, SourcePosition(37u, 2u, 2u), SourcePosition(53u, 2u, 18u))
    scanner.expectSequenceEntry(0u, SourcePosition(54u, 3u, 0u))
    scanner.expectDoubleQuotedScalar("\u0020 \u00a0 \u0085 \u2028 \u2029 A A A", 2u, SourcePosition(56u, 3u, 2u), SourcePosition(99u, 4u, 25u))
    scanner.expectStreamEnd(SourcePosition(99u, 4u, 25u))
  }

  @Test
  fun example_5_14_invalid_escaped_characters() {
    val input = """Bad escapes:
  "\c
  \xq-""""

    val scanner = makeScanner(input)

    scanner.expectStreamStart()
    scanner.expectPlainScalar("Bad escapes", 0u, SourcePosition(0u, 0u, 0u), SourcePosition(11u, 0u, 11u))
    scanner.expectMappingValue(0u, SourcePosition(11u, 0u, 11u))
    scanner.expectDoubleQuotedScalar("\\c \\xq-", 2u, SourcePosition(15u, 1u, 2u), SourcePosition(26u, 2u, 7u)) {
      assertEquals(2, it.size)
    }
    scanner.expectStreamEnd(SourcePosition(26u, 2u, 7u))
  }

  @Test
  fun example_6_1_indentation_spaces() {
    //language=yaml
    val input = """  # Leading comment line spaces are
   # neither content nor indentation.
    
Not indented:
 By one space: |
    By four
      spaces
 Flow style: [    # Leading spaces
   By two,        # in flow style
  Also by two,    # are neither
  	Still by two   # content nor
    ]             # indentation."""

    val scanner = makeScanner(input)

    scanner.expectStreamStart()

    scanner.expectComment("Leading comment line spaces are", 2u, false, SourcePosition(2u, 0u, 2u), SourcePosition(35u, 0u, 35u))
    scanner.expectComment("neither content nor indentation.", 3u, false, SourcePosition(39u, 1u, 3u), SourcePosition(73u, 1u, 37u))

    scanner.expectPlainScalar("Not indented", 0u, SourcePosition(79u, 3u, 0u), SourcePosition(91u, 3u, 12u))
    scanner.expectMappingValue(0u, SourcePosition(91u, 3u, 12u))

    scanner.expectPlainScalar("By one space", 1u, SourcePosition(94u, 4u, 1u), SourcePosition(106u, 4u, 13u))
    scanner.expectMappingValue(1u, SourcePosition(106u, 4u, 13u))

    scanner.expectLiteralScalar("By four\n  spaces\n", 1u, SourcePosition(108u, 4u, 15u), SourcePosition(135u, 7u, 0u))

    scanner.expectPlainScalar("Flow style", 1u, SourcePosition(136u, 7u, 1u), SourcePosition(146u, 7u, 11u))
    scanner.expectMappingValue(1u, SourcePosition(146u, 7u, 11u))

    scanner.expectFlowSequenceStart(1u, SourcePosition(148u, 7u, 13u))
    scanner.expectComment("Leading spaces", 1u, true, SourcePosition(153u, 7u, 18u), SourcePosition(169u, 7u, 34u))

    scanner.expectPlainScalar("By two", 3u, SourcePosition(173u, 8u, 3u), SourcePosition(179u, 8u, 9u))
    scanner.expectFlowItemSeparator(SourcePosition(179u, 8u, 9u))
    scanner.expectComment("in flow style", 3u, true, SourcePosition(188u, 8u, 18u), SourcePosition(203u, 8u, 33u))

    scanner.expectPlainScalar("Also by two", 2u, SourcePosition(206u, 9u, 2u), SourcePosition(217u, 9u, 13u))
    scanner.expectFlowItemSeparator(SourcePosition(217u, 9u, 13u))
    scanner.expectComment("are neither", 2u, true, SourcePosition(222u, 9u, 18u), SourcePosition(235u, 9u, 31u))

    scanner.expectPlainScalar("Still by two", 3u, SourcePosition(239u, 10u, 3u), SourcePosition(251u, 10u, 15u))
    scanner.expectComment("content nor", 3u, true, SourcePosition(254u, 10u, 18u), SourcePosition(267u, 10u, 31u))

    scanner.expectFlowSequenceEnd(SourcePosition(272u, 11u, 4u))
    scanner.expectComment("indentation.", 4u, true, SourcePosition(286u, 11u, 18u), SourcePosition(300u, 11u, 32u))

    scanner.expectStreamEnd(SourcePosition(300u, 11u, 32u))
  }

  @Test
  fun example_6_2_indentation_indicators() {
    //language=yaml
    val input = """? a
: -	b
  -  -	c
     - d"""

    val scanner = makeScanner(input)

    scanner.expectStreamStart()

    scanner.expectMappingKey(0u, SourcePosition(0u, 0u, 0u))
    scanner.expectPlainScalar("a", 2u, SourcePosition(2u, 0u, 2u), SourcePosition(3u, 0u, 3u))

    scanner.expectMappingValue(0u, SourcePosition(4u, 1u, 0u))

    scanner.expectSequenceEntry(2u, SourcePosition(6u, 1u, 2u))
    scanner.expectPlainScalar("b", 4u, SourcePosition(8u, 1u, 4u), SourcePosition(9u, 1u, 5u))

    scanner.expectSequenceEntry(2u, SourcePosition(12u, 2u, 2u))

    scanner.expectSequenceEntry(5u, SourcePosition(15u, 2u, 5u))
    scanner.expectPlainScalar("c", 7u, SourcePosition(17u, 2u, 7u), SourcePosition(18u, 2u, 8u))

    scanner.expectSequenceEntry(5u, SourcePosition(24u, 3u, 5u))
    scanner.expectPlainScalar("d", 7u, SourcePosition(26u, 3u, 7u), SourcePosition(27u, 3u, 8u))

    scanner.expectStreamEnd(SourcePosition(27u, 3u, 8u))
  }

  @Test
  fun example_6_3_separation_spaces() {
    //language=yaml
    val input = """- foo:	 bar
- - baz
  -	baz"""

    val scanner = makeScanner(input)

    scanner.expectStreamStart()
    scanner.expectSequenceEntry(0u, SourcePosition(0u, 0u, 0u))
    scanner.expectPlainScalar("foo", 2u, SourcePosition(2u, 0u, 2u), SourcePosition(5u, 0u, 5u))
    scanner.expectMappingValue(2u, SourcePosition(5u, 0u, 5u))
    scanner.expectPlainScalar("bar", 2u, SourcePosition(8u, 0u, 8u), SourcePosition(11u, 0u, 11u))
    scanner.expectSequenceEntry(0u, SourcePosition(12u, 1u, 0u))
    scanner.expectSequenceEntry(2u, SourcePosition(14u, 1u, 2u))
    scanner.expectPlainScalar("baz", 4u, SourcePosition(16u, 1u, 4u), SourcePosition(19u, 1u, 7u))
    scanner.expectSequenceEntry(2u, SourcePosition(22u, 2u, 2u))
    scanner.expectPlainScalar("baz", 4u, SourcePosition(24u, 2u, 4u), SourcePosition(27u, 2u, 7u))
    scanner.expectStreamEnd(SourcePosition(27u, 2u, 7u))
  }

  @Test
  fun example_6_4_line_prefixes() {
    //language=yaml
    val input = """plain: text
  lines
quoted: "text
  	lines"
block: |
  text
   	lines"""

    val scanner = makeScanner(input)

    scanner.expectStreamStart()
    scanner.expectPlainScalar("plain", 0u, SourcePosition(0u, 0u, 0u), SourcePosition(5u, 0u, 5u))
    scanner.expectMappingValue(0u, SourcePosition(5u, 0u, 5u))
    scanner.expectPlainScalar("text lines", 0u, SourcePosition(7u, 0u, 7u), SourcePosition(19u, 1u, 7u))
    scanner.expectPlainScalar("quoted", 0u, SourcePosition(20u, 2u, 0u), SourcePosition(26u, 2u, 6u))
    scanner.expectMappingValue(0u, SourcePosition(26u, 2u, 6u))
    scanner.expectDoubleQuotedScalar("text lines", 0u, SourcePosition(28u, 2u, 8u), SourcePosition(43u, 3u, 9u))
    scanner.expectPlainScalar("block", 0u, SourcePosition(44u, 4u, 0u), SourcePosition(49u, 4u, 5u))
    scanner.expectMappingValue(0u, SourcePosition(49u, 4u, 5u))
    scanner.expectLiteralScalar("text\n \tlines\n", 0u, SourcePosition(51u, 4u, 7u), SourcePosition(69u, 6u, 9u))
    scanner.expectStreamEnd(SourcePosition(69u, 6u, 9u))
  }

  @Test
  fun example_6_5_empty_lines() {
    //language=yaml
    val input = """Folding:
  "Empty line
   	
  as a line feed"
Chomping: |
  Clipped empty lines
 """

    val scanner = makeScanner(input)

    scanner.expectStreamStart()
    scanner.expectPlainScalar("Folding", 0u, SourcePosition(0u, 0u, 0u), SourcePosition(7u, 0u, 7u))
    scanner.expectMappingValue(0u, SourcePosition(7u, 0u, 7u))
    scanner.expectDoubleQuotedScalar("Empty line\nas a line feed", 2u, SourcePosition(11u, 1u, 2u), SourcePosition(45u, 3u, 17u))
    scanner.expectPlainScalar("Chomping", 0u, SourcePosition(46u, 4u, 0u), SourcePosition(54u, 4u, 8u))
    scanner.expectMappingValue(0u, SourcePosition(54u, 4u, 8u))
    scanner.expectLiteralScalar("Clipped empty lines\n", 0u, SourcePosition(56u, 4u, 10u), SourcePosition(80u, 6u, 0u))
    scanner.expectStreamEnd(SourcePosition(81u, 6u, 1u))
  }

  @Test
  fun example_6_6_line_folding() {
    val input = """>-
  trimmed
  
 

  as
  space"""

    val scanner = makeScanner(input)

    scanner.expectStreamStart()
    scanner.expectFoldedScalar("trimmed\n\n\nas space", 0u, SourcePosition(0u, 0u, 0u), SourcePosition(31u, 6u, 7u))
    scanner.expectStreamEnd(SourcePosition(31u, 6u, 7u))
  }

  @Test
  fun example_6_7_block_folding() {
    //language=yaml
    val input = """>
  foo 
 
  	 bar

  baz
"""
    val scanner = makeScanner(input)

    scanner.expectStreamStart()
    scanner.expectFoldedScalar("foo \n\n\t bar\n\nbaz\n", 0u, SourcePosition(0u, 0u, 0u), SourcePosition(26u, 6u, 0u))
    scanner.expectStreamEnd(SourcePosition(26u, 6u, 0u))
  }

  @Test
  fun example_6_8_flow_folding() {
    //language=yaml
    val input = """"
  foo 
 
  	 bar

  baz
 """"

    val scanner = makeScanner(input)

    scanner.expectStreamStart()
    scanner.expectDoubleQuotedScalar(" foo\nbar\nbaz ", 0u, SourcePosition(0u, 0u, 0u), SourcePosition(28u, 6u, 2u))
    scanner.expectStreamEnd(SourcePosition(28u, 6u, 2u))
  }

  @Test
  fun example_6_9_separated_comment() {
    //language=yaml
    val input = """key:    # Comment
  value"""

    val scanner = makeScanner(input)

    scanner.expectStreamStart()
    scanner.expectPlainScalar("key", 0u, SourcePosition(0u, 0u, 0u), SourcePosition(3u, 0u, 3u))
    scanner.expectMappingValue(0u, SourcePosition(3u, 0u, 3u))
    scanner.expectComment("Comment", 0u, true, SourcePosition(8u, 0u, 8u), SourcePosition(17u, 0u, 17u))
    scanner.expectPlainScalar("value", 2u, SourcePosition(20u, 1u, 2u), SourcePosition(25u, 1u, 7u))
    scanner.expectStreamEnd(SourcePosition(25u, 1u, 7u))
  }

  @Test
  fun example_6_10_comment_lines() {
    val input = """  # Comment
   

"""
    val scanner = makeScanner(input)

    scanner.expectStreamStart()
    scanner.expectComment("Comment", 2u, false, SourcePosition(2u, 0u, 2u), SourcePosition(11u, 0u, 11u))
    scanner.expectStreamEnd(SourcePosition(17u, 3u, 0u))
  }

  @Test
  fun example_6_11_multi_line_comments() {
    //language=yaml
    val input = """key:    # Comment
        # lines
  value

"""
    val scanner = makeScanner(input)

    scanner.expectStreamStart()
    scanner.expectPlainScalar("key", 0u, SourcePosition(0u, 0u, 0u), SourcePosition(3u, 0u, 3u))
    scanner.expectMappingValue(0u, SourcePosition(3u, 0u, 3u))
    scanner.expectComment("Comment", 0u, true, SourcePosition(8u, 0u, 8u), SourcePosition(17u, 0u, 17u))
    scanner.expectComment("lines", 8u, false, SourcePosition(26u, 1u, 8u), SourcePosition(33u, 1u, 15u))
    scanner.expectPlainScalar("value", 2u, SourcePosition(36u, 2u, 2u), SourcePosition(41u, 2u, 7u))
    scanner.expectStreamEnd(SourcePosition(43u, 4u, 0u))
  }

  @Test
  fun example_6_12_separation_spaces() {
    //language=yaml
    val input = """{ first: Sammy, last: Sosa }:
# Statistics:
  hr:  # Home runs
     65
  avg: # Average
   0.278"""
    val scanner = makeScanner(input)

    scanner.expectStreamStart()
    scanner.expectFlowMappingStart(0u, SourcePosition(0u, 0u, 0u))
    scanner.expectPlainScalar("first", 0u, SourcePosition(2u, 0u, 2u), SourcePosition(7u, 0u, 7u))
    scanner.expectMappingValue(0u, SourcePosition(7u, 0u, 7u))
    scanner.expectPlainScalar("Sammy", 0u, SourcePosition(9u, 0u, 9u), SourcePosition(14u, 0u, 14u))
    scanner.expectFlowItemSeparator(SourcePosition(14u, 0u, 14u))
    scanner.expectPlainScalar("last", 0u, SourcePosition(16u, 0u, 16u), SourcePosition(20u, 0u, 20u))
    scanner.expectMappingValue(0u, SourcePosition(20u, 0u, 20u))
    scanner.expectPlainScalar("Sosa", 0u, SourcePosition(22u, 0u, 22u), SourcePosition(26u, 0u, 26u))
    scanner.expectFlowMappingEnd(SourcePosition(27u, 0u, 27u))
    scanner.expectMappingValue(0u, SourcePosition(28u, 0u, 28u))
    scanner.expectComment("Statistics:", 0u, false, SourcePosition(30u, 1u, 0u), SourcePosition(43u, 1u, 13u))
    scanner.expectPlainScalar("hr", 2u, SourcePosition(46u, 2u, 2u), SourcePosition(48u, 2u, 4u))
    scanner.expectMappingValue(2u, SourcePosition(48u, 2u, 4u))
    scanner.expectComment("Home runs", 2u, true, SourcePosition(51u, 2u, 7u), SourcePosition(62u, 2u, 18u))
    scanner.expectPlainScalar("65", 5u, SourcePosition(68u, 3u, 5u), SourcePosition(70u, 3u, 7u))
    scanner.expectPlainScalar("avg", 2u, SourcePosition(73u, 4u, 2u), SourcePosition(76u, 4u, 5u))
    scanner.expectMappingValue(2u, SourcePosition(76u, 4u, 5u))
    scanner.expectComment("Average", 2u, true, SourcePosition(78u, 4u, 7u), SourcePosition(87u, 4u, 16u))
    scanner.expectPlainScalar("0.278", 3u, SourcePosition(91u, 5u, 3u), SourcePosition(96u, 5u, 8u))
    scanner.expectStreamEnd(SourcePosition(96u, 5u, 8u))
  }

  @Test
  fun example_6_13_reserved_directives() {
    //language=yaml
    val input = """%FOO  bar baz # Should be ignored
               # with a warning.
--- "foo""""

    val scanner = makeScanner(input)

    scanner.expectStreamStart()
    scanner.expectInvalid(0u, SourcePosition(0u, 0u, 0u), SourcePosition(13u, 0u, 13u)) {
      assertEquals(1, it.size)
    }
    scanner.expectComment("Should be ignored", 0u, true, SourcePosition(14u, 0u, 14u), SourcePosition(33u, 0u, 33u))
    scanner.expectComment("with a warning.", 15u, false, SourcePosition(49u, 1u, 15u), SourcePosition(66u, 1u, 32u))
    scanner.expectDocumentStart(SourcePosition(67u, 2u, 0u))
    scanner.expectDoubleQuotedScalar("foo", 0u, SourcePosition(71u, 2u, 4u), SourcePosition(76u, 2u, 9u))
    scanner.expectStreamEnd(SourcePosition(76u, 2u, 9u))
  }
}