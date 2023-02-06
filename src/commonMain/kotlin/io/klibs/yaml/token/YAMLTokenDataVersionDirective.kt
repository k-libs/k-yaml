package io.klibs.yaml.token

interface YAMLTokenDataVersionDirective : YAMLTokenData {
  val major: Int
  val minor: Int
}