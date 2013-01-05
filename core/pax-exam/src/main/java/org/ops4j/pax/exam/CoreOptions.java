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

import static org.ops4j.lang.NullArgumentException.validateNotEmptyContent;
import static org.ops4j.lang.NullArgumentException.validateNotNull;
import static org.ops4j.pax.exam.OptionUtils.expand;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.ops4j.pax.exam.options.BootClasspathLibraryOption;
import org.ops4j.pax.exam.options.BootDelegationOption;
import org.ops4j.pax.exam.options.BundleStartLevelOption;
import org.ops4j.pax.exam.options.CompositeOption;
import org.ops4j.pax.exam.options.DefaultCompositeOption;
import org.ops4j.pax.exam.options.FrameworkPropertyOption;
import org.ops4j.pax.exam.options.FrameworkStartLevelOption;
import org.ops4j.pax.exam.options.MavenArtifactDeploymentOption;
import org.ops4j.pax.exam.options.MavenArtifactProvisionOption;
import org.ops4j.pax.exam.options.MavenArtifactUrlReference;
import org.ops4j.pax.exam.options.OptionalCompositeOption;
import org.ops4j.pax.exam.options.ProvisionOption;
import org.ops4j.pax.exam.options.ServerModeOption;
import org.ops4j.pax.exam.options.SystemPackageOption;
import org.ops4j.pax.exam.options.SystemPropertyOption;
import org.ops4j.pax.exam.options.TimeoutOption;
import org.ops4j.pax.exam.options.UrlDeploymentOption;
import org.ops4j.pax.exam.options.UrlProvisionOption;
import org.ops4j.pax.exam.options.UrlReference;
import org.ops4j.pax.exam.options.WarProbeOption;
import org.ops4j.pax.exam.options.WrappedUrlProvisionOption;
import org.ops4j.pax.exam.options.extra.CleanCachesOption;
import org.ops4j.pax.exam.options.extra.RepositoryOption;
import org.ops4j.pax.exam.options.extra.RepositoryOptionImpl;
import org.ops4j.pax.exam.options.extra.VMOption;
import org.ops4j.pax.exam.options.extra.WorkingDirectoryOption;
import org.ops4j.pax.exam.options.libraries.JUnitBundlesOption;
import org.ops4j.store.Handle;
import org.ops4j.store.Store;
import org.ops4j.store.StoreFactory;

/**
 * Factory methods for core options.
 * 
 * @author Alin Dreghiciu (adreghiciu@gmail.com)
 * @author Toni Menzel (toni@okidokiteam.com
 * @author Harald Wellmann
 * @since 0.3.0, December 08, 2008
 */
public class CoreOptions {

    /**
     * Utility class. Meant to be used via the static factory methods.
     */
    private CoreOptions() {
        // utility class
    }

    /**
     * Convenience method (more to be used for a nice fluent api) for creating an array of options.
     * It also expands the composite options.
     * 
     * @param options
     *            to be used.
     * @return provided options, expanded
     * 
     * @see OptionUtils#expand(Option...)
     */
    public static Option[] options(final Option... options) {
        return expand(options);
    }

    /**
     * Convenience method (more to be used for a nice fluent api) for creating a composite option.
     * 
     * @param options
     *            options
     * 
     * @return provided options
     */
    public static Option composite(final Option... options) {
        return new DefaultCompositeOption(options);
    }

    /**
     * Creates a composite option of {@link ProvisionOption}s.
     * 
     * @param urls
     *            provision urls (cannot be null or containing null entries)
     * 
     * @return composite option of provision options
     * 
     * @throws IllegalArgumentException
     *             - If urls array is null or contains null entries
     */
    public static Option provision(final String... urls) {
        validateNotEmptyContent(urls, true, "URLs");
        final List<ProvisionOption<?>> options = new ArrayList<ProvisionOption<?>>();
        for (String url : urls) {
            options.add(new UrlProvisionOption(url));
        }
        return provision(options.toArray(new ProvisionOption[options.size()]));
    }

    /**
     * Creates a composite option of {@link ProvisionOption}s. This is handy when bundles are built
     * on the fly via TinyBundles.
     * 
     * @param streams
     *            provision sources
     * 
     * @return composite option of provision options
     * 
     * @throws IllegalArgumentException
     *             - If a problem occured while flushing streams
     */
    public static Option provision(final InputStream... streams) {
        validateNotNull(streams, "streams");

        final UrlProvisionOption[] options = new UrlProvisionOption[streams.length];
        int i = 0;
        for (InputStream stream : streams) {
            options[i++] = streamBundle(stream);
        }
        return provision(options);
    }

