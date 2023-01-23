package io.foxcapades.lib.k.yaml.util

@Suppress("NOTHING_TO_INLINE")
internal inline fun <T> Array<T>.toFlowSequence() = this.joinToString(", ", "[", "]")