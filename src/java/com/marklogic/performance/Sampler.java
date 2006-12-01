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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

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

    protected Sampler(TestIterator ti, Configuration cfg) {
        testIterator = ti;
        config = cfg;
    }

    public int getResultsCount() {
        return results.size();
    }

    public List<Result> getResults() {
        return results;
    }

    public TestInterface[] getResultsArray() {
        return results.toArray(new TestInterface[0]);
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
        results = new ArrayList<Result>();

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
        try {
            while (testTimeNanos != 0) {
                if (random != null) {
                    testIterator.shuffle(random);
                }
                while (testIterator.hasNext()) {
                    results.add(sample(testIterator.next()));
                    if (testTimeNanos != 0) {
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

                if (testTimeNanos == 0) {
                    // no more tests to run: exit the loop
                    break;
                }

                if (testTimeNanos < System.nanoTime() - startTime) {
                    // end of the timed test
                    break;
                }
                testIterator.reset();
                if (!testIterator.hasNext()) {
                    throw new SamplerException("reset did not work for "
                            + testIterator);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    protected abstract Result sample(TestInterface test)
            throws IOException;

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
        Iterator i = results.iterator();
        errorCount = 0;
        while (i.hasNext()) {
            r = (Result) i.next();
            if (r.isError()) {
                errorCount++;
            }
        }
        return errorCount;
    }

}