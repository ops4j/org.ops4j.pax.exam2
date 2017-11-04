package org.ops4j.pax.exam.karaf.container.internal;

import java.util.Map;
import java.util.Properties;

/**
 * Identifies and handles the possible logging backends used by Karaf.
 */
enum LoggingBackend {
    LOG4J {
        @Override
        void updatePaxLoggingConfiguration(Properties karafPropertyFile, String realLogLevel) {
            karafPropertyFile.put("log4j.rootLogger", realLogLevel + ", out, stdout, osgi:*");
        }
    },

    LOG4J2 {
        @Override
        void updatePaxLoggingConfiguration(Properties karafPropertyFile, String realLogLevel) {
            // Change the root logging level to that specified by the test environment
            karafPropertyFile.put("log4j2.rootLogger.level", realLogLevel);

            // Look for an existing console appender
            String consoleAppenderName = null;
            for (Map.Entry<Object, Object> property : karafPropertyFile.entrySet()) {
                String propertyValue = property.getValue().toString();
                if (propertyValue.equals("Console")) {
                    String propertyName = property.getKey().toString();
                    if (propertyName.matches("log4j2\\.appender\\.[^\\.]+\\.type")) {
                        consoleAppenderName = karafPropertyFile.getProperty(propertyName.replaceAll(".type", ".name"));
                        break;
                    }
                }
            }

            if (consoleAppenderName != null && consoleAppenderName.length() > 0) {
                boolean rootLoggerHasConsoleAppender = false;
                for (Map.Entry<Object, Object> property : karafPropertyFile.entrySet()) {
                    String propertyValue = property.getValue().toString();
                    if (propertyValue.equals(consoleAppenderName)) {
                        String propertyName = property.getKey().toString();
                        if (propertyName.matches("log4j2\\.rootLogger\\.appenderRef\\.[^\\.]+\\.ref")) {
                            rootLoggerHasConsoleAppender = true;
                            break;
                        }
                    }
                }

                if (!rootLoggerHasConsoleAppender) {
                    karafPropertyFile.put("log4j2.rootLogger.appenderRef." + consoleAppenderName + ".ref", consoleAppenderName);
                }
            }
        }
    };

    abstract void updatePaxLoggingConfiguration(Properties karafPropertyFile, String realLogLevel);
}
