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
public abstract class AbstractProvisionControl<T extends AbstractProvisionControl>
    implements ProvisionControl<T>
{

    /**
     * If the scanned bundles should be updated. Default behaviour is depending on used Test Container implementation.
     */
    private Boolean m_shouldUpdate;
    /**
     * If the scanned bundles should be started. Default behaviour is depending on used Test Container implementation.
     */
    private Boolean m_shouldStart;
    /**
     * Start level of scanned bundles. Default behaviour is depending on used Test Container implementation.
     */
    private Integer m_startLevel;

    /**
     * Constructor.
     */
    public AbstractProvisionControl()
    {
        m_shouldUpdate = true;
        m_shouldStart = true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean shouldUpdate()
    {
        return m_shouldUpdate;
    }

    /**
     * {@inheritDoc}
     */
    public T update( final Boolean shouldUpdate )
    {
        m_shouldUpdate = shouldUpdate;
        return itself();
    }

    /**
     * {@inheritDoc}
     */
    public T update()
    {
        return update( true );
    }

    /**
     * {@inheritDoc}
     */
    public T noUpdate()
    {
        return update( false );
    }

    /**
     * {@inheritDoc}
     */
    public boolean shouldStart()
    {
        return m_shouldStart;
    }

    /**
     * {@inheritDoc}
     */
    public T start( final Boolean shouldStart )
    {
        m_shouldStart = shouldStart;
        return itself();
    }

    /**
     * {@inheritDoc}
     */
    public T start()
    {
        return start( true );
    }

    /**
     * {@inheritDoc}
     */
    public T noStart()
    {
        return start( false );
    }

    /**
     * {@inheritDoc}
     */
    public Integer getStartLevel()
    {
        return m_startLevel;
    }

    /**
     * {@inheritDoc}
     */
    public T startLevel( final Integer startLevel )
    {
        m_startLevel = startLevel;
        return itself();
    }

    /**
     * Implemented by sub classes in order to return itself (this) for fluent api usage
     *
     * @return itself
     */
    protected abstract T itself();

}