    /**
     * Creates a composite option of {@link ProvisionOption}s.
     * 
     * @param urls
     *            provision options
     * 
     * @return composite option of provision options
     */
    public static Option provision(final ProvisionOption<?>... urls) {
        return composite(urls);
    }

    public static UrlProvisionOption streamBundle(final InputStream stream) {
        validateNotNull(stream, "stream");
        // TODO make the store more global to the exam session to control
        // caching load + shutdown.
        // For now we do it fully3 locally:

        try {
            Store<InputStream> store = StoreFactory.anonymousStore();
            Handle handle = store.store(stream);
            URL url = store.getLocation(handle).toURL();
            UrlProvisionOption option = new UrlProvisionOption(url.toExternalForm());
            return option;
        }
        catch (IOException e) {
            throw new IllegalArgumentException("A supplied stream blew up", e);
        }
    }

    /**
     * Creates a {@link UrlProvisionOption}.
     * 
     * @param url
     *            url as a string
     * 
     * @return url reference
     */
    public static UrlProvisionOption url(final String url) {
        return new UrlProvisionOption(url);
    }

    /**
     * Creates a {@link UrlProvisionOption}.
     * 
     * @param url
     *            bundle url
     * 
     * @return url provisioning option
     */
    public static UrlProvisionOption bundle(final String url) {
        return new UrlProvisionOption(url);
    }

    /**
     * Creates a {@link org.ops4j.pax.exam.options.MavenArtifactUrlReference}.
     * 
     * @return maven artifact url
     */
    public static MavenArtifactUrlReference maven() {
        return new MavenArtifactUrlReference();
    }

    /**
     * Convenience method (shorter) for referencing an maven artifact based on groupId/artifactId.
     * 
     * @param groupId
     *            artifact group id
     * @param artifactId
     *            artifact id
     * 
     * @return maven artifact url
     */
    public static MavenArtifactUrlReference maven(final String groupId, final String artifactId) {
        return maven().groupId(groupId).artifactId(artifactId);
    }

    /**
     * Convenience method (shorter) for referencing a maven artifact based on
     * groupId/artifactId/version.
     * 
     * @param groupId
     *            artifact group id
     * @param artifactId
     *            artifact id
     * @param version
     *            artifact version
     * 
     * @return maven artifact url
     */
    public static MavenArtifactUrlReference maven(final String groupId, final String artifactId,
        final String version) {
        return maven().groupId(groupId).artifactId(artifactId).version(version);
    }

    /**
     * Creates a {@link MavenArtifactProvisionOption}.
     * 
     * @return maven specific provisioning option
     */
    public static MavenArtifactProvisionOption mavenBundle() {
        return new MavenArtifactProvisionOption();
    }

    /**
     * Convenience method (shorter) for adding a maven bundle based on groupId/artifactId.
     * 
     * @param groupId
     *            artifact group id
     * @param artifactId
     *            artifact id
     * 
     * @return maven specific provisioning option
     */
    public static MavenArtifactProvisionOption mavenBundle(final String groupId,
        final String artifactId) {
        return mavenBundle().groupId(groupId).artifactId(artifactId);
    }

    /**
     * Convenience method (shorter) for adding a maven bundle based on groupId/artifactId/version.
     * 
     * @param groupId
     *            artifact group id
     * @param artifactId
     *            artifact id
     * @param version
     *            artifact version
     * 
     * @return maven specific provisioning option
     */
    public static MavenArtifactProvisionOption mavenBundle(final String groupId,
        final String artifactId, final String version) {
        return mavenBundle().groupId(groupId).artifactId(artifactId).version(version);
    }

    /**
     * Convenience factory method for adding a maven bundle based on a meven artifact.
     * 
     * @param artifact
     *            maven artifact
     * 
     * @return maven specific provisioning option
     */
    public static MavenArtifactProvisionOption mavenBundle(final MavenArtifactUrlReference artifact) {
        return new MavenArtifactProvisionOption(artifact);
    }

    /**
     * Creates a {@link WrappedUrlProvisionOption}.
     * 
     * @param jarToWrapUrl
     *            url of jar to be wrapped
     * 
     * @return wrap specific provisioning option
     */
    public static WrappedUrlProvisionOption wrappedBundle(final String jarToWrapUrl) {
        return new WrappedUrlProvisionOption(jarToWrapUrl);
    }

