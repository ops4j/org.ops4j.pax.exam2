/*
 * Copyright 2011 Toni Menzel.
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
package org.ops4j.pax.exam.container.def.internal;

import org.ops4j.pax.runner.platform.PlatformException;
import org.ops4j.pax.runner.platform.StoppableJavaRunner;

import java.io.File;

/**
 * Very simple asynchronous implementation of Java Runner.
 * Exec is being invoked in a fresh Thread.
 */
public class AsyncJavaRunner implements StoppableJavaRunner {
    final private StoppableJavaRunner m_delegateRunner;

    public AsyncJavaRunner(StoppableJavaRunner delegate) {
        m_delegateRunner = delegate;
    }

    public synchronized void exec(final String[] strings, final String[] strings1, final String s, final String[] strings2, final String s1, final File file) throws PlatformException {
        new Thread("AsyncJavaRunner") {
            @Override
            public void run() {
                try {
                    m_delegateRunner.exec(strings, strings1, s, strings2, s1, file);
                } catch (PlatformException e) {
                    throw new RuntimeException(e);
                }
            }
        }.start();
    }

    public synchronized void shutdown() {
        m_delegateRunner.shutdown();
    }
}
