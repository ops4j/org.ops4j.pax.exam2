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
package org.ops4j.pax.exam.container.externalframework.internal;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.felix.utils.manifest.Clause;
import org.apache.felix.utils.manifest.Parser;
import org.apache.karaf.features.BundleInfo;
import org.apache.karaf.features.Feature;
import org.apache.karaf.features.FeatureEvent;
import org.apache.karaf.features.FeaturesListener;
import org.apache.karaf.features.Repository;
import org.apache.karaf.features.RepositoryEvent;
import org.apache.karaf.features.internal.BundleInfoImpl;
import org.apache.karaf.features.internal.FeatureImpl;
import org.apache.karaf.features.internal.RepositoryImpl;
import org.ops4j.pax.exam.container.def.options.FeaturesScannerProvisionOption;
import org.ops4j.pax.exam.options.UrlProvisionOption;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.Version;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.startlevel.StartLevel;

/**
 * The Features service implementation.
 * Adding a repository url will load the features contained in this repository and
 * create dummy sub shells.  When invoked, these commands will prompt the user for
 * installing the needed bundles.
 *
 */
class FeaturesServiceImpl {

    public static final String CONFIG_KEY = "org.apache.karaf.features.configKey";

    private StartLevel startLevel;
    private long resolverTimeout = 5000;
    private Set<URI> uris;
    private Map<URI, RepositoryImpl> repositories = new HashMap<URI, RepositoryImpl>();
    private Map<String, Map<String, Feature>> features;
    private Map<Feature, Set<Long>> installed = new HashMap<Feature, Set<Long>>();
    private List<FeaturesListener> listeners = new CopyOnWriteArrayList<FeaturesListener>();
    private ThreadLocal<Repository> repo = new ThreadLocal<Repository>();
	private List<FeaturesScannerProvisionOption> featuresToInstall = new ArrayList<FeaturesScannerProvisionOption>();
    
    public FeaturesServiceImpl() {
    }


    public StartLevel getStartLevel() {
        return startLevel;
    }

    public void setStartLevel(StartLevel startLevel) {
        this.startLevel = startLevel;
    }

    public long getResolverTimeout() {
        return resolverTimeout;
    }

    public void setResolverTimeout(long resolverTimeout) {
        this.resolverTimeout = resolverTimeout;
    }

    public void registerListener(FeaturesListener listener) {
        listeners.add(listener);
        for (Repository repository : listRepositories()) {
            listener.repositoryEvent(new RepositoryEvent(repository, RepositoryEvent.EventType.RepositoryAdded, true));
        }
        for (Feature feature : listInstalledFeatures()) {
            listener.featureEvent(new FeatureEvent(feature, FeatureEvent.EventType.FeatureInstalled, true));
        }
    }

    public void unregisterListener(FeaturesListener listener) {
        listeners.remove(listener);
    }

    public void setUrls(String uris) throws URISyntaxException {
        String[] s = uris.split(",");
        this.uris = new HashSet<URI>();
        for (String value : s) {
            this.uris.add(new URI(value));
        }
    }

    public void addRepository(URI uri) throws Exception {
        if (!repositories.containsKey(uri)) {
            internalAddRepository(uri);
        }
    }

    protected RepositoryImpl internalAddRepository(URI uri) throws Exception {
    	RepositoryImpl repo = null;
        repo = new RepositoryImpl(uri);
        repositories.put(uri, repo);
        repo.load();
        features = null;
        return repo;
        
    }

    public void removeRepository(URI uri) {
        if (repositories.containsKey(uri)) {
            internalRemoveRepository(uri);
        }
    }

    public void internalRemoveRepository(URI uri) {
        Repository repo = repositories.remove(uri);
        this.repo.set(repo);
        features = null;
    }
    
    public void restoreRepository(URI uri) throws Exception {
    	repositories.put(uri, (RepositoryImpl)repo.get());
    	features = null;
    }

    public Repository[] listRepositories() {
        Collection<RepositoryImpl> repos = repositories.values();
        return repos.toArray(new Repository[repos.size()]);
    }

