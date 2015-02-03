package com.timgroup
package sbtplaylauncher

import sbt._
import Keys._

object SbtPlayLauncher extends Plugin {

  lazy val playLauncher = TaskKey[Seq[File]]("playLauncher")
  
  object autoImport {
    
    
  }
  
  override lazy val projectSettings = {
    Seq()
  }

  lazy val playLauncherSettings: Seq[Def.Setting[_]] = Seq(
    playLauncher <<= (sourceManaged in Compile) map {
      (dir) =>
        Seq(PlayLauncherTask(dir / "sbt-play-launcher").file)
    },
    sourceGenerators in Compile <+= (playLauncher in Compile)
  )
  
  override lazy val globalSettings = Seq()
  
  private case class PlayLauncherTask(dir: File) {
    def file = {
      val pkgDir = dir / "com" / "timgroup" / "playlauncher"
      pkgDir.mkdirs()
      val outputFile = new File(pkgDir, "Launcher.java")
      
      if (!outputFile.exists) {
        val is = getClass.getResourceAsStream("/Launcher.java")
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
