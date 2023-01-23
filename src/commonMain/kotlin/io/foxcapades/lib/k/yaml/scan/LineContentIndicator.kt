package io.foxcapades.lib.k.yaml.scan

internal typealias LineContentIndicator = Byte

internal const val LineContentIndicatorBlanksOnly:          LineContentIndicator = 0
internal const val LineContentIndicatorBlanksAndIndicators: LineContentIndicator = 1
internal const val LineContentIndicatorContent:             LineContentIndicator = 2