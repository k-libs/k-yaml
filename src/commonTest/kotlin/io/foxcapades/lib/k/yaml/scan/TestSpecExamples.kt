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
    scanner.expectPlainScalar("sequence", SourcePosition(0u, 0u, 0u), 0u)
    scanner.expectMappingValue(SourcePosition(8u, 0u, 8u), 0u)

    // - one
    scanner.expectSequenceEntry(SourcePosition(10u, 1u, 0u), 0u)
    scanner.expectPlainScalar("one", SourcePosition(12u, 1u, 2u), 2u)

    // - two
    scanner.expectSequenceEntry(SourcePosition(16u, 2u, 0u), 0u)
    scanner.expectPlainScalar("two", SourcePosition(18u, 2u, 2u), 2u)

    // mapping:
    scanner.expectPlainScalar("mapping", SourcePosition(22u, 3u, 0u), 0u)
    scanner.expectMappingValue(SourcePosition(29u, 3u, 7u), 0u)

    //   ? sky
    //   : blue
    scanner.expectMappingKey(SourcePosition(33u, 4u, 2u), 2u)
    scanner.expectPlainScalar("sky", SourcePosition(35u, 4u, 4u), 4u)
    scanner.expectMappingValue(SourcePosition(41u, 5u, 2u), 2u)
    scanner.expectPlainScalar("blue", SourcePosition(43u, 5u, 4u), 4u)

    //   sea : green
    scanner.expectPlainScalar("sea", SourcePosition(50u, 6u, 2u), 2u)
    scanner.expectMappingValue(SourcePosition(54u, 6u, 6u), 2u)
    scanner.expectPlainScalar("green", SourcePosition(56u, 6u, 8u), 2u)

    scanner.expectStreamEnd(SourcePosition(61u, 6u, 13u))
  }

  @Test
  fun example_5_4_flow_collection_indicators() {
    val input = "sequence: [ one, two, ]\n" +
      "mapping: { sky: blue, sea: green }"

    val scanner = makeScanner(input)

    scanner.expectStreamStart()
    scanner.expectPlainScalar("sequence", SourcePosition(0u, 0u, 0u), 0u)
    scanner.expectMappingValue(SourcePosition(8u, 0u, 8u), 0u)
    scanner.expectFlowSequenceStart(SourcePosition(10u, 0u, 10u), 0u)
    scanner.expectPlainScalar("one", SourcePosition(12u, 0u, 12u), 0u)
    scanner.expectFlowItemSeparator(SourcePosition(15u, 0u, 15u))
    scanner.expectPlainScalar("two", SourcePosition(17u, 0u, 17u), 0u)
    scanner.expectFlowItemSeparator(SourcePosition(20u, 0u, 20u))
    scanner.expectFlowSequenceEnd(SourcePosition(22u, 0u, 22u))
    scanner.expectPlainScalar("mapping", SourcePosition(24u, 1u, 0u), 0u)
    scanner.expectMappingValue(SourcePosition(31u, 1u, 7u), 0u)
    scanner.expectFlowMappingStart(SourcePosition(33u, 1u, 9u), 0u)
    scanner.expectPlainScalar("sky", SourcePosition(35u, 1u, 11u), 0u)
    scanner.expectMappingValue(SourcePosition(38u, 1u, 14u), 0u)
    scanner.expectPlainScalar("blue", SourcePosition(40u, 1u, 16u), 0u)
    scanner.expectFlowItemSeparator(SourcePosition(44u, 1u, 20u))
    scanner.expectPlainScalar("sea", SourcePosition(46u, 1u, 22u), 0u)
    scanner.expectMappingValue(SourcePosition(49u, 1u, 25u), 0u)
    scanner.expectPlainScalar("green", SourcePosition(51u, 1u, 27u), 0u)
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
    scanner.expectPlainScalar("anchored", SourcePosition(0u, 0u, 0u), 0u)
    scanner.expectMappingValue(SourcePosition(8u, 0u, 8u), 0u)
    scanner.expectTag("!", "local", SourcePosition(10u, 0u, 10u))
    scanner.expectAnchor("anchor", SourcePosition(17u, 0u, 17u), 0u)
    scanner.expectPlainScalar("value", SourcePosition(25u, 0u, 25u), 0u)
    scanner.expectPlainScalar("alias", SourcePosition(31u, 1u, 0u), 0u)
    scanner.expectMappingValue(SourcePosition(36u, 1u, 5u), 0u)
    scanner.expectAlias("anchor", SourcePosition(38u, 1u, 7u), 0u)
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
    scanner.expectPlainScalar("literal", SourcePosition(0u, 0u, 0u), 0u)
    scanner.expectMappingValue(SourcePosition(7u, 0u, 7u), 0u)

    // |
    //   some
    //   text
    scanner.expectLiteralScalar("some\ntext\n", 0u, SourcePosition(9u, 0u, 9u), SourcePosition(25u, 3u, 0u))

    // folded:
    scanner.expectPlainScalar("folded", SourcePosition(25u, 3u, 0u), 0u)
    scanner.expectMappingValue(SourcePosition(31u, 3u, 6u), 0u)

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
    scanner.expectPlainScalar("single", SourcePosition(0u, 0u, 0u), 0u)
    scanner.expectMappingValue(SourcePosition(6u, 0u, 6u), 0u)
    scanner.expectSingleQuotedScalar("text", SourcePosition(8u, 0u, 8u), 0u, SourcePosition(14u, 0u, 14u))
    scanner.expectPlainScalar("double", SourcePosition(15u, 1u, 0u), 0u, SourcePosition(21u, 1u, 6u))
    scanner.expectMappingValue(SourcePosition(21u, 1u, 6u), 0u)
    scanner.expectDoubleQuotedScalar("text", SourcePosition(23u, 1u, 8u), 0u, SourcePosition(29u, 1u, 14u))
    scanner.expectStreamEnd(SourcePosition(29u, 1u, 14u))
  }

  @Test
  fun example_5_9_directive_indicator() {
    val input   = "%YAML 1.2\n--- text"
    val scanner = makeScanner(input)

    scanner.expectStreamStart()
    scanner.expectYAMLDirective(1u, 2u, SourcePosition(0u, 0u, 0u), SourcePosition(9u, 0u, 9u))
    scanner.expectDocumentStart(SourcePosition(10u, 1u, 0u))
    scanner.expectPlainScalar("text", SourcePosition(14u, 1u, 4u), 0u, SourcePosition(18u, 1u, 8u))
    scanner.expectStreamEnd(SourcePosition(18u, 1u, 8u))
  }

  @Test
  fun example_5_10_invalid_use_of_reserved_indicators() {
    val input   = "commercial-at: @text\ngrave-accent: `text"
    val scanner = makeScanner(input)

    scanner.expectStreamStart()

    scanner.expectPlainScalar("commercial-at", SourcePosition(0u, 0u, 0u), 0u, SourcePosition(13u, 0u, 13u))
    scanner.expectMappingValue(SourcePosition(13u, 0u, 13u), 0u)
    scanner.expectPlainScalar("@text", SourcePosition(15u, 0u, 15u), 0u, SourcePosition(20u, 0u, 20u)) {
      assertEquals(1, it.size)
    }

    scanner.expectPlainScalar("grave-accent", SourcePosition(21u, 1u, 0u), 0u)
    scanner.expectMappingValue(SourcePosition(33u, 1u, 12u), 0u)
    scanner.expectPlainScalar("`text", SourcePosition(35u, 1u, 14u), 0u) {
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
    scanner.expectPlainScalar("quoted", SourcePosition(18u, 1u, 0u), 0u, SourcePosition(24u, 1u, 6u))
    scanner.expectMappingValue(SourcePosition(24u, 1u, 6u), 0u)
    scanner.expectDoubleQuotedScalar("Quoted \t", SourcePosition(26u, 1u, 8u), 0u, SourcePosition(36u, 1u, 18u))
    scanner.expectPlainScalar("block", SourcePosition(37u, 2u, 0u), 0u, SourcePosition(42u, 2u, 5u))
    scanner.expectMappingValue(SourcePosition(42u, 2u, 5u), 0u)
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
    scanner.expectSequenceEntry(SourcePosition(0u, 0u, 0u), 0u)
    scanner.expectDoubleQuotedScalar("Fun with \\", SourcePosition(2u, 0u, 2u), 2u, SourcePosition(15u, 0u, 15u))
    scanner.expectSequenceEntry(SourcePosition(16u, 1u, 0u), 0u)
    scanner.expectDoubleQuotedScalar(
      "\" \u0007 \u0008 \u001b \u000c",
      SourcePosition(18u, 1u, 2u),
      2u,
      SourcePosition(34u, 1u, 18u)
    )
    scanner.expectSequenceEntry(SourcePosition(35u, 2u, 0u), 0u)
    scanner.expectDoubleQuotedScalar(
      "\n \r \t \u000b \u0000",
      SourcePosition(37u, 2u, 2u),
      2u,
      SourcePosition(53u, 2u, 18u)
    )
    scanner.expectSequenceEntry(SourcePosition(54u, 3u, 0u), 0u)
    scanner.expectDoubleQuotedScalar(
      "\u0020 \u00a0 \u0085 \u2028 \u2029 A A A",
      SourcePosition(56u, 3u, 2u),
      2u,
      SourcePosition(99u, 4u, 25u)
    )
    scanner.expectStreamEnd(SourcePosition(99u, 4u, 25u))
  }

  @Test
  fun example_5_14_invalid_escaped_characters() {
    val input = """Bad escapes:
  "\c
  \xq-""""

    val scanner = makeScanner(input)

    scanner.expectStreamStart()
    scanner.expectPlainScalar("Bad escapes", SourcePosition(0u, 0u, 0u), 0u, SourcePosition(11u, 0u, 11u))
    scanner.expectMappingValue(SourcePosition(11u, 0u, 11u), 0u)
    scanner.expectDoubleQuotedScalar("\\c \\xq-", SourcePosition(15u, 1u, 2u), 2u, SourcePosition(26u, 2u, 7u)) {
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

    scanner.expectPlainScalar("Not indented", SourcePosition(79u, 3u, 0u), 0u, SourcePosition(91u, 3u, 12u))
    scanner.expectMappingValue(SourcePosition(91u, 3u, 12u), 0u)

    scanner.expectPlainScalar("By one space", SourcePosition(94u, 4u, 1u), 1u, SourcePosition(106u, 4u, 13u))
    scanner.expectMappingValue(SourcePosition(106u, 4u, 13u), 1u)

    scanner.expectLiteralScalar("By four\n  spaces\n", 1u, SourcePosition(108u, 4u, 15u), SourcePosition(135u, 7u, 0u))

    scanner.expectPlainScalar("Flow style", SourcePosition(136u, 7u, 1u), 1u, SourcePosition(146u, 7u, 11u))
    scanner.expectMappingValue(SourcePosition(146u, 7u, 11u), 1u)

    scanner.expectFlowSequenceStart(SourcePosition(148u, 7u, 13u), 1u)
    scanner.expectComment("Leading spaces", 1u, true, SourcePosition(153u, 7u, 18u), SourcePosition(169u, 7u, 34u))

    scanner.expectPlainScalar("By two", SourcePosition(173u, 8u, 3u), 3u, SourcePosition(179u, 8u, 9u))
    scanner.expectFlowItemSeparator(SourcePosition(179u, 8u, 9u))
    scanner.expectComment("in flow style", 3u, true, SourcePosition(188u, 8u, 18u), SourcePosition(203u, 8u, 33u))

    scanner.expectPlainScalar("Also by two", SourcePosition(206u, 9u, 2u), 2u, SourcePosition(217u, 9u, 13u))
    scanner.expectFlowItemSeparator(SourcePosition(217u, 9u, 13u))
    scanner.expectComment("are neither", 2u, true, SourcePosition(222u, 9u, 18u), SourcePosition(235u, 9u, 31u))

    scanner.expectPlainScalar("Still by two", SourcePosition(239u, 10u, 3u), 3u, SourcePosition(251u, 10u, 15u))
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

    scanner.expectMappingKey(SourcePosition(0u, 0u, 0u), 0u)
    scanner.expectPlainScalar("a", SourcePosition(2u, 0u, 2u), 2u, SourcePosition(3u, 0u, 3u))

    scanner.expectMappingValue(SourcePosition(4u, 1u, 0u), 0u)

    scanner.expectSequenceEntry(SourcePosition(6u, 1u, 2u), 2u)
    scanner.expectPlainScalar("b", SourcePosition(8u, 1u, 4u), 4u, SourcePosition(9u, 1u, 5u))

    scanner.expectSequenceEntry(SourcePosition(12u, 2u, 2u), 2u)

    scanner.expectSequenceEntry(SourcePosition(15u, 2u, 5u), 5u)
    scanner.expectPlainScalar("c", SourcePosition(17u, 2u, 7u), 7u, SourcePosition(18u, 2u, 8u))

    scanner.expectSequenceEntry(SourcePosition(24u, 3u, 5u), 5u)
    scanner.expectPlainScalar("d", SourcePosition(26u, 3u, 7u), 7u, SourcePosition(27u, 3u, 8u))

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
    scanner.expectSequenceEntry(SourcePosition(0u, 0u, 0u), 0u)
    scanner.expectPlainScalar("foo", SourcePosition(2u, 0u, 2u), 2u, SourcePosition(5u, 0u, 5u))
    scanner.expectMappingValue(SourcePosition(5u, 0u, 5u), 2u)
    scanner.expectPlainScalar("bar", SourcePosition(8u, 0u, 8u), 2u, SourcePosition(11u, 0u, 11u))
    scanner.expectSequenceEntry(SourcePosition(12u, 1u, 0u), 0u)
    scanner.expectSequenceEntry(SourcePosition(14u, 1u, 2u), 2u)
    scanner.expectPlainScalar("baz", SourcePosition(16u, 1u, 4u), 4u, SourcePosition(19u, 1u, 7u))
    scanner.expectSequenceEntry(SourcePosition(22u, 2u, 2u), 2u)
    scanner.expectPlainScalar("baz", SourcePosition(24u, 2u, 4u), 4u, SourcePosition(27u, 2u, 7u))
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
    scanner.expectPlainScalar("plain", SourcePosition(0u, 0u, 0u), 0u, SourcePosition(5u, 0u, 5u))
    scanner.expectMappingValue(SourcePosition(5u, 0u, 5u), 0u)
    scanner.expectPlainScalar("text lines", SourcePosition(7u, 0u, 7u), 0u, SourcePosition(19u, 1u, 7u))
    scanner.expectPlainScalar("quoted", SourcePosition(20u, 2u, 0u), 0u, SourcePosition(26u, 2u, 6u))
    scanner.expectMappingValue(SourcePosition(26u, 2u, 6u), 0u)
    scanner.expectDoubleQuotedScalar("text lines", SourcePosition(28u, 2u, 8u), 0u, SourcePosition(43u, 3u, 9u))
    scanner.expectPlainScalar("block", SourcePosition(44u, 4u, 0u), 0u, SourcePosition(49u, 4u, 5u))
    scanner.expectMappingValue(SourcePosition(49u, 4u, 5u), 0u)
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
    scanner.expectPlainScalar("Folding", SourcePosition(0u, 0u, 0u), 0u, SourcePosition(7u, 0u, 7u))
    scanner.expectMappingValue(SourcePosition(7u, 0u, 7u), 0u)
    scanner.expectDoubleQuotedScalar(
      "Empty line\nas a line feed",
      SourcePosition(11u, 1u, 2u),
      2u,
      SourcePosition(45u, 3u, 17u)
    )
    scanner.expectPlainScalar("Chomping", SourcePosition(46u, 4u, 0u), 0u, SourcePosition(54u, 4u, 8u))
    scanner.expectMappingValue(SourcePosition(54u, 4u, 8u), 0u)
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
    scanner.expectDoubleQuotedScalar(" foo\nbar\nbaz ", SourcePosition(0u, 0u, 0u), 0u, SourcePosition(28u, 6u, 2u))
    scanner.expectStreamEnd(SourcePosition(28u, 6u, 2u))
  }

  @Test
  fun example_6_9_separated_comment() {
    //language=yaml
    val input = """key:    # Comment
  value"""

    val scanner = makeScanner(input)

    scanner.expectStreamStart()
    scanner.expectPlainScalar("key", SourcePosition(0u, 0u, 0u), 0u, SourcePosition(3u, 0u, 3u))
    scanner.expectMappingValue(SourcePosition(3u, 0u, 3u), 0u)
    scanner.expectComment("Comment", 0u, true, SourcePosition(8u, 0u, 8u), SourcePosition(17u, 0u, 17u))
    scanner.expectPlainScalar("value", SourcePosition(20u, 1u, 2u), 2u, SourcePosition(25u, 1u, 7u))
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
    scanner.expectPlainScalar("key", SourcePosition(0u, 0u, 0u), 0u, SourcePosition(3u, 0u, 3u))
    scanner.expectMappingValue(SourcePosition(3u, 0u, 3u), 0u)
    scanner.expectComment("Comment", 0u, true, SourcePosition(8u, 0u, 8u), SourcePosition(17u, 0u, 17u))
    scanner.expectComment("lines", 8u, false, SourcePosition(26u, 1u, 8u), SourcePosition(33u, 1u, 15u))
    scanner.expectPlainScalar("value", SourcePosition(36u, 2u, 2u), 2u, SourcePosition(41u, 2u, 7u))
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
    scanner.expectFlowMappingStart(SourcePosition(0u, 0u, 0u), 0u)
    scanner.expectPlainScalar("first", SourcePosition(2u, 0u, 2u), 0u, SourcePosition(7u, 0u, 7u))
    scanner.expectMappingValue(SourcePosition(7u, 0u, 7u), 0u)
    scanner.expectPlainScalar("Sammy", SourcePosition(9u, 0u, 9u), 0u, SourcePosition(14u, 0u, 14u))
    scanner.expectFlowItemSeparator(SourcePosition(14u, 0u, 14u))
    scanner.expectPlainScalar("last", SourcePosition(16u, 0u, 16u), 0u, SourcePosition(20u, 0u, 20u))
    scanner.expectMappingValue(SourcePosition(20u, 0u, 20u), 0u)
    scanner.expectPlainScalar("Sosa", SourcePosition(22u, 0u, 22u), 0u, SourcePosition(26u, 0u, 26u))
    scanner.expectFlowMappingEnd(SourcePosition(27u, 0u, 27u))
    scanner.expectMappingValue(SourcePosition(28u, 0u, 28u), 0u)
    scanner.expectComment("Statistics:", 0u, false, SourcePosition(30u, 1u, 0u), SourcePosition(43u, 1u, 13u))
    scanner.expectPlainScalar("hr", SourcePosition(46u, 2u, 2u), 2u, SourcePosition(48u, 2u, 4u))
    scanner.expectMappingValue(SourcePosition(48u, 2u, 4u), 2u)
    scanner.expectComment("Home runs", 2u, true, SourcePosition(51u, 2u, 7u), SourcePosition(62u, 2u, 18u))
    scanner.expectPlainScalar("65", SourcePosition(68u, 3u, 5u), 5u, SourcePosition(70u, 3u, 7u))
    scanner.expectPlainScalar("avg", SourcePosition(73u, 4u, 2u), 2u, SourcePosition(76u, 4u, 5u))
    scanner.expectMappingValue(SourcePosition(76u, 4u, 5u), 2u)
    scanner.expectComment("Average", 2u, true, SourcePosition(78u, 4u, 7u), SourcePosition(87u, 4u, 16u))
    scanner.expectPlainScalar("0.278", SourcePosition(91u, 5u, 3u), 3u, SourcePosition(96u, 5u, 8u))
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
    scanner.expectDoubleQuotedScalar("foo", SourcePosition(71u, 2u, 4u), 0u, SourcePosition(76u, 2u, 9u))
    scanner.expectStreamEnd(SourcePosition(76u, 2u, 9u))
  }

  @Test
  fun example_6_14_yaml_directive() {
    //language=yaml
    val input = """
      %YAML 1.3 # Attempt parsing
                 # with a warning
      ---
      "foo"
    """.trimIndent()

    val scanner = makeScanner(input)

    scanner.expectStreamStart()
    scanner.expectYAMLDirective(1u, 3u, SourcePosition(0u, 0u, 0u), SourcePosition(9u, 0u, 9u)) {
      assertEquals(1, it.size)
    }
    scanner.expectComment("Attempt parsing", 0u, true, SourcePosition(10u, 0u, 10u), SourcePosition(27u, 0u, 27u))
    scanner.expectComment("with a warning", 11u, false, SourcePosition(39u, 1u, 11u), SourcePosition(55u, 1u, 27u))
    scanner.expectDocumentStart(SourcePosition(56u, 2u, 0u))
    scanner.expectDoubleQuotedScalar("foo", SourcePosition(60u, 3u, 0u), 0u, SourcePosition(65u, 3u, 5u))
    scanner.expectStreamEnd(SourcePosition(65u, 3u, 5u))
  }

  @Test
  fun example_6_15_invalid_repeated_yaml_directive() {
    //language=yaml
    val input   = "%YAML 1.2\n%YAML 1.1\nfoo"
    val scanner = makeScanner(input)
    var cursor: SourcePosition

    // [STREAM-START]
    cursor = scanner.expectStreamStart()

    // %YAML 1.2
    cursor = scanner.expectYAMLDirective(1u, 2u, cursor)
      .skipLine() // Skip over the newline

    // %YAML 1.1
    cursor = scanner.expectYAMLDirective(1u, 1u, cursor)
      .skipLine() // Skip over the newline

    // foo
    cursor = scanner.expectPlainScalar("foo", cursor, 0u)

    // [STREAM-END]
    scanner.expectStreamEnd(cursor)
  }

  @Test
  fun example_6_16_tag_directive() {
    //language=yaml
    val input = """
      %TAG !yaml! tag:yaml.org,2002:
      ---
      !yaml!str "foo"
    """.trimIndent()

    val scanner = makeScanner(input)
    var cursor: SourcePosition

    cursor = scanner.expectStreamStart()
    cursor = scanner.expectTagDirective("!yaml!", "tag:yaml.org,2002:", cursor)
      .skipLine()
    cursor = scanner.expectDocumentStart(cursor)
      .skipLine()
    cursor = scanner.expectTag("!yaml!", "str", cursor)
      .skipSpace()
    cursor = scanner.expectDoubleQuotedScalar("foo", cursor, 0u)
    scanner.expectStreamEnd(cursor)
  }

  @Test
  fun example_6_17_invalid_repeated_tag_directive() {
    //language=yaml
    val input = """
      %TAG ! !foo
      %TAG ! !foo
      bar
    """.trimIndent()

    val scanner = makeScanner(input)
    var cursor: SourcePosition

    cursor = scanner.expectStreamStart()
    cursor = scanner.expectTagDirective("!", "!foo", cursor)
      .skipLine()
    cursor = scanner.expectTagDirective("!", "!foo", cursor)
      .skipLine()
    cursor = scanner.expectPlainScalar("bar", cursor, 0u)
    scanner.expectStreamEnd(cursor)
  }

  @Test
  fun example_6_18_primary_tag_handle() {
    //language=yaml
    val input = """
      # Private
      !foo "bar"
      ...
      # Global
      %TAG ! tag:example.com,2000:app/
      ---
      !foo "bar"
    """.trimIndent()

    val scanner = makeScanner(input)
    var cursor: SourcePosition

    cursor = scanner.expectStreamStart()
    cursor = scanner.expectComment("Private", 0u, false, cursor)
    cursor = scanner.expectTag("!", "foo", cursor.skipLine())
    cursor = scanner.expectDoubleQuotedScalar("bar", cursor.skipSpace(), 0u)
    cursor = scanner.expectDocumentEnd(cursor.skipLine())
    cursor = scanner.expectComment("Global", 0u, false, cursor.skipLine())
    cursor = scanner.expectTagDirective("!", "tag:example.com,2000:app/", cursor.skipLine())
    cursor = scanner.expectDocumentStart(cursor.skipLine())
    cursor = scanner.expectTag("!", "foo", cursor.skipLine())
    cursor = scanner.expectDoubleQuotedScalar("bar", cursor.skipSpace(), 0u)
    scanner.expectStreamEnd(cursor)
  }

  @Test
  fun example_6_19_secondary_tag_handle() {
    //language=yaml
    val input = """
      %TAG !! tag:example.com,2000:app/
      ---
      !!int 1 - 3 # Interval, not integer
    """.trimIndent()

    val scanner = makeScanner(input)

    var cursor = scanner.expectStreamStart()

    cursor = scanner.expectTagDirective("!!", "tag:example.com,2000:app/", cursor)
    cursor = scanner.expectDocumentStart(cursor.skipLine())
    cursor = scanner.expectTag("!!", "int", cursor.skipLine())
    cursor = scanner.expectPlainScalar("1 - 3", cursor.skipSpace(), 0u)
    cursor = scanner.expectComment("Interval, not integer", 0u, true, cursor.skipSpace())

    scanner.expectStreamEnd(cursor)
  }

  @Test
  fun example_6_20_tag_handles() {
    //language=yaml
    val input = """
      %TAG !e! tag:example.com,2000:app/
      ---
      !e!foo "bar"
    """.trimIndent()

    val scanner = makeScanner(input)

    var cursor = scanner.expectStreamStart()

    cursor = scanner.expectTagDirective("!e!", "tag:example.com,2000:app/", cursor)
    cursor = scanner.expectDocumentStart(cursor.skipLine())
    cursor = scanner.expectTag("!e!", "foo", cursor.skipLine())
    cursor = scanner.expectDoubleQuotedScalar("bar", cursor.skipSpace(), 0u)

    scanner.expectStreamEnd(cursor)
  }

  @Test
  fun example_6_21_local_tag_prefix() {
    //language=yaml
    val input = """
      %TAG !m! !my-
      --- # Bulb here
      !m!light fluorescent
      ...
      %TAG !m! !my-
      --- # Color here
      !m!light green
    """.trimIndent()

    val scanner = makeScanner(input)

    var cursor = scanner.expectStreamStart()

    cursor = scanner.expectTagDirective("!m!", "!my-", cursor)
    cursor = scanner.expectDocumentStart(cursor.skipLine())
    cursor = scanner.expectComment("Bulb here", 0u, true, cursor.skipSpace())
    cursor = scanner.expectTag("!m!", "light", cursor.skipLine())
    cursor = scanner.expectPlainScalar("fluorescent", cursor.skipSpace())
    cursor = scanner.expectDocumentEnd(cursor.skipLine())
    cursor = scanner.expectTagDirective("!m!", "!my-", cursor.skipLine())
    cursor = scanner.expectDocumentStart(cursor.skipLine())
    cursor = scanner.expectComment("Color here", 0u, true, cursor.skipSpace())
    cursor = scanner.expectTag("!m!", "light", cursor.skipLine())
    cursor = scanner.expectPlainScalar("green", cursor.skipSpace())

    scanner.expectStreamEnd(cursor)
  }

  @Test
  fun example_6_22_global_tag_prefix() {
    //language=yaml
    val input = """
      %TAG !e! tag:example.com,2000:app/
      ---
      - !e!foo "bar"
    """.trimIndent()

    val test = makeScanner(input)

    var cursor = test.expectStreamStart()

    cursor = test.expectTagDirective("!e!", "tag:example.com,2000:app/", cursor)
    cursor = test.expectDocumentStart(cursor.skipLine())
    cursor = test.expectSequenceEntry(cursor.skipLine())
    cursor = test.expectTag("!e!", "foo", cursor.skipSpace(), 2u)
    cursor = test.expectDoubleQuotedScalar("bar", cursor.skipSpace(), 2u)

    test.expectStreamEnd(cursor)
  }

  @Test
  fun example_6_23_node_properties() {
    val input = """
      !!str &a1 "foo":
        !!str bar
      &a2 baz : *a1
    """.trimIndent()

    val test = makeScanner(input)

    var cursor = test.expectStreamStart()

    cursor = test.expectTag("!!", "str", cursor)
    cursor = test.expectAnchor("a1", cursor.skipSpace())
    cursor = test.expectDoubleQuotedScalar("foo", cursor.skipSpace())
    cursor = test.expectMappingValue(cursor)
    cursor = test.expectTag("!!", "str", cursor.skipLine(2), 2u)
    cursor = test.expectPlainScalar("bar", cursor.skipSpace(), 2u)
    cursor = test.expectAnchor("a2", cursor.skipLine())
    cursor = test.expectPlainScalar("baz", cursor.skipSpace())
    cursor = test.expectMappingValue(cursor.skipSpace())
    cursor = test.expectAlias("a1", cursor.skipSpace())

    test.expectStreamEnd(cursor)
  }

  @Test
  fun example_6_24_verbatim_tags() {
    //language=yaml
    val input = """
      !<tag:yaml.org,2002:str> foo :
        !<!bar> baz
    """.trimIndent()

    val test = makeScanner(input)


    var cursor = test.expectStreamStart()

    cursor = test.expectTag("!<tag:yaml.org,2002:str>", "", cursor)
    cursor = test.expectPlainScalar("foo", cursor.skipSpace())
    cursor = test.expectMappingValue(cursor.skipSpace())
    cursor = test.expectTag("!<!bar>", "", cursor.skipLine(2), 2u)
    cursor = test.expectPlainScalar("baz", cursor.skipSpace(), 2u)

    test.expectStreamEnd(cursor)
  }

  @Test
  fun example_6_25_invalid_verbatim_tags() {
    //language=yaml
    val input = """
      - !<!> foo
      - !<$:?> bar
    """.trimIndent()

    val test = makeScanner(input)

    var cursor =  test.expectStreamStart()

    cursor = test.expectSequenceEntry(cursor)
    cursor = test.expectTag("!<!>", "", cursor.skipSpace(), 2u)
    cursor = test.expectPlainScalar("foo", cursor.skipSpace(), 2u)
    cursor = test.expectSequenceEntry(cursor.skipLine())
    cursor = test.expectTag("!<\$:?>", "", cursor.skipSpace(), 2u)
    cursor = test.expectPlainScalar("bar", cursor.skipSpace(), 2u)

    test.expectStreamEnd(cursor)
  }

  @Test
  fun example_6_26_tag_shorthands() {
    //language=yaml
    val input = """
      %TAG !e! tag:example.com,2000:app/
      ---
      - !local foo
      - !!str bar
      - !e!tag%21 baz
    """.trimIndent()

    val test = makeScanner(input)

    var cursor = test.expectStreamStart()

    cursor = test.expectTagDirective("!e!", "tag:example.com,2000:app/", cursor)
    cursor = test.expectDocumentStart(cursor.skipLine())
    cursor = test.expectSequenceEntry(cursor.skipLine())
    cursor = test.expectTag("!", "local", cursor.skipSpace(), 2u)
    cursor = test.expectPlainScalar("foo", cursor.skipSpace(), 2u)
    cursor = test.expectSequenceEntry(cursor.skipLine())
    cursor = test.expectTag("!!", "str", cursor.skipSpace(), 2u)
    cursor = test.expectPlainScalar("bar", cursor.skipSpace(), 2u)
    cursor = test.expectSequenceEntry(cursor.skipLine())
    cursor = test.expectTag("!e!", "tag%21", cursor.skipSpace(), 2u)
    cursor = test.expectPlainScalar("baz", cursor.skipSpace(), 2u)

    test.expectStreamEnd(cursor)
  }

  @Test
  fun example_6_27_invalid_tag_shorthands() {
    //language=yaml
    val input = """
      %TAG !e! tag:example,2000:app/
      ---
      - !e! foo
      - !h!bar baz
    """.trimIndent()

    val test = makeScanner(input)

    var cursor = test.expectStreamStart()

    cursor = test.expectTagDirective("!e!", "tag:example,2000:app/", cursor)
    cursor = test.expectDocumentStart(cursor.skipLine())
    cursor = test.expectSequenceEntry(cursor.skipLine())
    cursor = test.expectTag("!e!", "", cursor.skipSpace(), 2u) {
      assertEquals(1, it.size)
    }
    cursor = test.expectPlainScalar("foo", cursor.skipSpace(), 2u)
    cursor = test.expectSequenceEntry(cursor.skipLine())
    cursor = test.expectTag("!h!", "bar", cursor.skipSpace(), 2u)
    cursor = test.expectPlainScalar("baz", cursor.skipSpace(), 2u)

    test.expectStreamEnd(cursor)
  }

  @Test
  fun example_6_28_non_specific_tags() {
    //language=yaml
    val input = """
      # Assuming conventional resolution:
      - "12"
      - 12
      - ! 12
    """.trimIndent()

    val test = makeScanner(input)

    var cursor = test.expectStreamStart()

    cursor = test.expectComment("Assuming conventional resolution:", 0u, false, cursor)
    cursor = test.expectSequenceEntry(cursor.skipLine())
    cursor = test.expectDoubleQuotedScalar("12", cursor.skipSpace(), 2u)
    cursor = test.expectSequenceEntry(cursor.skipLine())
    cursor = test.expectPlainScalar("12", cursor.skipSpace(), 2u)
    cursor = test.expectSequenceEntry(cursor.skipLine())
    cursor = test.expectTag("!", "", cursor.skipSpace(), 2u)
    cursor = test.expectPlainScalar("12", cursor.skipSpace(), 2u)

    test.expectStreamEnd(cursor)
  }

  @Test
  fun example_6_29_node_anchors() {
    //language=yaml
    val input = """
      First occurrence: &anchor Value
      Second occurrence: *anchor
    """.trimIndent()

    val test = makeScanner(input)

    var cursor = test.expectStreamStart()

    cursor = test.expectPlainScalar("First occurrence", cursor)
    cursor = test.expectMappingValue(cursor)
    cursor = test.expectAnchor("anchor", cursor.skipSpace())
    cursor = test.expectPlainScalar("Value", cursor.skipSpace())
    cursor = test.expectPlainScalar("Second occurrence", cursor.skipLine())
    cursor = test.expectMappingValue(cursor)
    cursor = test.expectAlias("anchor", cursor.skipSpace())

    test.expectStreamEnd(cursor)
  }

  @Test
  fun example_7_1_alias_nodes() {
    //language=yaml
    val input = """
      First occurrence: &anchor Foo
      Second occurrence: *anchor
      Override anchor: &anchor Bar
      Reuse anchor: *anchor
    """.trimIndent()

    val test = makeScanner(input)

    var cursor = test.expectStreamStart()

    cursor = test.expectPlainScalar("First occurrence", cursor)
    cursor = test.expectMappingValue(cursor)
    cursor = test.expectAnchor("anchor", cursor.skipSpace())
    cursor = test.expectPlainScalar("Foo", cursor.skipSpace())
    cursor = test.expectPlainScalar("Second occurrence", cursor.skipLine())
    cursor = test.expectMappingValue(cursor)
    cursor = test.expectAlias("anchor", cursor.skipSpace())
    cursor = test.expectPlainScalar("Override anchor", cursor.skipLine())
    cursor = test.expectMappingValue(cursor)
    cursor = test.expectAnchor("anchor", cursor.skipSpace())
    cursor = test.expectPlainScalar("Bar", cursor.skipSpace())
    cursor = test.expectPlainScalar("Reuse anchor", cursor.skipLine())
    cursor = test.expectMappingValue(cursor)
    cursor = test.expectAlias("anchor", cursor.skipSpace())

    test.expectStreamEnd(cursor)
  }

  @Test
  fun example_7_2_empty_nodes() {
    //language=yaml
    val input = """
      {
        foo : !!str,
        !!str : bar,
      }
    """.trimIndent()

    val test = makeScanner(input)

    var cursor = test.expectStreamStart()

    cursor = test.expectFlowMappingStart(cursor)
    cursor = test.expectPlainScalar("foo", cursor.skipLine(2), 2u)
    cursor = test.expectMappingValue(cursor.skipSpace(), 2u)
    cursor = test.expectTag("!!", "str", cursor.skipSpace(), 2u)
    cursor = test.expectFlowItemSeparator(cursor)
    cursor = test.expectTag("!!", "str", cursor.skipLine(2), 2u)
    cursor = test.expectMappingValue(cursor.skipSpace(), 2u)
    cursor = test.expectPlainScalar("bar", cursor.skipSpace(), 2u)
    cursor = test.expectFlowItemSeparator(cursor)
    cursor = test.expectFlowMappingEnd(cursor.skipLine())

    test.expectStreamEnd(cursor)
  }

  @Test
  fun example_7_3_completely_empty_flow_nodes() {
    //language=yaml
    val input = """
      {
        ? foo :,
        : bar,
      }
    """.trimIndent()

    val test = makeScanner(input)

    var cursor = test.expectStreamStart()

    cursor = test.expectFlowMappingStart(cursor)
    cursor = test.expectMappingKey(cursor.skipLine(2), 2u)
    cursor = test.expectPlainScalar("foo", cursor.skipSpace(), 4u)
    cursor = test.expectMappingValue(cursor.skipSpace(), 4u)
    cursor = test.expectFlowItemSeparator(cursor)
    cursor = test.expectMappingValue(cursor.skipLine(2), 2u)
    cursor = test.expectPlainScalar("bar", cursor.skipSpace(), 4u)
    cursor = test.expectFlowItemSeparator(cursor)
    cursor = test.expectFlowMappingEnd(cursor.skipLine())

    test.expectStreamEnd(cursor)
  }

  @Test
  fun example_7_4_double_quoted_implicit_keys() {
    //language=yaml
    val input = """
      "implicit block key" : [
        "implicit flow key" : value,
      ]
    """.trimIndent()

    val test = makeScanner(input)

    var cursor = test.expectStreamStart()

    cursor = test.expectDoubleQuotedScalar("implicit block key", cursor)
    cursor = test.expectMappingValue(cursor.skipSpace())
    cursor = test.expectFlowSequenceStart(cursor.skipSpace())
    cursor = test.expectDoubleQuotedScalar("implicit flow key", cursor.skipLine(2), 2u)
    cursor = test.expectMappingValue(cursor.skipSpace(), 2u)
    cursor = test.expectPlainScalar("value", cursor.skipSpace(), 2u)
    cursor = test.expectFlowItemSeparator(cursor)
    cursor = test.expectFlowSequenceEnd(cursor.skipLine())

    test.expectStreamEnd(cursor)
  }

  @Test
  fun example_7_5_double_quoted_line_breaks() {
    //language=yaml
    val input = "\"folded \n" +
      "to a space,\t\n" +
      " \n" +
      "to a line feed, or \t\\\n" +
      " \\ \tnon-content\""
    val test = makeScanner(input)

    var cursor = test.expectStreamStart()

    cursor = test.expectDoubleQuotedScalar("folded to a space,\nto a line feed, or \t \tnon-content", cursor, 0u, cursor.resolve(62, 4, 16))

    test.expectStreamEnd(cursor)
  }

  @Test
  fun example_7_6_double_quoted_lines() {
    //language=yaml
    val input = "\" 1st non-empty\n" +
      "\n 2nd non-empty \n" +
      "\t3rd non-empty \""
    val test = makeScanner(input)

    var pos = test.expectStreamStart()

    pos = test.expectDoubleQuotedScalar(" 1st non-empty\n2nd non-empty 3rd non-empty ", pos, 0u, pos.resolve(49, 3, 16))

    test.expectStreamEnd(pos)
  }

  @Test
  fun example_7_7_single_quoted_characters() {
    //language=yaml
    val input = "'here''s to \"quotes\"'"
    val test  = makeScanner(input)
    var pos   = test.expectStreamStart()

    pos = test.expectSingleQuotedScalar("here's to \"quotes\"", pos, 0u, pos.resolve(21, 0, 21))
    test.expectStreamEnd(pos)
  }

  @Test
  fun example_7_8_single_quoted_implicit_keys() {
    //language=yaml
    val input = "'implicit block key' : [\n" +
      "  'implicit flow key' : value,\n" +
      " ]"
    val test = makeScanner(input)

    var pos = test.expectStreamStart()

    pos = test.expectSingleQuotedScalar("implicit block key", pos)
    pos = test.expectMappingValue(pos.skipSpace())
    pos = test.expectFlowSequenceStart(pos.skipSpace())
    pos = test.expectSingleQuotedScalar("implicit flow key", pos.skipLine(2), 2u)
    pos = test.expectMappingValue(pos.skipSpace(), 2u)
    pos = test.expectPlainScalar("value", pos.skipSpace(), 2u)
    pos = test.expectFlowItemSeparator(pos)
    pos = test.expectFlowSequenceEnd(pos.skipLine(1))

    test.expectStreamEnd(pos)
  }

  @Test
  fun example_7_9_single_quoted_lines() {
    val input = "' 1st non-empty\n" +
      "\n" +
      " 2nd non-empty \n" +
      "\t3rd non-empty '"
    val test = makeScanner(input)

    var pos = test.expectStreamStart()

    pos = test.expectSingleQuotedScalar(" 1st non-empty\n2nd non-empty 3rd non-empty ", pos, 0u, pos.resolve(49, 3, 16))

    test.expectStreamEnd(pos)
  }

  @Test
  fun example_7_10_plain_characters() {
    //language=yaml
    val input = """
      | # Outside flow collection:
      | - ::vector
      | - ": - ()"
      | - Up, up, and away!
      | - -123
      | - https://example.com/foo#bar
      | # Inside flow collection:
      | - [ ::vector,
      |   ": - ()",
      |   "Up, up, and away!",
      |   -123,
      |   https://example.com/foo#bar ]
    """.trimMargin("| ")

    val test = makeScanner(input)

    var pos = test.expectStreamStart()

    pos = test.expectComment("Outside flow collection:", 0u, false, pos)

    pos = test.expectSequenceEntry(pos.skipLine())
    pos = test.expectPlainScalar("::vector", pos.skipSpace(), 2u)

    pos = test.expectSequenceEntry(pos.skipLine())
    pos = test.expectDoubleQuotedScalar(": - ()", pos.skipSpace(), 2u)

    pos = test.expectSequenceEntry(pos.skipLine())
    pos = test.expectPlainScalar("Up, up, and away!", pos.skipSpace(), 2u)

    pos = test.expectSequenceEntry(pos.skipLine())
    pos = test.expectPlainScalar("-123", pos.skipSpace(), 2u)

    pos = test.expectSequenceEntry(pos.skipLine())
    pos = test.expectPlainScalar("https://example.com/foo#bar", pos.skipSpace(), 2u)

    pos = test.expectComment("Inside flow collection:", 0u, false, pos.skipLine())

    pos = test.expectSequenceEntry(pos.skipLine())

    pos = test.expectFlowSequenceStart(pos.skipSpace(), 2u)

    pos = test.expectPlainScalar("::vector", pos.skipSpace(), 2u)
    pos = test.expectFlowItemSeparator(pos)

    pos = test.expectDoubleQuotedScalar(": - ()", pos.skipLine(2), 2u)
    pos = test.expectFlowItemSeparator(pos)

    pos = test.expectDoubleQuotedScalar("Up, up, and away!", pos.skipLine(2), 2u)
    pos = test.expectFlowItemSeparator(pos)

    pos = test.expectPlainScalar("-123", pos.skipLine(2), 2u)
    pos = test.expectFlowItemSeparator(pos)

    pos = test.expectPlainScalar("https://example.com/foo#bar", pos.skipLine(2), 2u)

    pos = test.expectFlowSequenceEnd(pos.skipSpace())

    test.expectStreamEnd(pos)
  }

  @Test
  fun example_7_11_plain_implicit_keys() {
    //language=yaml
    val input = """
      | implicit block key : [
      |   implicit flow key : value,
      | ]
    """.trimMargin("| ")

    val test = makeScanner(input)

    var pos = test.expectStreamStart()

    pos = test.expectPlainScalar("implicit block key", pos)
    pos = test.expectMappingValue(pos.skipSpace())
    pos = test.expectFlowSequenceStart(pos.skipSpace())
    pos = test.expectPlainScalar("implicit flow key", pos.skipLine(2), 2u)
    pos = test.expectMappingValue(pos.skipSpace(), 2u)
    pos = test.expectPlainScalar("value", pos.skipSpace(), 2u)
    pos = test.expectFlowItemSeparator(pos)
    pos = test.expectFlowSequenceEnd(pos.skipLine())

    test.expectStreamEnd(pos)
  }

  @Test
  fun example_7_12_plain_lines() {
    //language=yaml
    val input = """
      | 1st non-empty
      | 
      |  2nd non-empty 
      | 	3rd non-empty
    """.trimMargin("| ")

    val test = makeScanner(input)

    var pos = test.expectStreamStart()
    pos = test.expectPlainScalar("1st non-empty\n2nd non-empty 3rd non-empty", pos, 0u, pos.resolve(45, 3, 14))
    test.expectStreamEnd(pos)
  }

  @Test
  fun example_7_13_flow_sequence() {
    //language=yaml
    val input = """
      | - [ one, two, ]
      | - [three ,four]
    """.trimMargin("| ")

    val test = makeScanner(input)

    var pos = test.expectStreamStart()
    pos = test.expectSequenceEntry(pos)
    pos = test.expectFlowSequenceStart(pos.skipSpace(), 2u)
    pos = test.expectPlainScalar("one", pos.skipSpace(), 2u)
    pos = test.expectFlowItemSeparator(pos)
    pos = test.expectPlainScalar("two", pos.skipSpace(), 2u)
    pos = test.expectFlowItemSeparator(pos)
    pos = test.expectFlowSequenceEnd(pos.skipSpace())
    pos = test.expectSequenceEntry(pos.skipLine())
    pos = test.expectFlowSequenceStart(pos.skipSpace(), 2u)
    pos = test.expectPlainScalar("three", pos, 2u)
    pos = test.expectFlowItemSeparator(pos.skipSpace())
    pos = test.expectPlainScalar("four", pos, 2u)
    pos = test.expectFlowSequenceEnd(pos)
    test.expectStreamEnd(pos)
  }

  @Test
  fun example_7_14_flow_sequence_entries() {
    //language=yaml
    val input = """
      | [
      | "double
      |  quoted", 'single
      |            quoted',
      | plain
      |  text, [ nested ],
      | single: pair,
      | ]
    """.trimMargin("| ")

    val test = makeScanner(input)

    var pos = test.expectStreamStart()
    pos = test.expectFlowSequenceStart(pos)
    pos = pos.skipLine()
    pos = test.expectDoubleQuotedScalar("double quoted", pos, 0u, pos.resolve(16, 1, 8))
    pos = test.expectFlowItemSeparator(pos)
    pos = pos.skipSpace()
    pos = test.expectSingleQuotedScalar("single quoted", pos, 1u, pos.resolve(26, 1, 8))
    pos = test.expectFlowItemSeparator(pos)
    pos = pos.skipLine()
    pos = test.expectPlainScalar("plain text", pos, 0u, pos.resolve(11, 1, 5))
    pos = test.expectFlowItemSeparator(pos)
    pos = test.expectFlowSequenceStart(pos.skipSpace())
    pos = test.expectPlainScalar("nested", pos.skipSpace())
    pos = test.expectFlowSequenceEnd(pos.skipSpace())
    pos = test.expectFlowItemSeparator(pos)
    pos = test.expectPlainScalar("single", pos.skipLine())
    pos = test.expectMappingValue(pos)
    pos = test.expectPlainScalar("pair", pos.skipSpace())
    pos = test.expectFlowItemSeparator(pos)
    pos = test.expectFlowSequenceEnd(pos.skipLine())
    test.expectStreamEnd(pos)
  }

  @Test
  fun example_7_15_flow_mappings() {
    //language=yaml
    val input = """
      | - { one : two , three: four , }
      | - {five: six,seven : eight}
    """.trimMargin("| ")

    val test = makeScanner(input)

    var pos = test.expectStreamStart()
    pos = test.expectSequenceEntry(pos)
    pos = test.expectFlowMappingStart(pos.skipSpace(), 2u)
    pos = test.expectPlainScalar("one", pos.skipSpace(), 2u)
    pos = test.expectMappingValue(pos.skipSpace(), 2u)
    pos = test.expectPlainScalar("two", pos.skipSpace(), 2u)
    pos = test.expectFlowItemSeparator(pos.skipSpace())
    pos = test.expectPlainScalar("three", pos.skipSpace(), 2u)
    pos = test.expectMappingValue(pos, 2u)
    pos = test.expectPlainScalar("four", pos.skipSpace(), 2u)
    pos = test.expectFlowItemSeparator(pos.skipSpace())
    pos = test.expectFlowMappingEnd(pos.skipSpace())
    pos = test.expectSequenceEntry(pos.skipLine())
    pos = test.expectFlowMappingStart(pos.skipSpace(), 2u)
    pos = test.expectPlainScalar("five", pos, 2u)
    pos = test.expectMappingValue(pos, 2u)
    pos = test.expectPlainScalar("six", pos.skipSpace(), 2u)
    pos = test.expectFlowItemSeparator(pos)
    pos = test.expectPlainScalar("seven", pos, 2u)
    pos = test.expectMappingValue(pos.skipSpace(), 2u)
    pos = test.expectPlainScalar("eight", pos.skipSpace(), 2u)
    pos = test.expectFlowMappingEnd(pos)
    test.expectStreamEnd(pos)
  }

  @Test
  fun example_7_16_flow_mapping_entries() {
    //language=yaml
    val input = """
      | {
      | ? explicit: entry,
      | implicit: entry,
      | ?
      | }
    """.trimMargin("| ")

    val test = makeScanner(input)

    var pos = test.expectStreamStart()
    pos = test.expectFlowMappingStart(pos)
    pos = test.expectMappingKey(pos.skipLine())
    pos = test.expectPlainScalar("explicit", pos.skipSpace(), 2u)
    pos = test.expectMappingValue(pos, 2u)
    pos = test.expectPlainScalar("entry", pos.skipSpace(), 2u)
    pos = test.expectFlowItemSeparator(pos)
    pos = test.expectPlainScalar("implicit", pos.skipLine())
    pos = test.expectMappingValue(pos)
    pos = test.expectPlainScalar("entry", pos.skipSpace())
    pos = test.expectFlowItemSeparator(pos)
    pos = test.expectMappingKey(pos.skipLine())
    pos = test.expectFlowMappingEnd(pos.skipLine())
    test.expectStreamEnd(pos)
  }

  @Test
  fun example_7_17_flow_mapping_separate_values() {
    //language=yaml
    val input = """
      | {
      | unquoted : "separate",
      | https://foo.com,
      | omitted value:,
      | : omitted key,
      | }
    """.trimMargin("| ")

    val test = makeScanner(input)

    var pos = test.expectStreamStart()

    pos = test.expectFlowMappingStart(pos)
    pos = test.expectPlainScalar("unquoted", pos.skipLine())
    pos = test.expectMappingValue(pos.skipSpace())
    pos = test.expectDoubleQuotedScalar("separate", pos.skipSpace())
    pos = test.expectFlowItemSeparator(pos)
    pos = test.expectPlainScalar("https://foo.com", pos.skipLine())
    pos = test.expectFlowItemSeparator(pos)
    pos = test.expectPlainScalar("omitted value", pos.skipLine())
    pos = test.expectMappingValue(pos)
    pos = test.expectFlowItemSeparator(pos)
    pos = test.expectMappingValue(pos.skipLine())
    pos = test.expectPlainScalar("omitted key", pos.skipSpace(), 2u)
    pos = test.expectFlowItemSeparator(pos)
    pos = test.expectFlowMappingEnd(pos.skipLine())

    test.expectStreamEnd(pos)
  }

  @Test
  fun example_7_18_flow_mapping_adjacent_values() {
    //language=yaml
    val input = """
      | {
      | "adjacent":value,
      | "readable": value,
      | "empty":
      | }
    """.trimMargin("| ")

    val test = makeScanner(input)

    var pos = test.expectStreamStart()

    pos = test.expectFlowMappingStart(pos)
    pos = test.expectDoubleQuotedScalar("adjacent", pos.skipLine())
    pos = test.expectMappingValue(pos)
    pos = test.expectPlainScalar("value", pos)
    pos = test.expectFlowItemSeparator(pos)
    pos = test.expectDoubleQuotedScalar("readable", pos.skipLine())
    pos = test.expectMappingValue(pos)
    pos = test.expectPlainScalar("value", pos.skipSpace())
    pos = test.expectFlowItemSeparator(pos)
    pos = test.expectDoubleQuotedScalar("empty", pos.skipLine())
    pos = test.expectMappingValue(pos)
    pos = test.expectFlowMappingEnd(pos.skipLine())

    test.expectStreamEnd(pos)
  }

  @Test
  fun example_7_19_single_pair_flow_mappings() {
    //language=yaml
    val input = """
      | [
      | foo: bar
      | ]
    """.trimMargin("| ")

    val test = makeScanner(input)
    var pos = test.expectStreamStart()
    pos = test.expectFlowSequenceStart(pos)
    pos = test.expectPlainScalar("foo", pos.skipLine())
    pos = test.expectMappingValue(pos)
    pos = test.expectPlainScalar("bar", pos.skipSpace())
    pos = test.expectFlowSequenceEnd(pos.skipLine())
    test.expectStreamEnd(pos)
  }

  @Test
  fun example_7_20_single_pair_explicit_entry() {
    //language=yaml
    val input = """
      | [
      | ? foo
      |  bar : baz
      | ]
    """.trimMargin("| ")
    val test = makeScanner(input)
    var pos = test.expectStreamStart()
    pos = test.expectFlowSequenceStart(pos)
    pos = test.expectMappingKey(pos.skipLine())
    pos = pos.skipSpace()
    pos = test.expectPlainScalar("foo bar", pos, 2u, pos.resolve(8, 1, 2))
    pos = test.expectMappingValue(pos.skipSpace())
    pos = test.expectPlainScalar("baz", pos.skipSpace())
    pos = test.expectFlowSequenceEnd(pos.skipLine())
    test.expectStreamEnd(pos)
  }

  @Test
  fun example_7_21_single_pair_implicit_entries() {
    //language=yaml
    val input = """
      | - [ YAML : separate ]
      | - [ : empty key entry ]
      | - [ {JSON: like}:adjacent ]
    """.trimMargin("| ")

    val test = makeScanner(input)

    var pos = test.expectStreamStart()

    pos = test.expectSequenceEntry(pos)
    pos = test.expectFlowSequenceStart(pos.skipSpace(), 2u)
    pos = test.expectPlainScalar("YAML", pos.skipSpace(), 2u)
    pos = test.expectMappingValue(pos.skipSpace(), 2u)
    pos = test.expectPlainScalar("separate", pos.skipSpace(), 2u)
    pos = test.expectFlowSequenceEnd(pos.skipSpace())

    pos = test.expectSequenceEntry(pos.skipLine())
    pos = test.expectFlowSequenceStart(pos.skipSpace(), 2u)
    pos = test.expectMappingValue(pos.skipSpace(), 2u)
    pos = test.expectPlainScalar("empty key entry", pos.skipSpace(), 2u)
    pos = test.expectFlowSequenceEnd(pos.skipSpace())

    pos = test.expectSequenceEntry(pos.skipLine())
    pos = test.expectFlowSequenceStart(pos.skipSpace(), 2u)
    pos = test.expectFlowMappingStart(pos.skipSpace(), 2u)
    pos = test.expectPlainScalar("JSON", pos, 2u)
    pos = test.expectMappingValue(pos, 2u)
    pos = test.expectPlainScalar("like", pos.skipSpace(), 2u)
    pos = test.expectFlowMappingEnd(pos)
    pos = test.expectMappingValue(pos, 2u)
    pos = test.expectPlainScalar("adjacent", pos, 2u)
    pos = test.expectFlowSequenceEnd(pos.skipSpace())

    test.expectStreamEnd(pos)
  }

  @Test
  fun example_7_22_invalid_implicit_keys() {
    //language=yaml
    val input = """
      | [ foo
      |  bar: invalid,
      |  "foo_...>1K characters...aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa_bar": invalid ]
    """.trimMargin("| ")

    val test = makeScanner(input)

    var pos = test.expectStreamStart()

    pos = test.expectFlowSequenceStart(pos)
    pos = pos.skipSpace()
    pos = test.expectPlainScalar("foo bar", pos, 0u, pos.resolve(8, 1, 2))
    pos = test.expectMappingValue(pos)
    pos = test.expectPlainScalar("invalid", pos.skipSpace())
    pos = test.expectFlowItemSeparator(pos)
    pos = test.expectDoubleQuotedScalar(
      "foo_...>1K characters...aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
        "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
        "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
        "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
        "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
        "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
        "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
        "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
        "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
        "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
        "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa_bar",
      pos.skipLine(1),
      1u,
    )
    pos = test.expectMappingValue(pos, 1u)
    pos = test.expectPlainScalar("invalid", pos.skipSpace(), 1u)
    pos = test.expectFlowSequenceEnd(pos.skipSpace())

    test.expectStreamEnd(pos)
  }

  @Test
  fun example_8_10_folded_lines() {
    //language=yaml
    val input = """
      >

       folded
       line
      
       next
       line
         * bullet
      
         * list
         * lines

       last
       line

      # Comment
    """.trimIndent()

    val test = makeScanner(input)

    var cursor = test.expectStreamStart()

    // Official 1.2.2 spec has this example output as being
    // "\nfolded line\nnext line\n  * bullet\n \n  * list\n  * lines\n\nlast line\n"
    // The "\n \n" after "bullet" in this doesn't make any sense as the input
    // did not contain a space between those line breaks.
    cursor = test.expectFoldedScalar(
      "\nfolded line\nnext line\n  * bullet\n\n  * list\n  * lines\n\nlast line\n",
      0u,
      cursor,
      cursor.resolve(77, 14, 0)
    )

    cursor = test.expectComment("Comment", 0u, false, cursor.skipLine())

    test.expectStreamEnd(cursor)
  }

  @Test
  fun example_8_11_more_indented_lines() = example_8_10_folded_lines()

  @Test
  fun example_8_12_empty_separation_lines() = example_8_10_folded_lines()

  @Test
  fun examples_8_13_final_empty_lines() = example_8_10_folded_lines()

  @Test
  fun examples_8_14_block_sequence() {
    val input = """
      block sequence:
        - one
        - two : three
      
    """.trimIndent()

    val test = makeScanner(input)
    var cursor: SourcePosition

    cursor = test.expectStreamStart()
    cursor = test.expectPlainScalar("block sequence", cursor)
    cursor = test.expectMappingValue(cursor)
    cursor = test.expectSequenceEntry(cursor.skipLine(2), 2u)
    cursor = test.expectPlainScalar("one", cursor.skipSpace(), 4u)
    cursor = test.expectSequenceEntry(cursor.skipLine(2), 2u)
    cursor = test.expectPlainScalar("two", cursor.skipSpace(), 4u)
    cursor = test.expectMappingValue(cursor.skipSpace(), 4u)
    cursor = test.expectPlainScalar("three", cursor.skipSpace(), 4u)
    test.expectStreamEnd(cursor.skipLine())
  }

  @Test
  fun examples_8_15_block_sequence_entry_type() {
    val input = """
      - # Empty
      - |
       block node
      - - one # Compact
        - two # sequence
      - one: two # Compact mapping
    """.trimIndent()

    val test = makeScanner(input)
    var cursor: SourcePosition

    cursor = test.expectStreamStart()
    cursor = test.expectSequenceEntry(cursor)
    cursor = test.expectComment("Empty", 2u, true, cursor.skipSpace())
    cursor = test.expectSequenceEntry(cursor.skipLine())
    cursor = test.expectLiteralScalar("block node\n", 2u, cursor.skipSpace(), SourcePosition(26u, 3u, 0u))
    cursor = test.expectSequenceEntry(cursor)
    cursor = test.expectSequenceEntry(cursor.skipSpace(), 2u)
    cursor = test.expectPlainScalar("one", cursor.skipSpace(), 4u)
    cursor = test.expectComment("Compact", 4u, true, cursor.skipSpace())
    cursor = test.expectSequenceEntry(cursor.skipLine(2), 2u)
    cursor = test.expectPlainScalar("two", cursor.skipSpace(), 4u)
    cursor = test.expectComment("sequence", 4u, true, cursor.skipSpace())
    cursor = test.expectSequenceEntry(cursor.skipLine())
    cursor = test.expectPlainScalar("one", cursor.skipSpace(), 2u)
    cursor = test.expectMappingValue(cursor, 2u)
    cursor = test.expectPlainScalar("two", cursor.skipSpace(), 2u)
    cursor = test.expectComment("Compact mapping", 2u, true, cursor.skipSpace())

    test.expectStreamEnd(cursor)
  }

  @Test
  fun example_10_9_core_tag_resolution() {
    //language=yaml
    val input = """
      A null: null
      Also a null: # Empty
      Not a null: ""
      Booleans: [ true, True, false, FALSE ]
      Integers: [ 0, 0o7, 0x3A, -19 ]
      Floats: [
        0., -0.0, .5, +12e03, -2E+05 ]
      Also floats: [
        .inf, -.Inf, +.INF, .NAN ]
    """.trimIndent()

    val scanner = makeScanner(input)
    var cursor: SourcePosition

    // [STREAM-START]
    cursor = scanner.expectStreamStart()

    // A·null:·null↓
    cursor = scanner.expectPlainScalar("A null", cursor, 0u)
    cursor = scanner.expectMappingValue(cursor, 0u)
      .skipSpace()
    cursor = scanner.expectPlainScalar("null", cursor, 0u)
      .skipLine()

    // Also·a·null:·#·Empty↓
    cursor = scanner.expectPlainScalar("Also a null", cursor, 0u)
    cursor = scanner.expectMappingValue(cursor, 0u)
      .skipSpace()
    cursor = scanner.expectComment("Empty", 0u, true, cursor)
      .skipLine()

    // Not·a·null:·""↓
    cursor = scanner.expectPlainScalar("Not a null", cursor, 0u)
    cursor = scanner.expectMappingValue(cursor, 0u)
      .skipSpace()
    cursor = scanner.expectDoubleQuotedScalar("", cursor, 0u)
      .skipLine()

    // Booleans:·[·true,·True,·false,·FALSE·]↓
    cursor = scanner.expectPlainScalar("Booleans", cursor, 0u)
    cursor = scanner.expectMappingValue(cursor, 0u)
      .skipSpace()
    cursor = scanner.expectFlowSequenceStart(cursor, 0u)
      .skipSpace()
    cursor = scanner.expectPlainScalar("true", cursor, 0u)
    cursor = scanner.expectFlowItemSeparator(cursor)
      .skipSpace()
    cursor = scanner.expectPlainScalar("True", cursor, 0u)
    cursor = scanner.expectFlowItemSeparator(cursor)
      .skipSpace()
    cursor = scanner.expectPlainScalar("false", cursor, 0u)
    cursor = scanner.expectFlowItemSeparator(cursor)
      .skipSpace()
    cursor = scanner.expectPlainScalar("FALSE", cursor, 0u)
      .skipSpace()
    cursor = scanner.expectFlowSequenceEnd(cursor)
      .skipLine()

    // Integers:·[·0,·0o7,·0x3A,·-19·]↓
    cursor = scanner.expectPlainScalar("Integers", cursor, 0u)
    cursor = scanner.expectMappingValue(cursor, 0u)
      .skipSpace()
    cursor = scanner.expectFlowSequenceStart(cursor, 0u)
      .skipSpace()
    cursor = scanner.expectPlainScalar("0", cursor, 0u)
    cursor = scanner.expectFlowItemSeparator(cursor)
      .skipSpace()
    cursor = scanner.expectPlainScalar("0o7", cursor, 0u)
    cursor = scanner.expectFlowItemSeparator(cursor)
      .skipSpace()
    cursor = scanner.expectPlainScalar("0x3A", cursor, 0u)
    cursor = scanner.expectFlowItemSeparator(cursor)
      .skipSpace()
    cursor = scanner.expectPlainScalar("-19", cursor, 0u)
      .skipSpace()
    cursor = scanner.expectFlowSequenceEnd(cursor)
      .skipLine()

    // Floats:·[
    // ··0.,·-0.0,·.5,·+12e03,·-2E+05·]↓
    cursor = scanner.expectPlainScalar("Floats", cursor, 0u)
    cursor = scanner.expectMappingValue(cursor, 0u)
      .skipSpace()
    cursor = scanner.expectFlowSequenceStart(cursor, 0u)
      .skipLine(2)
    cursor = scanner.expectPlainScalar("0.", cursor, 2u)
    cursor = scanner.expectFlowItemSeparator(cursor)
      .skipSpace()
    cursor = scanner.expectPlainScalar("-0.0", cursor, 2u)
    cursor = scanner.expectFlowItemSeparator(cursor)
      .skipSpace()
    cursor = scanner.expectPlainScalar(".5", cursor, 2u)
    cursor = scanner.expectFlowItemSeparator(cursor)
      .skipSpace()
    cursor = scanner.expectPlainScalar("+12e03", cursor, 2u)
    cursor = scanner.expectFlowItemSeparator(cursor)
      .skipSpace()
    cursor = scanner.expectPlainScalar("-2E+05", cursor, 2u)
      .skipSpace()
    cursor = scanner.expectFlowSequenceEnd(cursor)
      .skipLine()

    // Also·floats:·[
    // ··.inf,·-.Inf,·+.INF,·.NAN·]
    cursor = scanner.expectPlainScalar("Also floats", cursor, 0u)
    cursor = scanner.expectMappingValue(cursor, 0u)
      .skipSpace()
    cursor = scanner.expectFlowSequenceStart(cursor, 0u)
      .skipLine(2)
    cursor = scanner.expectPlainScalar(".inf", cursor, 2u)
    cursor = scanner.expectFlowItemSeparator(cursor)
      .skipSpace()
    cursor = scanner.expectPlainScalar("-.Inf", cursor, 2u)
    cursor = scanner.expectFlowItemSeparator(cursor)
      .skipSpace()
    cursor = scanner.expectPlainScalar("+.INF", cursor, 2u)
    cursor = scanner.expectFlowItemSeparator(cursor)
      .skipSpace()
    cursor = scanner.expectPlainScalar(".NAN", cursor, 2u)
      .skipSpace()
    cursor = scanner.expectFlowSequenceEnd(cursor)

    // [STREAM-END]
    scanner.expectStreamEnd(cursor)
  }
}