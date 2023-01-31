package io.foxcapades.lib.k.yaml.scan.events

import io.foxcapades.lib.k.yaml.YAMLStreamTokenizer
import io.foxcapades.lib.k.yaml.YAMLTokenScanner
import io.foxcapades.lib.k.yaml.token.*
import io.foxcapades.lib.k.yaml.util.Queue
import io.foxcapades.lib.k.yaml.util.TokenStack

class YAMLTokenScannerImpl : YAMLTokenScanner {
  private var streamEndEmitted = false

  private var tokens = Queue<io.foxcapades.lib.k.yaml.scan.tokens.token.YAMLToken>(4)

  private lateinit var lastToken: io.foxcapades.lib.k.yaml.scan.tokens.token.YAMLToken

  private var context = TokenStack(8)

  private val inDocument
    get() = context.isNotEmpty

  private val scanner: YAMLStreamTokenizer

  override val hasNextToken: Boolean
    get() = !streamEndEmitted

  override fun nextToken(): io.foxcapades.lib.k.yaml.scan.tokens.token.YAMLToken {
    if (tokens.isEmpty)
      fetchNextToken()

    lastToken = tokens.pop()

    if (lastToken is YAMLTokenStreamEnd)
      streamEndEmitted = true

    return lastToken
  }

  private fun inFlow(): YAMLTokenFlow? {
    for (i in 0 .. context.lastIndex)
      if (context[i] is YAMLTokenFlow)
        return context[i] as YAMLTokenFlow

    return null
  }

  private fun queueNextToken() {
    tokens.push(scanner.nextToken())
  }

  private fun fetchNextToken() {
    queueNextToken()

    when (val token = inFlow()) {
      null -> fetchNextTokenInBlock()
      is YAMLTokenFlowMappingStart  -> TODO()
      is YAMLTokenFlowSequenceStart -> TODO()
      else -> throw IllegalStateException("invalid flow context: $token")
    }
  }

  private fun fetchNextTokenInBlock() {
    when (tokens[0]) {
      is YAMLTokenScalar -> TODO()

      is YAMLTokenFlow -> TODO()

      is YAMLTokenSequenceEntry -> TODO()

      is YAMLTokenMappingValue -> TODO()

      is YAMLTokenComment -> TODO()

      is YAMLTokenNodeProperty -> TODO()

      is YAMLTokenInvalid -> TODO()

      is YAMLTokenMappingKey -> TODO()

      is io.foxcapades.lib.k.yaml.scan.tokens.token.YAMLTokenAlias -> TODO()

      is YAMLTokenDirective -> TODO()

      is YAMLTokenDocumentEnd -> TODO()
      is YAMLTokenDocumentStart -> TODO()

      is YAMLTokenStreamEnd -> TODO()
      is YAMLTokenStreamStart -> TODO()
    }
  }

  private fun fetchNextTokenInFlowMapping() {}

  private fun fetchNextTokenInFlowSequence() {}

}