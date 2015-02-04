package com.timgroup
package sbtplaylauncher

import sbt._
import Keys._

object SbtPlayLauncher extends Plugin {

  lazy val playLauncher = TaskKey[Seq[File]]("playLauncher")

  lazy val playLauncherSettings: Seq[Def.Setting[_]] = Seq(
    playLauncher <<= (sourceManaged in Compile) map {
      (dir) =>
        Seq(PlayLauncherTask(dir / "sbt-play-launcher").file)
    },
    sourceGenerators in Compile <+= (playLauncher in Compile)
  )

  private case class PlayLauncherTask(dir: File) {
    def file = {
      dir.mkdirs()
      val outputFile = new File(dir, "Launcher.scala")
      
      if (!outputFile.exists) {
        val is = getClass.getResourceAsStream("/Launcher.scala")
        inputToFile(is, outputFile)
      }
      
      outputFile
    }
    
    private def inputToFile(is: java.io.InputStream, f: java.io.File) {
      val in = scala.io.Source.fromInputStream(is)
      val out = new java.io.PrintWriter(f)
      try { in.getLines().foreach(out.println(_)) }
      finally { out.close }
    }
  }
}
