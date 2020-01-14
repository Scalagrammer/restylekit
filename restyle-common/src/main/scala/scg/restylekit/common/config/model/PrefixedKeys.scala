package scg.restylekit.common.config.model

abstract class PrefixedKeys(val prefix : String) {
  def key(suffix : String) : String = (prefix + "." + suffix)
}
