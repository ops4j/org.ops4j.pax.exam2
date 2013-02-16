/*
 * Copyright 2013 Christoph Läubrich
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
package org.ops4j.pax.exam.osgi.internal.karaf;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.transform.sax.SAXSource;

import org.apache.karaf.xmlns.features.v1_0.Bundle;
import org.apache.karaf.xmlns.features.v1_0.Config;
import org.apache.karaf.xmlns.features.v1_0.ConfigFile;
import org.apache.karaf.xmlns.features.v1_0.Dependency;
import org.apache.karaf.xmlns.features.v1_0.Feature;
import org.apache.karaf.xmlns.features.v1_0.FeaturesRoot;
import org.apache.karaf.xmlns.features.v1_0.ObjectFactory;
import org.ops4j.io.StreamUtils;
import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.TestContainerException;
import org.ops4j.pax.exam.options.UrlProvisionOption;
import org.ops4j.pax.exam.options.extra.WorkingDirectoryOption;
import org.ops4j.pax.exam.osgi.ConfigurationAdminOptions;
import org.ops4j.pax.exam.osgi.ConfigurationOption;
import org.ops4j.pax.exam.osgi.KarafFeatureOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * Implementation for {@link KarafFeatureOption}
 */
public class KarafFeatureProvisionOption implements KarafFeatureOption {

    private static final String                    JAVA_PROTOCOL_HANDLER_PKGS = "java.protocol.handler.pkgs";

    private static final Logger                    LOG                        = LoggerFactory.getLogger(KarafFeatureProvisionOption.class);

    private static final ThreadLocal<Unmarshaller> UNMARSHALLER;

    static {
        try {
            final JAXBContext context = JAXBContext.newInstance(org.apache.karaf.xmlns.features.v1_0.ObjectFactory.class);
            UNMARSHALLER = new ThreadLocal<Unmarshaller>() {
                @Override
                protected Unmarshaller initialValue() {
                    try {
                        return context.createUnmarshaller();
                    } catch (JAXBException e) {
                        throw new TestContainerException("can't create Unmarshaller for parsing XML", e);
                    }
                };
            };
        } catch (JAXBException e) {
            throw new TestContainerException("can't create JAXBContext for parsing XML", e);
        }
    }

    private final String                           repositoryUrl;
    private final Set<String>                      featuresSet;

    private int                                    karafStartlevel            = 60;

    private WorkingDirectoryOption                 directoryOption;

    private boolean                                strictNamespaceHandling    = false;

    private boolean                                failOnMissingContraint     = true;

    /**
     * @param repositoryUrl
     *            the URL of the featurefile
     */
    public KarafFeatureProvisionOption(String repositoryUrl) {
        if (repositoryUrl == null) {
            throw new IllegalArgumentException("repositoryUrl can't be null");
        }
        this.repositoryUrl = repositoryUrl;
        featuresSet = new HashSet<String>();
    }

    @Override
    public KarafFeatureOption add(String... features) {
        featuresSet.addAll(Arrays.asList(features));
        return this;
    }

    @Override
    public KarafFeatureOption defaultStartLevel(int level) {
        karafStartlevel = level;
        return this;
    }

    /* (non-Javadoc)
     * @see org.ops4j.pax.exam.osgi.KarafFeatureOption#setWorkingDir(org.ops4j.pax.exam.options.extra.WorkingDirectoryOption)
     */
    @Override
    public KarafFeatureOption workingDir(WorkingDirectoryOption directoryOption) {
        this.directoryOption = directoryOption;
        return this;
    }

    @Override
    public Option toOption() throws TestContainerException {
        String oldHandler = System.getProperty(JAVA_PROTOCOL_HANDLER_PKGS);
        try {
            //This is... *urks* ... but otherwhise we can't load some special urls... we might find a better way once...
            System.setProperty(JAVA_PROTOCOL_HANDLER_PKGS, "org.ops4j.pax.url");
            //create the URL
            URL url = new URL(repositoryUrl);
            List<Feature> features = new ArrayList<Feature>();
            FeaturesRoot featuresRoot = getFeaturesRoot(url, strictNamespaceHandling);
            LOG.info("Provision feature repository with name {} from url {}", featuresRoot.getName(), repositoryUrl);
            addAllFeatures(features, featuresRoot, new HashSet<String>());
            List<Option> options = new ArrayList<Option>();
            for (Feature feature : features) {
                LOG.info("Adding feature {} (version: {})...", feature.getName(), feature.getVersion());
                addFeatureOptions(feature, options);
            }
            return CoreOptions.composite(options.toArray(new Option[0]));
        } catch (MalformedURLException e) {
            throw new TestContainerException("can't parse URL", e);
        } finally {
            if (oldHandler != null) {
                System.setProperty(JAVA_PROTOCOL_HANDLER_PKGS, oldHandler);
            } else {
                System.clearProperty(JAVA_PROTOCOL_HANDLER_PKGS);
            }
        }
    }

