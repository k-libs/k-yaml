package io.foxcapades.lib.k.yaml.scan.events.event

enum class YAMLEventType {
  StreamStart,
  StreamEnd,
  DocumentStart,
  DocumentEnd,
  Alias,
  Scalar,
  MappingStart,
  MappingEnd,
  SequenceStart,
  SequenceEnd
}