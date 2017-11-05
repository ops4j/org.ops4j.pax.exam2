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
package org.ops4j.pax.exam.regression.multi.inject;

import java.util.logging.Level;

import org.junit.gen5.engine.discovery.ClassSelector;
import org.junit.gen5.launcher.Launcher;
import org.junit.gen5.launcher.TestDiscoveryRequest;
import org.junit.gen5.launcher.listeners.LoggingListener;
import org.junit.gen5.launcher.main.LauncherFactory;
import org.junit.gen5.launcher.main.TestDiscoveryRequestBuilder;

/**
 * @author hwellmann
 *
 */
public class FilterInvoker {

    public static void main(String[] args) {
        Launcher launcher = LauncherFactory.create();
        TestDiscoveryRequest request = TestDiscoveryRequestBuilder.request()
            .select(ClassSelector.forClass(FilterTest.class)).build();
        launcher.registerTestExecutionListeners(LoggingListener.forJavaUtilLogging(Level.FINEST));
        launcher.execute(request);
    }

}
