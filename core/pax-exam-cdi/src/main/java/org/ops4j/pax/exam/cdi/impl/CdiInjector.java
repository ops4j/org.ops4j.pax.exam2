/*
 * Copyright 2011 Harald Wellmann
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
package org.ops4j.pax.exam.cdi.impl;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionTarget;

import org.ops4j.pax.exam.cdi.BeanManagerLookup;
import org.ops4j.pax.exam.util.Injector;

/**
 * Obtains a CDI {@link BeanManager} and injects fields into the given target.
 * 
 * @author Harald Wellmann
 * 
 */
public class CdiInjector implements Injector
{

    /**
     * Injects dependencies into the given target object whose lifecycle is not managed by the
     * BeanManager itself.
     * 
     * @param target an object with injection points
     */
    @SuppressWarnings( { "rawtypes", "unchecked" } )
    @Override
    public void injectFields( Object target )
    {
        BeanManager mgr = BeanManagerLookup.getBeanManager();
        AnnotatedType annotatedType = mgr.createAnnotatedType( target.getClass() );
        InjectionTarget injectionTarget = mgr.createInjectionTarget( annotatedType );
        CreationalContext context = mgr.createCreationalContext( null );
        injectionTarget.inject( target, context );
    }
}
