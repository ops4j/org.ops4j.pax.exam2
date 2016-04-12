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
package org.ops4j.pax.exam.testng.driver;

import java.lang.reflect.Method;

import org.ops4j.pax.exam.TestAddress;
import org.testng.ITestNGMethod;
import org.testng.internal.ClonedMethod;
import org.testng.internal.ConstructorOrMethod;

class ReactorTestNGMethod extends ClonedMethod {

    private static final long serialVersionUID = 1L;
    private TestAddress address;

    public ReactorTestNGMethod(ITestNGMethod method, Method javaMethod, TestAddress address) {
        super(method, javaMethod);
        this.address = address;
    }

    @Override
    public String getMethodName() {
        return String.format("%s:%s", super.getMethodName(), address.caption());
    }

    @Override
    public ConstructorOrMethod getConstructorOrMethod() {
        return new ConstructorOrMethod(getMethod());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((address == null) ? 0 : address.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ReactorTestNGMethod other = (ReactorTestNGMethod) obj;
        if (address == null) {
            if (other.address != null) {
                return false;
            }
        }
        else if (!address.equals(other.address)) {
            return false;
        }
        return true;
    }
}
