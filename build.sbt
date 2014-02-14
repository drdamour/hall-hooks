import CoverallsPlugin.CoverallsKeys._

name := "hall-hooks"

version := "1.0-SNAPSHOT"

resolvers ++= Seq(
  "Spray IO Release Repo" at "http://repo.spray.io",
  "Sonatype Nexus Repository Manager Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
)

libraryDependencies ++= Seq(
  //Testing
  "org.mockito" % "mockito-core" % "1.9.5" % "test",
  //WebJars
  "org.webjars" %% "webjars-play" % "2.2.0",
  "org.webjars" % "bootstrap" % "3.0.3",
  "org.webjars" % "jquery" % "2.0.3-1",
  "org.webjars" % "font-awesome" % "4.0.3",
  //Views
  "com.balihoo" %% "play2-bootstrap3" % (play.core.PlayVersion.current + "-SNAPSHOT")
)     

play.Project.playScalaSettings

//Coveralls settings: https://github.com/theon/xsbt-coveralls-plugin
seq(CoverallsPlugin.singleProject: _*)