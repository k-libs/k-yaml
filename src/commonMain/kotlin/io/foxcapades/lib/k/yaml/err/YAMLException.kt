package io.foxcapades.lib.k.yaml.err

import io.klibs.yaml.util.SourcePosition

open class YAMLException(message: String) : Throwable(message)

open class YAMLReaderException(val index: ULong, message: String) : YAMLException(message)

open class YAMLScannerException(message: String, val mark: SourcePosition) : YAMLException(message)

open class YAMLVersionException(message: String) : YAMLException(message)

open class UIntOverflowException(val startMark: SourcePosition) : Throwable()