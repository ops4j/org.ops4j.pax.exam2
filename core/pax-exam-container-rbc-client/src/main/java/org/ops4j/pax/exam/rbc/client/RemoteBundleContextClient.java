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
package org.ops4j.pax.exam.rbc.client;

import java.io.InputStream;

import org.ops4j.pax.exam.RelativeTimeout;
import org.ops4j.pax.exam.TestAddress;
import org.ops4j.pax.exam.rbc.internal.RemoteBundleContext;

/**
 * A {@link RemoteBundleContext} client, that takes away RMI handling.
 * 
 * @author Alin Dreghiciu (adreghiciu@gmail.com)
 * @author Toni Menzel (toni@okidokiteam.com)
 * @since 0.3.0, December 15, 2008
 */
public interface RemoteBundleContextClient {

    long install(String location, InputStream stream);

    void cleanup();

    void setBundleStartLevel(final long bundleId, final int startLevel);

    void start();

    void stop();

    void waitForState(final long bundleId, final int state, final RelativeTimeout timeout);

    void call(TestAddress address);
}