    /**
     * Creates a {@link WrappedUrlProvisionOption}.
     * 
     * @param jarToWrapUrl
     *            url of jar to be wrapped
     * 
     * @return wrap specific provisioning option
     */
    public static WrappedUrlProvisionOption wrappedBundle(final UrlReference jarToWrapUrl) {
        return new WrappedUrlProvisionOption(jarToWrapUrl);
    }

    /**
     * Creates a composite option of {@link BootDelegationOption}s.
     * 
     * @param packages
     *            boot delegation packages (cannot be null or containing null entries)
     * 
     * @return composite option of boot delegation package options
     * 
     * @throws IllegalArgumentException
     *             - If urls array is null or contains null entries
     */
    public static Option bootDelegationPackages(final String... packages) {
        validateNotEmptyContent(packages, true, "Packages");
        final List<BootDelegationOption> options = new ArrayList<BootDelegationOption>();
        for (String pkg : packages) {
            options.add(bootDelegationPackage(pkg));
        }
        return bootDelegationPackages(options.toArray(new BootDelegationOption[options.size()]));
    }

    /**
     * Creates a composite option of {@link BootDelegationOption}s.
     * 
     * @param packages
     *            boot delegation package options
     * 
     * @return composite option of boot delegation package options
     */
    public static Option bootDelegationPackages(final BootDelegationOption... packages) {
        return composite(packages);
    }

    /**
     * Creates a {@link BootDelegationOption}.
     * 
     * @param pkg
     *            boot delegation package
     * 
     * @return boot delegation package option
     */
    public static BootDelegationOption bootDelegationPackage(final String pkg) {
        return new BootDelegationOption(pkg);
    }

    /**
     * Creates a composite option of {@link org.ops4j.pax.exam.options.BootClasspathLibraryOption}s.
     * 
     * @param urls
     *            boot classpath library urls (cannot be null or containing null entries)
     * 
     * @return composite option of boot classpath options
     * 
     * @throws IllegalArgumentException
     *             - If urls array is null or contains null entries
     */
    public static Option bootClasspathLibraries(final String... urls) {
        validateNotEmptyContent(urls, true, "Urls");
        final List<BootClasspathLibraryOption> options = new ArrayList<BootClasspathLibraryOption>();
        for (String url : urls) {
            options.add(bootClasspathLibrary(url));
        }
        return bootClasspathLibraries(options
            .toArray(new BootClasspathLibraryOption[options.size()]));
    }

    /**
     * Creates a composite option of {@link org.ops4j.pax.exam.options.BootClasspathLibraryOption}s.
     * 
     * @param libraries
     *            boot classpath library options
     * 
     * @return composite option of boot classpath library options
     */
    public static Option bootClasspathLibraries(final BootClasspathLibraryOption... libraries) {
        return composite(libraries);
    }

    /**
     * Creates a {@link org.ops4j.pax.exam.options.BootClasspathLibraryOption}.
     * 
     * @param libraryUrl
     *            boot classpath library url
     * 
     * @return boot classpath option
     */
    public static BootClasspathLibraryOption bootClasspathLibrary(final String libraryUrl) {
        return new BootClasspathLibraryOption(libraryUrl);
    }

    /**
     * Creates a {@link org.ops4j.pax.exam.options.BootClasspathLibraryOption}.
     * 
     * @param libraryUrl
     *            boot classpath library url
     * 
     * @return boot classpath option
     */
    public static BootClasspathLibraryOption bootClasspathLibrary(final UrlReference libraryUrl) {
        return new BootClasspathLibraryOption(libraryUrl);
    }

    /**
     * Creates a composite option of {@link SystemPackageOption}s.
     * 
     * @param packages
     *            system packages (cannot be null or containing null entries)
     * 
     * @return composite option of system package options
     * 
     * @throws IllegalArgumentException
     *             - If urls array is null or contains null entries
     */
    public static Option systemPackages(final String... packages) {
        validateNotEmptyContent(packages, true, "Packages");
        final List<SystemPackageOption> options = new ArrayList<SystemPackageOption>();
        for (String pkg : packages) {
            options.add(systemPackage(pkg));
        }
        return systemPackages(options.toArray(new SystemPackageOption[options.size()]));
    }

