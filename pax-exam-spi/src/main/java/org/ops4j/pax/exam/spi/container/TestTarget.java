/*
 * Copyright 2009,2010 Toni Menzel.
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

import java.io.InputStream;

/**
 * @author Toni Menzel
 * @since Jan 22, 2010
 */
public interface TestTarget
{

    /**
     * @param serviceType     type of service
     * @param filter          filter (osgi)
     * @param timeoutInMillis timout of request. Zero means: no timeout.
     *
     * @return service (or at least client proxy) of service. Or Null if lookup failed.
     *
     * @throws TestContainerException in case a container problem occured.
     */
    <T> T getService( Class<T> serviceType, String filter, long timeoutInMillis )
        throws TestContainerException;

    /**
     * @param stream stream the content
     *
     * @return Bundle ID
     */
    long installBundle( InputStream stream );

    /**
     * @param id bundleid
     *
     */
    void uninstallBundle( long id );
}
