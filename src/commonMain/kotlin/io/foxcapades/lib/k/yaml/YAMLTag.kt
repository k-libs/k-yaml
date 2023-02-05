package io.foxcapades.lib.k.yaml

data class YAMLTag(
  val handle: String,
  val suffix: String,
) {
  override fun toString() = handle + suffix
}
