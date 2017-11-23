/*
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
package org.ops4j.pax.exam.container.eclipse;

import java.util.concurrent.Future;

import org.ops4j.pax.exam.Option;

/**
 * 
 * The sole purpose of this is that we can validate the result of an application run and look-up
 * this in the paxExam Options
 * 
 * @author Christoph Läubrich
 *
 */
public interface EclipseApplicationOption extends EclipseApplication, Option {

    public static final String PROPERTY_APPLICATION_ID = "eclipse.application";
    public static final String PROPERTY_PRODUCT_ID = "eclipse.product";

    /**
     * Check the result of the application, only used internally
     * 
     * @param resultFuture
     */
    void checkResult(Future<Object> resultFuture);

}
