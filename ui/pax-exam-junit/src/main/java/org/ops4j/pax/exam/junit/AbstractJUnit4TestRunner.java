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
package org.ops4j.pax.exam.junit;

import java.lang.reflect.Method;
import java.util.Collection;
import org.junit.internal.runners.InitializationError;
import org.ops4j.pax.exam.Option;

/**
 * This provides a convenient way to write a self-Runner that can be set in
 * {@RunWith} annotation.
 *
 * Extend this class and implement the getTestOptions method of a system wide default setup.
 * Now, writing tests for *your* system is just a matter of setting
 * {@RunWith(Yourclass.class)}
 *
 * @author Toni Menzel (tonit)
 * @since Mar 17, 2009
 */
public abstract class AbstractJUnit4TestRunner extends JUnit4TestRunner
{

    public AbstractJUnit4TestRunner( Class<?> aClass )
        throws InitializationError
    {
        super( aClass );
    }

    @Override
    protected Collection<JUnit4ConfigMethod> getConfigurationMethods()
        throws Exception
    {
        Collection<JUnit4ConfigMethod> configurationMethods = super.getConfigurationMethods();
        configurationMethods.add( new JUnit4ConfigMethod()
        {
            public boolean matches( Method testMethod )
            {
                return true;
            }

            public Option[] getOptions()
                throws Exception
            {
                return getTestOptions();
            }
        }
        );
        return configurationMethods;
    }

    protected abstract Option[] getTestOptions();


}

