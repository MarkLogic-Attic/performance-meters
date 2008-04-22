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

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import com.marklogic.performance.Configuration;
import com.marklogic.performance.Result;
import com.marklogic.performance.ResultInterface;
import com.marklogic.performance.TestInterface;
import com.marklogic.performance.TestIterator;

// TODO implement fixed number of test loops (iterations)

public abstract class Sampler extends Thread {
    TestIterator testIterator;

    protected List<Result> results;

    Configuration config;

    Random random = null;

    private int index = 0;

    private int errorCount = -1;

    protected static int readsize = Configuration.READSIZE_DEFAULT;

    // cache this stuff in case there's synchronization
    boolean recordResults = Configuration.RECORDRESULTS_DEFAULT;

    boolean reportTime = Configuration.REPORTTIME_DEFAULT;

    boolean checkResults = Configuration.CHECKRESULTS_DEFAULT;

    String user = Configuration.USER_DEFAULT;

    String password = Configuration.PASSWORD_DEFAULT;

    String host = Configuration.HOST_DEFAULT;

    int port = Configuration.PORT_DEFAULT;

    byte[] readBuffer = new byte[readsize];

    public Sampler(TestIterator ti, Configuration cfg) {
        testIterator = ti;
        config = cfg;
        // ensure that results are never null
        results = new ArrayList<Result>();
    }

    public Result sample(TestInterface test) {
        String name = test.getName();
        String query = null;
        try {
            query = test.getQuery();
        } catch (Exception e) {
            // turn this into a fatal runtime exception
            throw new SamplerException(e);
        }
        Result res = new Result(name, test.getCommentExpectedResult());
        res.setStart();
        try {
            String responseData = sample(res, query, test);
            res.incrementBytesReceived(responseData.length());
            if (!config.isReportTime() || config.getRecordResults()) {
                // TODO query result should be byte[] for binary results?
                res.setQueryResult(responseData);
            }
            if (checkResults
                    && !test.getCommentExpectedResult().equals(
                            res.getQueryResult())) {
                res.setError(true);
            }
        } catch (RuntimeException e) {
            // fatal!
            e.printStackTrace();
            Runtime.getRuntime().exit(1);
        } catch (Exception e) {
            String errorMessage = e.toString() + " " + e.getMessage();
            if (errorMessage == null) {
                errorMessage = e.toString();
            }
            System.err.println("Error running query "
                    + (null != name ? name : query) + ": " + errorMessage);
            res.setError(true);
            if (!config.isReportTime() || config.getRecordResults()) {
                res.setQueryResult(errorMessage);
            }
        }
        res.setEnd();
        return res;
    }

    // TODO query result should be byte[] for binary results?
    protected abstract String sample(Result result, String query,
            TestInterface test) throws Exception;

    public int getResultsCount() {
        return results.size();
    }

    public List<Result> getResults() {
        return results;
    }

    public ResultInterface[] getResultsArray() {
        return results.toArray(new Result[0]);
    }

    public void run() {
        // cache this stuff in case there's synchronization
        readsize = config.getReadSize();
        recordResults = config.getRecordResults();
        reportTime = config.isReportTime();
        checkResults = config.checkResults();
        user = config.getUser();
        password = config.getPassword();
        host = config.getHost();
        port = config.getPort();

        // random tests only make sense as timed tests
        if (config.isTimedTest()) {
            if (config.isRandomTest()) {
                random = new Random();
                long randomSeed = config.getRandomSeed();
                if (randomSeed != Configuration.RANDOMSEED_DEFAULT) {
                    // adjust for thread identity: the exact technique
                    // shouldn't matter much
                    randomSeed = randomSeed + index;
                    random.setSeed(randomSeed);
                }
            }
        }

        // timed test: run for a specified number of seconds
        // not timed test: run once
        long startTime = System.nanoTime();
        long testTimeNanos = config.getTestTimeNanos();
        long lastConfigUpdate = startTime;
        long updateNanos = Configuration.NANOS_PER_SECOND;
        long nowTime;
        do {
            if (null != random) {
                testIterator.shuffle(random);
            }
            while (testIterator.hasNext()) {
                results.add(sample(testIterator.next()));
                if (0 != testTimeNanos) {
                    if (testTimeNanos < System.nanoTime() - startTime) {
                        // end of the timed test
                        break;
                    }
                }
                // try to avoid thread starvation
                yield();
                // config may contain multiple hosts,
                // so balance load by updating once in a while,
                // but not every time, or we have locking issues.
                nowTime = System.nanoTime();
                if (nowTime - updateNanos > lastConfigUpdate) {
                    host = config.getHost();
                    lastConfigUpdate = System.nanoTime();
                }
            }

            if (0 == testTimeNanos) {
                // no more tests to run: exit the loop
                break;
            }

            if (testTimeNanos < System.nanoTime() - startTime) {
                // end of the timed test
                break;
            }
            testIterator.reset();
            if (!testIterator.hasNext()) {
                throw new SamplerException("reset failed for "
                        + testIterator);
            }
        } while (testTimeNanos != 0);
    }

