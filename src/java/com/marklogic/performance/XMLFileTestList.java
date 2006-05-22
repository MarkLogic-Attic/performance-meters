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

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 * @author Michael Blakeley, michael.blakeley@marklogic.com
 *
 */
public class XMLFileTestList extends TestList {

    public void initialize(Configuration _config) throws Exception {
        super.initialize(_config);

        // load the file into a DOM object.
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        String inputPath = configuration.getInputPath();
        if (inputPath == null) {
            throw new IOException(
                    "missing required configuration parameter: inputPath");
        }
        
        File inputFile = new File(inputPath);
        if (!inputFile.canRead()) {
            throw new IOException("missing or unreadable inputPath: "
                    + inputFile.getCanonicalPath());
        }
        
        Document scriptDocument = builder.parse(inputFile);
        NodeList testList = scriptDocument.getDocumentElement().getChildNodes();
        for (int i = 0; i < testList.getLength(); i++) {
            if (testList.item(i).getNodeType() != org.w3c.dom.Node.ELEMENT_NODE) {
                continue;
            }
            tests.add(new XMLFileTest(testList.item(i)));
        }
    }
    
}
