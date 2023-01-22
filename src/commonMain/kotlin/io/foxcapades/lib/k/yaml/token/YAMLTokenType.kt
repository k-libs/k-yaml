package io.foxcapades.lib.k.yaml.token

typealias YAMLTokenType = Byte

// Stream tokens.
const val YAMLTokenTypeStreamStart: YAMLTokenType = 0
const val YAMLTokenTypeStreamEnd:   YAMLTokenType = 1

// Directive Tokens
const val YAMLTokenTypeDirectiveYAML: YAMLTokenType = 10
const val YAMLTokenTypeDirectiveTag:  YAMLTokenType = 11

// Document Tokens
const val YAMLTokenTypeDocumentStart: YAMLTokenType = 20
const val YAMLTokenTypeDocumentEnd:   YAMLTokenType = 21

const val YAMLTokenTypeBlockSequenceStart: YAMLTokenType = 30
const val YAMLTokenTypeBlockSequenceEnd:   YAMLTokenType = 31
const val YAMLTokenTypeBlockMappingStart:  YAMLTokenType = 32
const val YAMLTokenTypeBlockMappingEnd:    YAMLTokenType = 33

const val YAMLTokenTypeFlowSequenceStart: YAMLTokenType = 40
const val YAMLTokenTypeFlowSequenceEnd:   YAMLTokenType = 41
const val YAMLTokenTypeFlowMappingStart:  YAMLTokenType = 42
const val YAMLTokenTypeFlowMappingEnd:    YAMLTokenType = 43

const val YAMLTokenTypeFlowEntrySeparator: YAMLTokenType = 50

const val YAMLTokenTypeBlockEntryIndicator: YAMLTokenType = 60

const val YAMLTokenTypeMappingKey:   YAMLTokenType = 70
const val YAMLTokenTypeMappingValue: YAMLTokenType = 71

const val YAMLTokenTypeAnchor: YAMLTokenType = 80
const val YAMLTokenTypeAlias:  YAMLTokenType = 81

const val YAMLTokenTypeTag: YAMLTokenType = 90

const val YAMLTokenTypeComment: YAMLTokenType = 100

const val YAMLTokenTypeScalarPlain:        YAMLTokenType = 110
const val YAMLTokenTypeScalarFolded:       YAMLTokenType = 111
const val YAMLTokenTypeScalarLiteral:      YAMLTokenType = 112
const val YAMLTokenTypeScalarDoubleQuoted: YAMLTokenType = 113
const val YAMLTokenTypeScalarSingleQuoted: YAMLTokenType = 114

const val YAMLTokenTypeInvalid: YAMLTokenType = 127

@Suppress("NOTHING_TO_INLINE")
inline fun YAMLTokenType.isScalar() = this >= YAMLTokenTypeScalarPlain && this <= YAMLTokenTypeScalarSingleQuoted