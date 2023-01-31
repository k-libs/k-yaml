package io.foxcapades.lib.k.yaml.scan.events.event

sealed interface YAMLEvent

class YAMLEventMappingStart : YAMLEvent
class YAMLEventMappingEnd : YAMLEvent

class YAMLEventSequenceStart : YAMLEvent
class YAMLEventSequenceEnd : YAMLEvent




