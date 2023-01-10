package io.foxcapades.lib.k.yaml.scan

class TokenQueue {
  private val raw = arrayOfNulls<YAMLToken>(4)
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

    val out = raw[head]!!

    raw[head] = null

    head = posMod(head + 1)
    size--

    return out
  }

  fun peek(): YAMLToken {
    if (isEmpty)
      throw IllegalStateException("attempted to peek a token in an empty TokenQueue")

    return raw[head]!!
  }

  fun push(token: YAMLToken) {
    if (size == raw.size)
      throw IllegalStateException("attempted to push a token into a full TokenQueue")
    raw[idx(size-1)] = token
  }

  fun clear() {
    for (i in 0 until size)
      raw[idx(i)] = null

    head = 0
    size = 0
  }

  @Suppress("NOTHING_TO_INLINE")
  private inline fun posMod(i: Int) = if (i >= raw.size) 0 else i

  @Suppress("NOTHING_TO_INLINE")
  private inline fun idx(i: Int) = if (head + i >= raw.size) head + i - raw.size else head + i
}