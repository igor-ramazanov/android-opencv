name := "mnist-server"

version := "0.1"

scalaVersion := "2.11.11"

libraryDependencies ++= Seq(
//  "org.apache.spark" %% "spark-mllib" % "2.2.1" % "provided",
//  "org.apache.spark" %% "spark-core" % "2.2.1" % "provided",
//  "org.apache.hadoop" % "hadoop-client" % "2.7.2",
  "com.trueaccord.scalapb" %% "scalapb-runtime" % com.trueaccord.scalapb.compiler.Version.scalapbVersion % "protobuf",
  "io.grpc" % "grpc-netty" % com.trueaccord.scalapb.compiler.Version.grpcJavaVersion,
  "com.trueaccord.scalapb" %% "scalapb-runtime-grpc" % com.trueaccord.scalapb.compiler.Version.scalapbVersion
)

libraryDependencies += "org.deeplearning4j" % "deeplearning4j-core" % "0.9.1"
libraryDependencies += "org.nd4j" % "nd4j-native-platform" % "0.9.1"

PB.targets in Compile := Seq(
  scalapb.gen() -> (sourceManaged in Compile).value
)

//assemblyMergeStrategy in assembly := {
//  case PathList("org", "apache", "spark", "unused", "UnusedStubClass.class") => MergeStrategy.first
//  case PathList("org", "apache", "commons", xs@_*) => MergeStrategy.first
//  case PathList("org", "apache", xs@_*) => MergeStrategy.first
//  case PathList("org", "aopalliance", xs@_*) => MergeStrategy.first
//  case PathList("javax", xs@_*) => MergeStrategy.first
//  case PathList("META-INF", "io.netty.versions.properties") => MergeStrategy.first
//  case "mime.types" | "log4j.properties" | ".gitkeep" | "overview.html" => MergeStrategy.first
//  case x => (assemblyMergeStrategy in assembly).value(x)
//}
//
//assemblyShadeRules in assembly := Seq(
//  ShadeRule.rename("io.netty.**" -> "my_conf.@1")
//    .inLibrary("io.netty" % "netty-all" % "4.0.23.Final")
//)