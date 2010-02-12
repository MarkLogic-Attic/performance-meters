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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import com.marklogic.xcc.types.XdmVariable;

/**
 * @author Michael Blakeley, michael.blakeley@marklogic.com
 *
 */
public class DocumentUriTest extends AbstractTest {

    private BufferedReader br;
    private File inputFile;

    /**
     * @param _file
     * @throws FileNotFoundException
     */
    public DocumentUriTest(File _file) throws FileNotFoundException {
        name = "DocumentUriTest";
        inputFile = _file;
        br = new BufferedReader(new FileReader(_file));
    }

    /*
     * (non-Javadoc)
     *
     * @see com.marklogic.performance.TestInterface#getQuery()
     */
    public String getQuery() throws IOException {
        // build a query to fetch the document by URI,
        // pulling the uri from the input file
        return "doc(" + getNextUri() + ")";
    }

    /**
     * @return
     * @throws IOException
     */
    private String getNextUri() throws IOException {
        if (!br.ready()) {
            // start over at the top of the file
            br.close();
            br = new BufferedReader(new FileReader(inputFile));
        }

        return br.readLine().trim();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.marklogic.performance.TestInterface#getCommentExpectedResult()
     */
    public String getCommentExpectedResult() {
        // invalid for this test type
        return null;
    }

    /* (non-Javadoc)
     * @see com.marklogic.performance.TestInterface#hasVariables()
     */
    public boolean hasVariables() {
        return false;
    }

    /* (non-Javadoc)
     * @see com.marklogic.performance.TestInterface#getVariables()
     */
    public XdmVariable[] getVariables() {
        return null;
    }

}
