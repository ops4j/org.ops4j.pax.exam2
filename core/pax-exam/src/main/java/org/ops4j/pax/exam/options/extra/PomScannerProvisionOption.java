/*
 * Copyright 2009 Toni Menzel.
 * Copyright 2009 Alin Dreghiciu.
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
package org.ops4j.pax.exam.options.extra;

import static org.ops4j.lang.NullArgumentException.*;
import static org.ops4j.pax.exam.options.extra.ScannerUtils.*;

import org.ops4j.pax.exam.options.AbstractProvisionOption;

/**
 * @deprecated Only supported by Pax Runner Container which will be removed in Pax Exam 3.0.
 * @author Toni Menzel (tonit)
 * @author Alin Dreghiciu (adreghiciu@gmail.com)
 * @since Mar 7, 2009
 */
@Deprecated
public class PomScannerProvisionOption
    extends AbstractProvisionOption<PomScannerProvisionOption>
    implements Scanner
{

    /**
     * url. can be null. then parts (groupid,artefactid) must be filled.
     * If url is not null, this will be used. (url has priority over artefact/group)
     */
    private String m_url;

    /**
     * artifactId part of maven style provisioning (will be part of mvn url being constructed)
     */
    private String m_artifact;

    /**
     * groupId part of maven style provisioning (will be part of mvn url being constructed)
     */
    private String m_groupId;

    /**
     * version part of maven style provisioning (will be part of mvn url being constructed)
     */
    private String m_version = "";

    /**
     * Constructor.
     *
     * @param url directory to be scanned path (cannot be null or empty)
     *
     * @throws IllegalArgumentException - If url is null or empty
     */
    public PomScannerProvisionOption( final String url )
    {
        validateNotEmpty( url, true, "url" );
        m_url = url;
    }

    /**
     * Constructor.
     */
    public PomScannerProvisionOption()
    {

    }

    /**
     * {@inheritDoc}
     */
    public String getURL()
    {
        if( m_url == null )
        {
            m_url = "mvn:" + m_groupId + "/" + m_artifact + "/" + m_version + "/pom";
        }
        final StringBuilder url = new StringBuilder().append( "scan-pom" ).append( ":" ).append( m_url );
        url.append( getOptions( this ) );
        return url.toString();
    }

    public PomScannerProvisionOption artifactId( String s )
    {
        m_artifact = s;
        return this;
    }

    public PomScannerProvisionOption groupId( String s )
    {
        m_groupId = s;
        return this;
    }

    public PomScannerProvisionOption version( String s )
    {
        m_version = s;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder();
        sb.append( "PomScannerProvisionOption" );
        sb.append( "{url='" ).append( getURL() ).append( '\'' );
        sb.append( '}' );
        return sb.toString();
    }

    /**
     * {@inheritDoc}
     */
    protected PomScannerProvisionOption itself()
    {
        return this;
    }
}