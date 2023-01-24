package io.foxcapades.lib.k.yaml.scan

internal typealias LineContentIndicator = Byte

internal const val LineContentIndicatorBlanksOnly:          LineContentIndicator = 0
internal const val LineContentIndicatorBlanksAndIndicators: LineContentIndicator = 1
internal const val LineContentIndicatorContent:             LineContentIndicator = 2

internal inline val LineContentIndicator.isTrailingIfAfter
  get() = this == LineContentIndicatorContent || this == LineContentIndicatorBlanksAndIndicators

internal inline val LineContentIndicator.haveAnyContent
  get() = this == LineContentIndicatorContent || this == LineContentIndicatorBlanksAndIndicators

internal inline val LineContentIndicator.haveHardContent
  get() = this == LineContentIndicatorContent