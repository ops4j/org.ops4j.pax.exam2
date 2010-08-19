/*
 * Copyright (C) 2010 Okidokiteam
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.exam;

import org.ops4j.pax.exam.libraryoptions.EasyMockBundlesOption;
import org.ops4j.pax.exam.libraryoptions.JMockBundlesOption;
import org.ops4j.pax.exam.libraryoptions.JUnitBundlesOption;
import org.ops4j.pax.exam.libraryoptions.MockitoBundlesOption;

/**
 * Convenience for built in libraries
 */
public class LibraryOptions
{

    /**
     * Utility class. Ment to be used via the static factory methods.
     */
    private LibraryOptions()
    {
        // utility class
    }

    /**
     * Creates a {@link EasyMockBundlesOption}.
     *
     * @return easy mock bundles option
     */
    public static EasyMockBundlesOption easyMockBundles()
    {
        return new EasyMockBundlesOption();
    }

    /**
     * Creates a {@link MockitoBundlesOption}.
     *
     * @return mockito bundles option
     */
    public static MockitoBundlesOption mockitoBundles()
    {
        return new MockitoBundlesOption();
    }

    /**
     * Creates a {@link JUnitBundlesOption}.
     *
     * @return junit bundles option
     */
    public static JUnitBundlesOption junitBundles()
    {
        return new JUnitBundlesOption();
    }

    /**
     * Creates a {@link JMockBundlesOption}.
     *
     * @return jmock bundles option
     */
    public static JMockBundlesOption jmockBundles()
    {
        return new JMockBundlesOption();
    }

   

}
