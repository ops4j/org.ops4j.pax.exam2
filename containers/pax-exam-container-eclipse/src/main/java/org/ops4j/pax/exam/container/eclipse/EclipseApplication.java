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
import java.util.concurrent.TimeUnit;

import org.ops4j.pax.exam.options.CompositeOption;

/**
 * 
 * Represents an Application running at the eclipse platform
 * 
 * @author Christoph Läubrich
 *
 */
public interface EclipseApplication extends CompositeOption {

    /**
     * ID for E4 Applications
     */
    public static final String E4 = "org.eclipse.e4.ui.workbench.swt.E4Application";

    /**
     * Tells the test-container that the Application has to return the specified value at the end
     * 
     * @param value
     * @return this for chaining
     */
    EclipseApplication mustReturnValue(Object value);

    /**
     * instructs the Container that the Application must be shut down at the end of the test
     * 
     * @return this for chaining
     */
    EclipseApplication mustReturn();

    /**
     * instructs the Container that the Application must return after the given timeout
     * 
     * @param value
     * @param unit
     * @return
     */
    EclipseApplication mustReturnWithin(long value, TimeUnit unit);

    /**
     * instructs the Container that the Application must return and not throw an {@link Exception}
     * 
     * @return this for chaining
     */
    EclipseApplication mustNotFail();

    /**
     * instructs the Container that the Application must return and throw an instance of the given
     * {@link Exception}
     * 
     * @return this for chaining
     */
    EclipseApplication mustFailWith(Class<Throwable> t);

    /**
     * Check the result of the application, only used internally
     * 
     * @param resultFuture
     */
    void checkResult(Future<Object> resultFuture);
}
