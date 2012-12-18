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
package org.ops4j.pax.exam.options;

import org.ops4j.pax.exam.Option;

/**
 * Option specifying initial bundle start level.
 *
 * @author Alin Dreghiciu (adreghiciu@gmail.com)
 * @since 0.5.0, April 23, 2009
 */
public class BundleStartLevelOption
    implements Option
{

    /**
     * Start level.
     */
    private final int startLevel;

    /**
     * Constructor.
     *
     * @param startLevel initial bundle start level (must be bigger then zero)
     *
     * @throws IllegalArgumentException - If start level is <= 0
     */
    public BundleStartLevelOption( final int startLevel )
    {
        if( startLevel <= 0 )
        {
            throw new IllegalArgumentException( "Start level must be bigger then zero" );
        }
        this.startLevel = startLevel;
    }

    /**
     * Getter.
     *
     * @return startlevel (bigger then zero)
     */
    public int getStartLevel()
    {
        return startLevel;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return new StringBuilder()
            .append( BundleStartLevelOption.class.getSimpleName() )
            .append( "{startlevel='" ).append( startLevel ).append( "\'}" )
            .toString();
    }

}