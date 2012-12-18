/*
 * Copyright 2011 Toni Menzel.
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
package org.ops4j.pax.exam.testforge;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.util.tracker.ServiceTracker;
import org.ops4j.pax.exam.TestContainerException;

/**
 * Simple pre-build test to be used with Pax Exam Player.
 * 
 * Takes an Interface Name as argument and tries to acquire that service in certain timespan.
 * 
 */
public class WaitForService {

    public void probe(BundleContext ctx, String servicename, Integer wait)
        throws InterruptedException, InvalidSyntaxException {
        if (wait == null) {
            wait = 1000;
        }
        ServiceTracker tracker = new ServiceTracker(ctx, servicename, null);
        tracker.open(true);
        long start = System.currentTimeMillis();

        while (((tracker.getTrackingCount()) == 0) && (start + wait > System.currentTimeMillis())) {
            Thread.sleep(100);
        }
        int c = tracker.getTrackingCount();
        tracker.close();
        if (c == 0) {
            throw new TestContainerException("Service " + servicename
                + " has not been available in time.");
        }
    }
}
