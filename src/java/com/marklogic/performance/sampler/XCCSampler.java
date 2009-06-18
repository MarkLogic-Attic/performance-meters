/*
 * Copyright (c)2005-2008 Mark Logic Corporation
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
import com.marklogic.xcc.ContentSource;
import com.marklogic.xcc.ContentSourceFactory;
import com.marklogic.xcc.Request;
import com.marklogic.xcc.RequestOptions;
import com.marklogic.xcc.ResultSequence;
import com.marklogic.xcc.Session;
import com.marklogic.xcc.types.XdmVariable;

public class XCCSampler extends Sampler {

    /**
     * @param ti
     * @param cfg
     */
    public XCCSampler(TestIterator ti, Configuration cfg) {
        super(ti, cfg);
    }

    public String sample(Result result, String query,
            TestInterface test) throws Exception {
        // time to make sure we have a connection:
        // do this per sample, in case Java's thread management isn't fair
        // new connection every time, to distribute load more evenly
        StringBuffer resultsBuffer = new StringBuffer();
        ContentSource cs = ContentSourceFactory.newContentSource(host,
                port, user, password);
        Session sess = cs.newSession();

        try {
            // do not cache results, in case recordResults=false
            // and the actual results are huge.
            RequestOptions requestOptions = sess
                    .getDefaultRequestOptions();
            requestOptions.setCacheResult(false);
            Request req = sess.newAdhocQuery(query, requestOptions);
            setVariables(req, test);
            ResultSequence rs = sess.submitRequest(req);
            result.incrementBytesSent(query.length());

            // handle results
            InputStream buf = null;
            while (rs.hasNext()) {
                buf = rs.next().asInputStream();
                resultsBuffer.append(new String(readResponse(result, buf)));
            }
        } finally {
            sess.close();
        }
        return resultsBuffer.toString();
    }

    /**
     * @param req
     * @param test
     */
    private void setVariables(Request req, TestInterface test) {
        if (!test.hasVariables()) {
            return;
        }

        XdmVariable[] variables = test.getVariables();
        for (int i = 0; i < variables.length; i++) {
            if (null != variables[i]) {
                req.setVariable(variables[i]);
            }
        }
    }

}