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
package org.ops4j.pax.exam.spi.container;

import org.ops4j.pax.exam.spi.ProbeCall;

/**
 * @author Toni Menzel
 * @since Jan 11, 2010
 */
public class ClassMethodProbeCall implements ProbeCall
{

    final private String m_clazz;
    final private String m_method;
    private String m_sig;

    public ClassMethodProbeCall( String sig, Class clazz, String method )
    {
    	System.out.println("CALL sig=" + sig + " clazz=" + clazz + " method=" + method);
        m_clazz = clazz.getName();
        m_method = method;
        m_sig = sig;
    }

    public ClassMethodProbeCall( String s )
    {
        // parse:
        String parts[] = s.split( ";" );
        m_clazz = parts[ 0 ];
        m_method = parts[ 1 ];
    	System.out.println("Parsed clazz=" + m_clazz + " method=" + m_method);

    }

    public String getInstruction()
    {
        return m_clazz + ";" + m_method;
    }


    public String signature()
    {
        return m_sig;
    }


}
