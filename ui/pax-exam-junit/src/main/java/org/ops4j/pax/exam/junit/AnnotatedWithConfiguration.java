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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.junit.internal.runners.TestClass;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.internal.AppliesToConfigMethod;
import org.ops4j.pax.exam.junit.internal.DefaultConfigMethod;

/**
 * Configuration strategy that will consider as configuration any static/non static methods that are marked with
 * {@link Configuration} annotation, has no parameters and has an {@link Option} array as return value.
 *
 * @author Alin Dreghiciu (adreghiciu@gmail.com)
 * @since 0.3.0, February 03, 2009
 */
public class AnnotatedWithConfiguration
    implements JUnit4ConfigMethods
{

    /**
     * {@inheritDoc}
     */
    public Collection<? extends JUnit4ConfigMethod> getConfigMethods( final TestClass testClass,
                                                                      final Object testInstance )
    {
        final List<JUnit4ConfigMethod> configMethods = new ArrayList<JUnit4ConfigMethod>();
        for( Method configMethod : testClass.getAnnotatedMethods( Configuration.class ) )
        {
            if( !Modifier.isAbstract( configMethod.getModifiers() )
                && configMethod.getParameterTypes() != null
                && configMethod.getParameterTypes().length == 0
                && configMethod.getReturnType().isArray()
                && Option.class.isAssignableFrom( configMethod.getReturnType().getComponentType() ) )
            {
                final AppliesTo appliesToAnnotation = configMethod.getAnnotation( AppliesTo.class );
                if( appliesToAnnotation != null )
                {
                    configMethods.add(
                        new AppliesToConfigMethod(
                            configMethod,
                            Modifier.isStatic( configMethod.getModifiers() ) ? null : testInstance
                        )
                    );
                }
                else
                {
                    configMethods.add(
                        new DefaultConfigMethod(
                            configMethod, Modifier.isStatic( configMethod.getModifiers() ) ? null : testInstance
                        )
                    );
                }
            }
        }
        return configMethods;
    }

}
