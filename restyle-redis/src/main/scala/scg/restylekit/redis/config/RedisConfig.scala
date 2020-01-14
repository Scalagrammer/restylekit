package scg.restylekit.redis.config

import java.util.concurrent.TimeUnit.SECONDS

import net.ceedubs.ficus.readers.ValueReader
import org.apache.commons.pool2.impl.GenericObjectPoolConfig

import scala.concurrent.duration.FiniteDuration
import scala.language.implicitConversions

case class RedisConfig(host              : String,
                       port              : Int,
                       maxConnections    : Int,
                       maxIdle           : Int,
                       minIdle           : Int,
                       borrowPingTest    : Boolean,
                       idlePingTest      : Boolean,
                       createPingTest    : Boolean,
                       exposeMetrics     : Boolean,
                       releasePingTest   : Boolean,
                       maxWait           : FiniteDuration,
                       connectionTimeout : FiniteDuration,
                       password          : Option[String])

object RedisConfig {

  import net.ceedubs.ficus.Ficus._

  implicit val redisConfigReader : ValueReader[RedisConfig] = ValueReader.relative { cfg =>
    RedisConfig(minIdle = cfg.as[Int]("min-idle"),
                maxIdle = cfg.as[Int]("max-idle"),
                password = cfg.getAs[String]("password"),
                maxConnections = cfg.as[Int]("max-connected"),
                port = cfg.getAs[Int]("port").getOrElse(6379),
                host = cfg.getAs[String]("host").getOrElse("localhost"),
                idlePingTest = cfg.getAs[Boolean]("idle-ping-test").getOrElse(false),
                exposeMetrics = cfg.getAs[Boolean]("expose-metrics").getOrElse(false),
                createPingTest = cfg.getAs[Boolean]("create-ping-test").getOrElse(false),
                borrowPingTest = cfg.getAs[Boolean]("borrow-ping-test").getOrElse(false),
                releasePingTest = cfg.getAs[Boolean]("release-ping-test").getOrElse(false),
                maxWait = cfg.getAs[FiniteDuration]("wait-timeout").getOrElse(FiniteDuration(1, SECONDS)),
                connectionTimeout = cfg.getAs[FiniteDuration]("connect-timeout").getOrElse(FiniteDuration(10, SECONDS)))
  }

  implicit def toPoolConfig(cfg : RedisConfig) : GenericObjectPoolConfig = {
    new GenericObjectPoolConfig {
      setMinIdle(cfg.minIdle)
      setMaxIdle(cfg.maxIdle)
      setMaxTotal(cfg.maxConnections)
      setJmxEnabled(cfg.exposeMetrics)
      setTestWhileIdle(cfg.idlePingTest)
      setTestOnBorrow(cfg.borrowPingTest)
      setTestOnCreate(cfg.createPingTest)
      setTestOnReturn(cfg.releasePingTest)
      setMaxWaitMillis(cfg.maxWait.toMillis)
    }
  }
}
