/*
 * Copyright (c)2005-2007 Mark Logic Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * The use of the Apache License does not indicate that this project is
 * affiliated with the Apache Software Foundation.
 */
package com.marklogic.performance;

import java.io.IOException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.marklogic.xcc.ValueFactory;
import com.marklogic.xcc.exceptions.UnimplementedFeatureException;
import com.marklogic.xcc.types.ValueType;
import com.marklogic.xcc.types.XName;
import com.marklogic.xcc.types.XdmValue;
import com.marklogic.xcc.types.XdmVariable;

/*
 * @author Michael Blakeley, michael.blakeley@marklogic.com
 * 
 * @author Ron Avnur
 */
class XMLFileTest extends AbstractTest {

    /**
     * 
     */
    private static final String TEST_LOCAL_NAME = "test";

    /**
     * 
     */
    public static final String HARNESS_NAMESPACE = "http://marklogic.com/xdmp/harness";

    /**
     * 
     */
    private static final String COMMENT_EXPECTED_RESULT_LOCAL_NAME = "comment-expected-result";

    /**
     * 
     */
    private static final String NAME_LOCAL_NAME = "name";

    /**
     * 
     */
    private static final String QUERY_LOCAL_NAME = "query";

    private static final String VARIABLES_LOCAL_NAME = "variables";

    private static final String VARIABLE_LOCAL_NAME = "variable";

    private static final String VARIABLE_NAMESPACE_LOCAL_NAME = "namespace";

    private static final String VARIABLE_NAME_LOCAL_NAME = "name";

    private static final String VARIABLE_TYPE_LOCAL_NAME = "type";

    private static final String VARIABLE_VALUE_LOCAL_NAME = "value";

    private static final String VARIABLE_VALUELIST_LOCAL_NAME = "value-csv";

    private static final String VARIABLE_MINVALUE_LOCAL_NAME = "min-value";

    private static final String VARIABLE_MAXVALUE_LOCAL_NAME = "max-value";

    private String commentExpectedResult;

    private String query;

    private XdmVariable[] variables = null;

    public XMLFileTest(Node node) throws IOException {
        if (node.getNamespaceURI() == null) {
            throw new IOException("invalid element: "
                    + node.getLocalName() + " in "
                    + node.getNamespaceURI() + " is not "
                    + TEST_LOCAL_NAME + " in " + HARNESS_NAMESPACE);
        }
        if (!node.getNamespaceURI().equals(HARNESS_NAMESPACE)
                || !node.getLocalName().equals(TEST_LOCAL_NAME)) {
            throw new IOException("invalid element: "
                    + node.getLocalName() + " in "
                    + node.getNamespaceURI() + " is not "
                    + TEST_LOCAL_NAME + " in " + HARNESS_NAMESPACE);
        }
        Node queryNode = (((Element) node).getElementsByTagNameNS(
                HARNESS_NAMESPACE, QUERY_LOCAL_NAME).item(0));
        Node variablesNode = (((Element) node).getElementsByTagNameNS(
                HARNESS_NAMESPACE, VARIABLES_LOCAL_NAME).item(0));
        Node nameNode = (((Element) node).getElementsByTagNameNS(
                HARNESS_NAMESPACE, NAME_LOCAL_NAME).item(0));
        Node commentExpectedResultNode = (((Element) node)
                .getElementsByTagNameNS(HARNESS_NAMESPACE,
                        COMMENT_EXPECTED_RESULT_LOCAL_NAME).item(0));
        if (queryNode == null) {
            throw new NullPointerException("missing required element: "
                    + QUERY_LOCAL_NAME + " in " + HARNESS_NAMESPACE);
        }
        if (nameNode == null) {
            throw new NullPointerException("missing required element: "
                    + NAME_LOCAL_NAME + " in " + HARNESS_NAMESPACE);
        }
        query = queryNode.getTextContent();
        configureVariables(variablesNode);
        name = nameNode.getTextContent();
        if (null != commentExpectedResultNode) {
            commentExpectedResult = commentExpectedResultNode
                    .getTextContent().trim();
        }
    }

