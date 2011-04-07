/*
 * Copyright 2011 Toni Menzel.
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
package org.ops4j.pax.exam.swoosh;

import org.ops4j.pax.exam.TestAddress;

/**
 * 
 */
public class ParameterizedAddress implements TestAddress {

    private Object[] m_args;
    private TestAddress m_address;

    ParameterizedAddress (TestAddress address, Object... args) {
        m_address = address;
        m_args = args;
    }

    public String identifier()
    {
        return m_address.identifier();
    }

    public String caption()
    {
        return m_address.caption();
    }

    public TestAddress root()
    {
        // we are root! We need to be able to cast the adress to ParametrizedAddress at a later point!
        return this;
    }

    public Object[] arguments() {
        return m_args;
    }
}
