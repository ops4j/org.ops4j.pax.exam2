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
package org.ops4j.pax.exam.karaf.container.internal;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.ops4j.pax.exam.Info;

public class ExamFeaturesFile {

    private String featuresXml;

    public ExamFeaturesFile() {
        this("", Constants.DEFAULT_START_LEVEL);
    }

    public ExamFeaturesFile(String featuresXml) {
        this(featuresXml, Constants.DEFAULT_START_LEVEL);
    }

    public ExamFeaturesFile(String extension, int startLevel) {
        featuresXml =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                        + "<features name=\"pax-exam-features-"
                        + Info.getPaxExamVersion()
                        + "\" xmlns=\"http://karaf.apache.org/xmlns/features/v1.0.0\">\n"
                        + "<feature name=\"exam\" version=\""
                        + Info.getPaxExamVersion()
                        + "\">\n"
                        + extension + "\n"
                        + "<bundle start-level='" + startLevel + "'>mvn:org.ops4j.base/ops4j-base-lang/"
                        + Info.getOps4jBaseVersion()
                        + "</bundle>\n"
                        + "<bundle start-level='" + startLevel + "'>mvn:org.ops4j.base/ops4j-base-monitors/"
                        + Info.getOps4jBaseVersion()
                        + "</bundle>\n"
                        + "<bundle start-level='" + startLevel + "'>mvn:org.ops4j.base/ops4j-base-net/"
                        + Info.getOps4jBaseVersion()
                        + "</bundle>\n"
                        + "<bundle start-level='" + startLevel + "'>mvn:org.ops4j.base/ops4j-base-store/"
                        + Info.getOps4jBaseVersion()
                        + "</bundle>\n"
                        + "<bundle start-level='" + startLevel + "'>mvn:org.ops4j.base/ops4j-base-io/"
                        + Info.getOps4jBaseVersion()
                        + "</bundle>\n"
                        + "<bundle start-level='" + startLevel + "'>mvn:org.ops4j.base/ops4j-base-spi/"
                        + Info.getOps4jBaseVersion()
                        + "</bundle>\n"
                        + "<bundle start-level='" + startLevel + "'>mvn:org.ops4j.base/ops4j-base-util-property/"
                        + Info.getOps4jBaseVersion()
                        + "</bundle>\n"
                        + "<bundle start-level='" + startLevel + "'>mvn:org.ops4j.pax.swissbox/pax-swissbox-core/"
                        + Info.getPaxSwissboxVersion()
                        + "</bundle>\n"
                        + "<bundle start-level='" + startLevel + "'>mvn:org.ops4j.pax.swissbox/pax-swissbox-extender/"
                        + Info.getPaxSwissboxVersion()
                        + "</bundle>\n"
                        + "<bundle start-level='" + startLevel + "'>mvn:org.ops4j.pax.swissbox/pax-swissbox-lifecycle/"
                        + Info.getPaxSwissboxVersion()
                        + "</bundle>\n"
                        + "<bundle start-level='" + startLevel + "'>mvn:org.ops4j.pax.swissbox/pax-swissbox-tracker/"
                        + Info.getPaxSwissboxVersion()
                        + "</bundle>\n"
                        + "<bundle start-level='" + startLevel + "'>mvn:org.ops4j.pax.swissbox/pax-swissbox-framework/"
                        + Info.getPaxSwissboxVersion()
                        + "</bundle>\n"
                        + "<bundle start-level='" + startLevel + "'>mvn:org.ops4j.pax.exam/pax-exam/"
                        + Info.getPaxExamVersion()
                        + "</bundle>\n"
                        + "<bundle start-level='" + startLevel + "'>mvn:org.ops4j.pax.exam/pax-exam-extender-service/"
                        + Info.getPaxExamVersion()
                        + "</bundle>\n"
                        + "<bundle start-level='" + startLevel + "'>mvn:org.ops4j.pax.exam/pax-exam-container-rbc/"
                        + Info.getPaxExamVersion()
                        + "</bundle>\n"
                        + "<bundle start-level='" + startLevel + "'>mvn:org.ops4j.pax.tipi/org.ops4j.pax.tipi.hamcrest.core/1.3.0.1" 
                        + "</bundle>\n"
                        + "<bundle start-level='" + startLevel + "'>mvn:org.ops4j.pax.tipi/org.ops4j.pax.tipi.junit/4.11.0.1" 
                        + "</bundle>\n"
                        + "<bundle start-level='" + startLevel + "'>mvn:org.ops4j.pax.exam/pax-exam-invoker-junit/"
                        + Info.getPaxExamVersion()
                        + "</bundle>\n"
                        + "<bundle start-level='" + startLevel
                        + "'>mvn:org.apache.geronimo.specs/geronimo-atinject_1.0_spec/" + Info.getAtinjectVersion()
                        + "</bundle>\n"
                        + "<bundle start-level='" + startLevel + "'>mvn:org.ops4j.pax.exam/pax-exam-inject/"
                        + Info.getPaxExamVersion() + "</bundle>\n"
                        + "</feature>\n"
                        + "</features>";
    }

    public void writeToFile(File featuresXmlFile) throws IOException {
        FileUtils.writeStringToFile(featuresXmlFile, featuresXml);
    }

    public void adaptDistributionToStartExam(File karafHome, File featuresXmlFile) throws IOException {
        KarafPropertiesFile karafPropertiesFile = new KarafPropertiesFile(karafHome, Constants.FEATURES_CFG_LOCATION);
        karafPropertiesFile.load();
        String finalFilePath = "file:" + featuresXmlFile.toString().replaceAll("\\\\", "/").replaceAll(" ", "%20");
        karafPropertiesFile.extend("featuresRepositories", finalFilePath);
        karafPropertiesFile.extend("featuresBoot", "exam");
        karafPropertiesFile.store();
    }

}
