package scg.restylekit.redis.service

import scalacache.CacheConfig
import scalacache.redis.RedisCache
import scalacache.serialization.Codec
import scg.restylekit.common.lifecycle.Hook

trait CacheManager {
  def hook : Hook

  def cache[V : Codec](implicit cacheConfig : CacheConfig) : RedisCache[V]
}
