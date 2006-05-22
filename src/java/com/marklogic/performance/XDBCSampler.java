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
import java.io.IOException;

import com.marklogic.xdbc.XDBCException;
import com.marklogic.xdbc.XDBCResultSequence;
import com.marklogic.xdbc.XDBCStatement;
import com.marklogic.xdmp.XDMPConnection;

class XDBCSampler extends Sampler {

    /**
     * @param ti
     * @param cfg
     */
    XDBCSampler(TestIterator ti, Configuration cfg) {
        super(ti, cfg);
    }

    protected Result sample(TestInterface test) throws SamplerException {

        // time to make sure we have a connection:
        // do this per sample, in case Java's thread management isn't fair
        XDMPConnection conn = null;

        try {
            // new connection every time, to distribute load more evenly
            conn = new XDMPConnection(host, port, user, password);
        } catch (XDBCException e) {
            e.printStackTrace();
            // bail!
            System.exit(1);
            // make Eclipse happy
            return null;
        }

        Result res = new Result(test.getName(), test.getCommentExpectedResult());
        char[] readBuffer = new char[READSIZE];
        StringBuffer resultsBuffer = new StringBuffer();
        XDBCResultSequence resultSeq = null;
        // do some work
        String query;
        try {
            query = test.getQuery();
        } catch (IOException e) {
            throw new SamplerException(e);
        }
        int actual;
        res.setStart(System.currentTimeMillis());
        XDBCStatement statement = null;
        try {
            statement = conn.createStatement();
            resultSeq = statement.executeQuery(query);
            res.incrementBytesSent(query.length());
            while (resultSeq.hasNext()) {
                actual = 1;
                BufferedReader buf = resultSeq.nextReader();
                while (actual > 0) {
                    actual = buf.read(readBuffer);
                    if (actual > 0) {
                        res.incrementBytesReceived(actual);
                        if (!reportTime || recordResults) {
                            resultsBuffer.append(readBuffer, 0, actual);
                        }
                    }
                }
            }

            // add the textual result to the results object,
            // if the configuration demands it.
            if (!reportTime || recordResults) {
                String resultsString = resultsBuffer.toString().trim();
                res.setQueryResult(resultsString);
                if (checkResults
                        && !test.getCommentExpectedResult().equals(
                                resultsString)) {
                    res.setError(true);
                }
            }
        } catch (Exception e) {
            System.err.println("Error running query: " + query);
            e.printStackTrace();

            res.setError(true);
            String errorMessage = e.getMessage();
            if (errorMessage == null) {
                errorMessage = "NULL";
            }
            if (!reportTime || recordResults) {
                res.setQueryResult(errorMessage);
            }
        }
        res.setEnd(System.currentTimeMillis());
        try {
            if (resultSeq != null && !resultSeq.isClosed()) {
                resultSeq.close();
            }
            resultSeq = null;
            if (statement != null && !statement.isClosed()) {
                statement.close();
            }
            statement = null;
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
            conn = null;
        } catch (XDBCException e) {
            res.setError(true);
            String errorMessage = e.getMessage();
            e.printStackTrace();
            if (errorMessage == null)
                errorMessage = "NULL";
            if (!reportTime || recordResults)
                res.setQueryResult(errorMessage);
        }
        return res;
    }

}