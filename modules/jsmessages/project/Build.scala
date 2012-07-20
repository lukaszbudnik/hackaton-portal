import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

    val appName         = "jsmessages"
    val appVersion      = "1.0-SNAPSHOT"


    val appDependencies = Seq(
       "commons-codec" % "commons-codec" % "1.4"
    )

    val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA)
}
