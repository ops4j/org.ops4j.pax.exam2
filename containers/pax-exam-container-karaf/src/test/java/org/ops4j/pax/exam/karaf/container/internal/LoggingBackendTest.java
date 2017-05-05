package org.ops4j.pax.exam.karaf.container.internal;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Properties;

import org.junit.Test;

public class LoggingBackendTest {

    @Test
    public void testLog4J2EmptyPropertiesSetsRootLoggerLevel() {
        Properties properties = new Properties();

        LoggingBackend.LOG4J2.updatePaxLoggingConfiguration(properties, "chosenLevel");

        Properties expectedProperties = makeProperties("log4j2.rootLogger.level", "chosenLevel");

        assertThat(properties, equalTo(expectedProperties));
    }

    @Test
    public void testLog4J2OverwritesAlreadySetRootLoggerLevel() {
        Properties properties = makeProperties("log4j2.rootLogger.level", "originalLevel");

        LoggingBackend.LOG4J2.updatePaxLoggingConfiguration(properties, "chosenLevel");

        Properties expectedProperties = makeProperties("log4j2.rootLogger.level", "chosenLevel");

        assertThat(properties, equalTo(expectedProperties));
    }

    @Test
    public void testLog4J2PropertiesWithUnusedConsoleAppenderAddsConsoleAppender() {
        Properties properties = makeProperties("log4j2.appender.console.type", "Console",
                                               "log4j2.appender.console.name", "RenamedConsole");

        LoggingBackend.LOG4J2.updatePaxLoggingConfiguration(properties, "chosenLevel");

        Properties expectedProperties = makeProperties("log4j2.rootLogger.level", "chosenLevel",
                                                       "log4j2.appender.console.type", "Console",
                                                       "log4j2.appender.console.name", "RenamedConsole",
                                                       "log4j2.rootLogger.appenderRef.RenamedConsole.ref", "RenamedConsole");

        assertThat(properties, equalTo(expectedProperties));
    }

    @Test
    public void testLog4J2PropertiesWithUsedConsoleAppenderDoesNotAddConsoleAppender() {
        Properties properties = makeProperties("log4j2.appender.console.type", "Console",
                                               "log4j2.appender.console.name", "RenamedConsole",
                                               "log4j2.rootLogger.appenderRef.ren_con.ref", "RenamedConsole");

        LoggingBackend.LOG4J2.updatePaxLoggingConfiguration(properties, "chosenLevel");

        Properties expectedProperties = makeProperties("log4j2.rootLogger.level", "chosenLevel",
                                                       "log4j2.appender.console.type", "Console",
                                                       "log4j2.appender.console.name", "RenamedConsole",
                                                       "log4j2.rootLogger.appenderRef.ren_con.ref", "RenamedConsole");

        assertThat(properties, equalTo(expectedProperties));
    }

    private Properties makeProperties(String... args) {
        Properties properties = new Properties();
        for (int i = 0; i < args.length; i += 2) {
            properties.setProperty(args[i], args[i + 1]);
        }
        return properties;
    }
}