    /**
     * @param features
     * @param featuresRoot
     */
    private void addAllFeatures(List<Feature> features, FeaturesRoot featuresRoot, Set<String> scannedURIs) {
        List<Object> repositoryOrFeature = featuresRoot.getRepositoryOrFeature();
        for (Object object : repositoryOrFeature) {
            if (object instanceof Feature) {
                features.add((Feature) object);
            } else if (object instanceof String) {
                //This is a repository with additional dependencies
                String repro = (String) object;
                if (scannedURIs.contains(repro)) {
                    //Ignore and warn already read URIs
                    String msg = "It seems you have a cyclic dependency for repository URI " + repro + " the scanned features might not be complete!";
                    if (failOnMissingContraint) {
                        throw new TestContainerException(msg);
                    } else {
                        LOG.warn(msg);
                    }

                } else {
                    scannedURIs.add(repro);
                    try {
                        URL url = new URL(repro);
                        FeaturesRoot repository = getFeaturesRoot(url, strictNamespaceHandling);
                        addAllFeatures(features, repository, scannedURIs);
                    } catch (MalformedURLException e) {
                        String msg = "Can't parse repository URI " + repro + ", the scanned features might not be complete! (" + e.toString() + ")";
                        if (failOnMissingContraint) {
                            throw new TestContainerException(msg);
                        } else {
                            LOG.error(msg);
                        }
                    } catch (RuntimeException e) {
                        String msg = "Can't parse repository URI " + repro + ", the scanned features might not be complete! (" + e.toString() + ")";
                        if (failOnMissingContraint) {
                            throw new TestContainerException(msg);
                        } else {
                            LOG.error(msg);
                        }
                    }
                }
            }
        }

    }

