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
package org.ops4j.pax.exam.container.eclipse.impl.parser;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * parses the classpath files of an Eclipse-Project
 * 
 * @author Christop Läubrich
 *
 */
public class ClasspathParser extends AbstractParser {

    private static final Logger LOG = LoggerFactory.getLogger(ClasspathParser.class);

    private boolean valid;

    private final List<String[]> entries = new ArrayList<>();

    public ClasspathParser(File projectFolder) {
        File file = new File(projectFolder, ".classpath");
        if (file.exists()) {
            try {
                Element document = parse(new FileInputStream(file));
                for (Node item : evaluate(document, "/classpath/classpathentry")) {
                    entries.add(
                        new String[] { item.getAttributes().getNamedItem("kind").getNodeValue(),
                            item.getAttributes().getNamedItem("path").getNodeValue() });
                }
                valid = true;
            }
            catch (Exception e) {
                LOG.warn("can't parse {}", file.getAbsolutePath(), e);
            }
        }
    }

    public String getOutput() {
        for (String[] strings : entries) {
            if ("output".equals(strings[0])) {
                return strings[1];
            }
        }
        return null;
    }

    public boolean isValid() {
        return valid;
    }

}
