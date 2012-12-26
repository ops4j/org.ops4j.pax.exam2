/*
 * Copyright 2009 Alin Dreghiciu.
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
package org.ops4j.pax.exam.options;

/**
 * Abstract implementation of {@link ProvisionControl}.
 * 
 * @author Alin Dreghiciu (adreghiciu@gmail.com)
 * @since 04 27, 2009
 */
public abstract class AbstractProvisionControl<T extends AbstractProvisionControl<T>> {

    /**
     * If the scanned bundles should be updated. Default behaviour is depending on used Test
     * Container implementation.
     */
    private Boolean shouldUpdate;
    /**
     * If the scanned bundles should be started. Default behaviour is depending on used Test
     * Container implementation.
     */
    private Boolean shouldStart;
    /**
     * Start level of scanned bundles. Default behaviour is depending on used Test Container
     * implementation.
     */
    private Integer startLevel;

    /**
     * Constructor.
     */
    public AbstractProvisionControl() {
        shouldUpdate = true;
        shouldStart = true;
    }

    public boolean shouldUpdate() {
        return shouldUpdate;
    }

    public T update(final Boolean _shouldUpdate) {
        this.shouldUpdate = _shouldUpdate;
        return itself();
    }

    public T update() {
        return update(true);
    }

    public T noUpdate() {
        return update(false);
    }

    public boolean shouldStart() {
        return shouldStart;
    }

    public T start(final Boolean _shouldStart) {
        this.shouldStart = _shouldStart;
        return itself();
    }

    public T start() {
        return start(true);
    }

    public T noStart() {
        return start(false);
    }

    public Integer getStartLevel() {
        return startLevel;
    }

    public T startLevel(final Integer _startLevel) {
        this.startLevel = _startLevel;
        return itself();
    }

    /**
     * Implemented by sub classes in order to return itself (this) for fluent api usage
     * 
     * @return itself
     */
    protected abstract T itself();

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((shouldStart == null) ? 0 : shouldStart.hashCode());
        result = prime * result + ((shouldUpdate == null) ? 0 : shouldUpdate.hashCode());
        result = prime * result + ((startLevel == null) ? 0 : startLevel.hashCode());
        return result;
    }

    // CHECKSTYLE:OFF : generated code
    @Override    
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        @SuppressWarnings("unchecked")
        AbstractProvisionControl<T> other = (AbstractProvisionControl<T>) obj;
        if (shouldStart == null) {
            if (other.shouldStart != null)
                return false;
        }
        else if (!shouldStart.equals(other.shouldStart))
            return false;
        if (shouldUpdate == null) {
            if (other.shouldUpdate != null)
                return false;
        }
        else if (!shouldUpdate.equals(other.shouldUpdate))
            return false;
        if (startLevel == null) {
            if (other.startLevel != null)
                return false;
        }
        else if (!startLevel.equals(other.startLevel))
            return false;
        return true;
    }
    // CHECKSTYLE:ON
}
