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
package org.ops4j.pax.exam.nat.internal;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.ops4j.pax.exam.Constants;
import org.ops4j.pax.exam.ExamSystem;
import org.ops4j.pax.exam.Info;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.ProbeInvoker;
import org.ops4j.pax.exam.RelativeTimeout;
import org.ops4j.pax.exam.TestAddress;
import org.ops4j.pax.exam.TestContainer;
import org.ops4j.pax.exam.TestContainerException;
import org.ops4j.pax.exam.options.ProvisionOption;
import static org.ops4j.pax.exam.OptionUtils.*;

import org.ops4j.pax.exam.options.SystemPropertyOption;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;
import org.osgi.service.startlevel.StartLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Toni Menzel
 * @since Jan 7, 2010
 */
public class NativeTestContainer implements TestContainer {

    final private static Logger LOG = LoggerFactory.getLogger(NativeTestContainer.class);
    final private static String PROBE_SIGNATURE_KEY = "Probe-Signature";
    final private Stack<Long> m_installed = new Stack<Long>();

    final private FrameworkFactory m_frameworkFactory;
    final private ExamSystem m_system;

    volatile Framework m_framework;
    
    public NativeTestContainer( FrameworkFactory frameworkFactory, ExamSystem system ) throws IOException {    	
    	m_system = system.fork(new Option[0]);    	
        m_frameworkFactory = frameworkFactory;
    }
    
	@SuppressWarnings("unchecked")
    private <T> T getService(Class<T> serviceType, String filter )
            throws TestContainerException {
        assert m_framework != null : "Framework should be up";
        assert serviceType != null : "serviceType not be null";

        LOG.debug("Aquiring Service " + serviceType.getName() + " " + (filter != null ? filter : ""));
        RelativeTimeout timeout = m_system.getTimeout();
        final Long start = System.currentTimeMillis();
        do {
            try {
                ServiceReference[] reference = m_framework.getBundleContext().getServiceReferences(serviceType.getName(), filter);
                if (reference != null && reference.length > 0) {
                    return ((T) m_framework.getBundleContext().getService(reference[0]));
                }

                Thread.sleep(200);
            } catch (Exception e) {
                LOG.error("Some problem during looking up service from framework: " + m_framework, e);
            }
        } while (timeout.isNoTimeout() || (System.currentTimeMillis()) < start + timeout.getValue() );
        printAvailableAlternatives(serviceType);

        throw new TestContainerException("Not found a matching Service " + serviceType.getName() + " for Filter:" + (filter != null ? filter : ""));

    }

    private <T> void printAvailableAlternatives(Class<T> serviceType) {
        try {
            ServiceReference[] reference = m_framework.getBundleContext().getAllServiceReferences(serviceType.getName(), null);
            if (reference != null) {
                LOG.warn("Test Endpoints: " + reference.length);

                for (ServiceReference ref : reference) {
                    LOG.warn("Endpoint: " + ref);
                }
            }

        } catch (Exception e) {
            LOG.error("Some problem during looking up alternative service. ", e);
        }
    }

    public synchronized void call( TestAddress address )
    {
        String filterExpression = "(" + PROBE_SIGNATURE_KEY + "=" + address.root().identifier() + ")";
        ProbeInvoker service = getService(ProbeInvoker.class, filterExpression);
        service.call(address.arguments());
    }

