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

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.osgi.framework.Version;
import org.osgi.framework.VersionRange;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Supports the parsing of the eclipse config files that are mostly small xml files
 * 
 * @author Christoph Läubrich
 *
 */
public abstract class AbstractParser {

    private static final VersionRange EMPTY_RANGE = new VersionRange(VersionRange.LEFT_CLOSED,
        Version.emptyVersion, null, VersionRange.RIGHT_CLOSED);

    private static final Map<String, Version> versionCache = new HashMap<>();
    private static final Map<String, VersionRange> versionRangeCache = new HashMap<>();

    private static final ThreadLocal<DocumentBuilderFactory> DBF = new ThreadLocal<DocumentBuilderFactory>() {

        @Override
        protected DocumentBuilderFactory initialValue() {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            return factory;
        }
    };

    private static final ThreadLocal<XPath> XPF = new ThreadLocal<XPath>() {

        @Override
        protected XPath initialValue() {
            return XPathFactory.newInstance().newXPath();
        }
    };

    /**
     * parse (and close) the Inputstream and return the containing document, xml error are converted
     * to IOExceptions for unified handling
     * 
     * @param stream
     * @return
     * @throws IOException
     */
    protected static Element parse(InputStream stream) throws IOException {
        try {
            return DBF.get().newDocumentBuilder().parse(stream).getDocumentElement();
        }
        catch (SAXException | ParserConfigurationException e) {
            throw new IOException("parsing stream failed", e);
        }
        finally {
            stream.close();
        }
    }

    /**
     * 
     * @return the thread-local xpath instance
     */
    protected static XPath getXPath() {
        return XPF.get();
    }

    /**
     * Get an Attribute from the given node and throwing an exception in the case it is required but
     * not present
     * 
     * @param node
     * @param name
     * @param required
     * @return
     */
    protected static String getAttribute(Node node, String name, boolean required) {
        NamedNodeMap attributes = node.getAttributes();
        Node idNode = attributes.getNamedItem(name);
        if (idNode == null) {
            if (required) {
                throw new IllegalArgumentException(toPath(node) + " has no " + name + " attribute");
            }
            else {
                return "";
            }
        }
        else {
            String value = idNode.getNodeValue();
            if (value == null) {
                return "";
            }
            return value;
        }
    }

    protected static String toPath(Node node) {
        String name = node.getNodeName();
        Node id = node.getAttributes().getNamedItem("id");
        if (id != null) {
            name = name + "[" + id.getNodeValue() + "]";
        }
        Node parentNode = node.getParentNode();
        if (parentNode == null) {
            return name;
        }
        else {
            return toPath(parentNode) + "/" + name;
        }
    }

    protected static Map<String, String> attributesToMap(Node node) {
        Map<String, String> flags = new HashMap<>();
        NamedNodeMap attributes = node.getAttributes();
        int length = attributes.getLength();
        for (int i = 0; i < length; i++) {
            Node item = attributes.item(i);
            flags.put(item.getNodeName(), item.getNodeValue());
        }
        return flags;
    }

    public static Version stringToVersion(String version) {
        if (version == null || version.isEmpty()) {
            return Version.emptyVersion;
        }
        Version v = versionCache.get(version);
        if (v == null) {
            v = Version.parseVersion(version);
            versionCache.put(version, v);
        }
        return v;
    }

    public static VersionRange stringToVersionRange(String versionRange) {
        if (versionRange == null || versionRange.trim().isEmpty()) {
            return EMPTY_RANGE;
        }
        VersionRange range = versionRangeCache.get(versionRange);
        if (range == null) {
            range = VersionRange.valueOf(versionRange);
            versionRangeCache.put(versionRange, range);
        }
        return range;
    }

    protected static Iterable<Node> evaluate(Node element, String xpath)
        throws XPathExpressionException {
        return evaluate(element, getXPath().compile(xpath), false);
    }

    protected static Iterable<Node> evaluate(Node element, XPathExpression expression)
        throws XPathExpressionException {
        return evaluate(element, expression, false);
    }

    protected static Node getNode(Node element, String xpath) throws XPathExpressionException {
        return (Node) getXPath().evaluate(xpath, element, XPathConstants.NODE);
    }

    protected static int getSize(Node node, int defaultSize) {
        String attribute = getAttribute(node, "size", false);
        if (attribute != null && !attribute.isEmpty()) {
            try {
                return Integer.parseInt(attribute);
            }
            catch (NumberFormatException e) {
                // ignore then
            }
        }
        return defaultSize;
    }

    /**
     * evaluates a XPath expression and loops over the nodeset result
     * 
     * @param element
     * @param xpath
     * @return
     * @throws XPathExpressionException
     */
    protected static Iterable<Node> evaluate(Node element, XPathExpression expression,
        boolean detatch) throws XPathExpressionException {
        final NodeList nodeList = (NodeList) expression.evaluate(element, XPathConstants.NODESET);
        return new Iterable<Node>() {

            @Override
            public Iterator<Node> iterator() {
                return new Iterator<Node>() {

                    int index = 0;

                    @Override
                    public boolean hasNext() {
                        return index < nodeList.getLength();
                    }

                    @Override
                    public Node next() {
                        Node item = nodeList.item(index);
                        if (detatch) {
                            // detaching the node from its parent dramatically improves performance
                            // for xpath!
                            item.getParentNode().removeChild(item);
                        }
                        index++;
                        return item;
                    }
                };
            }
        };
    }

}
