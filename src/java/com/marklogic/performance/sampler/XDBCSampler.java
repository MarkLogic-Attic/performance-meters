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
package com.marklogic.performance.sampler;

import java.io.InputStream;

import com.marklogic.performance.Configuration;
import com.marklogic.performance.Result;
import com.marklogic.performance.TestInterface;
import com.marklogic.performance.TestIterator;
import com.marklogic.xdbc.XDBCException;
import com.marklogic.xdbc.XDBCResultSequence;
import com.marklogic.xdbc.XDBCStatement;
import com.marklogic.xdmp.XDMPConnection;

public class XDBCSampler extends Sampler {

    /**
     * @param ti
     * @param cfg
     */
    public XDBCSampler(TestIterator ti, Configuration cfg) {
        super(ti, cfg);
    }

    public String sample(Result result, String query, TestInterface test)
            throws Exception {

        // time to make sure we have a connection:
        // do this per sample, in case Java's thread management isn't fair
        // new connection every time, to distribute load more evenly
        XDMPConnection conn = new XDMPConnection(host, port, user,
                password);
        XDBCStatement statement = null;
        XDBCResultSequence resultSeq = null;
        StringBuffer resultsBuffer = new StringBuffer();

        InputStream buf;
        try {
            statement = conn.createStatement();
            resultSeq = statement.executeQuery(query);
            result.incrementBytesSent(query.length());
            while (resultSeq.hasNext()) {
                buf = resultSeq.nextInputStream();
                resultsBuffer
                        .append(new String(readResponse(result, buf)));
            }
            return resultsBuffer.toString();
        } finally {
            try {
                if (null != resultSeq && !resultSeq.isClosed()) {
                    resultSeq.close();
                }
            } catch (XDBCException e) {
                // do nothing
            }
            try {
                if (null != statement && !statement.isClosed()) {
                    statement.close();
                }
            } catch (XDBCException e) {
                // do nothing
            }
            try {
                if (null != conn && !conn.isClosed()) {
                    conn.close();
                }
            } catch (XDBCException e) {
                // do nothing
            }
        }
    }

}