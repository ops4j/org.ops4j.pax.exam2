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
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

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

    private static String toPath(Node node) {
        String name = node.getNodeName();
        Node parentNode = node.getParentNode();
        if (parentNode == null) {
            return name;
        }
        else {
            return toPath(parentNode) + "/" + name;
        }
    }

    /**
     * evaluates a XPath expresion and loops over the nodeset result
     * 
     * @param element
     * @param xpath
     * @return
     * @throws XPathExpressionException
     */
    protected static Iterable<Node> evaluate(Node element, String xpath)
        throws XPathExpressionException {
        final NodeList nodeList = (NodeList) getXPath().evaluate(xpath, element,
            XPathConstants.NODESET);
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
                        index++;
                        return item;
                    }
                };
            }
        };
    }

}