    void printResults() {
        System.out.println(results.size());
        for (int i = 0; i < results.size(); i++) {
            results.get(i).print();
        }
    }

    /**
     * @return
     */
    public long getMinDurationNanos() {
        long min = Long.MAX_VALUE;
        long d;
        for (int i = 0; i < results.size(); i++) {
            d = results.get(i).getDurationNanos();
            if (d < min) {
                min = d;
            }
        }
        return min;
    }

    /**
     * @return
     */
    public long getMaxDurationNanos() {
        long max = Long.MIN_VALUE;
        long d;
        for (int i = 0; i < results.size(); i++) {
            d = results.get(i).getDurationNanos();
            if (d > max) {
                max = d;
            }
        }
        return max;
    }

    /**
     * @return
     */
    public long getTotalNanos() {
        long tm = 0;
        for (int i = 0; i < results.size(); i++) {
            tm += results.get(i).getDurationNanos();
        }
        return tm;
    }

    /**
     * @return
     */
    public double getTotalMillis() {
        return getTotalNanos() / Configuration.NANOS_PER_MILLI;
    }

    /**
     * @return
     */
    public long getBytesSent() {
        long bs = 0;
        for (int i = 0; i < results.size(); i++) {
            bs += results.get(i).getBytesSent();
        }
        return bs;
    }

    /**
     * @return
     */
    public long getBytesReceived() {
        long br = 0;
        for (int i = 0; i < results.size(); i++) {
            br += results.get(i).getBytesReceived();
        }
        return br;
    }

    /**
     * @param i
     */
    public void setIndex(int i) {
        index = i;
    }

    /**
     * @return
     */
    public int getErrorCount() {
        if (errorCount > -1) {
            return errorCount;
        }

        Result r = null;
        Iterator<Result> i = results.iterator();
        errorCount = 0;
        while (i.hasNext()) {
            r = i.next();
            if (r.isError()) {
                errorCount++;
            }
        }
        return errorCount;
    }

    protected HttpURLConnection setupConnection(URL url)
            throws IOException {
        HttpURLConnection.setFollowRedirects(true);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        // using keepalive
        conn.setRequestProperty("Connection", "keep-alive");
        conn.setRequestProperty("Authorization", "Basic "
                + Base64Encoder.encode(user + ":" + password));
        return conn;
    }

    protected byte[] readResponse(Result result, HttpURLConnection conn)
            throws IOException {
        return readResponse(result, conn.getInputStream());
    }

    protected byte[] readResponse(Result result, InputStream in)
            throws IOException {
        ByteArrayOutputStream w = null;
        BufferedInputStream bin = null;
        try {
            bin = new BufferedInputStream(in);
            w = new ByteArrayOutputStream();
            int actual = 0;
            while ((actual = bin.read(readBuffer)) > -1) {
                result.incrementBytesReceived(actual);
                w.write(readBuffer, 0, actual);
            }
            w.flush();
            return w.toByteArray();
        } finally {
            if (null != bin) {
                bin.close();
            }
            if (null != w) {
                w.close();
            }
        }
    }

}