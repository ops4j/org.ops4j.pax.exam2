/*
 * Copyright 2014 Harald Wellmann
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.ops4j.pax.exam.regression.maven;

import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.ClientFilter;

/**
 * Filter for Jersey client requests to retry the request up to a given number of times with a given
 * delay between retries.
 * 
 * @author Harald Wellmann
 * 
 */
public class RetryFilter extends ClientFilter {

    private final int maxRetries;
    private final long delay;

    /**
     * Creates retry filter.
     * 
     * @param numRetries
     *            number of retries
     * @param delay
     *            delay between retries in milliseconds
     */
    public RetryFilter(int numRetries, long delay) {
        this.maxRetries = numRetries;
        this.delay = delay;
    }

    @Override
    public ClientResponse handle(ClientRequest request) throws ClientHandlerException {

        int i = 0;

        while (i < maxRetries) {

            i++;

            try {
                return getNext().handle(request);
            }
            catch (ClientHandlerException exc) {

                try {
                    Thread.sleep(delay);
                }
                catch (InterruptedException iexc) {
                    throw new ClientHandlerException(iexc);
                }
            }
        }
        throw new ClientHandlerException("Connection retries limit exceeded.");
    }
}
