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

import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.marklogic.performance.Configuration;
import com.marklogic.performance.Result;
import com.marklogic.performance.TestInterface;
import com.marklogic.performance.TestIterator;
import com.marklogic.xcc.ContentSource;
import com.marklogic.xcc.ContentSourceFactory;
import com.marklogic.xcc.Request;
import com.marklogic.xcc.RequestOptions;
import com.marklogic.xcc.ResultSequence;
import com.marklogic.xcc.SecurityOptions;
import com.marklogic.xcc.Session;
import com.marklogic.xcc.types.XdmVariable;

/**
 * @author Michael Blakeley, michael.blakeley@marklogic.com
 * 
 *         TODO support SSL
 */
public class XCCSampler extends Sampler {

    protected SecurityOptions securityOptions;

    protected boolean isSecure = false;

    /**
     * @param ti
     * @param cfg
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     */
    public XCCSampler(TestIterator ti, Configuration cfg)
            throws KeyManagementException, NoSuchAlgorithmException {
        super(ti, cfg);
        isSecure = Configuration.PROTOCOL_HTTPS.equals(cfg.getProtocol());
        if (isSecure) {
            securityOptions = newTrustAnyoneOptions();
        }
    }

    public String sample(Result result, String query, TestInterface test)
            throws Exception {
        // time to make sure we have a connection:
        // do this per sample, in case Java's thread management isn't fair
        // new connection every time, to distribute load more evenly
        StringBuffer resultsBuffer = new StringBuffer();
        ContentSource cs = isSecure ? ContentSourceFactory
                .newContentSource(host, port, user, password, null,
                        securityOptions) : ContentSourceFactory
                .newContentSource(host, port, user, password);
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
                resultsBuffer
                        .append(new String(readResponse(result, buf)));
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

    protected static SecurityOptions newTrustAnyoneOptions()
            throws KeyManagementException, NoSuchAlgorithmException {
        TrustManager[] trust = new TrustManager[] { new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }

            /**
             * @throws CertificateException  
             */
            public void checkClientTrusted(
                    java.security.cert.X509Certificate[] certs,
                    String authType) throws CertificateException {
                // no exception means it's okay
            }

            /**
             * @throws CertificateException  
             */
            public void checkServerTrusted(
                    java.security.cert.X509Certificate[] certs,
                    String authType) throws CertificateException {
                // no exception means it's okay
            }
        } };

        SSLContext sslContext = SSLContext.getInstance("SSLv3");
        sslContext.init(null, trust, null);
        return new SecurityOptions(sslContext);
    }
}