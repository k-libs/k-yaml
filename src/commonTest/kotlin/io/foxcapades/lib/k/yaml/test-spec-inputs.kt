package io.foxcapades.lib.k.yaml

//language=yaml
const val INPUT_EXAMPLE_2_1 =
  "- Mark McGwire\n" +
  "- Sammy Sosa\n" +
  "- Ken Griffey"

//language=yaml
const val INPUT_EXAMPLE_2_2 =
  "hr:  65    # Home runs\n" +
  "avg: 0.278 # Batting average\n" +
  "rbi: 147   # Runs Batted In"

//language=yaml
val INPUT_EXAMPLE_8_1 = """
- | # Empty header
 literal
- >1 # Indentation indicator
  folded
- |+ # Chomping indicator
 keep

- >1- # Both indicators
  strip
""".trimIndent()

//language=yaml
val INPUT_EXAMPLE_8_2 = """
- |
 detected
- >
 
  
  # detected
- |1
  explicit
- >
 	
 detected
""".trimIndent()

//language=yaml
val INPUT_EXAMPLE_8_3 = """
- |
  
 text
- >
  text
 text
- |2
 text
""".trimIndent()

//language=yaml
const val INPUT_EXAMPLE_8_4 =
  "strip: |-\n" +
  "  text\n" +
  "clip: |\n" +
  "  text\n" +
  "keep: |+\n" +
  "  text\n"

//language=yaml
const val INPUT_EXAMPLE_8_5 =
  "# Strip\n" +
  "  # Comments:\n" +
  "strip: |-\n" +
  "  # text\n  " +
  "\n " +
  "# Clip\n  " +
  "# comments:\n" +
  "\n" +
  "clip: |\n" +
  "  # text\n " +
  "\n " +
  "# Keep\n  " +
  "# comments:\n" +
  "\n" +
  "keep: |+\n" +
  "  # text\n" +
  "\n " +
  "# Trail\n  " +
  "# comments."

//language=yaml
const val INPUT_EXAMPLE_8_6 =
  "strip: >-\n" +
  "\n" +
  "clip: >\n" +
  "\n" +
  "keep: |+\n" +
  "\n"

//language=yaml
const val INPUT_EXAMPLE_8_7 =
  "|\n" +
  " literal\n" +
  " \ttext\n" +
  "\n"

//language=yaml
const val INPUT_EXAMPLE_8_8 =
  "|\n" +
  " \n" +
  "  \n" +
  "  literal\n" +
  "   \n" +
  "  \n" +
  "  text\n" +
  "\n" +
  " # Comment"

//language=yaml
const val INPUT_EXAMPLE_8_9 =
  ">\n" +
  " folded\n" +
  " text\n" +
  "\n"

//language=yaml
const val INPUT_EXAMPLE_8_10 =
  ">\n" +
  "\n" +
  " folded\n" +
  " line\n" +
  "\n" +
  " next\n" +
  " line\n" +
  "   * bullet\n" +
  "\n" +
  "   * list\n" +
  "   * lines\n" +
  "\n" +
  " last\n" +
  " line\n" +
  "\n" +
  "# Comment"

//language=yaml
const val INPUT_EXAMPLE_8_14 =
  "block sequence:\n" +
  "  - one\n" +
  "  - two : three\n"

//language=yaml
const val INPUT_EXAMPLE_8_15 =
  "- # Empty\n" +
  "- |\n" +
  " block node\n" +
  "- - one # Compact\n" +
  "  - two # sequence\n" +
  "- one: two # Compact mapping"

//language=yaml
const val INPUT_EXAMPLE_8_16 =
  "block mapping:\n" +
  " key: value\n"

//language=yaml
const val INPUT_EXAMPLE_8_17 =
  "? explicit key # Empty value\n" +
  "? |\n" +
  "  block key\n" +
  ": - one # Explicit compact\n" +
  "  - two # block value\n"