package io.foxcapades.lib.k.yaml.scan.events.event

import kotlin.jvm.JvmInline

@JvmInline
value class AliasEventData(val anchor: String) : YAMLEventData