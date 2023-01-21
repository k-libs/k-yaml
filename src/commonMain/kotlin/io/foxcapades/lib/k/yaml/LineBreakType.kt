package io.foxcapades.lib.k.yaml

@Deprecated("This is actually illegal as per the YAML 1.2.2 spec section 5.4, fragment [29]")
enum class LineBreakType {
  CRLF,
  CR,
  LF,
  SameAsInput
}
