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
package org.ops4j.pax.exam;

import java.io.Serializable;

/**
 * Timout type that is being used in exam to specify a single timeout value but control different timout points in the system with that value.
 * In most cases, clients will just use the value directly as time in milliseconds.
 *
 * However, this type also says that the value is just a (possibly user specified) "hint" and not mean the exaclt value.
 * You can see it as a relative timeout value.
 *
 * @author Toni Menzel (toni@okidokiteam.com)
 */
public class RelativeTimeout implements Serializable {

    private static final long serialVersionUID = 3490846022856083260L;
    public final static RelativeTimeout TIMEOUT_NOWAIT = new RelativeTimeout( 0L );
    public final static RelativeTimeout TIMEOUT_NOTIMEOUT = new RelativeTimeout( Long.MAX_VALUE );

    public final static RelativeTimeout TIMEOUT_DEFAULT = new RelativeTimeout( 1000L * 180L );

    final private long value;
    final private long lower;
    final private long upper;

    public RelativeTimeout( final long time )
    {
        value = time;
        lower = time / 2;
        upper = time * 2;
    }

    /**
     * @return Default Timeout value.
     */
    public long getValue()
    {
        return value;
    }

    /**
     * @return Tendentiative lower timeout value. This is relative to {@link #getValue()}
     */
    public long getLowerValue()
    {
        return lower;
    }

    /**
     * @return Tendentiative higher timeout value. This is relative to {@link #getValue()}
     */
    public long getUpperValue()
    {
        return upper;
    }

    public boolean isNoWait()
    {
        return this.equals( TIMEOUT_NOWAIT );
    }

    public boolean isNoTimeout()
    {
        return this.equals( TIMEOUT_NOTIMEOUT );
    }

    public boolean isDefault()
    {
        return this.equals( TIMEOUT_DEFAULT );
    }

    @Override
    public boolean equals( Object obj )
    {
        if( obj instanceof RelativeTimeout ) {
            return ( (RelativeTimeout) obj ).getValue() == getValue();
        }
        else { return false; }
    }

    @Override
    public int hashCode()
    {
        return getClass().getName().hashCode() + new Long( value ).hashCode();
    }

    @Override
    public String toString()
    {
        return "[ RelativeTimeout value = " + value + " ]";
    }

}
