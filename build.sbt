name := "text_my_wife"

version := "0.1"

scalaVersion := "2.12.2"

libraryDependencies += "com.twilio.sdk" % "twilio" % "8.0.0"
libraryDependencies += "com.github.scopt" %% "scopt" % "4.0.0-RC2"
libraryDependencies += "org.clapper" %% "grizzled-slf4j" % "1.3.3"
libraryDependencies += "org.slf4j" % "slf4j-simple" % "1.7.29"
libraryDependencies += "com.jcabi" % "jcabi-aspects" % "0.22.6"
libraryDependencies += "org.scalaj" %% "scalaj-http" % "2.4.2"
libraryDependencies += "com.typesafe.play" %% "play-json" % "2.9.1"

javacOptions ++= Seq("-source", "1.8")

initialize := {
  val _ = initialize.value
  val requiredJavaVersion = "1.8"
  val javaVersionCurrent  = sys.props("java.specification.version")
  assert(javaVersionCurrent == requiredJavaVersion, s"Required JDK is $requiredJavaVersion, but $javaVersionCurrent found")
}

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}