/*
 * Copyright 2008 Alin Dreghiciu.
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
package org.ops4j.pax.exam;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import static org.ops4j.lang.NullArgumentException.*;
import static org.ops4j.pax.exam.OptionUtils.*;
import org.ops4j.pax.exam.options.BootClasspathLibraryOption;
import org.ops4j.pax.exam.options.BootDelegationOption;
import org.ops4j.pax.exam.options.BundleStartLevelOption;
import org.ops4j.pax.exam.options.CustomFrameworkOption;
import org.ops4j.pax.exam.options.DefaultCompositeOption;
import org.ops4j.pax.exam.options.EquinoxFrameworkOption;
import org.ops4j.pax.exam.options.FelixFrameworkOption;
import org.ops4j.pax.exam.options.FrameworkOption;
import org.ops4j.pax.exam.options.FrameworkStartLevelOption;
import org.ops4j.pax.exam.options.KnopflerfishFrameworkOption;
import org.ops4j.pax.exam.options.MavenArtifactProvisionOption;
import org.ops4j.pax.exam.options.MavenArtifactUrlReference;
import org.ops4j.pax.exam.options.MavenPluginGeneratedConfigOption;
import org.ops4j.pax.exam.options.OptionalCompositeOption;
import org.ops4j.pax.exam.options.ProvisionOption;
import org.ops4j.pax.exam.options.RawUrlReference;
import org.ops4j.pax.exam.options.SystemPackageOption;
import org.ops4j.pax.exam.options.SystemPropertyOption;
import org.ops4j.pax.exam.options.TestContainerStartTimeoutOption;
import org.ops4j.pax.exam.options.UrlProvisionOption;
import org.ops4j.pax.exam.options.UrlReference;
import org.ops4j.pax.exam.options.WrappedUrlProvisionOption;
import org.ops4j.pax.exam.options.DebugClassLoadingOption;
import org.ops4j.store.Store;
import org.ops4j.store.StoreFactory;

/**
 * Factory methods for core options.
 *
 * @author Alin Dreghiciu (adreghiciu@gmail.com)
 * @author Toni Menzel (toni@okidokiteam.com
 * @since 0.3.0, December 08, 2008
 */
public class CoreOptions
{

    private static final String DEFAULT_CONFIGURATION = "META-INF/maven/paxexam-config.args";

    /**
     * Utility class. Ment to be used via the static factory methods.
     */
    private CoreOptions()
    {
        // utility class
    }

    /**
     * Convenience method (more to be used for a nice fluent api) for creating an array of options.
     * It also expands the composite options.
     *
     * @param options options
     *
     * @return provided options, expanded
     *
     * @see OptionUtils#expand(Option...)
     */
    public static Option[] options( final Option... options )
    {
        return expand( options );
    }

    /**
     * Convenience method (more to be used for a nice fluent api) for creating a composite option.
     *
     * @param options options
     *
     * @return provided options
     */
    public static Option composite( final Option... options )
    {
        return new DefaultCompositeOption( options );
    }

    /**
     * Creates a composite option of {@link FrameworkOption}s.
     *
     * @param frameworks framework options
     *
     * @return composite option of framework options
     */
    public static Option frameworks( final FrameworkOption... frameworks )
    {
        return composite( frameworks );
    }

    /**
     * Creates a {@link FelixFrameworkOption}.
     *
     * @return felix framework option
     */
    public static FelixFrameworkOption felix()
    {
        return new FelixFrameworkOption();
    }

    /**
     * Creates a composite option of all {@link FelixFrameworkOption} versions (up to the date of Pax Exam release).
     *
     * @return all felix framework versions composite option
     */
    public static Option allFelixVersions()
    {
        return composite(
            felix().version( "1.0.0" ),
            felix().version( "1.0.1" ),
            felix().version( "1.0.3" ),
            felix().version( "1.0.4" ),
            felix().version( "1.2.0" ),
            felix().version( "1.2.1" ),
            felix().version( "1.2.2" ),
            felix().version( "1.4.0" ),
            felix().version( "1.4.1" ),
            felix().version( "1.6.0" ),
            felix().version( "1.6.1" ),
            felix().version( "1.8.0" ),
            felix().version( "1.8.1" ),
            felix().version( "2.0.0" ),
            felix().version( "2.0.1" )

        );
    }

    /**
     * Creates a {@link EquinoxFrameworkOption}.
     *
     * @return equinox framework option
     */
    public static EquinoxFrameworkOption equinox()
    {
        return new EquinoxFrameworkOption();
    }

