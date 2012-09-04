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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.ops4j.pax.exam.Option;

public class WarProbeOption implements Option
{
    private static String[] classPathExcludes = {
        ".cp",
        "org.eclipse.osgi",
        "scattered-archive-api",
        "simple-glassfish-api",
        "jersey-client",
        "pax-exam-container",
        "tinybundles",
        "geronimo-atinject",
        "servlet-api",
        "tomcat-",
        "jboss-log",
        "bndlib"
    };

    private List<String> overlays;
    private List<String> libraries;
    private List<String> resources;
    private List<Class<?>> classes;
    private List<String> metaInfResources;
    private List<String> webInfResources;
    private List<String> classpathFilters;
    private boolean useClasspath;
    private String name;

    public WarProbeOption()
    {
        overlays = new ArrayList<String>();
        libraries = new ArrayList<String>();
        classes = new ArrayList<Class<?>>();
        resources = new ArrayList<String>();
        metaInfResources = new ArrayList<String>();
        webInfResources = new ArrayList<String>();
        classpathFilters = new ArrayList<String>();
    }

    public WarProbeOption name( String name )
    {
        this.name = name;
        return this;
    }

    public WarProbeOption library( String libraryPath )
    {
        libraries.add( new File( libraryPath ).toURI().toString() );
        return this;
    }

    public WarProbeOption library( UrlReference libraryUrl )
    {
        libraries.add( libraryUrl.getURL() );
        return this;
    }

    public WarProbeOption overlay( String overlayPath )
    {
        overlays.add( new File( overlayPath ).toURI().toString() );
        return this;
    }

    public WarProbeOption overlay( UrlReference overlayUrl )
    {
        overlays.add( overlayUrl.getURL() );
        return this;
    }

    public WarProbeOption classes( Class<?>... klass )
    {
        for( Class<?> c : klass )
        {
            String resource = c.getName().replaceAll( "\\.", "/" ) + ".class";
            resources.add( resource );
        }
        return this;
    }

    public WarProbeOption resources( String... resourcePath )
    {
        for( String resource : resourcePath )
        {
            resources.add( resource );
        }
        return this;
    }

    public WarProbeOption metaInfResource( String resourcePath )
    {
        metaInfResources.add( resourcePath );
        return this;
    }

    public WarProbeOption webInfResource( String resourcePath )
    {
        webInfResources.add( resourcePath );
        return this;
    }

    public WarProbeOption autoClasspath( boolean includeDefaultFilters )
    {
        useClasspath = true;
        if( includeDefaultFilters )
        {
            for( String filter : classPathExcludes )
            {
                classpathFilters.add( filter );
            }
        }
        return this;
    }

    public WarProbeOption classPathDefaultExcludes()
    {
        useClasspath = true;
        return autoClasspath( true );
    }

    public WarProbeOption exclude( String... excludeRegExp )
    {
        useClasspath = true;
        for( String exclude : excludeRegExp )
        {
            classpathFilters.add( exclude );
        }
        return this;
    }

    public String getName()
    {
        return name;
    }

    public boolean isClassPathEnabled()
    {
        return useClasspath;
    }

    public List<String> getLibraries()
    {
        return libraries;
    }

    public List<String> getOverlays()
    {
        return overlays;
    }

    public List<String> getClassPathFilters()
    {
        return classpathFilters;
    }

    public List<String> getMetaInfResources()
    {
        return metaInfResources;
    }

    public List<String> getWebInfResources()
    {
        return webInfResources;
    }

    public List<String> getResources()
    {
        return resources;
    }

    public List<Class<?>> getClasses()
    {
        return classes;
    }
}
