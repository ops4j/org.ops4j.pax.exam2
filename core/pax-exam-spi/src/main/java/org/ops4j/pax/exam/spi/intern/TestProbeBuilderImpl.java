/*
 * Copyright 2009 Toni Menzel.
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
package org.ops4j.pax.exam.spi.intern;

import static org.ops4j.pax.exam.Constants.PROBE_EXECUTABLE;
import static org.ops4j.pax.tinybundles.TinyBundles.bundle;
import static org.ops4j.pax.tinybundles.TinyBundles.rawBuilder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.ops4j.pax.exam.TestAddress;
import org.ops4j.pax.exam.TestContainerException;
import org.ops4j.pax.exam.TestInstantiationInstruction;
import org.ops4j.pax.exam.TestProbeBuilder;
import org.ops4j.pax.exam.TestProbeProvider;
import org.ops4j.pax.exam.spi.ContentCollector;
import org.ops4j.pax.tinybundles.TinyBundle;
import org.ops4j.store.Store;
import org.osgi.framework.Constants;

/**
 * Default implementation allows you to dynamically create a probe from current classpath.
 * 
 * @author Toni Menzel
 * @since Dec 2, 2009
 */
public class TestProbeBuilderImpl implements TestProbeBuilder {

    private static final String DEFAULT_PROBE_METHOD_NAME = "probe";

    private final Map<TestAddress, TestInstantiationInstruction> probeCalls = new LinkedHashMap<TestAddress, TestInstantiationInstruction>();
    private final List<Class<?>> anchors;
    private final Properties extraProperties;
    private final Set<String> ignorePackages = new HashSet<String>();
    private File tempDir;
    private final Store<InputStream> store;

    public TestProbeBuilderImpl(File tempDir, Store<InputStream> store) throws IOException {
        this.anchors = new ArrayList<Class<?>>();
        this.tempDir = tempDir;
        this.store = store;
        extraProperties = new Properties();
    }

    public TestAddress addTest(Class<?> clazz, String methodName, Object... args) {
        TestAddress address = new DefaultTestAddress(clazz.getName() + "." + methodName, args);
        probeCalls.put(address,
            new TestInstantiationInstruction(clazz.getName() + ";" + methodName));
        addAnchor(clazz);
        return address;
    }

    public TestAddress addTest(Class<?> clazz, Object... args) {
        return addTest(clazz, DEFAULT_PROBE_METHOD_NAME, args);
    }

    public List<TestAddress> addTests(Class<?> clazz, Method... methods) {
        List<TestAddress> list = new ArrayList<TestAddress>();
        for (Method method : methods) {
            list.add(addTest(clazz, method.getName()));
        }
        return list;
    }

    public TestProbeBuilder addAnchor(Class<?> clazz) {
        anchors.add(clazz);
        return this;
    }

    public TestProbeBuilder setHeader(String key, String value) {
        extraProperties.put(key, value);
        return this;
    }

    // when your testclass contains clutter in non-test methods,
    // bnd generates too many impports.
    // This makes packages optional.
    public TestProbeBuilder ignorePackageOf(Class<?>... classes) {
        for (Class<?> c : classes) {
            ignorePackages.add(c.getPackage().getName());
        }

        return this;
    }

    public TestProbeProvider build() {
        if (anchors.size() == 0) {
            throw new TestContainerException("No tests added to setup!");
        }

        constructProbeTag(extraProperties);
        try {
            TinyBundle bundle = prepareProbeBundle(createExtraIgnores());
            return new DefaultTestProbeProvider(getTests(), store, store.store(bundle
                .build(rawBuilder())));

        }
        catch (IOException e) {
            throw new TestContainerException(e);
        }
    }

    private TinyBundle prepareProbeBundle(Properties p) throws IOException {
        TinyBundle bundle = bundle(store).setHeader(Constants.DYNAMICIMPORT_PACKAGE, "*");

        bundle.setHeader(Constants.BUNDLE_SYMBOLICNAME, "");
        bundle.setHeader(Constants.BUNDLE_MANIFESTVERSION, "2");
        for (Object key : extraProperties.keySet()) {
            bundle.setHeader((String) key, (String) extraProperties.get(key));
        }
        for (Object key : p.keySet()) {
            bundle.setHeader((String) key, (String) p.get(key));
        }

        Map<String, URL> map = collectResources();
        for (String item : map.keySet()) {
            bundle.addResource(item, map.get(item));
        }
        return bundle;
    }

    private Map<String, URL> collectResources() throws IOException {
        ContentCollector collector = selectCollector();
        Map<String, URL> map = new HashMap<String, URL>();
        collector.collect(map);
        return map;
    }

    static String convertClassToPath(Class<?> c) {
        return c.getName().replace(".", File.separator) + ".class";
    }

    /**
     * @param clazz
     *            to find the root classes folder for.
     * 
     * @return A File instance being the exact folder on disk or null, if it hasn't been found.
     * 
     * @throws java.io.IOException
     *             if a problem occurs (method crawls folders on disk..)
     */
    public static File findClassesFolder(Class<?> clazz) throws IOException {
        ClassLoader classLoader = clazz.getClassLoader();
        String clazzPath = convertClassToPath(clazz);
        URL url = classLoader.getResource(clazzPath);
        if (url == null || !"file".equals(url.getProtocol())) {
            return null;
        }
        else {
            try {
                File file = new File(url.toURI());
                String fullPath = file.getCanonicalPath();
                String parentDirPath = fullPath
                    .substring(0, fullPath.length() - clazzPath.length());
                return new File(parentDirPath);
            }
            catch (URISyntaxException e) {
                // this should not happen as the uri was obtained from getResource
                throw new TestContainerException(e);
            }
        }
    }

    private ContentCollector selectCollector() throws IOException {
        File root = findClassesFolder(anchors.get(0));

        if (root != null) {
            return new CompositeCollector(new CollectFromBase(root), new CollectFromItems(anchors));
        }
        else {
            return new CollectFromItems(anchors);
        }
    }

    public Set<TestAddress> getTests() {
        return probeCalls.keySet();
    }

    private Properties createExtraIgnores() {
        Properties properties = new Properties();
        StringBuilder sb = new StringBuilder();
        for (String p : ignorePackages) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(p);
        }
        properties.put("Ignore-Package", sb.toString());
        return properties;
    }

    private void constructProbeTag(Properties p) {
        // construct out of added Tests
        StringBuilder sbKeyChain = new StringBuilder();

        for (TestAddress address : probeCalls.keySet()) {
            sbKeyChain.append(address.identifier());
            sbKeyChain.append(",");
            p.put(address.identifier(), probeCalls.get(address).toString());
        }
        p.put(PROBE_EXECUTABLE, sbKeyChain.toString());
    }

    @Override
    public File getTempDir() {
        return tempDir;
    }

    @Override
    public void setTempDir(File tempDir) {
        this.tempDir = tempDir;
    }
}
