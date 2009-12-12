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
package org.ops4j.pax.exam.container.def.options;

import org.ops4j.pax.exam.options.TimeoutOption;

/**
 * Option specifying the timeout (in milliseconds) while looking up the container process.
 *
 * @author Alin Dreghiciu (adreghiciu@gmail.com)
 * @since 0.3.0 December 10, 2008
 */
public class RBCLookupTimeoutOption
    extends TimeoutOption
{

    /**
     * Constructor.
     *
     * @param timeoutInMillis timeout (in millis) to look up the server part
     *
     * @throws IllegalArgumentException - If timeout is null
     */
    public RBCLookupTimeoutOption( final long timeoutInMillis )
    {
        super( timeoutInMillis );
    }

}
