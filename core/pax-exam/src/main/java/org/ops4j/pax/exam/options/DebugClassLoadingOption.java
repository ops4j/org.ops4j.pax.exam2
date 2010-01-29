/*
 * Copyright 2008 Alin Dreghiciu.
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
 * Options enabling framework specific class loading debugging.
 *
 * @author Alin Dreghiciu (adreghiciu@gmail.com)
 * @since 1.2.0, September 22, 2009
 */
public class DebugClassLoadingOption
    implements Option
{

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder();
        sb.append( DebugClassLoadingOption.class.getSimpleName() );
        sb.append( "{on}" );
        return sb.toString();
    }

}