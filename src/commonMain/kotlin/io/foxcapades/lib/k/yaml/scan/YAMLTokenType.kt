package io.foxcapades.lib.k.yaml.scan

enum class YAMLTokenType {
  StreamStart,
  StreamEnd,

  VersionDirective,
  TagDirective,
  DocumentStart,
  DocumentEnd,

  BlockSequenceStart,
  BlockMappingStart,
  BlockEnd,

  FlowSequenceStart,
  FlowSequenceEnd,

  FlowMappingStart,
  FlowMappingEnd,

  BlockEntry,
  FlowEntry,
  MappingKey,
  MappingValue,

  Anchor,
  Alias,
  Tag,
  Scalar,
  Comment,
}