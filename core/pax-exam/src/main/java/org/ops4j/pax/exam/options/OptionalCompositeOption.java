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

import org.ops4j.pax.exam.Option;

/**
 * Composite option that will include the options that makes up the composite only in case that a boolean condition is
 * true.
 *
 * @author Alin Dreghiciu (adreghiciu@gmail.com)
 * @since 0.5.0, April 20, 2009
 */
public class OptionalCompositeOption
    implements CompositeOption
{

    /**
     * Boolean condition to evaluate. Cannot be null;
     */
    private final Condition m_condition;
    /**
     * Options holder. Cannot be null.
     */
    private final DefaultCompositeOption m_composite;

    /**
     * Constructor.
     *
     * @param condition condition to evaluate
     */
    public OptionalCompositeOption( final Condition condition )
    {
        m_condition = condition;
        m_composite = new DefaultCompositeOption();
    }

    /**
     * Convenience constructor.
     *
     * @param condition boolean condition to evaluate
     */
    public OptionalCompositeOption( final boolean condition )
    {
        this( new BooleanCondition( condition ) );
    }

    /**
     * Adds options to be used in case that condition evaluates to true.
     *
     * @param options to use
     *
     * @return this for fluent api
     */
    public OptionalCompositeOption useOptions( final Option... options )
    {
        m_composite.add( options );
        return this;
    }

    /**
     * If condition is true will return the composite options. Otherwise will return an empty array of options.
     * {@inheritDoc}
     */
    public Option[] getOptions()
    {
        if( m_condition.evaluate() )
        {
            return m_composite.getOptions();
        }
        return new Option[0];
    }

    /**
     * Condition to be evaluated.
     */
    public static interface Condition
    {

        /**
         * @return true when the composite option sshould be used
         */
        boolean evaluate();

    }

    /**
     * Boolean based {@link Condition} implementation.
     */
    public static class BooleanCondition
        implements Condition
    {

        /**
         * Boolean condition.
         */
        private final boolean m_condition;

        /**
         * Constructor.
         *
         * @param condition boolean consition
         */
        public BooleanCondition( final boolean condition )
        {
            m_condition = condition;
        }

        /**
         * {@inheritDoc}
         */
        public boolean evaluate()
        {
            return m_condition;
        }

    }

}
