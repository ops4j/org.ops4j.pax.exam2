/*
 * Copyright 2012 Harald Wellmann
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
package org.ops4j.pax.exam.options;

/**
 * 
 * @author Harald Wellmann
 */
public class UrlDeploymentOption implements DeploymentOption, UrlReference
{
    private UrlReference urlReference;
    private String name;
    private String contextRoot;
    
    /**
     * Constructor.
     *
     * @param url provision url (cannot be null or empty)
     *
     * @throws IllegalArgumentException - If url is null or empty
     */
    public UrlDeploymentOption( final String url )
    {
        this.urlReference = new RawUrlReference( url );
    }

    /**
     * Constructor.
     *
     * @param url provision url (cannot be null)
     *
     * @throws IllegalArgumentException - If url is null
     */
    public UrlDeploymentOption( final UrlReference url )
    {
        this.urlReference = url;
    }
    
    public UrlReference getUrlReference()
    {
        return urlReference;
    }
    
    public UrlDeploymentOption name( String name )
    {
        this.name = name;
        return this;
    }
    
    public String getName() 
    {
        return name;
    }

    public UrlDeploymentOption contextRoot( String contextRoot )
    {
        this.contextRoot = contextRoot;
        return this;
    }
    
    public String getContextRoot()
    {
        return contextRoot;
    }

    /**
     * {@inheritDoc}
     */
    protected UrlDeploymentOption itself()
    {
        return this;
    }

    @Override
    public String getURL()
    {
        return urlReference.getURL();
    }

}