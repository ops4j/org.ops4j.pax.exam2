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

import org.ops4j.pax.exam.container.eclipse.EclipseLauncher;
import org.ops4j.pax.exam.container.eclipse.EclipseProduct;

/**
 * Default implementation for an EclipseProduct
 * 
 * @author Christoph Läubrich
 *
 */
public final class DefaultEclipseProduct implements EclipseProduct {

    private final String productID;
    private final EclipseLauncher launcher;
    private DefaultEclipseApplication application;

    public DefaultEclipseProduct(String productID, EclipseLauncher launcher) {
        this.productID = productID;
        this.launcher = launcher;
    }

    @Override
    public DefaultEclipseApplication application(String applicationID) {
        return application = new DefaultEclipseApplication(this, applicationID);
    }

    @Override
    public EclipseLauncher getLauncher() {
        return launcher;
    }

    @Override
    public String productID() {
        return productID;
    }

    public DefaultEclipseApplication getApplication() {
        return application;
    }

}
