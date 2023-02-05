package io.foxcapades.lib.k.yaml.scan.events.event

import io.foxcapades.lib.k.yaml.YAMLDirectiveTag
import io.foxcapades.lib.k.yaml.YAMLDirectiveYAML
import io.foxcapades.lib.k.yaml.util.ImmutableArray

data class DocumentStartEventData(
  val yamlDirective: YAMLDirectiveYAML?,
  val tagDirectives: ImmutableArray<YAMLDirectiveTag>,
  val implicit: Boolean,
) : YAMLEventData