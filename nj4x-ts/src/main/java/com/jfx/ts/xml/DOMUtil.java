/*
 * Copyright (c) 2008-2014 by Gerasimenko Roman.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistribution of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistribution in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in
 *     the documentation and/or other materials provided with the
 *     distribution.
 *
 * 3. The name "JFX" must not be used to endorse or promote
 *     products derived from this software without prior written
 *     permission.
 *     For written permission, please contact roman.gerasimenko@gmail.com
 *
 * 4. Products derived from this software may not be called "JFX",
 *     nor may "JFX" appear in their name, without prior written
 *     permission of Gerasimenko Roman.
 *
 *  THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED
 *  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 *  OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED.  IN NO EVENT SHALL THE JFX CONTRIBUTORS
 *  BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 *  USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 *  OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 *  OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 *  SUCH DAMAGE.
 */

package com.jfx.ts.xml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.*;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Stack;

public class DOMUtil {
    private static final Class[] PARAMETER_TYPES = new Class[]{};
    private static final Object[] INITARGS = new Object[]{};
    private static final DOMUtil.NoElementIterator NO_ELEMENT_ITERATOR = new DOMUtil.NoElementIterator();

    /**
     * Reads DOM Document from xml file.
     */
    public static Document getDocument(String filePathName) throws RuntimeException {
        try {
            if (!(new File(filePathName).exists())) {
                return null;
            }
            java.io.FileInputStream fis = null;
            try {
                fis = new java.io.FileInputStream(filePathName);
                return getDocument(fis);
            } finally {
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (Exception ignore) {
                    }
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static void validateXMLFile(String xmlFileName, InputStream xsdStream) throws SAXException, IOException {
        Validator validator = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema").newSchema(new StreamSource(xsdStream)).newValidator();
        validator.validate(new SAXSource(new InputSource(xmlFileName)), new SAXResult(new DefaultHandler()));
    }

    public static class SAXDocument {
        InputStream is;
        DOMUtil.SAXNode root;

        public SAXDocument(InputStream is, final boolean readAttributes) throws IOException, SAXException, ParserConfigurationException {
            this.is = is;
            SAXParserFactory spf = SAXParserFactory.newInstance();
            SAXParser sp = spf.newSAXParser();
            sp.parse(
                    is,
                    new DefaultHandler() {
                        short distFromTop;
                        String lastName;

                        public void startElement(String string, String string1, String name, Attributes attributes) throws SAXException {
                            if (root == null) {
                                root = new DOMUtil.SAXNode(name);
                                root.setAttributes(attributes);
                            } else {
                                if (distFromTop == 0) {
                                    DOMUtil.SAXNode n = new DOMUtil.SAXNode(lastName == null || !lastName.equals(name) ? name : null);
                                    if (readAttributes) {
                                        n.setAttributes(attributes);
                                    }
                                    root.addChild(n);
                                    lastName = name;
                                }
                                distFromTop++;
                            }
                        }

                        public void endElement(String string, String string1, String name) throws SAXException {
                            distFromTop--;
                        }
                    }
            );
        }

        public DOMUtil.SAXNode getChildNode(String elementName) {
            DOMUtil.SAXNode node = null;
            Iterator i = getChildNodes(elementName);
            while (i.hasNext()) {
                node = node == null ? (DOMUtil.SAXNode) i.next() : node;
            }
            return node;
        }

        public Iterator getChildNodes(final String elementName) {
            return new Iterator() {
                int pos = 0;
                String lastName;
                DOMUtil.SAXNode node;
                private Thread thread;

                public boolean hasNext() {
                    if (node != null) {
                        node.clean();
                    }
                    //
                    @SuppressWarnings("rawtypes")
                    ArrayList list = root.childNodes;
                    if (list != null) {
                        int max = list.size();
                        for (; pos < max; pos++) {
                            DOMUtil.SAXNode n = (DOMUtil.SAXNode) list.get(pos);
                            if (lastName == null || n.getName() != null) {
                                lastName = n.getName();
                            }
                            if (lastName.equals(elementName)) {
                                node = n;
                                pos++;
                                initParserThread();
                                return true;
                            }
                        }
                        node = null;
                        pos = -1;
                        if (thread != null) {
                            synchronized (thread) {
                                thread.notify();
                                try {
                                    thread.wait();
                                } catch (InterruptedException e) {
                                }
                            }
                        }
                    }
                    return false;
                }

                private void initParserThread() {
                    if (thread == null) {
                        thread = new Thread() {
                            public synchronized void run() {
                                try {
//                                    System.out.println("Iterator Thread started [" + lastName + "]");
                                    //
                                    SAXParserFactory spf = SAXParserFactory.newInstance();
                                    SAXParser sp = spf.newSAXParser();
                                    is.reset();
                                    sp.parse(
                                            is,
//                                            new BufferedInputStream(new FileInputStream(fileName)),
                                            new DefaultHandler() {
                                                short distFromTop;
                                                String lastName;
                                                DOMUtil.SAXNode _root;
                                                int ix;
                                                DOMUtil.SAXNode fNode;
                                                Stack stack = new Stack();

                                                // 1. parse from "root" SAXNode till Iterator's "node"
                                                public void startElement(String string, String string1, String name, Attributes attributes) throws SAXException {
                                                    if (node != null) {
                                                        // 1st element will be root
                                                        // 2nd element will be 1st root's child
                                                        if (_root == null) {
                                                            _root = root;
                                                            distFromTop = 0;
                                                            ix = 0;
                                                        } else {
                                                            if (distFromTop == 0) {
                                                                DOMUtil.SAXNode n = (DOMUtil.SAXNode) _root.childNodes.get(ix++);
                                                                lastName = name;
                                                                if (n == node) {
                                                                    // start fill the "node"
                                                                    fNode = n;
                                                                    fNode.clean();
                                                                    fNode.setAttributes(attributes);
                                                                    stack.push(fNode);
                                                                }
                                                            } else if (fNode != null) {
                                                                DOMUtil.SAXNode n = new DOMUtil.SAXNode(name);
                                                                n.setAttributes(attributes);
                                                                ((DOMUtil.SAXNode) stack.peek()).addChild(n);
                                                                stack.push(n);
                                                            }
                                                            distFromTop++;
                                                        }
                                                    }
                                                }

                                                public void endElement(String string, String string1, String name) throws SAXException {
                                                    if (node != null) {
                                                        distFromTop--;
                                                        if (fNode != null) {
                                                            DOMUtil.SAXNode saxNode = ((DOMUtil.SAXNode) stack.pop());
                                                            if (saxNode == fNode) {
                                                                fNode = null;
                                                                // end of fill
                                                                //noinspection SynchronizeOnNonFinalField
                                                                synchronized (thread) {
                                                                    try {
                                                                        thread.notify();
                                                                        thread.wait();
                                                                    } catch (InterruptedException e) {
                                                                        e.printStackTrace();
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                    );
                                } catch (Exception e) {
                                    e.printStackTrace();
                                } finally {
//                                    System.out.println("Iterator Thread finished [" + lastName + "]");
                                }
                            }
                        };
                        //noinspection SynchronizeOnNonFinalField
                        synchronized (thread) {
                            try {
                                thread.start();
                                thread.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        //noinspection SynchronizeOnNonFinalField
                        synchronized (thread) {
                            try {
                                thread.notify();
                                thread.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

                public Object next() {
                    return node;
                }

                public void remove() {
                }
            };
        }
    }

    public static class SAXNode {
        private String name;
        private ArrayList childNodes;
        private String[] attributes;
        private boolean isPreserving;
        private static final String NO_SUCH_ATTRIBUTE = "";

        public void setPreserving(boolean preserving) {
            isPreserving = preserving;
        }

        public String getAttribute(String name) {
            if (attributes != null) {
                for (int i = 0; i < attributes.length; i++) {
                    String aName = attributes[i++];
                    if (aName.equals(name)) {
                        return attributes[i];
                    }
                }
            }
            return DOMUtil.SAXNode.NO_SUCH_ATTRIBUTE;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void clean() {
            if (!isPreserving) {
                attributes = null;
                childNodes = null;
                isPreserving = true;
            }
        }

        private void setAttributes(Attributes a) {
            int max = a.getLength();
            attributes = new String[max << 1];
            for (int i = 0; i < max; i++) {
                attributes[i << 1] = a.getQName(i);
                attributes[(i << 1) + 1] = a.getValue(i);
            }
        }

        private SAXNode(String name) {
            this.name = name;
            this.isPreserving = true;
        }

        public void addChild(DOMUtil.SAXNode cn) {
            //noinspection unchecked
            (childNodes = childNodes == null ? new ArrayList() : childNodes).add(cn);
        }

        public ArrayList getChildNodes() {
            return childNodes;
        }

        public ArrayList getElementsByTagName(String tagName) {
            ArrayList res = new ArrayList();
            if (childNodes != null) {
                int max = childNodes.size();
                for (int i = 0; i < max; i++) {
                    DOMUtil.SAXNode saxNode = (DOMUtil.SAXNode) childNodes.get(i);
                    if (saxNode.name != null && saxNode.name.equals(tagName)) {
                        res.add(saxNode);
                    }
                }
            }
            return res;
        }

        public String getName() {
            return name;
        }
    }

    public static DOMUtil.SAXDocument getSAXDocument(final String fName) throws IOException, ParserConfigurationException, SAXException {
        return getSAXDocument(fName, false);
    }

    public static DOMUtil.SAXDocument getSAXDocument(final String fName, boolean readAttributes) throws IOException, ParserConfigurationException, SAXException {
        return getSAXDocument(new InputStream() {
            InputStream fis = new BufferedInputStream(new FileInputStream(fName));

            public int read() throws IOException {
                return fis.read();
            }

            public synchronized void reset() throws IOException {
                if (fis != null) {
                    fis.close();
                }
                fis = new BufferedInputStream(new FileInputStream(fName));
            }
        });
    }

    public static DOMUtil.SAXDocument getSAXDocument(InputStream is) throws IOException, ParserConfigurationException, SAXException {
        return getSAXDocument(is, false);
    }

    public static DOMUtil.SAXDocument getSAXDocument(InputStream is, boolean readAttributes) throws IOException, ParserConfigurationException, SAXException {
        return new DOMUtil.SAXDocument(is, readAttributes);
    }

    /**
     * Reads DOM Document from xml file.
     */
    public static Document getDocument(InputStream is) throws RuntimeException {
        try {
            DocumentBuilderFactory dFactory = getFactory();
            DocumentBuilder docBuilder = dFactory.newDocumentBuilder();
            Document d = docBuilder.parse(is);
            return d;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
        }
    }

    /**
     * Reads DOM Document from string (e.g. it used in workflow)
     */
    public static Document getDocumentFromString(String xmlString) throws RuntimeException {
        try {
            DocumentBuilderFactory dFactory = getFactory();
            DocumentBuilder docBuilder = dFactory.newDocumentBuilder();
            Document d = docBuilder.parse(new InputSource(new StringReader(xmlString)));
            return d;
        } catch (Exception ex) {
            System.err.println("XML: " + xmlString.substring(0, xmlString.indexOf('\n')));
            throw new RuntimeException(ex);
        }
    }

    public static Document createDocument() throws RuntimeException {
        DocumentBuilderFactory dFactory = getFactory();
        DocumentBuilder docBuilder = null;
        try {
            docBuilder = dFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
        return docBuilder.newDocument();
    }

    private static DocumentBuilderFactory getFactory() throws RuntimeException {
        try {
            try {
                return DocumentBuilderFactory.newInstance();
            } catch (Exception x) {
                try {
                    Class dbfClass = Class.forName("org.apache.crimson.jaxp.DocumentBuilderFactoryImpl");
                    Constructor dbfConstructor = dbfClass.getConstructor(PARAMETER_TYPES);
                    Object dbf = dbfConstructor.newInstance(INITARGS);
                    return (DocumentBuilderFactory) dbf;
                } catch (Exception e) {
                    e.printStackTrace();
                    //
                    Class dbfClass = Class.forName("oracle.xml.jaxp.JXDocumentBuilderFactory");
                    Constructor dbfConstructor = dbfClass.getConstructor(PARAMETER_TYPES);
                    Object dbf = dbfConstructor.newInstance(INITARGS);
                    return (DocumentBuilderFactory) dbf;
                }
            }
        } catch (InstantiationException e) {
            throw new RuntimeException(e.getMessage());
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e.getMessage());
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e.getMessage());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e.getMessage());
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public static byte[] serializeDocumentToByteArray(Document d) throws RuntimeException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        serializeDocument(d, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    public static String serializeDocumentToString(Document d) throws RuntimeException {
        return new String(serializeDocumentToByteArray(d));
    }

    public static void serializeDocument(Document d, OutputStream os) throws RuntimeException {
        serializeDocument(d, os, "UTF-8");
    }

    public static void printDocument(Document d) throws RuntimeException {
        serializeDocument(d, new OutputStream() {
            public void write(int b) throws IOException {
                System.out.write(b);
            }
        });
    }

    public static void serializeDocument(Document d, OutputStream os, String encoding) throws RuntimeException {
        try {
            Class ofClass = null;
            try {
                ofClass = Class.forName("com.sun.org.apache.xml.internal.serialize.OutputFormat");
            } catch (ClassNotFoundException e) {
                ofClass = Class.forName("org.apache.xml.serialize.OutputFormat");
            }
            Constructor ofConstructor = ofClass.getConstructor(new Class[]{
                    String.class,
                    String.class,
                    Boolean.TYPE
            });
            Object of = ofConstructor.newInstance(new Object[]{
                    "xml",
                    null,
                    Boolean.FALSE
            });
            ofClass.getMethod("setEncoding", new Class[]{String.class})
                    .invoke(of, new Object[]{encoding});
            ofClass.getMethod("setOmitXMLDeclaration", new Class[]{Boolean.TYPE})
                    .invoke(of, new Object[]{Boolean.FALSE});
            ofClass.getMethod("setIndent", new Class[]{Integer.TYPE})
                    .invoke(of, new Object[]{new Integer(4)});
            ofClass.getMethod("setLineWidth", new Class[]{Integer.TYPE})
                    .invoke(of, new Object[]{new Integer(1000)});
            Class sClass = null;
            try {
                sClass = Class.forName("com.sun.org.apache.xml.internal.serialize.XMLSerializer");
            } catch (ClassNotFoundException e) {
                sClass = Class.forName("org.apache.xml.serialize.XMLSerializer");
            }
            Constructor sConstructor = sClass.getConstructor(new Class[]{
                    OutputStream.class,
                    ofClass
            });
            Object s = sConstructor.newInstance(new Object[]{
                    os,
                    of
            });
            sClass.getMethod("serialize", new Class[]{Class.forName("org.w3c.dom.Document")})
                    .invoke(s, new Object[]{d});
//            os.close();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
//        } catch (IOException e) {
//            throw new RuntimeException(e.getMessage());
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e.getMessage());
        } catch (InstantiationException e) {
            throw new RuntimeException(e.getMessage());
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e.getMessage());
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e/*.getMessage()*/);
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static Iterator getTopElements(Node n) {
        return getTopElements(n, (String) null);
    }

    public static Iterator getTopElements(Node n, final String elementNodeName) {
        if (n != null) {
            return new DOMUtil.ElementIterator(n, new DOMUtil.NodeFilter() {
                public boolean isOk(Node node) {
                    return node.getNodeType() == Node.ELEMENT_NODE
                            && (elementNodeName == null || node.getNodeName().equals(elementNodeName));
                }
            });
        } else {
            return NO_ELEMENT_ITERATOR;
        }
    }

    public static Iterator getTopElements(Node n, DOMUtil.NodeFilter filter) {
        return new DOMUtil.ElementIterator(n, filter);
    }

    public static Iterator getSiblingElements(Node startNode, DOMUtil.NodeFilter filter) {
        return new DOMUtil.SiblingIterator(startNode, filter);
    }

    public static Node getNextSiblingElement(Node startNode, DOMUtil.NodeFilter filter) {
        DOMUtil.SiblingIterator i = new DOMUtil.SiblingIterator(startNode, filter);
        if (i.hasNext()) {
            return (Node) i.next();
        } else {
            return null;
        }
    }

    public static String getCDATAChild(Element p) {
        Node cdata = p.getFirstChild();
        if (cdata != null) {
            while (cdata != null && cdata.getNodeType() != Node.CDATA_SECTION_NODE) {
                cdata = cdata.getNextSibling();
            }
        }
        return cdata == null ? null : cdata.getNodeValue();
    }

    public static interface NodeFilter {
        boolean isOk(Node n);
    }

    private static class NoElementIterator implements Iterator {

        public void remove() {
        }

        public boolean hasNext() {
            return false;
        }

        public Object next() {
            return null;
        }
    }

    private static class ElementIterator implements Iterator {
        NodeList nl;
        int max, current, next;
        boolean hasNextCalled, hasNextResult;
        DOMUtil.NodeFilter filter;

        ElementIterator(Node parentNode, DOMUtil.NodeFilter filter) {
            this.filter = filter;
            nl = parentNode.getChildNodes();
            max = nl.getLength();
            current = 0;
            hasNextCalled = false;
        }

        public boolean hasNext() {
            try {
                Node n = null;
                next = current;
                while (next < max) {
                    Node test = nl.item(next);
                    if (filter.isOk(test)) {
                        n = nl.item(next);
                        break;
                    }
                    next++;
                }
                return hasNextResult = (n != null);
            } finally {
                hasNextCalled = true;
            }
        }

        public Object next() throws java.util.NoSuchElementException {
            try {
                if (!hasNextCalled) {
                    hasNext();
                }
                if (hasNextResult) {
                    current = next + 1;
                    return nl.item(next);
                } else {
                    throw new java.util.NoSuchElementException();
                }
            } finally {
                hasNextCalled = false;
            }
        }

        public void remove() throws UnsupportedOperationException, IllegalStateException {
            throw new UnsupportedOperationException();
        }
    }

    private static class SiblingIterator implements Iterator {
        Node currentNode;
        DOMUtil.NodeFilter filter;
        boolean hasNextCalled, hasNextResult;

        SiblingIterator(Node startNode, DOMUtil.NodeFilter filter) {
            this.filter = filter;
            this.currentNode = startNode;
            this.hasNextCalled = false;
        }

        public boolean hasNext() {
            try {
                Node n = null;
                Node next = currentNode.getNextSibling();
                while (next != null) {
                    if (filter.isOk(next)) {
                        n = next;
                        currentNode = next;
                        break;
                    }
                    next = next.getNextSibling();
                }
                return hasNextResult = (n != null);
            } finally {
                hasNextCalled = true;
            }
        }

        public Object next() throws java.util.NoSuchElementException {
            try {
                if (!hasNextCalled) {
                    hasNext();
                }
                if (hasNextResult) {
                    return currentNode;
                } else {
                    throw new java.util.NoSuchElementException();
                }
            } finally {
                hasNextCalled = false;
            }
        }

        public void remove() throws UnsupportedOperationException, IllegalStateException {
            throw new UnsupportedOperationException();
        }
    }

    public static Element findElement(Node d, String nodeName) {
        return findElement(d, nodeName, null, null);
    }

    public static Element findElement(Node d, String nodeName, String attrName, String attrValue) {
        NodeList nl = d.getChildNodes();
        return findElement(nl, nodeName, attrName, attrValue);
    }

    public static Element findElement(NodeList nl, String nodeName, String attrName, String attrValue) {
        int max = nl.getLength();
        for (int i = 0; i < max; ++i) {
            if (nl.item(i).getNodeType() == Node.ELEMENT_NODE && nl.item(i).getNodeName().equals(nodeName)) {
                Element e = ((Element) nl.item(i));
                if (attrName == null || attrValue == null || e.getAttribute(attrName).equals(attrValue)) {
                    return e;
                }
            }
        }
        for (int i = 0; i < max; ++i) {
            Element e = findElement(nl.item(i).getChildNodes(), nodeName, attrName, attrValue);
            if (e != null) {
                return e;
            }
        }
        return null;
    }

    public static DOMUtil.SAXNode findElement(DOMUtil.SAXNode d, String nodeName) {
        return DOMUtil.findElement(d, nodeName, null, null);
    }

    public static DOMUtil.SAXNode findElement(DOMUtil.SAXNode d, String nodeName, String attrName, String attrValue) {
        ArrayList nl = d.getChildNodes();
        return findElement(nl, nodeName, attrName, attrValue);
    }

    public static DOMUtil.SAXNode findElement(ArrayList nl, String nodeName, String attrName, String attrValue) {
        if (nl != null) {
            int max = nl.size();
            for (int i = 0; i < max; ++i) {
                DOMUtil.SAXNode e = (DOMUtil.SAXNode) nl.get(i);
                if (e.getName().equals(nodeName)) {
                    if (attrName == null || attrValue == null || e.getAttribute(attrName).equals(attrValue)) {
                        return e;
                    }
                }
            }
            for (int i = 0; i < max; ++i) {
                DOMUtil.SAXNode _e = (DOMUtil.SAXNode) nl.get(i);
                DOMUtil.SAXNode e = findElement(_e.getChildNodes(), nodeName, attrName, attrValue);
                if (e != null) {
                    return e;
                }
            }
        }
        return null;
    }

    public static String getAttribute(Element element, String attrName, String dflt) {
        String attrValue = element.getAttribute(attrName);
        return attrValue == null || attrValue.length() == 0 ? dflt : attrValue;
    }

    public static int getAttributeAsInt(Element element, String attrName, int dflt) {
        String attrValue = element.getAttribute(attrName);
        return attrValue == null || attrValue.length() == 0 ? dflt : Integer.parseInt(attrValue);
    }

    public static boolean getAttributeAsBoolean(Element element, String attrName, boolean dflt) {
        String attrValue = element.getAttribute(attrName);
        return attrValue == null || attrValue.length() == 0 ? dflt : Boolean.valueOf(attrValue).booleanValue();//Boolean.parseBoolean(attrValue);
    }

    public static void setAttribute(Element eCol, String name, String val, String defaultVal) {
        if (val != null && !val.equals(defaultVal)) {
            eCol.setAttribute(name, val);
        }
    }

    public static int getAttribute(Element eCol, String name, int defaultVal) {
        String val = eCol.getAttribute(name);
        if (val == null || val.length() == 0) {
            return defaultVal;
        } else {
            return Integer.parseInt(val);
        }
    }

    public static boolean getAttribute(Element eCol, String name, boolean defaultVal) {
        String val = eCol.getAttribute(name);
        if (val == null || val.length() == 0) {
            return defaultVal;
        } else {
            return Boolean.valueOf(val).booleanValue();//Boolean.parseBoolean(val);
        }
    }

    public static void setAttribute(Element eCol, String name, int val, int defaultVal) {
        if (val != defaultVal) {
            eCol.setAttribute(name, "" + val);
        }
    }

    public static void setAttribute(Element eCol, String name, boolean val, boolean defaultVal) {
        if (val != defaultVal) {
            eCol.setAttribute(name, "" + val);
        }
    }

    public static String getNodeValue(Document doc, String xpathPrefix) {
        String result = null;
        try {
            Class cXPathAPI = null;
            try {
                cXPathAPI = Class.forName("com.sun.org.apache.xpath.internal.XPathAPI");
            } catch (ClassNotFoundException e) {
                cXPathAPI = Class.forName("org.apache.xpath.XPathAPI");
            }
            Method mselectSingleNode = cXPathAPI.getMethod("selectSingleNode", new Class[]{Document.class, String.class});
            Node nodeValue = (Node) mselectSingleNode.invoke(null, new Object[]{doc, xpathPrefix});
//            Node nodeValue = XPathAPI.selectSingleNode(doc, xpathPrefix);
            if (nodeValue != null) {
                result = nodeValue.getNodeValue();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return result;
    }
}
