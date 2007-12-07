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
package com.marklogic.performance.reporter;

import java.io.IOException;
import java.io.Writer;

import com.marklogic.performance.SummaryResults;

/**
 * @author Ron Avnur, ron.avnur@marklogic.com
 * @author Michael Blakeley, michael.blakeley@marklogic.com
 * 
 */
public interface Reporter {

    void report(Writer out, boolean reportTime) throws IOException;

    /**
     * @param summaryResults
     */
    void setSummaryResults(SummaryResults summaryResults);

    /**
     * Preferred file extension, which includes the dot.
     * 
     * @return
     */
    String getPreferredFileExtension();

}