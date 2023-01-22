package io.foxcapades.lib.k.yaml.token

import io.foxcapades.lib.k.yaml.scan.BlockScalarChompMode

@OptIn(ExperimentalUnsignedTypes::class)
data class YAMLTokenDataBlockScalar(
  override val value:      UByteArray,
           val style:      BlockScalarStyle,
           val indent:     UInt,
           val chomping:   BlockScalarChompMode,
           val indentHint: UInt,
) : YAMLTokenDataScalar