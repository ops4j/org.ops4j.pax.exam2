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

import static org.ops4j.lang.NullArgumentException.*;

/**
 * Abstract {@link ProvisionOption} that delegates to another provision option.
 *
 * @author Alin Dreghiciu (adreghiciu@gmail.com)
 * @since 0.3.0, December 08, 2008
 */
public abstract class AbstractDelegateProvisionOption<T extends AbstractDelegateProvisionOption>
    implements ProvisionOption<T>
{

    /**
     * Wrapped provision option (cannot be null).
     */
    private final ProvisionOption m_delegate;

    /**
     * Constructor.
     *
     * @param delegate wrapped provision option (cannot be null)
     *
     * @throws IllegalArgumentException - If delegate is null
     */
    protected AbstractDelegateProvisionOption( final ProvisionOption delegate )
    {
        validateNotNull( delegate, "Delegate" );
        m_delegate = delegate;
    }

    /**
     * {@inheritDoc}
     */
    public String getURL()
    {
        return m_delegate.getURL();
    }

    /**
     * {@inheritDoc}
     */
    public boolean shouldUpdate()
    {
        return m_delegate.shouldUpdate();
    }

    /**
     * {@inheritDoc}
     */
    public boolean shouldStart()
    {
        return m_delegate.shouldStart();
    }

    /**
     * {@inheritDoc}
     */
    public Integer getStartLevel()
    {
        return m_delegate.getStartLevel();
    }

    /**
     * {@inheritDoc}
     */
    public T update( final Boolean shouldUpdate )
    {
        m_delegate.update( shouldUpdate );
        return itself();
    }

    /**
     * {@inheritDoc}
     */
    public T update()
    {
        m_delegate.update();
        return itself();
    }

    /**
     * {@inheritDoc}
     */
    public T noUpdate()
    {
        m_delegate.noUpdate();
        return itself();
    }

    /**
     * {@inheritDoc}
     */
    public T start( final Boolean shouldStart )
    {
        m_delegate.start( shouldStart );
        return itself();
    }

    /**
     * {@inheritDoc}
     */
    public T start()
    {
        m_delegate.start();
        return itself();
    }

    /**
     * {@inheritDoc}
     */
    public T noStart()
    {
        m_delegate.noStart();
        return itself();
    }

    /**
     * {@inheritDoc}
     */
    public T startLevel( final Integer startLevel )
    {
        m_delegate.startLevel( startLevel );
        return itself();
    }

    /**
     * Getter.
     *
     * @return wrapped provision option (cannot be null)
     */
    public ProvisionOption getDelegate()
    {
        return m_delegate;
    }

    /**
     * Implemented by sub classes in order to return itself (this) for fluent api usage
     *
     * @return itself
     */
    protected abstract T itself();


}