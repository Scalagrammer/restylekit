import sbt._

object Dependencies {

  val silencer                      = "1.3.3"
  val ficusVersion                  = "1.4.4"
  val mongoVersion                  = "2.6.0"
  val macwireVersion                = "2.3.1"
  val akkaHttpVersion               = "10.1.8"
  val playJsonVersion               = "2.8.0"
  val scalaCacheVersion             = "0.28.0"
  val akkaActorVersion              = "2.5.25"
  val enumeratumVersion             = "1.5.13"
  val catsEffectVersion             = "1.3.1"
  val logbackJsonVersion            = "0.1.5"
  val scalaLoggingVersion           = "3.9.0"
  val reactiveMongoVersion          = "0.18.4"
  val betterMonadicForVersion       = "0.3.0"
  val logstashLogbackEncoderVersion = "6.2"

  def mongo : Seq[ModuleID] = {
    Seq("org.reactivemongo"                  %% "reactivemongo"            % reactiveMongoVersion) ++ compiler
  }

  def redis : Seq[ModuleID] = {
    Seq("com.github.cb372"                   %% "scalacache-redis"         % scalaCacheVersion) ++ compiler
  }

  def http : Seq[ModuleID] = {
    Seq("com.typesafe.akka"                  %% "akka-http"                % akkaHttpVersion,
        "com.typesafe.akka"                  %% "akka-stream"              % akkaActorVersion,
        "com.beachape"                       %% "enumeratum"               % enumeratumVersion,
        "com.beachape"                       %% "enumeratum-play-json"     % enumeratumVersion) ++ compiler
  }

  def common : Seq[ModuleID] = {
    Seq("com.typesafe.scala-logging"         %% "scala-logging"            % scalaLoggingVersion,
        "ch.qos.logback.contrib"              % "logback-json-classic"     % logbackJsonVersion,
        "org.typelevel"                      %% "cats-effect"              % catsEffectVersion,
        "ch.qos.logback.contrib"              % "logback-jackson"          % logbackJsonVersion,
        "com.typesafe.akka"                  %% "akka-actor"               % akkaActorVersion,
        "net.logstash.logback"                % "logstash-logback-encoder" % logstashLogbackEncoderVersion,
        "com.iheart"                         %% "ficus"                    % ficusVersion,
        "com.softwaremill.macwire"           %% "util"                     % macwireVersion,
        "com.softwaremill.macwire"           %% "macros"                   % macwireVersion) ++ compiler
  }

  def compiler : Seq[ModuleID] = {
    Seq(compilerPlugin("com.github.ghik"     %% "silencer-plugin"          % silencer), // warning-supressor (wiring)
        compilerPlugin("com.olegpy"          %% "better-monadic-for"       % betterMonadicForVersion),
        "com.github.ghik"                    %% "silencer-lib"             % silencer % Provided) // supressor API)
  }

  def model : Seq[ModuleID] = compiler

}