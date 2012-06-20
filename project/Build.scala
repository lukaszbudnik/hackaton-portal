import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

    val appName         = "hackaton-portal"
    val appVersion      = "0.1-SNAPSHOT"

    val appDependencies = Seq(
      "org.squeryl" %% "squeryl" % "0.9.5-2",
      "com.h2database" % "h2" % "1.3.167",
      "postgresql" % "postgresql" % "9.1-901.jdbc4"
    )

    val secureSocial = PlayProject(
        appName + "-securesocial", appVersion, mainLang = SCALA, path = file("modules/securesocial")
    )
    
    val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA).settings(
      // Add your own project settings here      
    ).dependsOn(secureSocial).aggregate(secureSocial)

}
