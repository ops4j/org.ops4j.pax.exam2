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
package org.ops4j.pax.exam.spi.intern;

import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ops4j.pax.exam.TestAddress;

/**
 *
 */
public class DefaultTestAddress implements TestAddress {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultTestAddress.class);

    private final String sig;
    private final TestAddress root;
    private final String caption;
    private final Object[] args;

    public DefaultTestAddress(String caption, Object... args) {
        this(null, caption, args);
    }

    public DefaultTestAddress(final TestAddress parent, String caption, Object... args) {
        sig = calculate();
        if (parent != null) {
            this.caption = parent.caption() + ":" + caption;
            this.args = parent.arguments();
        }
        else {
            this.caption = caption;
            this.args = args;
        }

        root = calculateRoot(parent);
        LOG.debug("NEW ADDRESS= " + sig + " parent=" + parent + " root=" + root + " args="
            + args.toString());
    }

    private String calculate() {
        // TODO just a temporary provider. ID should be given from a passed in context.
        return "PaxExam-" + UUID.randomUUID().toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((caption == null) ? 0 : caption.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        DefaultTestAddress other = (DefaultTestAddress) obj;
        if (caption == null) {
            if (other.caption != null) {
                return false;
            }
        }
        else if (!caption.equals(other.caption)) {
            return false;
        }
        return true;
    }

    public String identifier() {
        return sig;
    }

    public String caption() {
        return caption;
    }

    private TestAddress calculateRoot(TestAddress parent) {
        if (parent != null) {
            return parent.root();
        }
        else {
            return this;
        }
    }

    public TestAddress root() {
        return root;
    }

    public Object[] arguments() {
        return args;
    }

    @Override
    public String toString() {
        return "[TestAddress:" + sig + " root:" + root.identifier() + "]";
    }
}