    /**
     * Creates a composite option of {@link SystemPackageOption}s.
     * 
     * @param packages
     *            system package options
     * 
     * @return composite option of system package options
     */
    public static Option systemPackages(final SystemPackageOption... packages) {
        return composite(packages);
    }

    /**
     * Creates a {@link SystemPackageOption}.
     * 
     * @param pkg
     *            system package
     * 
     * @return system package option
     */
    public static SystemPackageOption systemPackage(final String pkg) {
        return new SystemPackageOption(pkg);
    }

    /**
     * Creates a composite option of {@link SystemPropertyOption}s.
     * 
     * @param systemProperties
     *            system property options
     * 
     * @return composite option of system property options
     */
    public static Option systemProperties(final SystemPropertyOption... systemProperties) {
        return composite(systemProperties);
    }

    /**
     * Creates a {@link SystemPropertyOption}.
     * 
     * @param key
     *            system property key
     * 
     * @return framework property option
     */
    public static SystemPropertyOption systemProperty(final String key) {
        return new SystemPropertyOption(key);
    }

    /**
     * Creates a {@link FrameworkPropertyOption}.
     * 
     * @param key
     *            framework property key
     * 
     * @return framework property option
     */
    public static FrameworkPropertyOption frameworkProperty(final String key) {
        return new FrameworkPropertyOption(key);
    }

    /**
     * Creates a composite option of {@link FrameworkPropertyOption}s.
     * 
     * @param frameworkProperties
     *            framework property options
     * 
     * @return composite option of framework property options
     */
    public static Option frameworkProperties(final FrameworkPropertyOption... frameworkProperties) {
        return composite(frameworkProperties);
    }
    /**
     * Creates a {@link OptionalCompositeOption}.
     * 
     * @param condition
     *            boolean condition to evaluate
     * 
     * @return optional composite option
     */
    public static OptionalCompositeOption when(final boolean condition) {
        return new OptionalCompositeOption(condition);
    }

    /**
     * Creates a {@link OptionalCompositeOption}.
     * 
     * @param condition
     *            condition to evaluate
     * 
     * @return optional composite option
     */
    public static OptionalCompositeOption when(final OptionalCompositeOption.Condition condition) {
        return new OptionalCompositeOption(condition);
    }

    /**
     * Creates an {@link FrameworkStartLevelOption}.
     * 
     * @param startLevel
     *            framework start level (must be bigger then zero)
     * 
     * @return framework start level option
     */
    public static FrameworkStartLevelOption frameworkStartLevel(final int startLevel) {
        return new FrameworkStartLevelOption(startLevel);
    }

    /**
     * Creates an {@link BundleStartLevelOption}.
     * 
     * @param startLevel
     *            initial bundle start level (must be bigger then zero)
     * 
     * @return bundle start level option
     */
    public static BundleStartLevelOption bundleStartLevel(final int startLevel) {
        return new BundleStartLevelOption(startLevel);
    }

    /**
     * Creates a {@link TimeoutOption} for a number of millis.
     * 
     * @param timeoutInMillis
     *            timeout in millis
     * 
     * @return timeout option
     */
    public static TimeoutOption systemTimeout(final long timeoutInMillis) {
        return new TimeoutOption(timeoutInMillis);
    }

    // -------------------- Libraries ------------

    /**
     * Creates a {@link JUnitBundlesOption}.
     * 
     * @return junit bundles option
     */
    public static CompositeOption junitBundles() {
        return new DefaultCompositeOption(new JUnitBundlesOption(), 
            systemProperty("pax.exam.invoker").value("junit"),
            bundle("mvn:org.ops4j.pax.tipi/org.ops4j.pax.tipi.hamcrest.core/1.3.0.1"),
            bundle("link:classpath:META-INF/links/org.ops4j.pax.exam.invoker.junit.link"));
    }

    // -------------------- Extra (not supported in all containers) ------------

    /**
     * Creates a {@link CleanCachesOption}.
     * 
     * @return clean caches option
     */
    public static CleanCachesOption cleanCaches(boolean value) {
        return new CleanCachesOption(value);
    }

    /**
     * Creates a {@link CleanCachesOption}.
     * 
     * @return clean caches option
     */
    public static CleanCachesOption cleanCaches() {
        return new CleanCachesOption();
    }

    /**
     * Creates a {@link CleanCachesOption}.
     * 
     * @return clean caches option
     */
    public static CleanCachesOption keepCaches() {
        return new CleanCachesOption(Boolean.FALSE);
    }

