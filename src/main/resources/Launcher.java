package com.timgroup.playlauncher;

import java.io.IOException;
import java.io.FileInputStream;
import java.lang.String;
import java.lang.reflect.Method;
import java.util.Properties;

import java.util.TimeZone;
import org.joda.time.DateTimeZone;

/*
 PLEASE NOTE: This class is only used for production deployment of the app.
 THIS CLASS IS NOT RUN BY THE TEST ENVIRONMENT - RNA/TW - 11/12/12
 */
public class Launcher {
    private static final String PLAY_NETTY = "play.core.server.NettyServer";
    private static final String DEFAULT_PORT = "8000";

    public static void main(String[] args) throws Exception {
        setTimeZoneToUTC();
        setNameAndVersionPropertiesFromManifest();
        setSystemPropertiesFromConfigFileProperties(args);
        launchPlayApp();
    }

    private static void setTimeZoneToUTC() {
        setProperty("user.timezone", "GMT");

        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        DateTimeZone.setDefault(DateTimeZone.UTC);
    }

    private static void setSystemPropertiesFromConfigFileProperties(String[] args) throws IOException {
        Properties props = new Properties();
        if (args.length > 0) {
            props.load(new FileInputStream(args[0]));
        }
        
        setProperty("pidfile.path", "/dev/null");
        setProperty("http.port", props.getProperty("port", DEFAULT_PORT));

        for (String name: props.stringPropertyNames()) {
            setFromConfig(name, props);
        }
    }

    private static void setNameAndVersionPropertiesFromManifest() {
        String version = getJarSpecificationVersion();
        if (version == null) {
            System.out.println("Cannot find Implementation-Version in MANIFEST.MF, aborting");
            System.exit(1);
        }
        setProperty("timgroup.app.version", version);
        String implTitle = getJarImplementationTitle();
        if (implTitle == null) {
            System.out.println("Cannot find Implementation-Title in MANIFEST.MF, aborting");
            System.exit(1);
        }
        setProperty("timgroup.app.name", getJarImplementationTitle());
    }

    private static void setFromConfig(String propName, Properties props) {
        if (props.getProperty(propName) != null) { setProperty(propName, possiblyRemoveQuotes(props.getProperty(propName))); }
    }

    private static String possiblyRemoveQuotes(String value) {
        if ((value.startsWith("\"") && value.endsWith("\""))) {
            return value.substring(1, value.length()-1);
        } else {
            return value;
        }
    }

    private static void setProperty(String key, String value) {
        if (key.contains("password")) {
            System.out.println("Launcher -- setProperty(" + key + ", *******)");
        } else {
            System.out.println("Launcher -- setProperty(" + key + ", " + value + ")");
        }
        System.setProperty(key, value);
    }

    private static Package getLauncherPackage() {
        return Launcher.class.getPackage();
    }

    private static String getJarImplementationTitle() {
        String version = getLauncherPackage().getImplementationVersion();
        return (version == null || version.isEmpty()) ? null : version;
    }

    private static String getJarSpecificationVersion() {
        String version = getLauncherPackage().getSpecificationVersion();
        return (version == null || version.isEmpty()) ? null : version;
    }

    private static void launchPlayApp() throws Exception {
        Class<?> launcher = Launcher.class.getClassLoader().loadClass(PLAY_NETTY);
        final Method main = launcher.getMethod("main", String[].class);
        main.invoke(null, new Object[]{new String[]{}});
    }
}
