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
 * Option specifying a system package (package exported by system bundle).
 * 
 * @author Alin Dreghiciu (adreghiciu@gmail.com)
 * @since 0.3.0, December 08, 2008
 */
public class SystemPackageOption implements ValueOption<String> {

    /**
     * System package (cannot be null or empty).
     */
    private final String pkg;

    /**
     * Constructor.
     * 
     * @param pkg
     *            system package (cannot be null or empty)
     * 
     * @throws IllegalArgumentException
     *             - If package is null or empty
     */
    public SystemPackageOption(final String pkg) {
        validateNotEmpty(pkg, true, "Package");
        this.pkg = pkg;
    }

    /**
     * Getter.
     * 
     * @return system package (cannot be null or empty)
     */
    public String getPackage() {
        return pkg;
    }

    /**
     * Getter.
     * 
     * @return system package (cannot be null or empty)
     */
    public String getValue() {
        return getPackage();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("SystemPackageOption");
        sb.append("{package='").append(pkg).append('\'');
        sb.append('}');
        return sb.toString();
    }

}
