name := "TAStreamer"

version := "0.1"

scalaVersion := "2.12.12"

val sparkVersion = "3.0.0"


libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % sparkVersion,
  "org.apache.spark" %% "spark-sql" % sparkVersion,

  "org.apache.spark" %% "spark-streaming" % sparkVersion,
)
libraryDependencies += "org.apache.bahir" %% "spark-streaming-twitter" % "2.4.0"