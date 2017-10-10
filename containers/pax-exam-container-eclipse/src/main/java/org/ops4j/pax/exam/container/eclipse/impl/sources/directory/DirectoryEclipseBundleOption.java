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
package org.ops4j.pax.exam.container.eclipse.impl.sources.directory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.container.eclipse.ArtifactNotFoundException;
import org.ops4j.pax.exam.container.eclipse.impl.BundleArtifactInfo;
import org.ops4j.pax.exam.container.eclipse.impl.options.AbstractEclipseBundleOption;
import org.ops4j.pax.exam.options.StreamReference;
import org.ops4j.pax.exam.options.UrlProvisionOption;

/**
 * Bundle Option for {@link DirectoryResolver}
 * 
 * @author Christoph Läubrich
 *
 */
public final class DirectoryEclipseBundleOption extends AbstractEclipseBundleOption<File>
    implements StreamReference {

    public DirectoryEclipseBundleOption(BundleArtifactInfo<File> bundleInfo)
        throws ArtifactNotFoundException {
        super(bundleInfo);
        final File file = bundleInfo.getContext();
        if (!file.exists()) {
            throw new ArtifactNotFoundException(
                "Can't resolve bundle '" + file.getAbsolutePath() + "' does not exists");
        }
    }

    @Override
    protected Option toOption() {
        UrlProvisionOption bundle = CoreOptions
            .bundle(getBundleInfo().getContext().getAbsolutePath());
        bundle.startLevel(getStartLevel());
        bundle.start(shouldStart());
        bundle.update(shouldUpdate());
        return bundle;
    }

    @Override
    public InputStream createStream() throws IOException {
        return new FileInputStream(getBundleInfo().getContext());
    }
}
