/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ops4j.pax.exam.regression.karaf;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.editConfigurationFilePut;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.features;
import static org.ops4j.pax.exam.karaf.options.configs.FeaturesCfg.BOOT;
import static org.ops4j.pax.exam.regression.karaf.RegressionConfiguration.featureRepoStandard;
import static org.ops4j.pax.exam.regression.karaf.RegressionConfiguration.regressionDefaults;

import javax.inject.Inject;

import org.apache.karaf.features.BootFinished;
import org.apache.karaf.features.FeaturesService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;

@RunWith(PaxExam.class)
public class FeaturesAndBootFeaturesTest {

    @Inject
    private FeaturesService featuresService;

    @Inject
    BootFinished bootFinished;

    @Configuration
    public Option[] config() {
        return new Option[]{
            regressionDefaults(),
            editConfigurationFilePut(BOOT, "config, log, framework, feature"),
            features(featureRepoStandard(), "kar")
        };
    }

    @Test
    public void test() throws Exception {
        assertFalse("Feature management should not be installed",
                    featuresService.isInstalled(featuresService.getFeature("management")));
        assertTrue("Feature kar should be installed",
                   featuresService.isInstalled(featuresService.getFeature("kar")));
    }

}
