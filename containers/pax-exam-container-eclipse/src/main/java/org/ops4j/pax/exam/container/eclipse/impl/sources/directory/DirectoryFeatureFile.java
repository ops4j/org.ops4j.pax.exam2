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

import org.ops4j.pax.exam.container.eclipse.impl.parser.FeatureParser;

/**
 * Container that holds feature and file information
 * 
 * @author Christoph Läubrich
 *
 */
public final class DirectoryFeatureFile {

    private final File file;
    private final FeatureParser feature;

    public DirectoryFeatureFile(File file, FeatureParser feature) {
        this.file = file;
        this.feature = feature;
    }

    @Override
    public String toString() {
        return file.getAbsolutePath();
    }

    public FeatureParser getFeature() {
        return feature;
    }

    public File getFile() {
        return file;
    }
}