    /**
     * Creates a composite option of all {@link EquinoxFrameworkOption} versions (up to the date of Pax Exam release).
     *
     * @return all equinox framework versions composite option
     */
    public static Option allEquinoxVersions()
    {
        return composite(
            equinox().version( "3.2.1" ),
            equinox().version( "3.3.0" ),
            equinox().version( "3.3.1" ),
            equinox().version( "3.3.2" ),
            equinox().version( "3.4.0" ),
            equinox().version( "3.4.1" ),
            equinox().version( "3.4.2" ),
            equinox().version( "3.5.0" ),
            equinox().version( "3.5.1" )
        );
    }

    /**
     * Creates a {@link KnopflerfishFrameworkOption}.
     *
     * @return knopflerfish framework option
     */
    public static KnopflerfishFrameworkOption knopflerfish()
    {
        return new KnopflerfishFrameworkOption();
    }

    /**
     * Creates a composite option of all {@link KnopflerfishFrameworkOption} versions (up to the date of Pax Exam
     * release).
     *
     * @return all knopflerfish framework versions composite option
     */
    public static Option allKnopflerfishVersions()
    {
        return composite(
            //exclude version 2.0.0 as it looks like it has an internal problem of NPE in BundlePackages.java:266 while
            //looking for annotations from CallableTestMethodImpl.getAnnotatedMethods
            //knopflerfish().version( "2.0.0" ),
            knopflerfish().version( "2.0.1" ),
            knopflerfish().version( "2.0.2" ),
            knopflerfish().version( "2.0.3" ),
            knopflerfish().version( "2.0.4" ),
            knopflerfish().version( "2.0.5" ),
            knopflerfish().version( "2.1.0" ),
            knopflerfish().version( "2.1.1" ),
            knopflerfish().version( "2.2.0" ),
            knopflerfish().version( "2.3.0" ),
            knopflerfish().version( "2.3.1" ),
            knopflerfish().version( "2.3.2" ),
            knopflerfish().version( "2.3.3" )
        );
    }

    /**
     * Creates a composite option of latest versions of Felix, Equinox and Knopflerfish.
     *
     * @return latest versions of all frameworks composite option
     */
    public static Option allFrameworks()
    {
        return composite(
            felix(),
            equinox(),
            knopflerfish()
        );
    }

    /**
     * Creates a composite option of all versions of Felix, Equinox and Knopflerfish.
     *
     * @return all framework versions composite option
     */
    public static Option allFrameworksVersions()
    {
        return composite(
            allFelixVersions(),
            allEquinoxVersions(),
            allKnopflerfishVersions()
        );
    }

    /**
     *
     * @param baseFrameworkName one of the frameworks supported by pax runner. Must match the framework you link to via #definitionURL
     * @param definitionURL url to a custom pax runner platform xml descriptor
     * @param name a custom name you give this combo (will show up in pax runner startup and error msg)
     * @return instance of CustomFrameworkOption
     */
    public static CustomFrameworkOption customFramework( String baseFrameworkName, String definitionURL, String name )
    {
        return new CustomFrameworkOption( definitionURL, baseFrameworkName,name );
    }

    /**
     * Creates a composite option of {@link ProvisionOption}s.
     *
     * @param urls provision urls (cannot be null or containing null entries)
     *
     * @return composite option of provision options
     *
     * @throws IllegalArgumentException - If urls array is null or contains null entries
     */
    public static Option provision( final String... urls )
    {
        validateNotEmptyContent( urls, true, "URLs" );
        final List<ProvisionOption> options = new ArrayList<ProvisionOption>();
        for( String url : urls )
        {
            options.add( new UrlProvisionOption( url ) );
        }
        return provision( options.toArray( new ProvisionOption[options.size()] ) );
    }

    /**
     * Creates a composite option of {@link ProvisionOption}s.
     * This is handy when bundles are built on the fly via TinyBundles.
     *
     * @param streams provision sources
     *
     * @return composite option of provision options
     *
     * @throws IllegalArgumentException - If a problem occured while flushing streams
     */
    public static Option provision( final InputStream... streams )
    {
        validateNotNull( streams, "streams" );
        //TODO make the store more global to the exam session to control caching load + shutdown.
        // For now we do it fully3 locally:
        Store<InputStream> store = StoreFactory.defaultStore();

        final List<ProvisionOption> options = new ArrayList<ProvisionOption>();
        for( InputStream stream : streams )
        {
            try
            {
                options.add( new UrlProvisionOption( store.getLocation( store.store( stream ) ).toURL().toExternalForm() ) );
            } catch( IOException e )
            {
                throw new IllegalArgumentException( "A supplied stream blew up..", e );
            }
        }
        return provision( options.toArray( new ProvisionOption[options.size()] ) );
    }

