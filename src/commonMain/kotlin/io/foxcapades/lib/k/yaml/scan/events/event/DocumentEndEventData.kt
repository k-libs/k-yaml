package io.foxcapades.lib.k.yaml.scan.events.event

import kotlin.jvm.JvmInline

@JvmInline
value class DocumentEndEventData(val implicit: Boolean) : YAMLEventData