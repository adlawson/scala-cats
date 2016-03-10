lazy val cats = (project in file("."))
  .enablePlugins(GitVersioning)
  .settings(
    organization := "com.adlawson",
    name := "cats",
    git.useGitDescribe := true,
    scalaVersion := "2.11.7",
    scalacOptions ++= Seq(
      "-encoding", "UTF-8",
      "-deprecation",
      "-feature",
      "-language:higherKinds",
      "-language:implicitConversions",
      "-Ywarn-unused-import"
    ),
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core" % "0.4.1"
    ),
    libraryDependencies ++= Seq(
      "com.ironcorelabs" %% "cats-scalatest" % "1.1.2",
      "org.scalamock" %% "scalamock-scalatest-support" % "3.2.2",
      "org.scalatest" %% "scalatest" % "2.2.5"
    ).map(_ % Test),
    licenses := Seq("MIT" -> url("http://opensource.org/licenses/mit")),
    homepage := Some(url("https://github.com/adlawson/scala-cats")),
    publishTo := {
      val nexus = "https://oss.sonatype.org/"
      if (isSnapshot.value) Some("snapshots" at nexus + "content/repositories/snapshots")
      else Some("releases" at nexus + "service/local/staging/deploy/maven2")
    },
    credentials ++= (for {
      user <- scala.util.Properties.envOrNone("SONATYPE_USERNAME")
      pass <- scala.util.Properties.envOrNone("SONATYPE_PASSWORD")
    } yield Credentials("Sonatype Nexus Repository Manager", "oss.sonatype.org", user, pass)).toSeq
  )
