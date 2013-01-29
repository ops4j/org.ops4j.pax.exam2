/*
 * Copyright 2013 Christoph Läubrich
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
package org.ops4j.pax.exam.osgi.internal.obr;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.TestContainerException;
import org.ops4j.pax.exam.osgi.OBRBundleOption;
import org.ops4j.pax.exam.osgi.OBRRepositoryOption;
import org.ops4j.pax.exam.osgi.OBRValidation;
import org.ops4j.pax.tinybundles.core.TinyBundle;
import org.ops4j.pax.tinybundles.core.TinyBundles;
import org.osgi.framework.Constants;

/**
 * provides implementation for the {@link OBRRepositoryOption}
 */
public class OBRRepositoryProvisionOption implements OBRRepositoryOption {

    private final Set<String>    urlsList    = new HashSet<String>();
    private final List<String[]> bundlesList = new ArrayList<String[]>();

    @Override
    public OBRRepositoryOption repository(String... urls) {
        urlsList.addAll(Arrays.asList(urls));
        return this;
    }

    @Override
    public Option toOption() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ObjectOutputStream os;
        try {
            os = new ObjectOutputStream(outputStream);
            os.writeObject(urlsList);
            os.writeObject(bundlesList);
            os.flush();
            os.close();
        } catch (IOException e) {
            throw new TestContainerException("can't write stream headers", e);
        }
        ByteArrayInputStream stream = new ByteArrayInputStream(outputStream.toByteArray());
        TinyBundle bundle = TinyBundles.bundle();
        bundle.add("obrdata.obj", stream);
        bundle.add(OBROptionActivator.class);
        bundle.add(OBRResolverRunnable.class);
        bundle.add(OBRValidation.class);
        bundle.set(Constants.BUNDLE_SYMBOLICNAME, "PAXExamOBROption-" + UUID.randomUUID());
        bundle.set(Constants.IMPORT_PACKAGE, "org.osgi.framework,org.osgi.service.obr,org.osgi.util.tracker,org.slf4j");
        bundle.set(Constants.EXPORT_PACKAGE, OBRValidation.class.getPackage().getName());
        bundle.set(Constants.BUNDLE_ACTIVATOR, org.ops4j.pax.exam.osgi.internal.obr.OBROptionActivator.class.getName());
        bundle.set(Constants.BUNDLE_MANIFESTVERSION, "2");
        return CoreOptions.streamBundle(bundle.build()).startLevel(2).start(true).update(false);
    }

    @Override
    public OBRBundleOption bundle(String symbolicName) {
        String[] nameAndVersion = new String[2];
        nameAndVersion[0] = symbolicName;
        OBRBundle bundle = new OBRBundle(nameAndVersion);
        bundlesList.add(nameAndVersion);
        return bundle;
    }

    @Override
    public OBRRepositoryOption bundles(String... symbolicNames) {
        for (String symbolicName : symbolicNames) {
            bundle(symbolicName);
        }
        return this;
    }

    private class OBRBundle implements OBRBundleOption {

        private final String[] nameAndVersion;

        public OBRBundle(String[] nameAndVersion) {
            this.nameAndVersion = nameAndVersion;
        }

        @Override
        public OBRRepositoryOption version(String version) {
            nameAndVersion[1] = version;
            return OBRRepositoryProvisionOption.this;
        }

        @Override
        public OBRBundleOption bundle(String symbolicName) {
            return OBRRepositoryProvisionOption.this.bundle(symbolicName);
        }

        @Override
        public Option toOption() {
            return OBRRepositoryProvisionOption.this.toOption();
        }

    }

}
