package io.foxcapades.lib.k.yaml.scan.events

internal data class EventStreamContext(
  val type:   EventStreamContextType,
  val indent: UInt,
)

internal typealias EventStreamContextType = Byte

internal const val EventStreamContextTypeDocument: EventStreamContextType = 0
internal const val EventStreamContextTypeBlockMapping: EventStreamContextType = 1
internal const val EventStreamContextTypeBlockSequence: EventStreamContextType = 2
internal const val EventStreamContextTypeFlowMapping: EventStreamContextType = 3
internal const val EventStreamContextTypeFlowSequence: EventStreamContextType = 4
