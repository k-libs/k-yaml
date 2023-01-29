package io.foxcapades.lib.k.yaml.scan.tokens

import io.foxcapades.lib.k.yaml.YAMLStreamTokenizer
import io.foxcapades.lib.k.yaml.YAMLTokenScanner
import io.foxcapades.lib.k.yaml.token.*
import io.foxcapades.lib.k.yaml.util.Queue

class YAMLTokenScannerImpl : YAMLTokenScanner {
  private var streamEndEmitted = false

  private var tokens = Queue<YAMLToken>(4)

  private var inDocument = false

  private var flowLevel = 0

  private val scanner: YAMLStreamTokenizer

  override val hasNextToken: Boolean
    get() = !streamEndEmitted

  override fun nextToken(): YAMLToken {
    if (tokens.isEmpty)
      fetchNextToken()

    val out = tokens.pop()

    if (out is YAMLTokenStreamEnd)
      streamEndEmitted = true

    return out
  }

  fun fetchNextToken() {
    val next = scanner.nextToken()

    when (next) {
      is YAMLTokenStreamStart -> TODO()
      is YAMLTokenStreamEnd   -> TODO()

      is YAMLTokenDirectiveTag -> TODO()
      is YAMLTokenDirectiveYAML -> TODO()

      is YAMLTokenDocumentStart -> TODO()
      is YAMLTokenDocumentEnd -> TODO()

      is YAMLTokenInvalid -> TODO()

      is YAMLTokenAlias -> TODO()
      is YAMLTokenAnchor -> TODO()
      is YAMLTokenComment -> TODO()
      is YAMLTokenFlowItemSeparator -> TODO()
      is YAMLTokenFlowMappingEnd -> TODO()
      is YAMLTokenFlowMappingStart -> TODO()
      is YAMLTokenFlowSequenceEnd -> TODO()
      is YAMLTokenFlowSequenceStart -> TODO()
      is YAMLTokenMappingKey -> TODO()
      is YAMLTokenMappingValue -> TODO()
      is YAMLTokenScalarFolded -> TODO()
      is YAMLTokenScalarLiteral -> TODO()
      is YAMLTokenScalarPlain -> TODO()
      is YAMLTokenScalarQuotedDouble -> TODO()
      is YAMLTokenScalarQuotedSingle -> TODO()
      is YAMLTokenSequenceEntry -> TODO()
      is YAMLTokenTag -> TODO()
    }
  }
}