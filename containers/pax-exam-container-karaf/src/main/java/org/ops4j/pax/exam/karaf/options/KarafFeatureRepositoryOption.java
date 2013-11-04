package org.ops4j.pax.exam.karaf.options;

import org.ops4j.pax.exam.karaf.options.configs.FeaturesCfg;

public class KarafFeatureRepositoryOption extends KarafDistributionConfigurationFileExtendOption {

    public KarafFeatureRepositoryOption(String url) {
        super(FeaturesCfg.REPOSITORIES, url);
    }

}