    /**
     * Creates a {@link CleanCachesOption}.value(false) + workingDirectory(folder) options.
     * 
     * @return options set so it should just be used to kick a single process. Not for test runners.
     *         (they would interfere).
     */
    public static Option serverMode() {
        return composite(keepCaches(), new ServerModeOption() // marker
        );
    }

    /**
     * Creates a composite option of {@link VMOption}s.
     * 
     * @param vmOptions
     *            virtual machine options (cannot be null or containing null entries)
     * 
     * @return composite option of virtual machine options
     * 
     * @throws IllegalArgumentException
     *             - If urls array is null or contains null entries
     */
    public static Option vmOptions(final String... vmOptions) {
        validateNotEmptyContent(vmOptions, true, "VM options");
        final List<VMOption> options = new ArrayList<VMOption>();
        for (String vmOption : vmOptions) {
            options.add(vmOption(vmOption));
        }
        return vmOptions(options.toArray(new VMOption[options.size()]));
    }

    /**
     * Creates a composite option of {@link VMOption}s.
     * 
     * @param vmOptions
     *            virtual machine options
     * 
     * @return composite option of virtual machine options
     */
    public static Option vmOptions(final VMOption... vmOptions) {
        return new DefaultCompositeOption(vmOptions);
    }

    /**
     * Creates a {@link VMOption}.
     * 
     * @param vmOption
     *            virtual machine option
     * 
     * @return virtual machine option
     */
    public static VMOption vmOption(final String vmOption) {
        return new VMOption(vmOption);
    }

    // FIXME Repositories options may be required after all, even by Native and Forked Continer
    // for interacting with Pax URL. Need to check...

    /**
     * Creates a composite option of {@link RepositoryOption}s.
     * 
     * @param repositoryUrls
     *            Maven repository URLs
     * 
     * @return composite option of repository options
     * 
     * @throws IllegalArgumentException
     *             - If urls array is null or contains null entries
     */
    public static Option repositories(final String... repositoryUrls) {
        validateNotEmptyContent(repositoryUrls, true, "Repository URLs");
        final List<RepositoryOption> options = new ArrayList<RepositoryOption>();
        for (String repositoryUrl : repositoryUrls) {
            options.add(repository(repositoryUrl));
        }
        return repositories(options.toArray(new RepositoryOption[options.size()]));
    }

    /**
     * Creates a composite option of {@link RepositoryOption}s.
     * 
     * @param repositoryOptions
     *            repository options
     * 
     * @return composite option of repository options
     */
    public static Option repositories(final RepositoryOption... repositoryOptions) {
        return new DefaultCompositeOption(repositoryOptions);
    }

    /**
     * Creates a {@link RepositoryOption}.
     * 
     * @param repositoryUrl
     *            repository url
     * 
     * @return repository option
     */
    public static RepositoryOption repository(final String repositoryUrl) {
        return new RepositoryOptionImpl(repositoryUrl);
    }

    /**
     * Creates a {@link WorkingDirectoryOption}.
     * 
     * @param directory
     *            url of the bundle to be scanned
     * 
     * @return working directory option
     */
    public static WorkingDirectoryOption workingDirectory(final String directory) {
        return new WorkingDirectoryOption(directory);
    }

    /**
     * Deploys a WAR from the given URL.
     * 
     * @param url
     *            URL of a WAR
     * @return deployment option
     */
    public static UrlDeploymentOption war(String url) {
        return new UrlDeploymentOption(url);
    }

    /**
     * Deploys a Maven WAR artifact. The Maven coordinates need to be added in fluent syntax.
     * 
     * @return Maven artifact option
     */
    public static MavenArtifactDeploymentOption mavenWar() {
        return new MavenArtifactDeploymentOption().type("war");
    }

    /**
     * Deploys a Maven WAR artifact with the given Maven coordinates.
     * 
     * @param groupId
     *            group ID
     * @param artifactId
     *            artifact ID
     * @param version
     * @return Maven artifact option
     */
    public static MavenArtifactDeploymentOption mavenWar(final String groupId,
        final String artifactId, final String version) {
        return mavenWar().groupId(groupId).artifactId(artifactId).version(version).type("war");
    }

    /**
     * Creates an option for a user-defined WAR probe. This option needs to be customized in fluent
     * syntax.
     * 
     * @return WAR probe option
     */
    public static WarProbeOption warProbe() {
        return new WarProbeOption();
    }
}
