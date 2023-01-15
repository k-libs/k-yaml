package io.foxcapades.lib.k.yaml

internal val DefaultYAMLVersion = YAMLVersion.VERSION_1_2

enum class YAMLVersion {
  VERSION_1_1,
  VERSION_1_2,
  ;

  inline val majorVersion
    get() = 1u

  inline val minorVersion
    get() = when(this) {
      VERSION_1_1 -> 1u
      VERSION_1_2 -> 2u
    }

  override fun toString() =
    when (this) {
      VERSION_1_1 -> "1.1"
      VERSION_1_2 -> "1.2"
    }
}