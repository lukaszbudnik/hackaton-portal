import sbt._
import Keys._
import PlayProject._
import com.typesafe.sbteclipse.core.EclipsePlugin.EclipseKeys

object ApplicationBuild extends Build {

  val appName = "hackaton-portal"
  val appVersion = "0.2-SNAPSHOT"

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
    "org.mindrot" % "jbcrypt" % "0.3m",
    "com.github.mumoshu" %% "play2-memcached" % "0.2.1-SNAPSHOT")

  val plugins = PlayProject(
    appName + "-play-cloud-plugins", appVersion, appDependencies, path = file("modules/play-cloud-plugins"))

  val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA)
    .settings(
      coffeescriptOptions := Seq("bare"),
      resolvers ++= Seq(
        "jBCrypt Repository" at "http://repo1.maven.org/maven2/",
        "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
        "Sonatype OSS Snapshots Repository" at "http://oss.sonatype.org/content/groups/public",
        "Spy Repository" at "http://files.couchbase.com/maven2")).dependsOn(plugins).aggregate(plugins)
}
