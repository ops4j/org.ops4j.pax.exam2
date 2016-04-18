/*
 * Copyright 2016 Harald Wellmann.
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
package org.ops4j.pax.exam.junit;

import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.FrameworkMethod;
import org.ops4j.pax.exam.junit.RunnerExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author hwellmann
 *
 */
public class MockContainerExtension extends RunnerExtension {

    private static final Logger LOG = LoggerFactory.getLogger(MockContainerExtension.class);

    @Override
    public void beforeClassBlock(RunNotifier notifier) {
        LOG.debug("Container beforeClassBlock");
    }

    @Override
    public void afterClassBlock(RunNotifier notifier) {
        LOG.debug("Container afterClassBlock");
    }

    @Override
    public void beforeMethodBlock(FrameworkMethod method) {
        LOG.debug("Container beforeMethodBlock");
    }

    @Override
    public void afterMethodBlock(FrameworkMethod method) {
        LOG.debug("Container afterMethodBlock");
    }

    @Override
    public Object processTestInstance(Object test) {
        LOG.debug("Container processTestInstance");
        return test;
    }
}
