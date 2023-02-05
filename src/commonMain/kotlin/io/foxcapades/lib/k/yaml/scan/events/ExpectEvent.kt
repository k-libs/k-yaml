package io.foxcapades.lib.k.yaml.scan.events

internal enum class ExpectEvent {
  StreamStart,
  StreamEnd,

  ImplicitDocumentStart,
  ExplicitDocumentStart,

  DocumentContent,
  DocumentEnd,

  Scalar,

  BlockMappingValue,

  FlowMappingKey,

  FlowSequenceEntry,
}