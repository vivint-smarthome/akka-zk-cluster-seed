import com.typesafe.sbt.SbtScalariform._

import com.typesafe.sbt.SbtMultiJvm

import com.typesafe.sbt.SbtMultiJvm.MultiJvmKeys.MultiJvm

name := "akka-zk-cluster-seed"

version := "0.1.6-SNAPSHOT"

scalaVersion := "2.11.7"

crossScalaVersions := Seq("2.11.7", "2.10.4")

val akkaVersion      = "2.4.0"

val akkaDependencies = Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion % "provided",
  "com.typesafe.akka" %% "akka-cluster" % akkaVersion % "provided",
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion % "provided",
  "org.slf4j" % "log4j-over-slf4j" % "1.7.7" % "provided",
  "ch.qos.logback" % "logback-classic" % "1.1.2"  % "provided",
  "io.spray" %% "spray-json" % "1.2.6" % "provided",
  "io.spray" %% "spray-client" % "1.3.2" % "provided"
)

val zkDependencies = Seq(
  "org.apache.curator" % "curator-framework" % "2.8.0" exclude("log4j", "log4j") exclude("org.slf4j", "slf4j-log4j12"),
  "org.apache.curator" % "curator-recipes" % "2.8.0"  exclude("log4j", "log4j") exclude("org.slf4j", "slf4j-log4j12")
)

val testDependencies = Seq(
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test",
  "com.typesafe.akka" %% "akka-actor" % akkaVersion % "test",
  "com.typesafe.akka" %% "akka-cluster" % akkaVersion % "test",
  "org.scalatest" %% "scalatest" % "2.1.6" % "test",
  "com.typesafe.akka" %% "akka-multi-node-testkit" % akkaVersion % "test",
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion % "test",
  "org.slf4j" % "log4j-over-slf4j" % "1.7.7" % "test",
  "ch.qos.logback" % "logback-classic" % "1.1.2"  % "test"
)

lazy val rootProject = (project in file(".")).
  settings(
    libraryDependencies ++= (akkaDependencies ++ zkDependencies ++ testDependencies),
    scalacOptions in Compile ++= Seq("-encoding", "UTF-8", "-target:jvm-1.6", "-deprecation", "-feature", "-unchecked", "-Xlog-reflective-calls", "-Xlint", "-language:postfixOps"),
    javacOptions in Compile ++= Seq("-source", "1.6", "-target", "1.6", "-Xlint:unchecked", "-Xlint:deprecation"),
    organization := "com.sclasen",
    name := "akka-zk-cluster-seed",
    version := "0.1.5-SNAPSHOT",
    scalaVersion := "2.11.7",
    crossScalaVersions := Seq("2.11.7", "2.10.4"),
    parallelExecution in Test := false,
    // scalariformSettings
    parallelExecution in Test := false,

    pomExtra := (
      <url>http://github.com/sclasen/akka-zk-cluster-seed</url>
      <licenses>
        <license>
          <name>The Apache Software License, Version 2.0</name>
          <url>http://www.apache.org/licenses/LICENSE-2.0.html</url>
          <distribution>repo</distribution>
        </license>
      </licenses>
      <scm>
        <url>git@github.com:sclasen/akka-zk-cluster-seed.git</url>
        <connection>scm:git:git@github.com:sclasen/akka-zk-cluster-seed.git</connection>
      </scm>
      <developers>
        <developer>
          <id>sclasen</id>
          <name>Scott Clasen</name>
          <url>http://github.com/sclasen</url>
        </developer>
      </developers>),

    publishTo <<= version {
      (v: String) =>
      val nexus = "https://oss.sonatype.org/"
      if (v.trim.endsWith("SNAPSHOT")) Some("snapshots" at nexus + "content/repositories/snapshots")
      else Some("releases" at nexus + "service/local/staging/deploy/maven2")
    }
  ).
  settings(Defaults.itSettings:_*).
  settings(SbtMultiJvm.multiJvmSettings:_*).
  settings(compile in MultiJvm <<= (compile in MultiJvm) triggeredBy (compile in IntegrationTest)).
  settings(executeTests in IntegrationTest <<= (executeTests in Test, executeTests in MultiJvm) map {
    case (testResults, multiNodeResults)  =>
      val overall =
        if (testResults.overall.id < multiNodeResults.overall.id)
          multiNodeResults.overall
        else
          testResults.overall
      Tests.Output(overall,
        testResults.events ++ multiNodeResults.events,
        testResults.summaries ++ multiNodeResults.summaries)
  }).
  configs(IntegrationTest, MultiJvm)
