package org.ops4j.pax.exam.karaf.options;

import org.ops4j.pax.exam.karaf.options.LogLevelOption.LogLevel;
import org.ops4j.pax.exam.karaf.options.configs.LoggingCfg;

public class LogCategoryLevelOption extends KarafDistributionConfigurationFilePutOption {

    public LogCategoryLevelOption(String category, LogLevel level) {
        super(LoggingCfg.FILE_PATH, "log4j.logger." + category, level.toString());
    }

}
