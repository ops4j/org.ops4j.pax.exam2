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
package org.ops4j.pax.exam.rbc.internal;

/**
 * Thrown when a particular sercice cannot be found.
 * 
 * @author Alin Dreghiciu (adreghiciu@gmail.com)
 * @since 0.3.0, December 15, 2008
 */
public class NoSuchServiceException extends Exception {

    private static final long serialVersionUID = 2694795422298434762L;
    /**
     * Class of the service that was not found. Cannot be null.
     */
    private final Class<?> serviceType;

    /**
     * Constructor.
     * 
     * @param serviceType
     *            class of the service that was not found (cannot be null)
     */
    public NoSuchServiceException(final Class<?> serviceType) {
        super("No service of type [" + serviceType.getName() + "] found in the service registry");
        this.serviceType = serviceType;
    }

    /**
     * Getter.
     * 
     * @return class of the service that was not found
     */
    public Class<?> getServiceType() {
        return serviceType;
    }

}
