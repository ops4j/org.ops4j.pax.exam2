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
package org.ops4j.pax.exam;

import java.io.File;
import java.io.InputStream;

/**
 * Hooks to inject certain activities into pax exam execution phases.
 * All phases can contain code to customize things at different phases.
 */
public abstract class Customizer implements Option
{

    /**
     * Callback method that can  contain steps to finally change/set up the working environment.
     * It is being called just before the OSGi platform of choice boots.
     * An exception at this point will make exam stop and not running the regression. (actually, the regression will fail)
     *
     * @param workingFolder final workingfolder (new File(".")) of your osgi setup. Be careful there,
     *                      the platform may not start if you (for example) delete things in there.
     */
    public void customizeEnvironment( File workingFolder )
    {
    }

    /**
     * Callback that allow to customize the ready built regression probe.
     * Examples are:
     * - need to obfuscate bytecode
     * - need to add checksums
     * - want to share/copy the probe somewhere
     *
     * You can use the Tinybundles library to easily change bits of your bundle on the fly.
     *
     * @param testProbe stream of the probe
     *
     * @return probe to be installed instead of probe.
     *
     * @throws Exception delegate exception handling to container as it would just clutter implementations.
     */
    public InputStream customizeTestProbe( InputStream testProbe )
        throws Exception
    {
        return testProbe;
    }


}
