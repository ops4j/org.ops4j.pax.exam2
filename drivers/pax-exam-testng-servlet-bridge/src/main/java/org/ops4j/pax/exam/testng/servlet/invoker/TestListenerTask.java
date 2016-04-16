/*
 * Copyright 2016 Harald Wellmann.
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
package org.ops4j.pax.exam.testng.servlet.invoker;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.ops4j.pax.exam.TestContainerException;
import org.ops4j.pax.exam.TestEvent;
import org.ops4j.pax.exam.TestFailure;
import org.ops4j.pax.exam.TestListener;

/**
 * @author Harald Wellmann
 */
public class TestListenerTask implements Runnable {

    private Future<InputStream> is;
    private TestListener delegate;
    private boolean closed;

    public TestListenerTask(Future<InputStream> is, TestListener delegate) {
        this.is = is;
        this.delegate = delegate;
    }

    @Override
    public void run() {
        try (ObjectInputStream ois = new ObjectInputStream(is.get())) {
            while (!closed) {
                Object obj = ois.readObject();
                if (obj instanceof TestEvent) {
                    TestEvent event = (TestEvent) obj;
                    handleEvent(event);
                }
            }
        }
        catch (EOFException exc) {
            closed = true;
        }
        catch (ExecutionException exc) {
            throw new TestContainerException(exc.getCause());
        }
        catch (ClassNotFoundException | IOException | InterruptedException exc) {
            throw new TestContainerException(exc);
        }
    }

    private void handleEvent(TestEvent event) {
        switch (event.getType()) {
            case TEST_ASSUMPTION_FAILED:
                delegate.testAssumptionFailure(
                    new TestFailure(event.getDescription(), event.getException()));
                break;
            case TEST_FAILED:
                delegate.testFailure(new TestFailure(event.getDescription(), event.getException()));
                break;
            case TEST_FINISHED:
                delegate.testFinished(event.getDescription());
                break;
            case TEST_IGNORED:
                delegate.testIgnored(event.getDescription());
                break;
            case TEST_STARTED:
                delegate.testStarted(event.getDescription());
                break;
            default:
                throw new IllegalStateException(event.getType().toString());
        }
    }
}
