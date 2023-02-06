package io.klibs.yaml.token

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

  BlockSequenceEntry,
  FlowItemSeparator,

  MappingKey,
  MappingValue,

  Alias,
  Anchor,
  Tag,
  Scalar,
}