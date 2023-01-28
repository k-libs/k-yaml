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