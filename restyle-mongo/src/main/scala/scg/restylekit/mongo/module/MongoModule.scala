package scg.restylekit.mongo.module

import cats.effect.Async
import com.typesafe.config.Config
import reactivemongo.api.{MongoConnection, MongoDriver}
import scg.restylekit.common.config.module.ConfigModule
import scg.restylekit.common.lifecycle.{Hook, ShutdownHook}

import scala.language.higherKinds

trait MongoModule {
  def connectMongo[F[_] : Async](cfg : Config) : F[MongoConnection]
}

trait MongoModuleImpl extends MongoModule {

  this : ConfigModule =>

  import net.ceedubs.ficus.Ficus._

  override def connectMongo[F[_] : Async](cfg : Config) : F[MongoConnection] = {
    Async[F].async(_.apply(driver.connection(cfg.as[String]("mongo.uri")).toEither))
  }

  lazy val mongoDriverShutdownHook : Hook = ShutdownHook(driver.close())

  private[this] lazy val driver : MongoDriver = MongoDriver(cfg)

}
