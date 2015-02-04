package com.timgroup.playlauncher

import java.util.TimeZone

import play.core.server.NettyServer
import java.io.FileInputStream
import java.util.Properties
import java.util.TimeZone
import org.joda.time.DateTimeZone

object Launcher extends App {
  private val DEFAULT_PORT = "8000"
  
  setTimeZoneToUTC()
  
  args.headOption match {
    case Some(filename) => setSystemPropertiesFromConfigFileProperties(filename)
    case None => exit("Expected config file as argument")
  }
  
  NettyServer.main(Array())


  private def setTimeZoneToUTC() {
    sys.props.put("user.timezone", "GMT")
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    DateTimeZone.setDefault(DateTimeZone.UTC)
  }

  private def setSystemPropertiesFromConfigFileProperties(filename: String) {
    val props = new Properties
    props.load(new FileInputStream(filename))

    maybeSetProperty("pidfile.path", "/dev/null")
    maybeSetProperty("http.port", props.getProperty("port", DEFAULT_PORT))

    import scala.collection.JavaConverters._
    props.stringPropertyNames.asScala.foreach(maybeSetFromConfig(_, props))
  }

  private def setNameAndVersionPropertiesFromManifest {
    maybeJarSpecificationVersion match {
      case Some(version) => maybeSetProperty("timgroup.app.version", version)
      case None => exit("Cannot find Implementation-Version in MANIFEST.MF, aborting")
    }

    maybeJarImplementationTitle match {
      case Some(title) => maybeSetProperty("timgroup.app.name", title)
      case None => exit("Cannot find Implementation-Title in MANIFEST.MF, aborting")
    }
  }

  private def exit(msg: String) = {
    Console.println(msg)
    System.exit(1)
  }

  private def maybeSetFromConfig(propName: String, props: Properties) {
    Option(props.getProperty(propName)).foreach { value =>
      maybeSetProperty(propName, possiblyRemoveQuotes(props.getProperty(propName)))
    }
  }

  private def possiblyRemoveQuotes(value: String): String = {
    if ((value.startsWith("\"") && value.endsWith("\""))) {
      value.substring(1, value.length - 1)
    } else {
      value
    }
  }

  private def maybeSetProperty(key: String, value: String) {
    if (!sys.props.contains(key)) {
      if (key.toLowerCase.contains("password")) {
        Console.println(s"Launcher -- setProperty($key, *******)")
      } else {
        Console.println(s"Launcher -- setProperty($key, $value)")
      }
      sys.props.put(key, value)
    } else {
      Console.println(s"Launcher -- Skipping ${key}, already set in System properties")
    }
  }

  private def getLauncherPackage = Launcher.getClass.getPackage
  private def maybeJarImplementationTitle  = Option(getLauncherPackage.getImplementationVersion).filterNot(_.isEmpty)
  private def maybeJarSpecificationVersion = Option(getLauncherPackage.getSpecificationVersion).filterNot(_.isEmpty)
}

