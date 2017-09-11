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
package org.ops4j.pax.exam.container.eclipse.impl;

import org.ops4j.pax.exam.container.eclipse.EclipseApplicationOption;
import org.ops4j.pax.exam.container.eclipse.EclipseLauncher;
import org.ops4j.pax.exam.container.eclipse.EclipseProvision;

/**
 * default implementation for the eclipse launcher
 * 
 * @author Christoph Läubrich
 *
 */
public final class DefaultEclipseLauncher implements EclipseLauncher {

    private final boolean forked;
    private DefaultEclipseProduct product;
    private final EclipseProvision provision;

    public DefaultEclipseLauncher(boolean forked, EclipseProvision provision) {
        this.forked = forked;
        this.provision = provision;
    }

    @Override
    public EclipseProvision getProvision() {
        return provision;
    }

    @Override
    public boolean isForked() {
        return forked;
    }

    @Override
    public EclipseApplicationOption ignoreApp() {
        return product(null).application(null);
    }

    @Override
    public EclipseApplicationOption application(String applicationID) {
        return product(null).application(applicationID);
    }

    @Override
    public DefaultEclipseProduct product(final String productID) {
        return product = new DefaultEclipseProduct(productID, this);
    }

    public DefaultEclipseProduct getProduct() {
        return product;
    }

}
