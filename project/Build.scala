import sbt._
import Keys._
import PlayProject._
import com.typesafe.sbteclipse.core.EclipsePlugin.EclipseKeys

object ApplicationBuild extends Build {

    val appName         = "hackaton-portal"
    val appVersion      = "0.1-SNAPSHOT"

    override def settings = super.settings ++ Seq(
	EclipseKeys.skipParents in ThisBuild := false)

    val appDependencies = Seq(
      "org.squeryl" %% "squeryl" % "0.9.5-2",
      "com.h2database" % "h2" % "1.3.168",
      "postgresql" % "postgresql" % "9.1-901.jdbc4",
      "rome" % "rome" % "1.0",
      "net.databinder" %% "dispatch-http" % "0.8.7",
      "net.databinder" %% "dispatch-mime" % "0.8.7",
      "net.databinder" %% "dispatch-json" % "0.8.7",
      "com.typesafe" %% "play-plugins-util" % "2.0.1",
      "org.mindrot" % "jbcrypt" % "0.3m"
    )
    
    val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA)
    .settings(
      coffeescriptOptions := Seq("bare"),
      resolvers ++= Seq(
        "jBCrypt Repository" at "http://repo1.maven.org/maven2/org/",
        "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
      )
    )
}
