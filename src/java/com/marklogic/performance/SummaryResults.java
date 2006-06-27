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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author Michael Blakeley, michael.blakeley@marklogic.com
 * 
 */
public class SummaryResults {

    /**
     * actual number will be appended to the field name
     */
    private static final String PERCENTILE_DURATION = "percentile-duration-";

    /**
     * 
     */
    private static final String BYTES_PER_SECOND = "bytes-per-second";

    /**
     * 
     */
    private static final String TESTS_PER_SECOND = "tests-per-second";

    /**
     * 
     */
    private static final String TOTAL_BYTES_RECEIVED = "total-bytes-received";

    /**
     * 
     */
    private static final String AVERAGE_MS = "average-ms";

    /**
     * 
     */
    private static final String TOTAL_MS = "total-ms";

    /**
     * 
     */
    private static final String TEST_DURATION_MS = "test-duration";

    /**
     * 
     */
    private static final String TOTAL_BYTES_SENT = "total-bytes-sent";

    /**
     * 
     */
    private static final String MAXIMUM_MS = "maximum-ms";

    /**
     * 
     */
    private static final String MINIMUM_MS = "minimum-ms";

    /**
     * 
     */
    private static final String NUMBER_OF_TESTS = "number-of-tests";

    private static final String NUMBER_OF_THREADS = "number-of-threads";

    private static final String NUMBER_OF_ERRORS = "number-of-errors";

    private long numberOfTests = 0;

    private long numberOfErrors = 0;

    long minMillis = Long.MAX_VALUE;

    long maxMillis = Long.MIN_VALUE;

    long totalMillis = 0;

    long durationMillis = 0;

    long avgMillis;

    long bytesSent = 0;

    long bytesReceived = 0;

    double testsPerSecond, bytesPerSecond;

    private Sampler[] samplers;

    private int reportPercentileDuration = 0;

    private int numberOfThreads = 1;

    public SummaryResults(Configuration _config, long startMillis,
            long endMillis, Sampler[] _samplers) {
        samplers = _samplers;
        durationMillis = endMillis - startMillis;

        // gather min, max, avg response times
        // gather bytes sent, received
        Sampler sampler;
        long min, max;
        for (int i = 0; i < samplers.length; i++) {
            sampler = samplers[i];
            numberOfTests += sampler.getResultsCount();
            numberOfErrors += sampler.getErrorCount();
            min = sampler.getMinDurationMillis();
            if (min < minMillis)
                minMillis = min;
            max = sampler.getMaxDurationMillis();
            if (max > maxMillis)
                maxMillis = max;
            totalMillis += sampler.getTotalMillis();
            bytesSent += sampler.getBytesSent();
            bytesReceived += sampler.getBytesReceived();
        }

        avgMillis = totalMillis / numberOfTests;

        // gather throughput, in tps and bps
        testsPerSecond = (((double) numberOfTests) / ((double) durationMillis)) * 1000;
        bytesPerSecond = (1000 * (bytesSent + bytesReceived))
                / durationMillis;

        // gather configuration information
        numberOfThreads = _config.getNumThreads();
    }

    public void setReportPercentileDuration(int p) {
        reportPercentileDuration = p;
    }

    /**
     * implement percentiles
     * 
     * @return
     */
    public long getPercentileDuration() {
        if (reportPercentileDuration < 1)
            return 0;

        if (samplers.length < 1)
            return 0;

        // we need a list of all the Result timings from every Sampler
        List events = new ArrayList();
        for (int i = 0; i < samplers.length; i++) {
            // results are a List of Test objects
            events.addAll(samplers[i].getResults());
        }

        double size = events.size();
        Comparator c = new ResultDurationComparator();
        Collections.sort(events, c);
        int pidx = (int) (reportPercentileDuration * size * .01);
        return ((Result) events.get(pidx)).getDuration();
    }

    public String[] getFieldNames() {
        if (reportPercentileDuration > 0) {
            return new String[] { NUMBER_OF_TESTS, NUMBER_OF_ERRORS,
                    NUMBER_OF_THREADS, MINIMUM_MS, MAXIMUM_MS,
                    AVERAGE_MS, TOTAL_MS, TEST_DURATION_MS,
                    TOTAL_BYTES_SENT, TOTAL_BYTES_RECEIVED,
                    TESTS_PER_SECOND, BYTES_PER_SECOND,
                    PERCENTILE_DURATION + reportPercentileDuration };
        }
        return new String[] { NUMBER_OF_TESTS, NUMBER_OF_ERRORS,
                NUMBER_OF_THREADS, MINIMUM_MS, MAXIMUM_MS, AVERAGE_MS,
                TOTAL_MS, TEST_DURATION_MS, TOTAL_BYTES_SENT,
                TOTAL_BYTES_RECEIVED, TESTS_PER_SECOND, BYTES_PER_SECOND };
    }

    /**
     * @param _field
     * @return
     * @throws UnknownResultFieldException
     */
    public String getFieldValue(String _field)
            throws UnknownResultFieldException {
        // TODO change data structure to hash, to simplify this code?
        if (_field.equals(NUMBER_OF_TESTS))
            return "" + getNumberOfTests();

        if (_field.equals(NUMBER_OF_ERRORS))
            return "" + getNumberOfErrors();

        if (_field.equals(NUMBER_OF_THREADS))
            return "" + getNumberOfThreads();

        if (_field.equals(MINIMUM_MS))
            return "" + getMinMillis();

        if (_field.equals(MAXIMUM_MS))
            return "" + getMaxMillis();

        if (_field.equals(AVERAGE_MS))
            return "" + getAvgMillis();

        if (_field.equals(TOTAL_MS))
            return "" + getTotalMillis();

        if (_field.equals(TEST_DURATION_MS))
            return "" + getDurationMillis();

        if (_field.equals(TOTAL_BYTES_SENT))
            return "" + getBytesSent();

        if (_field.equals(TOTAL_BYTES_RECEIVED))
            return "" + getBytesReceived();

        if (_field.equals(TESTS_PER_SECOND))
            return "" + getTestsPerSecond();

        if (_field.equals(BYTES_PER_SECOND))
            return "" + getBytesPerSecond();

        if (_field.startsWith(PERCENTILE_DURATION)) {
            return "" + getPercentileDuration();
        }

        throw new UnknownResultFieldException("unknown result field: "
                + _field);
    }

    /**
     * @return
     */
    private int getNumberOfThreads() {
        return numberOfThreads;
    }

    public long getAvgMillis() {
        return avgMillis;
    }

    public long getBytesReceived() {
        return bytesReceived;
    }

    public long getBytesSent() {
        return bytesSent;
    }

    public long getDurationMillis() {
        return durationMillis;
    }

    public long getMaxMillis() {
        return maxMillis;
    }

    public long getMinMillis() {
        return minMillis;
    }

    public long getNumberOfTests() {
        return numberOfTests;
    }

    public double getTestsPerSecond() {
        return testsPerSecond;
    }

    public long getTotalMillis() {
        return totalMillis;
    }

    public double getBytesPerSecond() {
        return bytesPerSecond;
    }

    /**
     * @return
     */
    public Sampler[] getSamplers() {
        return samplers;
    }

    /**
     * @return
     */
    public long getNumberOfErrors() {
        return numberOfErrors;
    }

}