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
