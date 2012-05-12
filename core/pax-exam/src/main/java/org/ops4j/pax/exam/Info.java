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
public class Info
{

    /**
     * Pax Exam version.
     */
    private static final String m_paxExamVersion;
    /**
     * Pax URL version.
     */
    private static final String m_paxUrlVersion;
    /**
     * Pax Runner version.
     */
    private static final String m_paxRunnerVersion;
    /**
     * True if pax exam is a snapshot version.
     */
    private static boolean m_paxExamSnapshotVersion;
    /**
     * True if pax url is a snapshot version.
     */
    private static boolean m_paxUrlSnapshotVersion;

    static
    {
        String paxExamVersion = "";
        String paxUrlVersion = "";
        String paxRunnerVersion = "";
        try
        {
            final InputStream is = Info.class.getClassLoader().getResourceAsStream(
                "META-INF/pax-exam-version.properties"
            );
            if( is != null )
            {
                final Properties properties = new Properties();
                properties.load( is );
                paxExamVersion = properties.getProperty( "pax.exam.version", "" ).trim();
                paxUrlVersion = properties.getProperty( "pax.url.version", "" ).trim();
                paxRunnerVersion = properties.getProperty( "pax.runner.version", "" ).trim();
            }
        }
        catch( Exception ignore )
        {
            // use default versions
        }
        m_paxExamVersion = paxExamVersion;
        m_paxUrlVersion = paxUrlVersion;
        m_paxRunnerVersion = paxRunnerVersion;
        m_paxExamSnapshotVersion = paxExamVersion.endsWith( "SNAPSHOT" );
        m_paxUrlSnapshotVersion = paxUrlVersion.endsWith( "SNAPSHOT" );
    }

    /**
     * No instances should be made (does not make sense).
     */
    private Info()
    {

    }

    /**
     * Discovers the Pax Exam version. If version cannot be determined returns an empty string.
     *
     * @return pax exam version
     */
    public static String getPaxExamVersion()
    {
        return m_paxExamVersion;
    }

    /**
     * Discovers the Pax Url version. If version cannot be determined returns an empty string.
     *
     * @return pax url version
     */
    public static String getPaxUrlVersion()
    {
        return m_paxUrlVersion;
    }

    /**
     * Discovers the Pax Runner version. If version cannot be determined returns an empty string.
     *
     * @return pax runner version
     */
    public static String getPaxRunnerVersion()
    {
        return m_paxRunnerVersion;
    }

    /**
     * Getter.
     *
     * @return true if pax exam is a snapshot version, false otherwise
     */
    public static boolean isPaxExamSnapshotVersion()
    {
        return m_paxExamSnapshotVersion;
    }

    /**
     * Getter.
     *
     * @return true if pax url is a snapshot version, false otherwise
     */
    public static boolean isPaxUrlSnapshotVersion()
    {
        return m_paxUrlSnapshotVersion;
    }

    /**
     * Display ops4j logo to console.
     */
    public static void showLogo()
    {
        System.out.println( "__________                 ___________" );
        System.out.println( "\\______   \\_____  ___  ___ \\_   _____/__  ________    _____" );
        System.out.println( " |     ___/\\__  \\ \\  \\/  /  |    __)_\\  \\/  /\\__  \\  /     \\" );
        System.out.println( " |    |     / __ \\_>    <   |        \\>    <  / __ \\|  Y Y  \\" );
        System.out.println( " |____|    (____  /__/\\_ \\ /_______  /__/\\_ \\(____  /__|_|  /" );
        System.out.println( "                \\/      \\/         \\/      \\/     \\/      \\/" );

        System.out.println();
        final String logo = "Pax Exam " + Info.getPaxExamVersion() + " from OPS4J - http://www.ops4j.org";
        System.out.println( logo );
        System.out.println(
            "---------------------------------------------------------------------------------------------------------"
                .substring( 0, logo.length() )
        );
        System.out.println();
    }

}
