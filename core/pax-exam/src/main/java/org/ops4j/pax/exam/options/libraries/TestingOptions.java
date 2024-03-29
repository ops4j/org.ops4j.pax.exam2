/*
 * Copyright 2024 Oliver Lietz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.exam.options.libraries;

import org.ops4j.pax.exam.options.ModifiableCompositeOption;

import static org.ops4j.pax.exam.CoreOptions.composite;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;

public class TestingOptions {

    private TestingOptions() { //
    }

    public static ModifiableCompositeOption testng() {
        return composite(
            mavenBundle().groupId("org.testng").artifactId("testng").version("7.9.0"),
            mavenBundle().groupId("org.jcommander").artifactId("jcommander").version("1.83")
        );
    }

    public static ModifiableCompositeOption config() {
        return composite(
            mavenBundle().groupId("org.apache.felix").artifactId("org.apache.felix.configadmin").version("1.9.26")
        );
    }

    public static ModifiableCompositeOption paxUrl() {
        return composite(
            mavenBundle().groupId("org.ops4j.pax.url").artifactId("pax-url-commons").version("2.6.14"),
            mavenBundle().groupId("org.ops4j.base").artifactId("ops4j-base-lang").version("1.5.1"),
            mavenBundle().groupId("org.ops4j.base").artifactId("ops4j-base-util-property").version("1.5.1"),
            mavenBundle().groupId("org.ops4j.pax.swissbox").artifactId("pax-swissbox-property").version("1.8.5"),
            config()
        );
    }

    public static ModifiableCompositeOption paxUrlWrap() {
        return composite(
            mavenBundle().groupId("org.ops4j.pax.url").artifactId("pax-url-wrap").version("2.6.14"),
            mavenBundle().groupId("org.ops4j.pax.swissbox").artifactId("pax-swissbox-bnd").version("1.8.5"),
            mavenBundle().groupId("biz.aQute.bnd").artifactId("bndlib").version("2.4.0"),
            paxUrl()
        );
    }

    public static ModifiableCompositeOption scr() {
        return composite(
            mavenBundle().groupId("org.apache.felix").artifactId("org.apache.felix.configadmin").version("1.9.26"),
            mavenBundle().groupId("org.apache.felix").artifactId("org.apache.felix.metatype").version("1.2.4"),
            mavenBundle().groupId("org.apache.felix").artifactId("org.apache.felix.scr").version("2.2.10"),
            mavenBundle().groupId("org.osgi").artifactId("org.osgi.service.component").version("1.5.1"),
            mavenBundle().groupId("org.osgi").artifactId("org.osgi.util.function").version("1.2.0"),
            mavenBundle().groupId("org.osgi").artifactId("org.osgi.util.promise").version("1.3.0")
        );
    }

}
