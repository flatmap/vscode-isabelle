name := "vscode-isabelle"
description := "Isabelle Support for VSCode"
version := "0.0.1"
organization := "net.flatmap"
licenses += "MIT" -> url("https://opensource.org/licenses/MIT")
resolvers += Resolver.bintrayRepo("flatmap", "maven")
libraryDependencies += "info.hupel" %% "libisabelle-setup" % "0.5"
libraryDependencies += "org.jfree" % "jfreechart" % "1.0.14"
libraryDependencies += "net.flatmap" %% "vscode-languageserver" % "0.4.12"
scalaVersion := "2.11.8"

stagingDirectory in Universal := baseDirectory.value / ".." / "client" / "server"

enablePlugins(JavaAppPackaging)