    /**
     * Creates a composite option of {@link ProvisionOption}s.
     *
     * @param urls provision options
     *
     * @return composite option of provision options
     */
    public static Option provision( final ProvisionOption... urls )
    {
        return composite( urls );
    }

    /**
     * Creates a {@link UrlProvisionOption}.
     *
     * @param url url as a string
     *
     * @return url reference
     */
    public static UrlProvisionOption url( final String url )
    {
        return new UrlProvisionOption( url );
    }

    /**
     * Creates a {@link UrlProvisionOption}.
     *
     * @param url bundle url
     *
     * @return url provisioning option
     */
    public static UrlProvisionOption bundle( final String url )
    {
        return new UrlProvisionOption( url );
    }

    /**
     * Creates a {@link org.ops4j.pax.exam.options.MavenArtifactUrlReference}.
     *
     * @return maven artifact url
     */
    public static MavenArtifactUrlReference maven()
    {
        return new MavenArtifactUrlReference();
    }

    /**
     * Convenience method (shorter) for referencing an maven artifact based on groupId/artifactId.
     *
     * @param groupId    artifact group id
     * @param artifactId artifact id
     *
     * @return maven artifact url
     */
    public static MavenArtifactUrlReference maven( final String groupId,
                                                   final String artifactId )
    {
        return maven().groupId( groupId ).artifactId( artifactId );
    }

    /**
     * Convenience method (shorter) for referencing a maven artifact based on groupId/artifactId/version.
     *
     * @param groupId    artifact group id
     * @param artifactId artifact id
     * @param version    artifact version
     *
     * @return maven artifact url
     */
    public static MavenArtifactUrlReference maven( final String groupId,
                                                   final String artifactId,
                                                   final String version )
    {
        return maven().groupId( groupId ).artifactId( artifactId ).version( version );
    }

    /**
     * Creates a {@link MavenArtifactProvisionOption}.
     *
     * @return maven specific provisioning option
     */
    public static MavenArtifactProvisionOption mavenBundle()
    {
        return new MavenArtifactProvisionOption();
    }

    /**
     * Convenience method (shorter) for adding a maven bundle based on groupId/artifactId.
     *
     * @param groupId    artifact group id
     * @param artifactId artifact id
     *
     * @return maven specific provisioning option
     */
    public static MavenArtifactProvisionOption mavenBundle( final String groupId,
                                                            final String artifactId )
    {
        return mavenBundle().groupId( groupId ).artifactId( artifactId );
    }

    /**
     * Convenience method (shorter) for adding a maven bundle based on groupId/artifactId/version.
     *
     * @param groupId    artifact group id
     * @param artifactId artifact id
     * @param version    artifact version
     *
     * @return maven specific provisioning option
     */
    public static MavenArtifactProvisionOption mavenBundle( final String groupId,
                                                            final String artifactId,
                                                            final String version )
    {
        return mavenBundle().groupId( groupId ).artifactId( artifactId ).version( version );
    }

    /**
     * Convenience factory method for adding a maven bundle based on a meven artifact.
     *
     * @param artifact maven artifact
     *
     * @return maven specific provisioning option
     */
    public static MavenArtifactProvisionOption mavenBundle( final MavenArtifactUrlReference artifact )
    {
        return new MavenArtifactProvisionOption( artifact );
    }

    /**
     * Creates a {@link WrappedUrlProvisionOption}.
     *
     * @param jarToWrapUrl url of jar to be wrapped
     *
     * @return wrap specific provisioning option
     */
    public static WrappedUrlProvisionOption wrappedBundle( final String jarToWrapUrl )
    {
        return new WrappedUrlProvisionOption( jarToWrapUrl );
    }

    /**
     * Creates a {@link WrappedUrlProvisionOption}.
     *
     * @param jarToWrapUrl url of jar to be wrapped
     *
     * @return wrap specific provisioning option
     */
    public static WrappedUrlProvisionOption wrappedBundle( final UrlReference jarToWrapUrl )
    {
        return new WrappedUrlProvisionOption( jarToWrapUrl );
    }

