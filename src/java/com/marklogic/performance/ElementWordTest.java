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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

/**
 * @author Michael Blakeley, michael.blakeley@marklogic.com
 * 
 */
public class ElementWordTest extends AbstractTest {

    private String elementQName;

    private String[] words;

    private int wordsIndex;

    /**
     * @param inputFile
     * @param elementQName
     * @throws FileNotFoundException
     */
    public ElementWordTest(File _file, String _elementQName)
            throws FileNotFoundException {
        name = "ElementWordTest";
        elementQName = _elementQName;

        System.err.println(new Date() + ": building word list...");
        BufferedReader br = new BufferedReader(new FileReader(_file));
        String word = null;
        ArrayList tempList = new ArrayList();
        try {
            while ((word = br.readLine()) != null) {
                tempList.add(word);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        System.err.println(new Date() + ": shuffling word list...");
        Collections.shuffle(tempList);
        words = (String[]) tempList.toArray(new String[0]);
        wordsIndex = 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.marklogic.performance.TestInterface#getQuery()
     */
    public String getQuery() {
        return "cts:search(//" + elementQName + ", '" + getNextValue() + "')";
    }

    /**
     * @return
     */
    private String getNextValue() {
        if (wordsIndex >= words.length) {
            wordsIndex = 0;
        }
        return words[wordsIndex++];
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

}
