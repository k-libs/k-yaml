package io.foxcapades.lib.k.yaml.scan.events.event

import io.foxcapades.lib.k.yaml.YAMLEncoding
import kotlin.jvm.JvmInline

@JvmInline
value class StreamStartEventData(val encoding: YAMLEncoding) : YAMLEventData