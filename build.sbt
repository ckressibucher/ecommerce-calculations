name         := "ckrecom"

version      := "1.0-SNAPSHOT"

scalaVersion := "2.11.8"

scalacOptions := Seq("-unchecked", "-deprecation", "-feature", "-encoding", "utf8")

resolvers += "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"

libraryDependencies ++= Seq(
    "org.scalatest"     %% "scalatest" 	     			   % "2.2.6" % "test"
    //"javax.money"       % "money-api"                % "1.0",
    //"org.javamoney"     % "moneta"                   % "1.1" % "test"
    //"org.joda"          % "joda-money"       % "0.11"
    // "joda-time"          % "joda-time"       % "2.4",
    //"org.reactivemongo" %% "reactivemongo"   			   % "0.11.7",
    //"org.reactivemongo" %% "reactivemongo-extensions-bson" % "0.11.7.play24",
    //"com.typesafe"	 	 % "config" 		 			   % "1.3.0"
)

initialCommands in console := """
import javax.money._
"""
