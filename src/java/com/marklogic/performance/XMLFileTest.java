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

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

class XMLFileTest extends AbstractTest {
    
    private String commentExpectedResult;

    private String query;

    private String GetNodeText(Node t) {
        if ((t == null) || (t.getNodeType() != org.w3c.dom.Node.ELEMENT_NODE))
            return null;
        NodeList children = t.getChildNodes();
        StringBuffer text = new StringBuffer();
        for (int c = 0; c < children.getLength(); c++) {
            Node child = children.item(c);
            short nodeType = child.getNodeType();
            if (nodeType == Node.TEXT_NODE) {
                text.append(child.getNodeValue());
            } else if (nodeType == Node.CDATA_SECTION_NODE) {
                text.append(child.getNodeValue());
            }
            // otherwise we ignore it...
        }
        return text.toString();
    }

    public XMLFileTest(Node node) {
        Node queryNode = (((Element) node).getElementsByTagName("h:query")
                .item(0));
        Node nameNode = (((Element) node).getElementsByTagName("h:name")
                .item(0));
        Node commentExpectedResultNode = (((Element) node)
                .getElementsByTagName("h:comment-expected-result").item(0));
        query = GetNodeText(queryNode);
        name = GetNodeText(nameNode);
        commentExpectedResult = GetNodeText(commentExpectedResultNode).trim();
    }

    /* (non-Javadoc)
     * @see com.marklogic.performance.TestInterface#getQuery()
     */
    public String getQuery() {
        return query;
    }

    /* (non-Javadoc)
     * @see com.marklogic.performance.TestInterface#getCommentExpectedResult()
     */
    public String getCommentExpectedResult() {
        return commentExpectedResult;
    }
}