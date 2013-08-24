package org.ops4j.pax.exam.karaf.options;

import org.ops4j.pax.exam.karaf.options.configs.FeaturesCfg;

public class KarafBootFeatureOption extends KarafDistributionConfigurationFileExtendOption {

    public KarafBootFeatureOption(String name) {
        super(FeaturesCfg.BOOT, name);
    }

    public KarafBootFeatureOption(String name, String version) {
        super(FeaturesCfg.BOOT, name + ";version=" + version);
    }
}
