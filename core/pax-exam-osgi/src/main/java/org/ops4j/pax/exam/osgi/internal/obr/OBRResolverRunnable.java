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
package org.ops4j.pax.exam.osgi.internal.obr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.osgi.service.obr.RepositoryAdmin;
import org.osgi.service.obr.Requirement;
import org.osgi.service.obr.Resolver;
import org.osgi.service.obr.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Used in the runtime to provision bundles via OBR...
 */
public class OBRResolverRunnable implements Runnable {

    private static final Logger   LOG = LoggerFactory.getLogger(OBRResolverRunnable.class);

    private final RepositoryAdmin repositoryAdmin;
    private final List<String[]>  bundleList;
    private final List<Exception> exceptions;

    /**
     * @param repositoryAdmin
     * @param bundleList
     */
    public OBRResolverRunnable(RepositoryAdmin repositoryAdmin, List<String[]> bundleList, List<Exception> exceptions) {
        this.repositoryAdmin = repositoryAdmin;
        this.exceptions = exceptions;
        this.bundleList = new ArrayList<String[]>(bundleList);
    }

    @Override
    public void run() {
        Thread thread = Thread.currentThread();
        String name = thread.getName();
        try {
            thread.setName("OBR resolve thread");
            Resolver resolver = repositoryAdmin.resolver();
            if (thread.isInterrupted()) {
                return;
            }
            for (String[] bundleItem : bundleList) {
                String symbolicName = bundleItem[0];
                String version = bundleItem[1];
                String filter;
                String filterSymbolicName = "(symbolicname=" + symbolicName + ")";
                if (version == null) {
                    filter = filterSymbolicName;
                } else {
                    filter = "(&(symbolicname=" + symbolicName + ")(version=" + version + "))";
                }
                Resource[] resources = repositoryAdmin.discoverResources(filter);
                if (resources != null && resources.length > 0) {
                    for (Resource resource : resources) {
                        LOG.info("Add resource {} (version = {}) to resolver...", resource.getSymbolicName(), resource.getVersion());
                        resolver.add(resource);
                    }
                } else {
                    synchronized (exceptions) {
                        exceptions.add(new RuntimeException("can't resolve item " + filter));
                    }
                    LOG.error("can't resolve item with filter {}!", filter);
                    if (version != null && LOG.isInfoEnabled()) {
                        Resource[] discoverResources = repositoryAdmin.discoverResources(filterSymbolicName);
                        if (discoverResources != null && discoverResources.length > 0) {
                            for (Resource resource : discoverResources) {
                                LOG.info("Alternative version for resource {} is {}", filter, resource.getVersion());
                            }
                        } else {
                            LOG.info("No alternative versions found, are you sure you specified the right fragment name for item {}?", filter);
                        }
                    }
                }
            }
            if (thread.isInterrupted()) {
                return;
            }
            boolean resolve = resolver.resolve();
            if (resolve) {
                for (Resource resource : resolver.getRequiredResources()) {
                    LOG.info("required resource {} (version = {}) discovered by resolver...", resource.getSymbolicName(), resource.getVersion());
                }
                for (Resource resource : resolver.getOptionalResources()) {
                    LOG.info("optional resource {} (version = {}) discovered by resolver...", resource.getSymbolicName(), resource.getVersion());
                }
                LOG.info("Deploy bundles...");
                resolver.deploy(true);
                LOG.info("Deploy finished!");
            } else {
                Requirement[] requirements = resolver.getUnsatisfiedRequirements();
                String stringReq = Arrays.toString(requirements);
                synchronized (exceptions) {
                    exceptions.add(new IllegalStateException("can't resolve the following requirements are not meet: " + stringReq));
                }
                LOG.error("can't resolve the following requirements are not meet: {}", stringReq);
            }
        } catch (RuntimeException e) {
            synchronized (exceptions) {
                exceptions.add(e);
            }
        } finally {
            thread.setName(name);
        }
    }
}