    /**
     * Creates a composite option of {@link BootDelegationOption}s.
     *
     * @param packages boot delegation packages (cannot be null or containing null entries)
     *
     * @return composite option of boot delegation package options
     *
     * @throws IllegalArgumentException - If urls array is null or contains null entries
     */
    public static Option bootDelegationPackages( final String... packages )
    {
        validateNotEmptyContent( packages, true, "Packages" );
        final List<BootDelegationOption> options = new ArrayList<BootDelegationOption>();
        for( String pkg : packages )
        {
            options.add( bootDelegationPackage( pkg ) );
        }
        return bootDelegationPackages( options.toArray( new BootDelegationOption[options.size()] ) );
    }

    /**
     * Creates a composite option of {@link BootDelegationOption}s.
     *
     * @param packages boot delegation package options
     *
     * @return composite option of boot delegation package options
     */
    public static Option bootDelegationPackages( final BootDelegationOption... packages )
    {
        return composite( packages );
    }

    /**
     * Creates a {@link BootDelegationOption}.
     *
     * @param pkg boot delegation package
     *
     * @return boot delegation package option
     */
    public static BootDelegationOption bootDelegationPackage( final String pkg )
    {
        return new BootDelegationOption( pkg );
    }

    /**
     * Creates a composite option of {@link org.ops4j.pax.exam.options.BootClasspathLibraryOption}s.
     *
     * @param urls boot classpath library urls (cannot be null or containing null entries)
     *
     * @return composite option of boot classpath options
     *
     * @throws IllegalArgumentException - If urls array is null or contains null entries
     */
    public static Option bootClasspathLibraries( final String... urls )
    {
        validateNotEmptyContent( urls, true, "Urls" );
        final List<BootClasspathLibraryOption> options = new ArrayList<BootClasspathLibraryOption>();
        for( String url : urls )
        {
            options.add( bootClasspathLibrary( url ) );
        }
        return bootClasspathLibraries( options.toArray( new BootClasspathLibraryOption[options.size()] ) );
    }

    /**
     * Creates a composite option of {@link org.ops4j.pax.exam.options.BootClasspathLibraryOption}s.
     *
     * @param libraries boot classpath library options
     *
     * @return composite option of boot classpath library  options
     */
    public static Option bootClasspathLibraries( final BootClasspathLibraryOption... libraries )
    {
        return composite( libraries );
    }

    /**
     * Creates a {@link org.ops4j.pax.exam.options.BootClasspathLibraryOption}.
     *
     * @param libraryUrl boot classpath library url
     *
     * @return boot classpath option
     */
    public static BootClasspathLibraryOption bootClasspathLibrary( final String libraryUrl )
    {
        return new BootClasspathLibraryOption( libraryUrl );
    }

    /**
     * Creates a {@link org.ops4j.pax.exam.options.BootClasspathLibraryOption}.
     *
     * @param libraryUrl boot classpath library url
     *
     * @return boot classpath option
     */
    public static BootClasspathLibraryOption bootClasspathLibrary( final UrlReference libraryUrl )
    {
        return new BootClasspathLibraryOption( libraryUrl );
    }

    /**
     * Creates a composite option of {@link SystemPackageOption}s.
     *
     * @param packages system packages (cannot be null or containing null entries)
     *
     * @return composite option of system package options
     *
     * @throws IllegalArgumentException - If urls array is null or contains null entries
     */
    public static Option systemPackages( final String... packages )
    {
        validateNotEmptyContent( packages, true, "Packages" );
        final List<SystemPackageOption> options = new ArrayList<SystemPackageOption>();
        for( String pkg : packages )
        {
            options.add( systemPackage( pkg ) );
        }
        return systemPackages( options.toArray( new SystemPackageOption[options.size()] ) );
    }

    /**
     * Creates a composite option of {@link SystemPackageOption}s.
     *
     * @param packages system package options
     *
     * @return composite option of system package options
     */
    public static Option systemPackages( final SystemPackageOption... packages )
    {
        return composite( packages );
    }

    /**
     * Creates a {@link SystemPackageOption}.
     *
     * @param pkg system package
     *
     * @return system package option
     */
    public static SystemPackageOption systemPackage( final String pkg )
    {
        return new SystemPackageOption( pkg );
    }

    /**
     * Creates a composite option of {@link SystemPropertyOption}s.
     *
     * @param systemProperties system property options
     *
     * @return composite option of system property options
     */
    public static Option systemProperties( final SystemPropertyOption... systemProperties )
    {
        return composite( systemProperties );
    }

