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

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/*
 * this class expects the query to contain a simple URI, which it attempts to
 * GET from the server
 */
class URISampler extends Sampler {

    URISampler(TestIterator ti, Configuration cfg) {
        super(ti, cfg);
    }

    private HttpURLConnection setupConnection(String uri)
            throws IOException {

        URL url = new URL("http", host, port, uri);
        HttpURLConnection.setFollowRedirects(true);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        // using keepalive
        conn.setRequestProperty("Connection", "keep-alive");
        String authHeader = "Basic "
                + Base64Encoder.encode(user + ":"
                        + password);
        conn.setRequestProperty("Authorization", authHeader);
        // set post headers
        conn.setRequestMethod("GET");
        conn.setDoOutput(true);
        return conn;
    }

    private byte[] readResponse(HttpURLConnection conn)
            throws IOException {
        BufferedInputStream in = null;
        ByteArrayOutputStream w = null;
        try {
            in = new BufferedInputStream(conn.getInputStream());
            w = new ByteArrayOutputStream();
            int x = 0;
            boolean first = true;
            while ((x = in.read(readBuffer)) > -1) {
                if (first)
                    first = false; // to capture latency end
                w.write(readBuffer, 0, x);
            }
            w.flush();
            return w.toByteArray();
        } finally {
            if (null != in) {
                in.close();
            }
            if (null != w) {
                w.close();
            }
        }
    }

    protected Result sample(TestInterface test) throws IOException {
        Result res = new Result(test.getName(), test
                .getCommentExpectedResult());
        byte[] responseData = null;
        res.setStart();
        String uri = test.getQuery();

        try {
            HttpURLConnection conn = setupConnection(uri);
            res.incrementBytesSent(uri.length());
            // get response
            responseData = readResponse(conn);
            res.incrementBytesReceived(responseData.length);
            
            if (!config.isReportTime()
                    || config.getRecordResults()) {
                res.setQueryResult(new String(responseData));
            }
        } catch (IOException e) {
            res.setError(true);
            String errorMessage = e.getMessage();
            if (errorMessage == null)
                errorMessage = "NULL";
            if (!config.isReportTime() || config.getRecordResults()) {
                res.setQueryResult(errorMessage);
            }
        }
        res.setEnd();
        return res;
    }

}
