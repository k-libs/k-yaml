package io.foxcapades.lib.k.yaml.scan

enum class YAMLScalarStyle {
  Plain,
  Literal,
  Folded,
  DoubleQuoted,
  SingleQuoted,
  ;

  inline val isBlock
    get() = this == Literal || this == Folded

  inline val isFlow
    get() = this == DoubleQuoted || this == SingleQuoted
}