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
package org.ops4j.pax.exam.container.eclipse.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.TestContainerException;
import org.ops4j.pax.exam.container.eclipse.EclipseApplication;
import org.ops4j.pax.exam.container.eclipse.EclipseApplicationOption;
import org.ops4j.pax.exam.container.eclipse.EclipseLauncher;
import org.ops4j.pax.exam.container.eclipse.EclipseOptions;
import org.ops4j.pax.exam.container.eclipse.EclipsePlatformOption;

/**
 * Implementation of the {@link EclipseApplication} interface, use {@link EclipseOptions} to
 * construct instance of this
 * 
 * @author Christoph Läubrich
 *
 */
public final class EclipseApplicationImpl implements EclipseApplication {

    private Collection<Option> options = new ArrayList<>();
    private Object returnValue;
    private boolean mustReturnValue;
    private boolean mustReturn;
    private long timeoutValue = 5;
    private TimeUnit timeUnit = TimeUnit.SECONDS;
    private boolean mustNotFail;
    private Class<Throwable> expectedThrowable;

    public EclipseApplicationImpl(final EclipseLauncher launcher, boolean ignore,
        Option... options) {
        if (options != null) {
            this.options.addAll(Arrays.asList(options));
        }
        this.options.add(new EclipsePlatformOption() {

            @Override
            public EclipseLauncher getLauncher() {
                return launcher;
            }
        });
        this.options.add(new EclipseApplicationOption() {

            @Override
            public void checkResult(Future<Object> resultFuture) {
                if (!mustReturn) {
                    // don't care if it must not return at all...
                    return;
                }
                try {
                    Object actualValue = resultFuture.get(timeoutValue, timeUnit);
                    if (mustReturnValue) {
                        if (actualValue == null) {
                            if (returnValue != null) {
                                throw new TestContainerException("Application returned null but "
                                    + returnValue + " was expected!");
                            }
                        }
                        else if (!actualValue.equals(returnValue)) {
                            throw new TestContainerException("Application returned " + actualValue
                                + " but " + returnValue + " was expected!");
                        }
                    }
                }
                catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                catch (ExecutionException ex) {
                    Throwable e = ex.getCause();
                    if (mustNotFail || (expectedThrowable == null && e != null)) {
                        throw new TestContainerException(
                            "Execution of EclipseApplication has failed!", e);
                    }
                    else if (e != null && e.getClass().isAssignableFrom(expectedThrowable)) {
                        // all fine!
                        return;
                    }
                    else {
                        throw new TestContainerException("Application has thrown " + e + " but "
                            + expectedThrowable.getName() + " was expected!");
                    }
                }
                catch (TimeoutException e) {
                    throw new TestContainerException(
                        "Waiting for EclipseApplication to return timed out!");
                }
                if (expectedThrowable != null) {
                    throw new TestContainerException(
                        "Application has not thrown " + expectedThrowable.getName());
                }
            }

            @Override
            public boolean isIgnore() {
                return ignore;
            }
        });
    }

    @Override
    public Option[] getOptions() {
        return options.toArray(new Option[0]);
    }

    @Override
    public EclipseApplication mustReturnValue(Object value) {
        mustReturn();
        mustNotFail();
        this.returnValue = value;
        this.mustReturnValue = true;
        return this;
    }

    @Override
    public EclipseApplication mustReturn() {
        this.mustReturn = true;
        return this;
    }

    @Override
    public EclipseApplication mustReturnWithin(long value, TimeUnit unit) {
        timeoutValue = value;
        timeUnit = unit;
        mustReturn();
        return this;
    }

    @Override
    public EclipseApplication mustNotFail() {
        mustNotFail = true;
        mustReturn();
        return this;
    }

    @Override
    public EclipseApplication mustFailWith(Class<Throwable> t) {
        this.expectedThrowable = t;
        mustNotFail = false;
        mustReturnValue = false;
        mustReturn();
        return this;
    }

}
