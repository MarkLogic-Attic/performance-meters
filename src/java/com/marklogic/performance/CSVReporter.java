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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Michael Blakeley, michael.blakeley@marklogic.com
 * 
 */
class CSVReporter extends AbstractReporter {

    Pattern multilineWhitespace = Pattern.compile(".*\\s+.*", Pattern.DOTALL);
    private Matcher matcher;

    public CSVReporter() {
        super();
    }

    /**
     * @param _items
     * @param _delim
     * @return
     */
    public static String join(String[] _items, String _delim) {
        String rval = "";
        for (int i = 0; i < _items.length; i++)
            if (i == 0)
                rval = _items[0];
            else
                rval += _delim + _items[i];
        return rval;
    }

    public void report(Writer out, boolean reportTime) throws IOException {

        // output summary stats
        if (reportTime) {
            String[] fields = summaryResults.getFieldNames();
            out.write(join(fields, ","));
            out.write("\n");

            for (int i = 0; i < fields.length; i++) {
                if (i > 0)
                    out.write(",");
                out.write(summaryResults.getFieldValue(fields[i]));
            }
            out.write("\n");
            out.write("\n");
        }

        // grab results from each sampler
        Sampler[] samplers = summaryResults.getSamplers();

        // use the built-in fieldnames to report results
        String[] fields = Result.getFieldNames(reportTime);
        out.write(join(fields, ","));
        out.write("\n");

        for (int i = 0; i < samplers.length; i++) {
            List results = samplers[i].getResults();
            // put in a result for end time, total time and queries per second.
            for (int j = 0; j < results.size(); j++) {
                putResult(out, fields, (Result) (results.get(j)), reportTime);
            }
        }

    }

    private void putResult(Writer out, String[] fields, Result res,
            boolean reportTime) throws IOException {
        for (int i = 0; i < fields.length; i++) {
            if (i > 0)
                out.write(",");

            out.write(escape(res.getFieldValue(fields[i])));
        }
        out.write("\n");
    }

    /**
     * @param fieldValue
     * @return
     */
    private String escape(String fieldValue) {
        if (fieldValue == null)
            return "";

        // CSV escaping format
        String rv = fieldValue;
        // check for embedded double-quotes and double-escape
        if (rv.indexOf('"') > -1) {
            rv = rv.replace("\"", "\"\"");
        }
        matcher = multilineWhitespace.matcher(rv);
        if (matcher.matches()) {
            rv = "\"" + rv + "\"";
        }
        return rv;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.marklogic.performance.Reporter#getPreferredFileExtension()
     */
    public String getPreferredFileExtension() {
        return ".csv";
    }

}
