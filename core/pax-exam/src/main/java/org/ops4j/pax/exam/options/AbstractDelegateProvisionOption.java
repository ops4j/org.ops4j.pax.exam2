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
package org.ops4j.pax.exam.options;

import static org.ops4j.lang.NullArgumentException.validateNotNull;

/**
 * Abstract {@link ProvisionOption} that delegates to another provision option.
 * 
 * @author Alin Dreghiciu (adreghiciu@gmail.com)
 * @since 0.3.0, December 08, 2008
 */
public abstract class AbstractDelegateProvisionOption<T extends AbstractDelegateProvisionOption<?>>
    implements ProvisionOption<T> {

    /**
     * Wrapped provision option (cannot be null).
     */
    private final ProvisionOption<?> delegate;

    /**
     * Constructor.
     * 
     * @param delegate
     *            wrapped provision option (cannot be null)
     * 
     * @throws IllegalArgumentException
     *             - If delegate is null
     */
    protected AbstractDelegateProvisionOption(final ProvisionOption<?> delegate) {
        validateNotNull(delegate, "Delegate");
        this.delegate = delegate;
    }

    public String getURL() {
        return delegate.getURL();
    }

    public boolean shouldUpdate() {
        return delegate.shouldUpdate();
    }

    public boolean shouldStart() {
        return delegate.shouldStart();
    }

    public Integer getStartLevel() {
        return delegate.getStartLevel();
    }

    public T update(final Boolean shouldUpdate) {
        delegate.update(shouldUpdate);
        return itself();
    }

    public T update() {
        delegate.update();
        return itself();
    }

    public T noUpdate() {
        delegate.noUpdate();
        return itself();
    }

    public T start(final Boolean shouldStart) {
        delegate.start(shouldStart);
        return itself();
    }

    public T start() {
        delegate.start();
        return itself();
    }

    public T noStart() {
        delegate.noStart();
        return itself();
    }

    public T startLevel(final Integer startLevel) {
        delegate.startLevel(startLevel);
        return itself();
    }

    /**
     * Getter.
     * 
     * @return wrapped provision option (cannot be null)
     */
    public ProvisionOption<?> getDelegate() {
        return delegate;
    }

    /**
     * Implemented by sub classes in order to return itself (this) for fluent api usage
     * 
     * @return itself
     */
    protected abstract T itself();

}
