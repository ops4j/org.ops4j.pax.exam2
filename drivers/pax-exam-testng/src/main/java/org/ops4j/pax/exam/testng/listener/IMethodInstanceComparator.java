/*
 * Copyright 2012 Harald Wellmann
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
package org.ops4j.pax.exam.testng.listener;

import java.util.Comparator;

import org.testng.IMethodInstance;
import org.testng.ITestNGMethod;

public class IMethodInstanceComparator implements Comparator<IMethodInstance>
{

    @Override
    public int compare( IMethodInstance left, IMethodInstance right )
    {
        ITestNGMethod m1 = left.getMethod();
        ITestNGMethod m2 = right.getMethod();
        int result = m1.getRealClass().getName().compareTo( m2.getRealClass().getName() );
        if (result == 0)
        {
            result = m1.getMethodName().compareTo( m2.getMethodName() );
        }
        return result;
    }
}
