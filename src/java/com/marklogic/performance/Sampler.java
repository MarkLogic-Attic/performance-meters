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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

// TODO implement fixed number of test loops (iterations)

abstract class Sampler implements Runnable {
    TestIterator testIterator;

    List results;

    Configuration config;

    Random random = null;

    private int index = 0;

    private int errorCount = -1;

    protected static int READSIZE = Configuration.DEFAULT_READSIZE;

    // cache this stuff in case there's synchronization
    boolean recordResults = Configuration.DEFAULT_RECORDRESULTS;

    boolean reportTime = Configuration.DEFAULT_REPORTTIME;
    
    boolean checkResults = Configuration.DEFAULT_CHECKRESULTS;

    String user = Configuration.DEFAULT_USER;
    
    String password = Configuration.DEFAULT_PASSWORD;

    String host = Configuration.DEFAULT_HOST;

    int port = Configuration.DEFAULT_PORT;

    byte[] readBuffer = new byte[READSIZE];

    Sampler(TestIterator ti, Configuration cfg) {
        testIterator = ti;
        config = cfg;
        // cache this stuff in case there's synchronization
        recordResults = config.getRecordResults();
        reportTime = config.getReportTime();
        checkResults = config.checkResults();
        user = config.getUser();
        password = config.getPassword();
        host = config.getHost();
        port = config.getPort();
        results = new ArrayList();
    }

    public int getResultsCount() {
        return results.size();
    }

    public List getResults() {
        return results;
    }

    public TestInterface[] getResultsArray() {
        return (TestInterface[]) results.toArray(new TestInterface[0]);
    }

    public void run() {
        READSIZE = config.getReadSize();

        // random tests only make sense as timed tests
        if (config.isTimedTest()) {
            if (config.isRandomTest()) {
                random = new Random();
                long randomSeed = config.getRandomSeed();
                if (randomSeed != Configuration.DEFAULT_RANDOMSEED) {
                    // adjust for thread identity: the exact technique
                    // shouldn't matter much
                    randomSeed = randomSeed + index;
                    random.setSeed(randomSeed);
                }
            }
        }

        // timed test: run for a specified number of seconds
        // not timed test: run once
        long startTime = System.currentTimeMillis();
        try {
            while (!config.isTimedTest()
                    || (config.getTestTimeMillis()
                            - (System.currentTimeMillis() - startTime) > 0)) {
                if (random != null) {
                    testIterator.shuffle(random);
                }
                while (testIterator.hasNext()) {
                    results.add(sample(testIterator.next()));
                }
                if (config.isTimedTest()) {
                    testIterator.reset();
                    if (!testIterator.hasNext()) {
                        throw new SamplerException("reset did not work for "
                                + testIterator);
                    }
                } else {
                    // exit the loop
                    break;
                }
            }
        } catch (SamplerException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    protected abstract Result sample(TestInterface test) throws SamplerException;

    void printResults() {
        System.out.println(results.size());
        for (int i = 0; i < results.size(); i++)
            ((Result) (results.get(i))).print();
    }

    /**
     * @return
     */
    public long getMinDurationMillis() {
        long min = Long.MAX_VALUE;
        long d;
        for (int i = 0; i < results.size(); i++) {
            d = ((Result) results.get(i)).getDuration();
            if (d < min)
                min = d;
        }
        return min;
    }

    /**
     * @return
     */
    public long getMaxDurationMillis() {
        long max = Long.MIN_VALUE;
        long d;
        for (int i = 0; i < results.size(); i++) {
            d = ((Result) results.get(i)).getDuration();
            if (d > max)
                max = d;
        }
        return max;
    }

    /**
     * @return
     */
    public long getTotalMillis() {
        long tm = 0;
        for (int i = 0; i < results.size(); i++) {
            tm += ((Result) results.get(i)).getDuration();
        }
        return tm;
    }

    /**
     * @return
     */
    public long getBytesSent() {
        long bs = 0;
        for (int i = 0; i < results.size(); i++) {
            bs += ((Result) results.get(i)).getBytesSent();
        }
        return bs;
    }

    /**
     * @return
     */
    public long getBytesReceived() {
        long br = 0;
        for (int i = 0; i < results.size(); i++) {
            br += ((Result) results.get(i)).getBytesReceived();
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