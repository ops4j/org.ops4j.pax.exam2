/*
 * Copyright 2015 Mario Krizmanić.
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

import java.lang.reflect.Method;

import org.ops4j.pax.swissbox.tracker.ServiceLookup;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.launch.Framework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for setting up the {@link Bundle}'s and the {@link Framework}'s start
 * levels.
 *
 * TODO move class to the pax swissbox project, maybe to the {@link org.ops4j.pax.swissbox.core.BundleUtils BundleUtils}
 *      class
 */
public class StartLevelUtil {

    private static final Logger LOG = LoggerFactory.getLogger(StartLevelUtil.class);
 
    /**
     * Hidden utility class constructor
     */
    private StartLevelUtil() {
    }

    /**
     * Safely sets a bundle start level.
     */
    @SuppressWarnings("deprecation")
    public static void setBundleStartLevel(Bundle bundle, BundleContext context, int startLevel) {
        Method adaptMethod = getAdaptMethod(bundle);
        if (adaptMethod != null) {
            org.osgi.framework.startlevel.BundleStartLevel sl;
            try {
                sl = (org.osgi.framework.startlevel.BundleStartLevel)
                    adaptMethod.invoke(bundle, org.osgi.framework.startlevel.BundleStartLevel.class);
            } catch (Exception e) {
                LOG.error("Unable to call adapt method", e);
                throw new RuntimeException(e);
            }
            sl.setStartLevel(startLevel);
        } else {
            org.osgi.service.startlevel.StartLevel sl = ServiceLookup.getService(
                            context,
                            org.osgi.service.startlevel.StartLevel.class);
            sl.setBundleStartLevel(bundle, startLevel);
        }
    }

    /**
     * Safely gets frameworks start level.
     *
     * @return the {@link Framework}'s start level
     */
    @SuppressWarnings("deprecation")
    public static org.osgi.service.startlevel.StartLevel  getFrameworkStartLevel(Framework framework) {
        org.osgi.service.startlevel.StartLevel startLevel = null;
        Method adaptMethod = getAdaptMethod(framework);
        if (adaptMethod != null) {
            org.osgi.framework.startlevel.FrameworkStartLevel fsl;
            try {
                fsl = (org.osgi.framework.startlevel.FrameworkStartLevel)
                adaptMethod.invoke(framework, org.osgi.framework.startlevel.FrameworkStartLevel.class);
            } catch (Exception e) {
                LOG.error("Unable to invoke adapt method", e);
                throw new RuntimeException(e);
            }
            startLevel = new StartLevelAdapter(fsl);
        } else {
            startLevel = ServiceLookup.getService(
                            framework.getBundleContext(),
                            org.osgi.service.startlevel.StartLevel.class);
        }
        return startLevel;
    }

    /**
     * Inspects and returns an <code>adapt</code> method from the {@link Framework}
     * or {@link Bundle} object because it is introduced in the org.osgi.framework 3.7
     * package version and it should be used to resolve the targeted start level.
     * For instance,<pre>
     * {@code
     * BundleStartLevel sl = bundle.adapt(BundleStartLevel.class);
     * sl.setStartLevel(startLevel);
     * }</pre>
     *
     * In the legacy 3.6 org.osgi.framework package version, the {@link org.osgi.service.startlevel.StartLevel StartLevel}
     * service should be resolved and used for setting up the start level.
     *
     * @param object the object that can contain the <code>adapt</code> method
     * @return the <code>adapt</code> method or null if it is not found
     */
    private static Method getAdaptMethod(Object object) {
        Method[] methods = object.getClass().getMethods();
        for (Method method : methods) {
            if ("adapt".equals(method.getName())) {
                Class<?>[] paramTypes = method.getParameterTypes();
                if (paramTypes.length == 1
                        && paramTypes[0].isLocalClass()) {
                    return method;
                }
            }
        }
        return null;
    }
}
