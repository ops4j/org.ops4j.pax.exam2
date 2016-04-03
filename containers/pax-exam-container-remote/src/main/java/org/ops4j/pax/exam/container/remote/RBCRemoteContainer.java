/*
 * Copyright 2009 Toni Menzel.
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
package org.ops4j.pax.exam.container.remote;

import java.io.InputStream;

import org.ops4j.pax.exam.TestAddress;
import org.ops4j.pax.exam.TestContainer;
import org.ops4j.pax.exam.TestDescription;
import org.ops4j.pax.exam.TestListener;

/**
 * Graceful delegator to underlying target instance. All other TestContainer services are do nothing
 * calls.
 *
 * @author Toni Menzel
 * @since Jan 26, 2010
 */
public class RBCRemoteContainer implements TestContainer {

    private final TestContainer target;

    public RBCRemoteContainer(final TestContainer target) {
        this.target = target;
    }

    @Override
    public void call(TestAddress address) {
        target.call(address);
    }

    @Override
    public long install(String location, InputStream stream) {
        return target.install(location, stream);
    }

    @Override
    public long install(InputStream stream) {
        return install("local", stream);
    }

    @Override
    public TestContainer start() {
        // do nothing
        return this;
    }

    @Override
    public TestContainer stop() {
        return this;
    }

    @Override
    public String toString() {
        return "Remote:" + target.toString();
    }

    @Override
    public long installProbe(InputStream stream) {
        return install(stream);
    }

    @Override
    public void uninstallProbe() {
        target.uninstallProbe();
    }

    @Override
    public void runTest(TestDescription description, TestListener listener) {
        // TODO Auto-generated method stub

    }
}
