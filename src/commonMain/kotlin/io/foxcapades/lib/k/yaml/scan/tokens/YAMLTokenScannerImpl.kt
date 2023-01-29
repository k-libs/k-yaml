package io.foxcapades.lib.k.yaml.scan.tokens

import io.foxcapades.lib.k.yaml.YAMLStreamTokenizer
import io.foxcapades.lib.k.yaml.YAMLTokenScanner
import io.foxcapades.lib.k.yaml.scan.stream.FlowTypeMapping
import io.foxcapades.lib.k.yaml.scan.stream.FlowTypeSequence
import io.foxcapades.lib.k.yaml.token.*
import io.foxcapades.lib.k.yaml.util.ByteStack
import io.foxcapades.lib.k.yaml.util.Queue

class YAMLTokenScannerImpl : YAMLTokenScanner {
  private var streamEndEmitted = false

  private var tokens = Queue<YAMLToken>(4)

  private var inDocument = false

  private var flows = ByteStack()
  private inline val flowLevel
    get() = flows.size
  private inline val inFlow
    get() = flows.isNotEmpty
  private inline val inFlowMapping
    get() = inFlow && flows.peek() == FlowTypeMapping
  private inline val inFlowSequence
    get() = inFlow && flows.peek() == FlowTypeSequence

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
      is YAMLTokenScalar -> TODO()

      is YAMLTokenMappingValue -> TODO()
      is YAMLTokenMappingKey -> TODO()

      is YAMLTokenSequenceEntry -> TODO()

      is YAMLTokenFlowItemSeparator -> TODO()

      is YAMLTokenFlowMappingEnd -> TODO()
      is YAMLTokenFlowMappingStart -> TODO()

      is YAMLTokenFlowSequenceEnd -> TODO()
      is YAMLTokenFlowSequenceStart -> TODO()

      is YAMLTokenInvalid -> TODO()

      is YAMLTokenNodeProperty -> TODO()
      is YAMLTokenAlias -> TODO()
      is YAMLTokenComment -> TODO()

      is YAMLTokenDirectiveTag -> TODO()
      is YAMLTokenDirectiveYAML -> TODO()

      is YAMLTokenDocumentStart -> TODO()
      is YAMLTokenDocumentEnd -> TODO()

      is YAMLTokenStreamStart -> TODO()
      is YAMLTokenStreamEnd   -> TODO()
    }
  }

}