    protected void doInstallFeature(Feature feature, Set<String> features, 
    		Map<String, BundleInfo> bundles, 
    		Map<String, String> configRet,
    		int sl) throws Exception {
        if (features.contains(feature.getName()))
        	return;
        features.add(feature.getName());
    	for (Feature dependency : feature.getDependencies()) {
            Feature f = getFeature(dependency.getName(), dependency.getVersion());
            if (f == null) {
                throw new Exception("No feature named '" + dependency.getName()
                        + "' with version '" + dependency.getVersion() + "' available");
            }
        	doInstallFeature(f, features, bundles, configRet, sl);
        }
        for (String config : feature.getConfigurations().keySet()) {
            Map<String, String> values = feature.getConfigurations().get(config);
            for (String  key : values.keySet()) {
				if (configRet.containsKey(key)) continue;
				configRet.put(key, values.get(key));
			}
        }
        for (BundleInfo bInfo : resolve(feature)) {
        	bundles.put(bInfo.getLocation(), bInfo);
        	if (bInfo.getStartLevel() <= 0)
        		((BundleInfoImpl) bInfo).setStartLevel(sl);
        }
    }

    protected List<BundleInfo> resolve(Feature feature) throws Exception {
        String resolver = feature.getResolver();
        // If no resolver is specified, we expect a list of uris
        if (resolver == null || resolver.length() == 0) {
        	return feature.getBundles();
        }
        return feature.getBundles();
//        // Else, find the resolver
//        String filter = "(&(" + Constants.OBJECTCLASS + "=" + Resolver.class.getName() + ")(name=" + resolver + "))";
//        ServiceTracker tracker = new ServiceTracker(bundleContext, FrameworkUtil.createFilter(filter), null);
//        tracker.open();
//        try {
//            Resolver r = (Resolver) tracker.waitForService(resolverTimeout);
//            return r.resolve(feature);
//        } finally {
//            tracker.close();
//        }
    }

    /*
     * Get the list of optional imports from an OSGi Import-Package string
     */
    protected List<Clause> getOptionalImports(String importsStr) {
        Clause[] imports = Parser.parseHeader(importsStr);
        List<Clause> result = new LinkedList<Clause>();
        for (int i = 0; i < imports.length; i++) {
            String resolution = imports[i].getDirective(Constants.RESOLUTION_DIRECTIVE);
            if (Constants.RESOLUTION_OPTIONAL.equals(resolution)) {
                result.add(imports[i]);
            }
        }
        return result;
    }

    public Feature[] listFeatures() throws Exception {
        Collection<Feature> features = new ArrayList<Feature>();
        for (Map<String, Feature> featureWithDifferentVersion : getFeatures().values()) {
			for (Feature f : featureWithDifferentVersion.values()) {
                features.add(f);
            }
        }
        return features.toArray(new Feature[features.size()]);
    }

    public Feature[] listInstalledFeatures() {
        Set<Feature> result = installed.keySet();
        return result.toArray(new Feature[result.size()]);
    }

    public boolean isInstalled(Feature f) {
        return installed.containsKey(f);
    }

    public Feature getFeature(String name) throws Exception {
        return getFeature(name, FeatureImpl.DEFAULT_VERSION);
    }

    public Feature getFeature(String name, String version) throws Exception {
        if (version != null) {
            version = version.trim();
        }
        Map<String, Feature> versions = getFeatures().get(name);
        if (versions == null || versions.isEmpty()) {
            return null;
        } else {
            Feature feature = versions.get(version);
            if (feature == null && FeatureImpl.DEFAULT_VERSION.equals(version)) {
                Version latest = new Version(cleanupVersion(version));
                for (String available : versions.keySet()) {
                    Version availableVersion = new Version(cleanupVersion(available));
                    if (availableVersion.compareTo(latest) > 0) {
                        feature = versions.get(available);
                        latest = availableVersion;
                    }
                }
            }
            return feature;
        }
    }

    protected Map<String, Map<String, Feature>> getFeatures() throws Exception {
        if (features == null) {
        	//the outer map's key is feature name, the inner map's key is feature version       
            Map<String, Map<String, Feature>> map = new HashMap<String, Map<String, Feature>>();
            // Two phase load:
            // * first load dependent repositories
            for (;;) {
                boolean newRepo = false;
                for (Repository repo : listRepositories()) {
                    for (URI uri : repo.getRepositories()) {
                        if (!repositories.containsKey(uri)) {
                            internalAddRepository(uri);
                            newRepo = true;
                        }
                    }
                }
                if (!newRepo) {
                    break;
                }
            }
            // * then load all features
            for (Repository repo : repositories.values()) {
                for (Feature f : repo.getFeatures()) {
                	if (map.get(f.getName()) == null) {
                		Map<String, Feature> versionMap = new HashMap<String, Feature>();
                		versionMap.put(f.getVersion(), f);
                		map.put(f.getName(), versionMap);
                	} else {
                		map.get(f.getName()).put(f.getVersion(), f);
                	}
                }
            }
            features = map;
        }
        return features;
    }