    private void configureVariables(Node variablesNode)
            throws DOMException {
        if (null == variablesNode) {
            return;
        }
        NodeList children = variablesNode.getChildNodes();
        int length = children.getLength();
        variables = new ListVariable[length];
        Node n, name, namespaceNode, typeNode, value = null;
        // TODO support variable ranges and lists of values
        Node valuesCSV, minValue, maxValue = null;
        String namespace, type;
        NamedNodeMap attr;
        for (int i = 0; i < length; i++) {
            n = children.item(i);
            if (Node.ELEMENT_NODE != n.getNodeType()
                    || n.getNamespaceURI() != HARNESS_NAMESPACE
                    || n.getLocalName() != VARIABLE_LOCAL_NAME) {
                // NB - some variable entries may be null!
                continue;
            }
            attr = n.getAttributes();
            name = attr.getNamedItem(VARIABLE_NAME_LOCAL_NAME);
            namespaceNode = attr
                    .getNamedItem(VARIABLE_NAMESPACE_LOCAL_NAME);
            typeNode = attr.getNamedItem(VARIABLE_TYPE_LOCAL_NAME);
            namespace = null == namespaceNode ? null : namespaceNode
                    .getNodeValue();
            type = null == typeNode ? "xs:string" : typeNode
                    .getNodeValue();
            value = attr.getNamedItem(VARIABLE_VALUE_LOCAL_NAME);
            if (null == name) {
                throw new NullPointerException(
                        "missing required variable attribute: "
                                + VARIABLE_NAME_LOCAL_NAME);
            }
            if (null == value) {

                // TODO look for value lists and ranges
                valuesCSV = attr
                        .getNamedItem(VARIABLE_VALUELIST_LOCAL_NAME);
                if (null != valuesCSV) {
                    variables[i] = new ListVariable(name.getNodeValue(),
                            namespace, type, valuesCSV.getNodeValue()
                                    .split(","));
                    continue;
                }

                minValue = attr
                        .getNamedItem(VARIABLE_MINVALUE_LOCAL_NAME);
                maxValue = attr
                        .getNamedItem(VARIABLE_MAXVALUE_LOCAL_NAME);
                if (null != minValue && null != maxValue) {
                    variables[i] = new RangeVariable(name.getNodeValue(),
                            namespace, type, minValue.getNodeValue(),
                            maxValue.getNodeValue());
                    continue;
                }

                // values may not be needed
                if (type.endsWith("?") || type.endsWith("*")) {
                    variables[i] = newVariable(name.getNodeValue(),
                            namespace, type);
                    continue;
                }
                throw new NullPointerException(
                        "missing required variable attribute: "
                                + VARIABLE_VALUE_LOCAL_NAME
                                + " (or child items)");
            }
            variables[i] = newVariable(name.getNodeValue(), namespace,
                    type, value.getNodeValue());
        }
    }

    /**
     * @param name
     * @param namespace
     * @param type
     * @return
     */
    private static XdmVariable newVariable(String name, String namespace,
            String type) {
        return newVariable(name, namespace, type, null);
    }

    /**
     * @param name
     * @param namespace
     * @param type
     * @param value
     * @return
     */
    private static XdmVariable newVariable(String name, String namespace,
            String type, String value) {
        XName xname = (null == namespace) ? new XName(name) : new XName(
                namespace, name);
        // System.err.println("variable " + xname + " = " + value + " ("
        // + type + ")");
        XdmValue xvalue = newValue(type, value);
        return ValueFactory.newVariable(xname, xvalue);
    }

    static XdmValue newValue(String type, String value) {
        // if type is empty, we assume a string
        if (null == type) {
            return newValue("xs:string", value);
        }
        if (type.equals("xs:string")) {
            return ValueFactory.newXSString(value);
        }
        if (type.equals("xs:boolean")) {
            return ValueFactory.newXSBoolean(Boolean.parseBoolean(value));
        }
        if (type.equals("xs:integer")) {
            return ValueFactory.newXSInteger(Integer.parseInt(value));
        }
        if (type.equals("xs:double")) {
            return ValueFactory.newValue(ValueType.XS_DOUBLE, Double
                    .parseDouble(value));
        }
        if (type.equals("xs:date")) {
            return ValueFactory.newXSDate(value, null, null);
        }
        if (type.equals("xs:dateTime")) {
            return ValueFactory.newXSDateTime(value, null, null);
        }
        if (type.equals("xs:time")) {
            return ValueFactory.newXSTime(value, null, null);
        }

        // TODO implement more types as needed
        throw new UnimplementedFeatureException(
                "variable type not implemented: " + type);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.marklogic.performance.TestInterface#getQuery()
     */
    public String getQuery() {
        return query;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.marklogic.performance.TestInterface#getCommentExpectedResult()
     */
    public String getCommentExpectedResult() {
        return commentExpectedResult;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.marklogic.performance.TestInterface#hasVariables()
     */
    public boolean hasVariables() {
        if (null == variables || 0 == variables.length) {
            return false;
        }
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.marklogic.performance.TestInterface#getVariables()
     */
    public XdmVariable[] getVariables() {
        return variables;
    }

}