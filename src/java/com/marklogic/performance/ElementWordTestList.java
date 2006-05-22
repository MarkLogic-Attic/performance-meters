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

/**
 * @author Michael Blakeley, michael.blakeley@marklogic.com
 *
 */
public class ElementWordTestList extends TestList {

    public void initialize(Configuration _config) throws Exception {
        super.initialize(_config);
        
        if (configuration.checkResults()) {
            System.err.println("WARNING: ignoring checkResults");
        }
        
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

        String elementQName = configuration.getElementQName();
        if (elementQName == null) {
            throw new IOException("missing required configuration parameter: elementQName");
        }
        
        // the inputpath contains one line per word to test
        // we want the file to be unlimited in size
        tests.add(new ElementWordTest(inputFile, elementQName));
    }

}
