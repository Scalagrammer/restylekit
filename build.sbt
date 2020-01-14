import sbt._
import RestyleFlow._
import Dependencies._
import Keys.resolvers
import librarymanagement.Resolver

import java.net.{URI => JURI}

val `build-version` : String = "20.0.0"

val blockadeFile : JURI = file("./blockade.json").toURI

lazy val commonsSettings : Seq[Def.Setting[_]] = {
  Seq(scalaVersion := "2.12.8",
      blockadeFailTransitive := true,
      blockadeUris := Seq(blockadeFile),
      compile in Compile := (compile in Compile).dependsOn(blockade).value,
      resolvers ++= Seq(Resolver.bintrayRepo("cakesolutions", "maven"), Resolver.bintrayRepo("lightshed", "maven")),
      organization := "scg.restyle",
      Compile / compile / scalacOptions ++= {
        Seq("-Ywarn-dead-code", // Warn when dead code is identified.
          "-Ywarn-inaccessible", // Warn about inaccessible types in method signatures.
          "-Ywarn-infer-any", // Warn when a type argument is inferred to be Any.
          "-Ywarn-nullary-override", // Warn when non-nullary `def f()' overrides nullary `def f'.
          "-Ywarn-nullary-unit", // Warn when nullary methods return Unit.
          "-Ywarn-unused:imports", // Warn if an import selector is not referenced.
          "-Ywarn-unused:locals", // Warn if a local definition is unused.
          "-Ywarn-unused:params", // Warn if a value parameter is unused.
          "-Ywarn-unused:patvars", // Warn if a variable bound in a pattern is unused.
          "-Ywarn-unused:privates", // Warn if a private member is unused.
          "-Ywarn-value-discard", // Warn when non-Unit expression results are unused.
          "-Ypartial-unification",

          "-Ycache-plugin-class-loader:last-modified",
          "-Ycache-macro-class-loader:last-modified",
          "-Ybackend-parallelism", java.lang.Runtime.getRuntime.availableProcessors.toString,

          "-unchecked",
          "-feature",
          "-deprecation:false",
          "-Xfatal-warnings",
          "-Xmax-classfile-name", "240",
          "-encoding", "UTF-8")
      },
      version := {
        git.gitCurrentBranch.value match {
          case "master" => `build-version`
          case _ =>
            s"${`build-version`}-SNAPSHOT"
        }
      },
      publishMavenStyle := true,
      credentials += Credentials(Path.userHome / ".sbt" / ".credentials"),
      Global / parallelExecution := System.getProperty("globalParallel") == "true",
      updateOptions := {
        updateOptions.value
          .withLatestSnapshots(System.getProperty("withLatestSnapshots") != "false")
          .withGigahorse(false)
      },
      publishTo := {
        git.gitCurrentBranch.value match {
          case "master" =>
            Some("restyle-releases" at nexusRepo + "/maven-releases/")
          case _ =>
            Some("restyle-snapshots" at nexusRepo + "/maven-snapshots/")
        }
      },
      addCompilerPlugin("org.spire-math" % "kind-projector" % "0.9.9" cross CrossVersion.binary)
  )
}

lazy val coverageSettings = {
  Seq(coverageExcludedPackages := ".*\\.wiring\\..*;.*\\.module\\..*",
      coverageOutputTeamCity := true,
      coverageMinimum := 0,
      coverageFailOnMinimum := true)
}

lazy val restyleModel : Project = {
  (project in file("restyle-model"))
    .withRestyleFlow
    .settings(name := "restyle-model",
      commonsSettings,
      coverageSettings,
      libraryDependencies ++= model)
}

lazy val restyleRedis : Project = {
  (project in file("restyle-redis"))
    .withRestyleFlow
    .settings(name := "restyle-redis",
      commonsSettings,
      coverageSettings,
      libraryDependencies ++= redis)
    .dependsOn(restyleCommon)
}

lazy val restyleCommon : Project = {
  (project in file("restyle-common"))
    .withRestyleFlow
    .settings(name := "restyle-common",
      commonsSettings,
      coverageSettings,
      libraryDependencies ++= common)
    .dependsOn(restyleModel)
    .settings(RestyleFlow.buildRestyleConfSettings : _ *)
}

lazy val restyleHttp : Project = {
  (project in file("restyle-http"))
    .withRestyleFlow
    .settings(name := "restyle-http",
      commonsSettings,
      coverageSettings,
      libraryDependencies ++= http)
    .dependsOn(restyleModel, restyleCommon)
}

lazy val restyleMongo : Project = {
  (project in file("restyle-mongo"))
    .withRestyleFlow
    .settings(name := "restyle-mongo",
      commonsSettings,
      coverageSettings,
      libraryDependencies ++= mongo)
    .dependsOn(restyleModel, restyleCommon)
}

lazy val `restyle-core-parent` = {
  (project in file("."))
    .settings(publish := ())
    .aggregate(restyleModel, restyleCommon, restyleHttp, restyleRedis, restyleMongo)
}