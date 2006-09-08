/*
 * Copyright (c)2005-2006 Mark Logic Corporation
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

import org.w3c.dom.Element;
import org.w3c.dom.Node;

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

    private String commentExpectedResult;

    private String query;

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
        name = nameNode.getTextContent();
        if (commentExpectedResultNode != null) {
            commentExpectedResult = commentExpectedResultNode
                    .getTextContent().trim();
        }
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
}