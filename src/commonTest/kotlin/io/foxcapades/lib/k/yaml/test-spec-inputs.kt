package io.foxcapades.lib.k.yaml

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
  "keep: |+\n"