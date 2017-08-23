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
package org.ops4j.pax.exam;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

// TODO make the store more global to the exam session to control
// caching load + shutdown.
// For now we do it fully3 locally:
class StreamBundle {
    private InputStream stream;

    StreamBundle(InputStream stream) {
        this.stream = stream;
    }
    
    public URL toTemp() {
        try {
            File temp = File.createTempFile( "tinybundles_", ".tmp" );
            Files.copy(stream, temp.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return temp.toURI().toURL();
        }
        catch (IOException e) {
            throw new RuntimeException("Error writing stream to temp file." + e.getMessage(), e);
        }
    }
}
