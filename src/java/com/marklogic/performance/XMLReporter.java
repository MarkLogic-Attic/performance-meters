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
import java.io.Writer;
import java.util.List;

/**
 * @author Ron Avnur, ron.avnur@marklogic.com
 * @author Michael Blakeley, michael.blakeley@marklogic.com
 * 
 */
class XMLReporter extends AbstractReporter {

    public XMLReporter() {
        super();
    }

    static final String resultsNodeBegin = "<h:results xmlns:h=\"http://marklogic.com/xdmp/harness\">";

    static final String resultsNodeEnd = "</h:results>";

    static final String resultNodeBegin = "<h:result>";

    static final String resultNodeEnd = "</h:result>";

    // from harness.java
    private static final String padding = "                                        ";

    private static final int charsperindent = 2;

    private boolean reportTime = true;

    public void report(Writer out, boolean _reportTime)
            throws IOException {

        reportTime = _reportTime;

        formatNode(out, resultsNodeBegin, 0, true, true);

        // use summary stats
        if (reportTime) {
            String[] fields = summaryResults.getFieldNames();

            for (int i = 0; i < fields.length; i++) {
                formatElement(out, fields[i], summaryResults
                        .getFieldValue(fields[i]), 1);
            }
        }

        // grab results from each sampler
        Sampler[] samplers = summaryResults.getSamplers();
        // use the built-in fieldnames to report results
        String[] fields = Result.getFieldNames(reportTime);
        for (int i = 0; i < samplers.length; i++) {
            List<Result> results = samplers[i].getResults();
            // put in a result for end time, total time and queries per second.
            // hack in a thread index, too
            for (int j = 0; j < results.size(); j++) {
                putResult(out, fields, results.get(j), reportTime, i);
            }
        }

        formatNode(out, resultsNodeEnd, 0, true, true);

    }

    private void putResult(Writer out, String[] fields, ResultInterface res,
            boolean reportTime, int threadIndex) throws IOException {
        formatNode(out, resultNodeBegin, 1, true, true);

        for (int i = 0; i < fields.length; i++) {
            formatElement(out, fields[i], res.getFieldValue(fields[i]), 2);
        }
        // hack in the threadIndex, too
        if (reportTime) {
            formatElement(out, "thread", "" + threadIndex, 2);
        }

        // close element
        formatNode(out, resultNodeEnd, 1, true, true);
    }

    public static String escapeXml(String _in) {
        if (_in == null)
            return "";
        return _in.replaceAll("&", "&amp;").replaceAll("<", "&lt;")
                .replaceAll(">", "&gt;");
    }

    private void formatElement(Writer out, String qName, String value,
            int level) throws IOException {
        if (value == null || value.trim().equals("")) {
            formatNode(out, "<h:" + qName + "/>", level, true, true);
            return;
        }

        formatNode(out, "<h:" + qName + ">", level, true, false);
        formatNode(out, escapeXml(value.trim()), level, false, false);
        formatNode(out, "</h:" + qName + ">", level, false, true);
    }

    // got from harness.java
    private void formatNode(Writer out, String node, int level,
            boolean indent, boolean newline) throws IOException {
        if (indent) {
            int chunk = (level * charsperindent);
            while (chunk > 0) {
                out.write(padding, 0, chunk);
                chunk -= padding.length();
            }
        }

        if (node != null)
            out.write(node);

        if (newline)
            out.write('\n');
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.marklogic.performance.Reporter#getPreferredFileExtension()
     */
    public String getPreferredFileExtension() {
        return ".xml";
    }

}