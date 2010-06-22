/*
 * Copyright (c)2005-2010 Mark Logic Corporation
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
 * @author Michael Blakeley
 *
 * @author Ron Avnur
 *
 * @author Joe Hoy
 *
 */
public class XMLFileTest extends AbstractTest {

    public static final String HARNESS_NAMESPACE = "http://marklogic.com/xdmp/harness";

    public static final String TEST_LOCAL_NAME = "test";

    public static final String COMMENT_EXPECTED_RESULT_LOCAL_NAME = "comment-expected-result";

    public static final String NAME_LOCAL_NAME = "name";

    public static final String QUERY_LOCAL_NAME = "query";

    public static final String VARIABLES_LOCAL_NAME = "variables";

    public static final String VARIABLE_LOCAL_NAME = "variable";

    public static final String VARIABLE_NAMESPACE_LOCAL_NAME = "namespace";

    public static final String VARIABLE_NAME_LOCAL_NAME = "name";

    public static final String VARIABLE_TYPE_LOCAL_NAME = "type";

    public static final String VARIABLE_VALUE_LOCAL_NAME = "value";

    public static final String VARIABLE_SPECIAL_LOCAL_NAME = "special";

    public static final String VARIABLE_VALUELIST_LOCAL_NAME = "value-csv";

    public static final String VARIABLE_MINVALUE_LOCAL_NAME = "min-value";

    public static final String VARIABLE_MAXVALUE_LOCAL_NAME = "max-value";

    public static final String TEST_WEIGHT_LOCAL_NAME = "weight";

    public static final String USER_LOCAL_NAME = "user";

    public static final String PASSWORD_LOCAL_NAME = "password";

    public XMLFileTest(Node node) throws IOException {
        if (null == node.getNamespaceURI()) {
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
        Element elem = (Element) node;
        Node queryNode = (elem.getElementsByTagNameNS(HARNESS_NAMESPACE,
                QUERY_LOCAL_NAME).item(0));
        Node nameNode = (elem.getElementsByTagNameNS(HARNESS_NAMESPACE,
                NAME_LOCAL_NAME).item(0));
        if (null == queryNode) {
            throw new NullPointerException("missing required element: "
                    + QUERY_LOCAL_NAME + " in " + HARNESS_NAMESPACE);
        }
        if (null == nameNode) {
            throw new NullPointerException("missing required element: "
                    + NAME_LOCAL_NAME + " in " + HARNESS_NAMESPACE);
        }
        name = nameNode.getTextContent();
        query = queryNode.getTextContent();

        Node commentExpectedResultNode = (elem.getElementsByTagNameNS(
                HARNESS_NAMESPACE, COMMENT_EXPECTED_RESULT_LOCAL_NAME)
                .item(0));
        if (null != commentExpectedResultNode) {
            commentExpectedResult = commentExpectedResultNode
                    .getTextContent().trim();
        }

        configureVariables(elem.getElementsByTagNameNS(HARNESS_NAMESPACE,
                VARIABLES_LOCAL_NAME).item(0));
        configureAuthentication(elem.getElementsByTagNameNS(
                HARNESS_NAMESPACE, USER_LOCAL_NAME).item(0), elem
                .getElementsByTagNameNS(HARNESS_NAMESPACE,
                        PASSWORD_LOCAL_NAME).item(0));
    }

    /**
     * @param _user
     * @param _password
     */
    protected void configureAuthentication(Node _user, Node _password) {
        if (null == _user || null == _password) {
            return;
        }
        user = _user.getTextContent().trim();
        password = _password.getTextContent().trim();
    }

    protected void configureVariables(Node variablesNode)
            throws DOMException {
        if (null == variablesNode) {
            return;
        }
        NodeList children = variablesNode.getChildNodes();
        int length = children.getLength();
        variables = new XdmVariable[length];
        Node n, name, namespaceNode, typeNode, value = null;
        // support variable ranges and lists of values
        Node valuesCSV, minValue, maxValue, specialValue = null;
        String namespace, type;
        NamedNodeMap attr;
        for (int i = 0; i < length; i++) {
            n = children.item(i);
            if (Node.ELEMENT_NODE != n.getNodeType()
                || ! HARNESS_NAMESPACE.equals(n.getNamespaceURI())
                || ! VARIABLE_LOCAL_NAME.equals(n.getLocalName())) {
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
                // look for value lists and ranges
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

                // does type specifies that value may be null?
                if (type.endsWith("?") || type.endsWith("*")) {
                    variables[i] = newVariable(name.getNodeValue(),
                            namespace, type);
                    continue;
                }

                // check for special values
                specialValue = attr
                        .getNamedItem(VARIABLE_SPECIAL_LOCAL_NAME);
                if (null != specialValue) {
                    variables[i] = new SpecialVariable(name
                            .getNodeValue(), namespace, specialValue
                            .getNodeValue());
                    continue;
                }

                throw new NullPointerException(
                        "missing required variable attribute: "
                                + VARIABLE_VALUE_LOCAL_NAME + " or "
                                + VARIABLE_VALUELIST_LOCAL_NAME + " or "
                                + VARIABLE_SPECIAL_LOCAL_NAME + " or "
                                + VARIABLE_MINVALUE_LOCAL_NAME + " and "
                                + VARIABLE_MAXVALUE_LOCAL_NAME
                                + " (or child items)");
            }

            // use fixed value
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
    protected static XdmVariable newVariable(String name,
            String namespace, String type) {
        return newVariable(name, namespace, type, null);
    }

    /**
     * @param name
     * @param namespace
     * @param type
     * @param value
     * @return
     */
    protected static XdmVariable newVariable(String name,
            String namespace, String type, String value) {
        XName xname = (null == namespace) ? new XName(name) : new XName(
                namespace, name);
        XdmValue xvalue = newValue(type, value);
        return ValueFactory.newVariable(xname, xvalue);
    }

    protected static XdmValue newValue(String type, String value) {
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

}