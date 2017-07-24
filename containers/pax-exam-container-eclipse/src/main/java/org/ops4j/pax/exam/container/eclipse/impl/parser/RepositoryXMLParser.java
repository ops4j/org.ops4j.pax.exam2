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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import javax.xml.xpath.XPathException;

import org.osgi.framework.InvalidSyntaxException;
import org.w3c.dom.Element;

/**
 * Base class for reading eclipse repository xmls, these files can exits in several formats (plain
 * XML, jared, XY compressed) so a fallback is required and the server must be queried what files
 * are there...
 * 
 * @author Christoph Läubrich
 *
 */
public abstract class RepositoryXMLParser extends AbstractParser {

    protected abstract void readXML(URL baseUrl, Element element)
        throws IOException, XPathException, InvalidSyntaxException;

    protected abstract String getXMLName();

    public final boolean openRepositoryFile(URL url) throws IOException {
        // TODO support Query parameters?!?
        String externalForm = url.toExternalForm();
        if (!externalForm.endsWith("/")) {
            externalForm = externalForm + "/";
        }
        try {
            String xmlName = getXMLName();
            String xmlFileName = xmlName + ".xml";
            InputStream jarStream = tryOpen(new URL(externalForm + xmlName + ".jar"));
            if (jarStream != null) {
                try {
                    try (JarInputStream stream = new JarInputStream(jarStream)) {
                        JarEntry entry;
                        while ((entry = stream.getNextJarEntry()) != null) {
                            if (entry.getName().equals(xmlFileName)) {
                                readXML(url, parse(stream));
                                return true;
                            }
                        }
                    }
                }
                finally {
                    jarStream.close();
                }
            }
            // fallback to read a pure xml file then...
            InputStream xmlStream = tryOpen(new URL(externalForm + xmlFileName));
            if (xmlStream != null) {
                try {
                    readXML(url, parse(xmlStream));
                    return true;
                }
                finally {
                    xmlStream.close();
                }
            }

            return false;
        }
        catch (XPathException | InvalidSyntaxException e) {
            throw new IOException(e);
        }
    }

    private InputStream tryOpen(URL url) throws IOException {
        URLConnection connection = url.openConnection();
        connection.setUseCaches(true);
        if (connection instanceof HttpURLConnection) {
            HttpURLConnection http = (HttpURLConnection) connection;
            http.setInstanceFollowRedirects(true);
            int code = http.getResponseCode();
            if (code == HttpURLConnection.HTTP_OK) {
                return http.getInputStream();
            }
            else {
                http.getErrorStream().close();
                return null;
            }
        }
        try {
            return connection.getInputStream();
        }
        catch (FileNotFoundException e) {
            return null;
        }
    }
}
