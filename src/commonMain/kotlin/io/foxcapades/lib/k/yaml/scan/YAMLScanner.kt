package io.foxcapades.lib.k.yaml.scan

class YAMLScanner {
  private var streamStarted = false
  private var streamEnded = false

  fun hasNextToken() = !streamEnded

  fun nextToken(): YAMLToken
}