    private static FeaturesRoot getFeaturesRoot(final URL url, final boolean strictNamespaceHandling) {
        try {
            Unmarshaller unmarshaller = UNMARSHALLER.get();
            XMLReader reader = XMLReaderFactory.createXMLReader();
            final QName rootQName = new ObjectFactory().createFeatures(new FeaturesRoot()).getName();
            XMLFilterImpl rootNameSpaceFilter = new XMLFilterImpl() {
                @Override
                public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
                    if (!strictNamespaceHandling) {
                        if (rootQName.getLocalPart().equals(localName)) {
                            if (!uri.equals(rootQName.getNamespaceURI())) {
                                LOG.warn("The feature with url {} does not declare the required namespace {} on the root element, in favor of well-formed xml you should correct this or inform the author about the issue!", url, rootQName.getNamespaceURI());
                                super.startElement(rootQName.getNamespaceURI(), localName, qName, atts);
                                return;
                            }
                        }
                    }
                    super.startElement(uri, localName, qName, atts);
                }
            };
            rootNameSpaceFilter.setParent(reader);
            SAXSource source = new SAXSource(rootNameSpaceFilter, new InputSource(url.openStream()));
            Object object = unmarshaller.unmarshal(source);
            if (object instanceof JAXBElement<?>) {
                object = ((JAXBElement<?>) object).getValue();
            }
            if (object instanceof FeaturesRoot) {
                return (FeaturesRoot) object;
            } else {
                throw new TestContainerException("The parsed object is not of type FeaturesRoot");
            }
        } catch (JAXBException e) {
            throw new TestContainerException("parsing the featurefile from url " + url + " failed!", e);
        } catch (SAXException e) {
            throw new TestContainerException("parsing the featurefile from url " + url + " failed!", e);
        } catch (IOException e) {
            throw new TestContainerException("parsing the featurefile from url " + url + " failed!", e);
        }
    }

    /**
     * @param feature
     * @param options
     */
    private void addFeatureOptions(Feature feature, List<Option> options) {
        String name = feature.getName();
        if (featuresSet.contains(name)) {
            //The feature should be provisioned!
            String version = feature.getVersion();
            LOG.info("Provision feature {} with version {}", name, version);
            String resolver = feature.getResolver();
            if (resolver != null) {
                //TODO: We can (similar to the ConfigurationAdminOptions) create a tiny bundle here that searches
                //  for (&(objectClass=org.apache.karaf.features.Resolver)(name=resolver)) service and try to resolve it at runtime then!
                //  or we even can support some resolvers (e.g. OBR) natively...
                String msg = "Using resolvers (specified in feature " + name + ") is currently not supported (resolver specified: " + resolver
                        + "), the feature will be ignored!";
                if (failOnMissingContraint) {
                    throw new TestContainerException(msg);
                } else {
                    LOG.error(msg);
                }
                return;
            }
            List<Object> content = feature.getDetailsOrConfigOrConfigfile();
            for (Object object : content) {
                if (object instanceof Dependency) {
                    addDependency((Dependency) object, options);
                } else if (object instanceof Bundle) {
                    addBundle((Bundle) object, options);
                } else if (object instanceof Config) {
                    addConfig((Config) object, options);
                } else if (object instanceof ConfigFile) {
                    addConfigFile((ConfigFile) object, options);
                } else if (object instanceof String) {
                    //Long info displayed in features:info command result.
                    //We can ignore this here
                }
            }
        }

    }

    /**
     * Adds a {@link ConfigFile} to the options
     * 
     * @param configFile
     * @param options
     */
    private void addConfigFile(ConfigFile configFile, List<Option> options) {
        if (directoryOption != null) {
            try {
                URL url = new URL(configFile.getValue().trim());
                String finalname = configFile.getFinalname();
                String workingDirectory = directoryOption.getWorkingDirectory();
                File destinationFile = new File(workingDirectory, finalname);
                FileOutputStream outputStream = new FileOutputStream(destinationFile);
                try {
                    InputStream inputStream = url.openStream();
                    StreamUtils.copyStream(inputStream, outputStream, true);
                } finally {
                    outputStream.close();
                }
            } catch (IOException e) {
                LOG.error("The deployment of configFile {} failed and will not take place (final name = {})", new Object[] { configFile.getValue(),
                        configFile.getFinalname(), e });
            }
        } else {
            LOG.warn("No working directory set, the deployment of configFile {} will not take place (final name = {})", configFile.getValue(), configFile.getFinalname());
        }

    }

    /**
     * Adds a {@link Config} to the options
     * 
     * @param config
     * @param options
     */
    private void addConfig(Config config, List<Option> options) {
        String name = config.getName();
        String value = config.getValue();
        int indexOf = name.indexOf('-');
        ConfigurationOption configuration;
        Properties properties = new Properties();
        try {
            properties.load(new StringReader(value));
        } catch (IOException e) {
            String msg = "Can't read the properties for configuration " + name + ": " + e;
            if (failOnMissingContraint) {
                throw new TestContainerException(msg);
            } else {
                LOG.error(msg, e);
            }
            return;
        }
        if (indexOf > -1) {
            //a factory configuration
            String fpid = name.substring(0, indexOf);
            configuration = ConfigurationAdminOptions.factoryConfiguration(fpid);
            LOG.info("Provision factory configuration for PID {} and values = {}", fpid, properties);
        } else {
            //a "normal" configuration
            configuration = ConfigurationAdminOptions.newConfiguration(name);
            LOG.info("Provision configuration for PID {} and values = {}", name, properties);
        }
        options.add(configuration.asOption());
    }

    /**
     * Adds a {@link Bundle} to the options
     * 
     * @param bundle
     * @param options
     */
    private void addBundle(Bundle bundle, List<Option> options) {
        Integer startLevel = bundle.getStartLevel();
        String uri = bundle.getValue();
        Boolean start = bundle.isStart();
        Boolean dependency = bundle.isDependency();
        UrlProvisionOption option = CoreOptions.bundle(uri);
        if (startLevel != null) {
            option.startLevel(startLevel);
        } else {
            option.startLevel(karafStartlevel);
        }
        if (start == null || start.booleanValue()) {
            //The default is to start the bundle...
            option.start(true);
        } else {
            option.start(false);
        }
        options.add(option);
        if (dependency != null && dependency.booleanValue()) {
            //TODO: support it...
            LOG.warn("The dependency option is currently not supported and will be ignored!");
        }

    }

    /**
     * "adds" a {@link Dependency} to the options
     * 
     * @param object
     * @param options
     */
    private void addDependency(Dependency object, List<Option> options) {
        if (!featuresSet.contains(object.getValue())) {
            LOG.info("One of the features has a dependency to feature {} what is not part of this provision, make sure it will be provided by some other means", object.getValue());
        }
    }

    @Override
    public KarafFeatureOption strictNamespaceHandling(boolean strict) {
        this.strictNamespaceHandling = strict;
        return this;
    }

    /* (non-Javadoc)
     * @see org.ops4j.pax.exam.osgi.KarafFeatureOption#failOnMissingConstraint(boolean)
     */
    @Override
    public KarafFeatureOption failOnMissingConstraint(boolean fail) {
        this.failOnMissingContraint = fail;
        return this;
    }

}
