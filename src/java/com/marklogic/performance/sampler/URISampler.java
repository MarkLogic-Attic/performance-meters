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

import java.net.HttpURLConnection;
import java.net.URL;

import com.marklogic.performance.Configuration;
import com.marklogic.performance.Result;
import com.marklogic.performance.TestInterface;
import com.marklogic.performance.TestIterator;

/*
 * this class expects the query to contain a simple URI, which it attempts to
 * GET from the server
 */
public class URISampler extends Sampler {

    public URISampler(TestIterator ti, Configuration cfg) {
        super(ti, cfg);
    }

    public String sample(Result result, String uri, TestInterface test)
            throws Exception {
        if (null == host) {
            throw new NullPointerException("host is null");
        }
        if (null == uri) {
            throw new NullPointerException("uri is null");
        }
        HttpURLConnection conn = setupConnection(new URL("http", host, port, uri));
        conn.setRequestMethod("GET");
        conn.setDoOutput(true);
        result.incrementBytesSent(uri.length());
        return new String(readResponse(result, conn));
    }

}
