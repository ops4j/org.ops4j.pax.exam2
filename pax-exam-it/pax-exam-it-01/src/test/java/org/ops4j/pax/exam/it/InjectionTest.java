/*
 * Copyright 2009 Toni Menzel
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.exam.it;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.framework.BundleContext;
import org.ops4j.pax.exam.Inject;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;

/**
 * Inject annotation integration tests.
 *
 * @author Toni Menzel (toni@okidokiteam.com)
 * @since 0.3.0, January 12, 2009
 */
@RunWith( JUnit4TestRunner.class )
public class InjectionTest
{

    /**
     * The value should be injected by Pax Exam.
     */
    @Inject private BundleContext bundleContext;

    /**
     * The value will not be injected by Pax Exam even if annotated because is on an unsupported type.
     */
    @Inject private String unknownType;

    /**
     * The value will not be injected by Pax Exam even if annotated because is not marked with @Inject.
     */
    private BundleContext notMarkedAsInjectable;

    /**
     * Tests if bundleContext is being injected correctly.
     */
    @Test
    public void injectBundleContext()
    {
        assertNotNull( bundleContext );
        assertNull( unknownType );
        assertNull( notMarkedAsInjectable );
    }

    /**
     * Tests that there is no value injected, as even if annotated is not of an supported type.
     */
    @Test
    public void injectUnknownType()
    {
        assertNull( unknownType );
    }

    /**
     * Tests that there is no value injected, because the field is not marked with @Inject.
     */
    @Test
    public void injectBundleContextNotMarked()
    {
        assertNull( notMarkedAsInjectable );
    }

}