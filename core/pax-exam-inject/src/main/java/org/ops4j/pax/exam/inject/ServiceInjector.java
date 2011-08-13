/*
 * Copyright 2011 Harald Wellmann.
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
package org.ops4j.pax.exam.inject;

import java.lang.reflect.Field;

import javax.inject.Inject;

import org.ops4j.pax.exam.util.Injector;
import org.ops4j.pax.exam.util.ServiceLookup;
import org.osgi.framework.BundleContext;

public class ServiceInjector implements Injector
{
    public void injectFields( BundleContext bc, Object target )
    {
        Class<?> targetClass = target.getClass();
        while ( targetClass != Object.class )
        {
            injectDeclaredFields( bc, target, targetClass );
            targetClass = targetClass.getSuperclass();
        }
    }

    private void injectDeclaredFields( BundleContext bc, Object target, Class<?> targetClass )
    {
        for ( Field field : targetClass.getDeclaredFields() )
        {
            if( field.getAnnotation( Inject.class ) != null )
            {
                injectField( bc, target, field );
            }
        }
    }

    private void injectField( BundleContext bc, Object target, Field field )
    {
        Class<?> type = field.getType();
        Object service = (BundleContext.class == type) ? bc : ServiceLookup.getService( bc, type );
        try
        {
            if( field.isAccessible() )
            {
                field.set( target, service );
            }
            else
            {
                field.setAccessible( true );
                try
                {
                    field.set( target, service );
                }
                finally
                {
                    field.setAccessible( false );
                }
            }
        }
        catch ( IllegalAccessException exc )
        {
            throw new RuntimeException( exc );
        }
    }
}
