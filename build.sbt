lazy val cats = (project in file("."))
  //.enablePlugins(GitVersioning)
  .settings(
    organization := "com.adlawson",
    name := "cats",
    version := "1.0.0-SNAPSHOT",
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
    ).map(_ % Test)
  )
