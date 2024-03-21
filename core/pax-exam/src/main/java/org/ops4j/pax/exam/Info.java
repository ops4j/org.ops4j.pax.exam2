/*
 * Copyright 2008 Toni Menzel
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.exam;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * A helper class to find versioning and other meta information about this pax exam delivery.
 * 
 * Fully static
 * 
 * @author Toni Menzel (tonit)
 * @since Jul 25, 2008
 */
public class Info {

    /**
     * Snapshot constant to avoid typos in analysing code.
     */
    private static final String SNAPSHOT = "SNAPSHOT";

    /**
     * Pax Exam version.
     */
    private static final String PAX_EXAM_VERSION;
    /**
     * Pax URL version.
     */
    private static final String PAX_URL_VERSION;
    /**
     * Ops4J Base libraries version
     */
    private static final String OPS4J_BASE_VERSION;
    /**
     * Pax Swissbox libraries version
     */
    private static final String PAX_SWISSBOX_VERSION;
    
    private static final String PAX_TINYBUNDLES_VERSION;
    
    private static final String ATINJECT_VERSION;
    
    /**
     * True if pax exam is a snapshot version.
     */
    private static boolean paxExamSnapshotVersion;
    /**
     * True if pax url is a snapshot version.
     */
    private static boolean paxUrlSnapshotVersion;
    /**
     * True if ops4j base is a snapshot verison.
     */
    private static boolean ops4jBaseSnapshotVersion;
    /**
     * True if pax swissbox is a snapshot version.
     */
    private static boolean paxSwissboxSnapshotVersion;

    /**
     * True if Pax Tinybundles is a snapshot version.
     */
    private static boolean paxTinybundlesSnapshotVersion;

    static {
        String paxExamVersion = "";
        String paxUrlVersion = "";
        String ops4jBaseVersion = "";
        String paxSwissboxVersion = "";
        String paxTinybundlesVersion = "";
        String atinjectVersion = "";
        try {
            final InputStream is = Info.class.getClassLoader().getResourceAsStream(
                "META-INF/pax-exam-version.properties");
            if (is != null) {
                final Properties properties = new Properties();
                properties.load(is);
                paxExamVersion = properties.getProperty("pax.exam.version", "").trim();
                paxUrlVersion = properties.getProperty("pax.url.version", "").trim();
                ops4jBaseVersion = properties.getProperty("ops4j.base.version", "").trim();
                paxSwissboxVersion = properties.getProperty("pax.swissbox.version").trim();
                paxTinybundlesVersion = properties.getProperty("pax.tinybundles.version").trim();
                atinjectVersion = properties.getProperty("atinject.version").trim();
            }
        }
        catch (IOException ignore) {
            // use default versions
        }
        PAX_EXAM_VERSION = paxExamVersion;
        PAX_URL_VERSION = paxUrlVersion;
        OPS4J_BASE_VERSION = ops4jBaseVersion;
        PAX_SWISSBOX_VERSION = paxSwissboxVersion;
        PAX_TINYBUNDLES_VERSION = paxTinybundlesVersion;
        ATINJECT_VERSION = atinjectVersion;
        paxExamSnapshotVersion = paxExamVersion.endsWith(SNAPSHOT);
        paxUrlSnapshotVersion = paxUrlVersion.endsWith(SNAPSHOT);
        ops4jBaseSnapshotVersion = ops4jBaseVersion.endsWith(SNAPSHOT);
        paxSwissboxSnapshotVersion = paxSwissboxVersion.endsWith(SNAPSHOT);
        paxTinybundlesSnapshotVersion = paxTinybundlesVersion.endsWith(SNAPSHOT);
    }

    /**
     * No instances should be made (does not make sense).
     */
    private Info() {

    }

    /**
     * Discovers the Pax Exam version. If version cannot be determined returns an empty string.
     * 
     * @return pax exam version
     */
    public static String getPaxExamVersion() {
        return PAX_EXAM_VERSION;
    }

    /**
     * Discovers the Pax Url version. If version cannot be determined returns an empty string.
     * 
     * @return pax url version
     */
    public static String getPaxUrlVersion() {
        return PAX_URL_VERSION;
    }

    /**
     * Discovers the Ops4j base version. If version cannot be determined returns an empty string.
     * 
     * @return the ops4j base version.
     */
    public static String getOps4jBaseVersion() {
        return OPS4J_BASE_VERSION;
    }

    /**
     * Discovers the Pax Swissbox version. If version cannot be determined returns an empty string.
     * 
     * @return pax swissbox version
     */
    public static String getPaxSwissboxVersion() {
        return PAX_SWISSBOX_VERSION;
    }

    /**
     * Discovers the Pax Tinybundles version. If version cannot be determined returns an empty string.
     * 
     * @return Pax Tinybundles version
     */
    public static String getPaxTinybundlesVersion() {
        return PAX_TINYBUNDLES_VERSION;
    }

    /**
     * Discovers the Jakarta (javax) Inject version. If version cannot be determined returns an empty string.
     * 
     * @return Pax Tinybundles version
     */
    public static String getAtinjectVersion() {
        return ATINJECT_VERSION;
    }

    /**
     * Getter.
     * 
     * @return true if pax exam is a snapshot version, false otherwise
     */
    public static boolean isPaxExamSnapshotVersion() {
        return paxExamSnapshotVersion;
    }

    /**
     * Getter.
     * 
     * @return true if pax url is a snapshot version, false otherwise
     */
    public static boolean isPaxUrlSnapshotVersion() {
        return paxUrlSnapshotVersion;
    }

    /**
     * Getter.
     * 
     * @return true if ops4j base is a snapshot version, false otherwise
     */
    public static boolean isOps4jBaseSnapshotVersion() {
        return ops4jBaseSnapshotVersion;
    }

    /**
     * Getter.
     * 
     * @return true if pax swissbox is a snapshot version, false otherwise.
     */
    public static boolean isPaxSwissboxSnapshotVersion() {
        return paxSwissboxSnapshotVersion;
    }

    /**
     * Getter.
     * 
     * @return true if pax swissbox is a snapshot version, false otherwise.
     */
    public static boolean isPaxTinybundlesSnapshotVersion() {
        return paxTinybundlesSnapshotVersion;
    }

    /**
     * Display ops4j logo to console.
     */
    public static void showLogo() {
        System.out.println("__________                 ___________");
        System.out.println("\\______   \\_____  ___  ___ \\_   _____/__  ________    _____");
        System.out.println(" |     ___/\\__  \\ \\  \\/  /  |    __)_\\  \\/  /\\__  \\  /     \\");
        System.out.println(" |    |     / __ \\_>    <   |        \\>    <  / __ \\|  Y Y  \\");
        System.out.println(" |____|    (____  /__/\\_ \\ /_______  /__/\\_ \\(____  /__|_|  /");
        System.out.println("                \\/      \\/         \\/      \\/     \\/      \\/");

        System.out.println();
        final String logo = "Pax Exam " + Info.getPaxExamVersion()
            + " from OPS4J - https://github.com/ops4j";
        System.out.println(logo);
        System.out
            .println("---------------------------------------------------------------------------------------------------------"
                .substring(0, logo.length()));
        System.out.println();
    }
}
