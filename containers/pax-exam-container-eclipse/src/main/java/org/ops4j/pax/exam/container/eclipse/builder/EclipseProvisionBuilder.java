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
package org.ops4j.pax.exam.container.eclipse.builder;

import java.io.IOException;

import org.ops4j.pax.exam.container.eclipse.EclipseArtifactSource;
import org.ops4j.pax.exam.container.eclipse.EclipseEnvironment;
import org.ops4j.pax.exam.container.eclipse.EclipseOptions;
import org.ops4j.pax.exam.container.eclipse.EclipseProvision;

/**
 * Builder that collects information for creating a provision
 * 
 * @author Christoph Läubrich
 *
 */
public abstract class EclipseProvisionBuilder<O, T extends EclipseProvisionBuilder<O, T>> {

    private String[] ignoreItems;
    private EclipseEnvironment environment;
    private EclipseArtifactSource source;

    @SuppressWarnings("unchecked")
    public T ignore(String... ignoreItems) {
        this.ignoreItems = ignoreItems;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T env(EclipseEnvironment environment) {
        this.environment = environment;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T source(EclipseArtifactSource source) {
        this.source = source;
        return (T) this;
    }

    private String[] getIgnore() {
        if (ignoreItems == null) {
            return new String[0];
        }
        return ignoreItems;
    }

    private EclipseEnvironment getEnv() {
        if (environment == null) {
            return EclipseOptions.getSystemEnvironment();
        }
        return environment;
    }

    private EclipseArtifactSource getSource() {
        if (source == null) {
            return EclipseOptions.combine();
        }
        return source;
    }

    protected EclipseProvision provision() {
        return EclipseOptions.provision(getSource(), getEnv(), getIgnore());
    }

    public abstract O create() throws IOException;

}
