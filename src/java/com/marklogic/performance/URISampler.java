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
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

class URISampler extends Sampler {

    byte[] readBuffer = new byte[READSIZE];

    public final static String AUTO_REDIRECTS = "HTTPSampler.auto_redirects";

    URISampler(TestIterator ti, Configuration cfg) {
        super(ti, cfg);
    }

    private HttpURLConnection setupConnection(Result res, String uri)
            throws IOException {

        URL url = new URL("http", config.getHost(), config.getPort(), uri);
        HttpURLConnection conn;
        HttpURLConnection.setFollowRedirects(true);
        conn = (HttpURLConnection) url.openConnection();

        // using keepalive
        conn.setRequestProperty("Connection", "keep-alive");
        // setConnectionAuthorization(conn, u, getAuthManager());
        String authHeader = "Basic "
                + Base64Encoder.encode(config.getUser() + ":"
                        + config.getPassword());
        conn.setRequestProperty("Authorization", authHeader);
        // set post headers
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Length", "" + 9);
        conn.setRequestProperty("Content-Type",
                "application/x-www-form-urlencoded");
        conn.setDoOutput(true);
        // setPostHeaders(conn);
        return conn;
    }

    private void sendPostData(URLConnection connection, String query)
            throws IOException {
        String postData = "query=foo";
        PrintWriter out = new PrintWriter(connection.getOutputStream());
        out.print(postData);
        out.flush();
    }

    private byte[] readResponse(HttpURLConnection conn) throws IOException {
        BufferedInputStream in;
        try {
            in = new BufferedInputStream(conn.getInputStream());
        } catch (IOException e) {
            in = new BufferedInputStream(conn.getErrorStream());
        } catch (Exception e) {
            in = new BufferedInputStream(conn.getErrorStream());
        }
        java.io.ByteArrayOutputStream w = new ByteArrayOutputStream();
        int x = 0;
        boolean first = true;
        while ((x = in.read(readBuffer)) > -1) {
            if (first)
                first = false; // to capture latency end
            w.write(readBuffer, 0, x);
        }
        in.close();
        w.flush();
        w.close();
        return w.toByteArray();
    }

    protected Result sample(TestInterface test) throws SamplerException {
        Result res = new Result(test.getName(), test.getCommentExpectedResult());
        byte[] responseData = null;
        res.setStart(System.currentTimeMillis());
        // do some work
        String url;
        try {
            url = test.getQuery();
        } catch (IOException e) {
            throw new SamplerException(e);
        }
        try {
            HttpURLConnection conn = setupConnection(res, url);
            // send post data
            sendPostData(conn, url);
            // get response
            responseData = readResponse(conn);
        } catch (IOException e) {
            res.setError(true);
            String errorMessage = e.getMessage();
            if (errorMessage == null)
                errorMessage = "NULL";
            if (!config.getReportTime() || config.getRecordResults())
                res.setQueryResult(errorMessage);
        } finally {
            if (!config.getReportTime() || config.getRecordResults()) {
                res.setQueryResult(new String(responseData));
            }
        }
        res.setEnd(System.currentTimeMillis());
        return res;
    }

}
