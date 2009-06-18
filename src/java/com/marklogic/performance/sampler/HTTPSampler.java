/*
 * Copyright (c)2005-2009 Mark Logic Corporation
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

import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import com.marklogic.performance.Configuration;
import com.marklogic.performance.Result;
import com.marklogic.performance.TestInterface;
import com.marklogic.performance.TestIterator;

/**
 * @author Ron Avnur, ron.avnur@marklogic.com
 * @author Michael Blakeley, michael.blakeley@marklogic.com
 * @author Wayne Feick, wayne.feick@marklogic.com
 * 
 */
public class HTTPSampler extends Sampler {

    private static final String ENCODING = "UTF-8";

    public HTTPSampler(TestIterator ti, Configuration cfg) {
        super(ti, cfg);
    }

    public String sample(Result result, String query, TestInterface test)
            throws Exception {
        String urlEncoded = URLEncoder.encode(query, ENCODING);
        HttpURLConnection conn = setupConnection(new URL(
                protocol, host,
                port, "/evaluate.xqy"));
        // set post headers
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Length", ""
                + urlEncoded.length());
        conn.setRequestProperty("Content-Type",
                "application/x-www-form-urlencoded");
        conn.setDoOutput(true);
        result.incrementBytesSent(query.length());
        PrintWriter out = new PrintWriter(conn.getOutputStream());
        out.print("query=" + urlEncoded);
        out.flush();
        out.close(); // needed?
        result.incrementBytesSent(urlEncoded.length());
        return new String(readResponse(result, conn));
    }

}