import sbt.Keys._
import sbt.{Def, _}

object RestyleFlow {

  val nexusRepo : String = ???

  lazy val `make restyle-build.conf file` = taskKey[Unit]("Make restyle-build.conf file")

  lazy val buildRestyleConfSettings : Seq[Def.Setting[_]] = inConfig(Compile) {
    Seq(compile := (compile dependsOn `make restyle-build.conf file`).value,
      `make restyle-build.conf file` := makeRestyleBuildConfFile(Compile).value)
  }

  def makeRestyleBuildConfFile(config : Configuration) : Def.Initialize[Task[Unit]] = Def.task {

    val file = (classDirectory in config).value / "restyle-build.conf"

    def content : String = s"""
      |restyle-build {
      | version = "${version.value}"
      |}
    """

    IO.writeLines(file, Seq(content.stripMargin))
  }

  lazy val testSettings : Seq[Def.Setting[_]] = inConfig(Test) {
    Seq(fork := false,
      testOptions := Seq(Tests.Filter(!isIntegrationTest(_))))
  }

  lazy val Serial = config("serial") extend Test

  lazy val serialTestSettings : Seq[Def.Setting[_]] = inConfig(Serial) {
    Defaults.testTasks ++ {
      Seq(fork := true,
          testOptions := Seq(Tests.Filter(isIntegrationTest)),
          parallelExecution := false,
          baseDirectory := file(s"${Keys.name.value}/target"))
    }
  }

  def makeVersion(major : String, minor : Option[String] = None) : String = major + minor.fold("")("." + _)

  implicit class ProjectExt(val project : Project) extends AnyVal {

    def withRestyleFlow : Project = {
      project
        .configs(Serial)
        .configs(IntegrationTest)
        .settings(testSettings : _*)
        .settings(serialTestSettings : _*)
    }
  }

  def isIntegrationTest(name : String) : Boolean = name.endsWith("ISpec")

}

