package io.foxcapades.lib.k.yaml.scan.tokens

import io.foxcapades.lib.k.yaml.bytes.A_DASH
import io.foxcapades.lib.k.yaml.bytes.A_PLUS

typealias BlockScalarChompMode = UByte

const val BlockScalarChompModeClip: BlockScalarChompMode = 0u
const val BlockScalarChompModeStrip: BlockScalarChompMode = A_DASH
const val BlockScalarChompModeKeep: BlockScalarChompMode = A_PLUS
