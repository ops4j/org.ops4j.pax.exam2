/*
 * Copyright 2009 Alin Dreghiciu.
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

import java.util.Collection;
import org.junit.internal.runners.TestClass;

/**
 * Configuration strategy. Finds configuration methods.
 *
 * @author Alin Dreghiciu (adreghiciu@gmail.com)
 * @since 0.3.0, February 03, 2009
 */
public interface JUnit4ConfigMethods
{

    /**
     * Returns configuration methods determined by this strategy.
     *
     * @param testClass    the regression class
     * @param testInstance an instance of the regression class
     *
     * @return configuration options (cannot be null, but can be empty)
     *
     * @throws Exception - If finding configuration methods fails
     */
    Collection<? extends JUnit4ConfigMethod> getConfigMethods( TestClass testClass, Object testInstance )
        throws Exception;

}