    protected String[] parsePid(String pid) {
        int n = pid.indexOf('-');
        if (n > 0) {
            String factoryPid = pid.substring(n + 1);
            pid = pid.substring(0, n);
            return new String[]{pid, factoryPid};
        } else {
            return new String[]{pid, null};
        }
    }

    protected Configuration createConfiguration(ConfigurationAdmin configurationAdmin,
                                                String pid, String factoryPid) throws IOException, InvalidSyntaxException {
        if (factoryPid != null) {
            return configurationAdmin.createFactoryConfiguration(pid, null);
        } else {
            return configurationAdmin.getConfiguration(pid, null);
        }
    }

    
    protected String createValue(Set<Long> set) {
        StringBuilder sb = new StringBuilder();
        for (long i : set) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(i);
        }
        return sb.toString();
    }

    protected Set<Long> readValue(String val) {
        Set<Long> set = new HashSet<Long>();
        if (val != null && val.length() != 0) {
        	for (String str : val.split(",")) {
        		set.add(Long.parseLong(str));
        	}
        }
        return set;
    }

    static Pattern fuzzyVersion  = Pattern.compile("(\\d+)(\\.(\\d+)(\\.(\\d+))?)?([^a-zA-Z0-9](.*))?",
                                                   Pattern.DOTALL);
    static Pattern fuzzyModifier = Pattern.compile("(\\d+[.-])*(.*)",
                                                   Pattern.DOTALL);

    /**
     * Clean up version parameters. Other builders use more fuzzy definitions of
     * the version syntax. This method cleans up such a version to match an OSGi
     * version.
     *
     * @param version
     * @return
     */
    static public String cleanupVersion(String version) {
        Matcher m = fuzzyVersion.matcher(version);
        if (m.matches()) {
            StringBuffer result = new StringBuffer();
            String d1 = m.group(1);
            String d2 = m.group(3);
            String d3 = m.group(5);
            String qualifier = m.group(7);

            if (d1 != null) {
                result.append(d1);
                if (d2 != null) {
                    result.append(".");
                    result.append(d2);
                    if (d3 != null) {
                        result.append(".");
                        result.append(d3);
                        if (qualifier != null) {
                            result.append(".");
                            cleanupModifier(result, qualifier);
                        }
                    } else if (qualifier != null) {
                        result.append(".0.");
                        cleanupModifier(result, qualifier);
                    }
                } else if (qualifier != null) {
                    result.append(".0.0.");
                    cleanupModifier(result, qualifier);
                }
                return result.toString();
            }
        }
        return version;
    }

    static void cleanupModifier(StringBuffer result, String modifier) {
        Matcher m = fuzzyModifier.matcher(modifier);
        if (m.matches())
            modifier = m.group(2);

        for (int i = 0; i < modifier.length(); i++) {
            char c = modifier.charAt(i);
            if ((c >= '0' && c <= '9') || (c >= 'a' && c <= 'z')
                    || (c >= 'A' && c <= 'Z') || c == '_' || c == '-')
                result.append(c);
        }
    }

    

	public void addReference(FeaturesScannerProvisionOption reference) throws Exception {
		URI uri = new URI(reference.getUrlReference().getURL());
		addRepository(uri);
		this.featuresToInstall.add(reference);
	}


	public Map<String, String> install(File bundleDir, TreeMap<Integer, Map<String, NamedUrlProvition>> references,
			int defaultStartlevel) throws Exception {
		Map<String, BundleInfo> bundles = new HashMap<String, BundleInfo>();
		Set<String> lfeatures = new HashSet<String>();
		Map<String, String> configRet = new HashMap<String, String>();
		for (FeaturesScannerProvisionOption ref : featuresToInstall) {
			for (String f : ref.getFeatures()) {
				String[] featureId = f.split(";");
				String featureName = featureId[0];
				String version = FeatureImpl.DEFAULT_VERSION;
				int sl = defaultStartlevel;
				for (int i = 1; i < featureId.length; i++) {
					String part = featureId[i].trim();
					if (part.startsWith("version=")) {
						version = part.substring("version=".length()).trim();
					} else if (part.startsWith("sl=")) {
						try {
							sl = Integer.parseInt(part.substring("sl=".length()).trim());
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
				Feature fObj = getFeature(featureName, version);
				doInstallFeature(fObj, lfeatures, bundles, configRet, sl);
			}
		}
		for (BundleInfo bi : bundles.values()) {
			DefaultOptionsParser.add(bundleDir,
					references, 
					new UrlProvisionOption(bi.getLocation()).startLevel(new Integer(bi.getStartLevel())).start(bi.isStart()),
					bi.getStartLevel());
		}
		return configRet;		
	}
}