    public synchronized long install(InputStream stream) {
        try {
            Bundle b = m_framework.getBundleContext().installBundle("local", stream);
            m_installed.push(b.getBundleId());
            LOG.debug("Installed bundle " + b.getSymbolicName() + " as Bundle ID " + b.getBundleId());
            setBundleStartLevel(b.getBundleId(), Constants.START_LEVEL_TEST_BUNDLE);
            // stream.close();
            b.start();
            return b.getBundleId();
        } catch (BundleException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public synchronized void cleanup() {
        while ((!m_installed.isEmpty())) {
            try {
                Long id = m_installed.pop();
                Bundle bundle = m_framework.getBundleContext().getBundle(id);
                bundle.uninstall();
                LOG.debug("Uninstalled bundle " + id);
            } catch (BundleException e) {
                // Sometimes bundles go mad when install + uninstall happens too fast.
            }
        }
    }

    public void setBundleStartLevel(long bundleId, int startLevel)
            throws TestContainerException {
        StartLevel sl = getStartLevelService(m_framework.getBundleContext());
        sl.setBundleStartLevel(m_framework.getBundleContext().getBundle(bundleId), startLevel);
    }

    public TestContainer stop() {
        if (m_framework != null) {
            try {
                cleanup();
                m_framework.stop();
                m_framework.waitForStop(m_system.getTimeout().getValue());
                m_framework = null;
                m_system.clear();
            } catch (BundleException e) {
                LOG.warn("Problem during stopping fw.", e);
            } catch (InterruptedException e) {
                LOG.warn("InterruptedException during stopping fw.", e);
            }
        } else {
            LOG.warn("Framework does not exist. Called start() before ? ");
        }
        return this;
    }
  
    public TestContainer start()  throws TestContainerException {        
    	ClassLoader parent = null;
        try {
        	final Map<String, String> p = createFrameworkProperties();
            
            
            parent = Thread.currentThread().getContextClassLoader();
            //Thread.currentThread().setContextClassLoader( null );

            m_framework = m_frameworkFactory.newFramework(p);
            m_framework.init();
            installAndStartBundles(m_framework.getBundleContext());
        } catch (Exception e) {
            throw new TestContainerException("Problem starting test container.", e);
        } finally {
            if (parent != null) {
                Thread.currentThread().setContextClassLoader(parent);
            }
        }
        return this;
    }

	private Map<String, String> createFrameworkProperties() throws IOException {
		final Map<String, String> p = new HashMap<String, String>( );
		String cache =  m_system.getTempFolder().getAbsolutePath();
		p.put("org.osgi.framework.storage",cache );
		LOG.info("Cache: " + cache);
		
		p.put("org.osgi.framework.system.packages.extra", "org.ops4j.pax.exam;version=" + skipSnapshotFlag(Info.getPaxExamVersion()));
		for (SystemPropertyOption option : m_system.getOptions(SystemPropertyOption.class) ) {
        	System.setProperty(option.getKey(), option.getValue());
        }
		return p;
	}
   
    private void installAndStartBundles(BundleContext context)
            throws BundleException {
        m_framework.start();
        StartLevel sl = getStartLevelService(context);
        for (ProvisionOption<?> bundle : m_system.getOptions(ProvisionOption.class)) {
            Bundle b = null;
            b = context.installBundle(bundle.getURL());
            int startLevel = getStartLevel(bundle);
            sl.setBundleStartLevel(b, startLevel);
            b.start();
            LOG.debug("+ Install (start@" + startLevel + ") " + bundle);
        }
        LOG.debug("Jump to startlevel: " + Constants.START_LEVEL_TEST_BUNDLE);
        sl.setStartLevel(Constants.START_LEVEL_TEST_BUNDLE);
        // Work around for FELIX-2942
        final CountDownLatch latch = new CountDownLatch(1);
        context.addFrameworkListener(new FrameworkListener() {
            public void frameworkEvent(FrameworkEvent frameworkEvent) {
                switch (frameworkEvent.getType()) {
                    case FrameworkEvent.STARTLEVEL_CHANGED:
                        latch.countDown();
                }
            }
        });
        try {
            latch.await(m_system.getTimeout().getValue(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private StartLevel getStartLevelService(BundleContext context) {
        StartLevel sl;
        while ((sl = (StartLevel) context.getService(context.getServiceReference(StartLevel.class.getName()))) == null) {
            
        }
        return sl;
    }

    private int getStartLevel(ProvisionOption<?> bundle) {
        Integer start = bundle.getStartLevel();
        if (start == null) {
            start = Constants.START_LEVEL_DEFAULT_PROVISION;
        }
        return start;
    }

    private String skipSnapshotFlag(String version) {
        int idx = version.indexOf("-");
        if (idx >= 0) {
            return version.substring(0, idx);
        } else {
            return version;
        }
    }
    
    @Override
    public String toString() {
        return "NativeContainer:" + m_frameworkFactory.toString();
    }
}
