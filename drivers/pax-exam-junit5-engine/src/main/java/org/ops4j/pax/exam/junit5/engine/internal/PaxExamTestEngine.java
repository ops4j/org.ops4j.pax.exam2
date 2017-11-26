/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.ops4j.pax.exam.junit5.engine.internal;

import static org.apiguardian.api.API.Status.INTERNAL;

import java.util.Iterator;
import java.util.Optional;
import java.util.ServiceLoader;

import org.apiguardian.api.API;
import org.junit.jupiter.engine.descriptor.JupiterEngineDescriptor;
import org.junit.jupiter.engine.discovery.DiscoverySelectorResolver;
import org.junit.jupiter.engine.execution.JupiterEngineExecutionContext;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestEngine;
import org.junit.platform.engine.UniqueId;
import org.ops4j.pax.exam.junit5.engine.extension.DelegatingExecutionExtension;

/**
 * The JUnit Jupiter {@link org.junit.platform.engine.TestEngine}.
 *
 * @since 5.0
 */
@API(status = INTERNAL, since = "5.0")
public final class PaxExamTestEngine implements TestEngine {

	public static final String ENGINE_ID = "org.ops4j.pax.exam";

	@Override
	public String getId() {
		return ENGINE_ID;
	}

	/**
	 * Returns {@code org.ops4j.pax.exam} as the group ID.
	 */
	@Override
	public Optional<String> getGroupId() {
		return Optional.of(ENGINE_ID);
	}

	/**
	 * Returns {@code junit-jupiter-engine} as the artifact ID.
	 */
	@Override
	public Optional<String> getArtifactId() {
		return Optional.of("pax-exam-junit5-engine");
	}

	@Override
	public TestDescriptor discover(EngineDiscoveryRequest discoveryRequest, UniqueId uniqueId) {
		JupiterEngineDescriptor engineDescriptor = new JupiterEngineDescriptor(uniqueId);
		resolveDiscoveryRequest(discoveryRequest, engineDescriptor);
		return engineDescriptor;
	}

	private void resolveDiscoveryRequest(EngineDiscoveryRequest discoveryRequest,
			JupiterEngineDescriptor engineDescriptor) {
		DiscoverySelectorResolver resolver = new DiscoverySelectorResolver();
		resolver.resolveSelectors(discoveryRequest, engineDescriptor);
		applyDiscoveryFilters(discoveryRequest, engineDescriptor);
	}

	private void applyDiscoveryFilters(EngineDiscoveryRequest discoveryRequest,
			JupiterEngineDescriptor engineDescriptor) {
		new DiscoveryFilterApplier().applyAllFilters(discoveryRequest, engineDescriptor);
	}

	protected JupiterEngineExecutionContext createExecutionContext(ExecutionRequest request) {
		return new JupiterEngineExecutionContext(request.getEngineExecutionListener(),
			request.getConfigurationParameters());
	}

        @Override
        public void execute(ExecutionRequest request) {
            ServiceLoader<DelegatingExecutionExtension> loader = ServiceLoader.load(DelegatingExecutionExtension.class, PaxExamTestEngine.class.getClassLoader());
            Iterator<DelegatingExecutionExtension> it = loader.iterator();

            DelegatingExecutionExtension extension;
                if (! isDelegating(request) && it.hasNext()) {
                    extension = it.next();
                    extension.setExecutionRequest(request);
                }
                else {
                    extension = new ContainerExecutionExtension();
                }
                new PaxExamTestExecutor<>(request, createExecutionContext(request), extension).execute();
        }

        private boolean isDelegating(ExecutionRequest request) {
            return request.getConfigurationParameters().getBoolean("pax.exam.delegating").orElse(false);
        }

}
