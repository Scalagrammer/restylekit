package scg.restylekit.common.actors.module

import akka.actor.ActorSystem
import com.typesafe.config.{Config, ConfigValueFactory}
import scg.restylekit.common.actors.dispatch.MdcPropagatingDispatcherConfigurator
import scg.restylekit.common.config.module.ConfigModule

trait ActorsModule {
  implicit def actors : ActorSystem
}

trait ActorsModuleImpl extends ActorsModule {

  this : ConfigModule =>

  import ActorsModuleImpl.withMdcPropagationDispatch

  override implicit lazy val actors : ActorSystem = {
    ActorSystem("actors", withMdcPropagationDispatch(cfg))
  }

}

object ActorsModuleImpl {

  import scala.collection.convert.ImplicitConversionsToJava._

  private[module] def withMdcPropagationDispatch(cfg : Config) : Config = {
    cfg.withValue("akka.actor.default-dispatcher", ConfigValueFactory.fromMap(Map("type" -> classOf[MdcPropagatingDispatcherConfigurator].getCanonicalName)))
  }
}