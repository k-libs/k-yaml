package io.foxcapades.lib.k.yaml.scan

typealias BlockScalarChompMode = Byte

const val BlockScalarChompModeClip:  BlockScalarChompMode = 0
const val BlockScalarChompModeStrip: BlockScalarChompMode = 1
const val BlockScalarChompModeKeep:  BlockScalarChompMode = 2
