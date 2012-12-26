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
 * Composite option that will include the options that makes up the composite only in case that a
 * boolean condition is true.
 * 
 * @author Alin Dreghiciu (adreghiciu@gmail.com)
 * @since 0.5.0, April 20, 2009
 */
public class OptionalCompositeOption implements CompositeOption {

    /**
     * Boolean condition to evaluate. Cannot be null;
     */
    private final Condition condition;
    /**
     * Options holder. Cannot be null.
     */
    private final DefaultCompositeOption composite;

    /**
     * Constructor.
     * 
     * @param condition
     *            condition to evaluate
     */
    public OptionalCompositeOption(final Condition condition) {
        this.condition = condition;
        composite = new DefaultCompositeOption();
    }

    /**
     * Convenience constructor.
     * 
     * @param condition
     *            boolean condition to evaluate
     */
    public OptionalCompositeOption(final boolean condition) {
        this(new BooleanCondition(condition));
    }

    /**
     * Adds options to be used in case that condition evaluates to true.
     * 
     * @param options
     *            to use
     * 
     * @return this for fluent api
     */
    public OptionalCompositeOption useOptions(final Option... options) {
        composite.add(options);
        return this;
    }

    /**
     * If condition is true will return the composite options. Otherwise will return an empty array
     * of options. 
     */
    public Option[] getOptions() {
        if (condition.evaluate()) {
            return composite.getOptions();
        }
        return new Option[0];
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((composite == null) ? 0 : composite.hashCode());
        result = prime * result + ((condition == null) ? 0 : condition.hashCode());
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
        OptionalCompositeOption other = (OptionalCompositeOption) obj;
        if (composite == null) {
            if (other.composite != null)
                return false;
        }
        else if (!composite.equals(other.composite))
            return false;
        if (condition == null) {
            if (other.condition != null)
                return false;
        }
        else if (!condition.equals(other.condition))
            return false;
        return true;
    }
    // CHECKSTYLE:ON

    /**
     * Condition to be evaluated.
     */
    public interface Condition {

        /**
         * @return true when the composite option sshould be used
         */
        boolean evaluate();

    }

    /**
     * Boolean based {@link Condition} implementation.
     */
    public static class BooleanCondition implements Condition {

        /**
         * Boolean condition.
         */
        private final boolean condition;

        /**
         * Constructor.
         * 
         * @param condition
         *            boolean consition
         */
        public BooleanCondition(final boolean condition) {
            this.condition = condition;
        }

        public boolean evaluate() {
            return condition;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + (condition ? 1231 : 1237);
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
            BooleanCondition other = (BooleanCondition) obj;
            if (condition != other.condition)
                return false;
            return true;
        }
        // CHECKSTYLE:ON
    }

}
