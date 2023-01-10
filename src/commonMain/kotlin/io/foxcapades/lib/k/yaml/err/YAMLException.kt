package io.foxcapades.lib.k.yaml.err

open class YAMLException(message: String) : Throwable(message)

open class YAMLReaderException(val index: ULong, message: String) : YAMLException(message)