package io.foxcapades.lib.k.yaml.scan

class TokenQueue {
  private val raw = arrayOfNulls<YAMLToken>(16)
  private var head = 0

  var size = 0
    private set

  inline val isEmpty
    get() = size == 0

  inline val isNotEmpty
    get() = size > 0

  fun pop(): YAMLToken {
    if (isEmpty)
      throw IllegalStateException("attempted to pop a token from an empty TokenQueue")
  }

  fun peek(): YAMLToken {
    if (isEmpty)
      throw IllegalStateException("attempted to peek a token in an empty TokenQueue")

    return raw[head]!!
  }

  fun push(token: YAMLToken)

  fun clear()

  @Suppress("NOTHING_TO_INLINE")
  private inline fun posMod(i: Int) = if (i >= raw.size) 0 else i
}