    /**
     * Creates a {@link SystemPropertyOption}.
     *
     * @param key system property key
     *
     * @return system property option
     */
    public static SystemPropertyOption systemProperty( final String key )
    {
        return new SystemPropertyOption( key );
    }

    /**
     * Creates a {@link DebugClassLoadingOption}.
     *
     * @return system property option
     */
    public static DebugClassLoadingOption debugClassLoading( )
    {
        return new DebugClassLoadingOption();
    }

    /**
     * Creates a {@link org.ops4j.pax.exam.options.MavenPluginGeneratedConfigOption}.
     *
     * @return Args option with file written from paxexam plugin
     */
    public static MavenPluginGeneratedConfigOption mavenConfiguration()
    {
        URL url = CoreOptions.class.getClassLoader().getResource( DEFAULT_CONFIGURATION );
        if( url != null )
        {
            return mavenConfiguration( url );
        }
        else
        {
            throw new IllegalArgumentException(
                "Maven PaxExam Plugin does not look like being configured or run properly. "
                + "File (usually produced by the plugin upfront) " + DEFAULT_CONFIGURATION + " has not been found."
            );
        }
    }

    /**
     * Creates a {@link org.ops4j.pax.exam.options.MavenPluginGeneratedConfigOption}.
     *
     * @param url of configuration to be used
     *
     * @return Args option with file written from paxexam plugin
     */
    public static MavenPluginGeneratedConfigOption mavenConfiguration( String url )
    {
        validateNotEmpty( url, "specified configuration url must not be empty " );
        try
        {
            return mavenConfiguration( new URL( url ) );
        } catch( MalformedURLException mE )
        {
            throw new IllegalArgumentException( "url " + url + " is not a valid url", mE );
        }
    }

    /**
     * Creates a {@link org.ops4j.pax.exam.options.MavenPluginGeneratedConfigOption}.
     *
     * @param url of configuration to be used
     *
     * @return Args option with file written from paxexam plugin
     */
    public static MavenPluginGeneratedConfigOption mavenConfiguration( URL url )
    {
        validateNotNull( url, "specified configuration url " );
        return new MavenPluginGeneratedConfigOption( url );
    }

    /**
     * Creates a {@link OptionalCompositeOption}.
     *
     * @param condition boolean condition to evaluate
     *
     * @return optional composite option
     */
    public static OptionalCompositeOption when( final boolean condition )
    {
        return new OptionalCompositeOption( condition );
    }

    /**
     * Creates a {@link OptionalCompositeOption}.
     *
     * @param condition condition to evaluate
     *
     * @return optional composite option
     */
    public static OptionalCompositeOption when( final OptionalCompositeOption.Condition condition )
    {
        return new OptionalCompositeOption( condition );
    }

    /**
     * Creates an {@link FrameworkStartLevelOption}.
     *
     * @param startLevel framework start level (must be bigger then zero)
     *
     * @return framework start level option
     */
    public static FrameworkStartLevelOption frameworkStartLevel( final int startLevel )
    {
        return new FrameworkStartLevelOption( startLevel );
    }

    /**
     * Creates an {@link BundleStartLevelOption}.
     *
     * @param startLevel initial bundle start level (must be bigger then zero)
     *
     * @return bundle start level option
     */
    public static BundleStartLevelOption bundleStartLevel( final int startLevel )
    {
        return new BundleStartLevelOption( startLevel );
    }

    /**
     * Creates a {@link TestContainerStartTimeoutOption} for a number of millis.
     *
     * @param timeoutInMillis timeout in millis
     *
     * @return timeout option
     */
    public static TestContainerStartTimeoutOption waitForFrameworkStartupFor( final long timeoutInMillis )
    {
        return new TestContainerStartTimeoutOption( timeoutInMillis );
    }

    /**
     * Creates a {@link TestContainerStartTimeoutOption} that will not wait for framework startup.
     *
     * @return timeout option
     */
    public static TestContainerStartTimeoutOption doNotWaitForFrameworkStartup()
    {
        return waitForFrameworkStartupFor( Constants.NO_WAIT );
    }

    /**
     * Creates a {@link TestContainerStartTimeoutOption} that will wait for framework startup without timing out.
     *
     * @return timeout option
     */
    public static TestContainerStartTimeoutOption waitForFrameworkStartup()
    {
        return waitForFrameworkStartupFor( Constants.WAIT_5_MINUTES );
    }

}
