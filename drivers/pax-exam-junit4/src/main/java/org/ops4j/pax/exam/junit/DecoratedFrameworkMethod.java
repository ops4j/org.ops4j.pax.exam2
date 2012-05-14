/*
 * Copyright 2012 Harald Wellmann
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.ops4j.pax.exam.junit;

import org.junit.runners.model.FrameworkMethod;
import org.ops4j.pax.exam.TestAddress;

/**
 * Decorates a framework method with a test address. The caption of the test address
 * is appended to the user visible framework method name, using a colon as a separator.
 * <p>
 * The background is that each method from the test class may be executed in multiple 
 * different configurations or containers.
 * <p>
 * TODO Can we tweak the names so that we can run a single configuration of a given methods
 * from Eclipse? This works for the Parameterized runner, but maybe this is hard-coded in
 * Eclipse...
 * 
 * @author Harald Wellmann
 *
 */
class DecoratedFrameworkMethod extends FrameworkMethod
{
    private final TestAddress address;
    private final FrameworkMethod frameworkMethod;

    DecoratedFrameworkMethod( TestAddress address,
            FrameworkMethod frameworkMethod )
    {
        super( frameworkMethod.getMethod() );
        this.address = address;
        this.frameworkMethod = frameworkMethod;
    }

    @Override
    public String getName()
    {
        return frameworkMethod.getName() + ":" + address.caption();
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ( ( address == null ) ? 0 : address.hashCode() );
        return result;
    }

    @Override
    public boolean equals( Object obj )
    {
        if( this == obj )
            return true;
        if( !super.equals( obj ) )
            return false;
        if( getClass() != obj.getClass() )
            return false;
        DecoratedFrameworkMethod other = (DecoratedFrameworkMethod) obj;
        if( address == null )
        {
            if( other.address != null )
                return false;
        }
        else if( !address.equals( other.address ) )
            return false;
        return true;
    }
}