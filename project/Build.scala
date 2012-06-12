import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

    val appName         = "hackaton-portal"
    val appVersion      = "1.0-SNAPSHOT"

    val appDependencies = Seq(
      "org.squeryl" %% "squeryl" % "0.9.5-2",
      "com.h2database" % "h2" % "1.3.167"
    )

    val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA).settings(
      // Add your own project settings here      
    )

}
