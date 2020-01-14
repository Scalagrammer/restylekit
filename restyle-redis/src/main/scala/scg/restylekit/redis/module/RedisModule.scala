package scg.restylekit.redis.module

import cats.effect.IO.unit

import cats.effect.IO
import scalacache.CacheConfig
import scalacache.redis.RedisCache
import redis.clients.jedis.{Jedis, JedisPool}
import scalacache.serialization.Codec
import scg.restylekit.common.lifecycle.Hook
import scg.restylekit.redis.config.RedisConfig
import scg.restylekit.redis.service.CacheManager

import scala.sys.error

trait RedisModule {
  def cacheManager(config : RedisConfig) : CacheManager
}

trait RedisModuleImpl extends RedisModule {
  override def cacheManager(cfg : RedisConfig) : CacheManager = {
    new CacheManager {

      lazy val pool = new JedisPool(cfg, cfg.host, cfg.port, cfg.connectionTimeout.toMillis.toInt, cfg.password.orNull)

      override lazy val hook : Hook = new Hook {

        import cats.syntax.flatMap._
        import cats.effect.Resource.{fromAutoCloseable => AutoCloseable}

        override def startup() : IO[Unit] = {
          AutoCloseable(IO[Jedis](pool.getResource))
        }.use(jedis => IO(jedis.ping())) >>= {
          case "PONG" => unit
          case _ =>
            IO(error("Redis cache-pool ping failed"))
        }

        override def shutdown() : IO[Unit] = IO(pool.destroy())

      }

      override def cache[V : Codec](implicit cacheConfig : CacheConfig) : RedisCache[V] = RedisCache(pool)

    }
  }
}
