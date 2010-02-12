/*
 * Copyright (c)2005-2010 Mark Logic Corporation
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
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import com.marklogic.performance.sampler.Sampler;

/**
 * @author Michael Blakeley, michael.blakeley@marklogic.com
 * 
 */
public class SummaryResults implements ResultInterface {

    private static final String STANDARD_DEVIATION = "standard-deviation";

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

    long minNanos = Long.MAX_VALUE;

    long maxNanos = Long.MIN_VALUE;

    long totalNanos = 0;

    long durationNanos = 0;

    long bytesSent = 0;

    long bytesReceived = 0;

    private Sampler[] samplers;

    private int numberOfThreads = 1;

    private String[] fields;

    private int[] reportPercentilesArray;

    private boolean reportStandardDeviation;

    private List<Result> sortedResults;

    public SummaryResults(Configuration _config, long startNanos,
            long endNanos, Sampler[] _samplers) {
        samplers = _samplers;
        durationNanos = endNanos - startNanos;

        // gather min, max, avg response times
        // gather bytes sent, received
        Sampler sampler;
        long min, max;
        for (int i = 0; i < samplers.length; i++) {
            sampler = samplers[i];
            numberOfTests += sampler.getResultsCount();
            numberOfErrors += sampler.getErrorCount();
            min = sampler.getMinDurationNanos();
            if (min < minNanos) {
                minNanos = min;
            }
            max = sampler.getMaxDurationNanos();
            if (max > maxNanos) {
                maxNanos = max;
            }
            totalNanos += sampler.getTotalNanos();
            bytesSent += sampler.getBytesSent();
            bytesReceived += sampler.getBytesReceived();
        }

        // gather configuration information
        numberOfThreads = _config.getNumThreads();

        // reporting flags
        reportStandardDeviation = _config.isReportStandardDeviation();
        if (_config.hasReportPercentileDuration()) {
            reportPercentilesArray = _config
                    .getReportPercentileDuration();
        } else {
            reportPercentilesArray = null;
        }
    }

    private void loadSortedResults() {
        sortedResults = new ArrayList<Result>();
        for (int i = 0; i < samplers.length; i++) {
            // results are a List of Test objects
            sortedResults.addAll(samplers[i].getResults());
        }

        Comparator<Result> c = new ResultDurationComparator();
        Collections.sort(sortedResults, c);
    }

    /**
     * implement percentiles
     * 
     * @param percentile
     * 
     * @return
     */
    public long getPercentileDurationNanos(int percentile) {
        if (percentile < 1)
            return 0;

        if (samplers.length < 1)
            return 0;

        if (sortedResults == null) {
            loadSortedResults();
            // DEBUG
            // Iterator<Result> iter = sortedResults.iterator();
            // while (iter.hasNext()) {
            // System.err.println("DEBUG: sorted = "
            // + iter.next().getDurationNanos());
            // }
        }

        double size = sortedResults.size();

        int pidx = (int) (percentile * size * .01);
        if (pidx > size - 1) {
            pidx = (int) (size - 1);
        }
        // System.err.println("DEBUG: percentile=" + percentile + ", size="
        // + size + ", pidx=" + pidx);
        return sortedResults.get(pidx).getDurationNanos();
    }

    public double getPercentileDurationMillis(int percentile) {
        return (double) getPercentileDurationNanos(percentile)
                / Configuration.NANOS_PER_MILLI;
    }

    /**
     * @see http://en.wikipedia.org/wiki/Standard_deviation
     * @return
     */
    public double getStandardDeviationNanos() {
        if (samplers.length < 1) {
            return 0;
        }

        if (sortedResults == null) {
            loadSortedResults();
        }

        // iterate through the results from all the results
        Iterator<Result> iter = sortedResults.iterator();
        double sumOfSquares = 0;
        double avgNanos = getAvgNanos();
        while (iter.hasNext()) {
            sumOfSquares += Math.pow(iter.next().getDurationNanos()
                    - avgNanos, 2);
        }
        return Math.sqrt(sumOfSquares / getNumberOfTests());
    }

    public double getStandardDeviationMillis() {
        return getStandardDeviationNanos()
                / Configuration.NANOS_PER_MILLI;
    }

    // not static, because we might report different summary info in a run
    public String[] getFieldNames() {
        if (fields == null) {
            List<String> fieldsList = new Vector<String>();
            fieldsList.add(NUMBER_OF_TESTS);
            fieldsList.add(NUMBER_OF_ERRORS);
            fieldsList.add(NUMBER_OF_THREADS);
            fieldsList.add(MINIMUM_MS);
            fieldsList.add(MAXIMUM_MS);
            fieldsList.add(AVERAGE_MS);
            fieldsList.add(TOTAL_MS);
            fieldsList.add(TEST_DURATION_MS);
            fieldsList.add(TOTAL_BYTES_SENT);
            fieldsList.add(TOTAL_BYTES_RECEIVED);
            fieldsList.add(TESTS_PER_SECOND);
            fieldsList.add(BYTES_PER_SECOND);
            if (reportPercentilesArray != null) {
                for (int i = 0; i < reportPercentilesArray.length; i++) {
                    fieldsList.add(PERCENTILE_DURATION
                            + reportPercentilesArray[i]);
                }
            }
            if (reportStandardDeviation) {
                fieldsList.add(STANDARD_DEVIATION);
            }
            fields = fieldsList.toArray(new String[0]);
        }
        return fields;
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
            int percentile = java.lang.Integer.parseInt(_field
                    .replaceFirst(PERCENTILE_DURATION + "(\\d+)$", "$1"));
            return "" + getPercentileDurationMillis(percentile);
        }

        if (_field.startsWith(STANDARD_DEVIATION)) {
            return "" + getStandardDeviationMillis();
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

    public double getAvgNanos() {
        return (double) totalNanos / numberOfTests;
    }

    public double getAvgMillis() {
        return getAvgNanos() / Configuration.NANOS_PER_MILLI;
    }

    public long getBytesReceived() {
        return bytesReceived;
    }

    public long getBytesSent() {
        return bytesSent;
    }

    public long getDurationNanos() {
        return durationNanos;
    }

    public double getDurationMillis() {
        return (double) durationNanos / Configuration.NANOS_PER_MILLI;
    }

    public long getMaxNanos() {
        return maxNanos;
    }

    public double getMaxMillis() {
        return (double) maxNanos / Configuration.NANOS_PER_MILLI;
    }

    public long getMinNanos() {
        return minNanos;
    }

    public double getMinMillis() {
        return (double) minNanos / Configuration.NANOS_PER_MILLI;
    }

    public long getNumberOfTests() {
        return numberOfTests;
    }

    public double getTestsPerSecond() {
        return (double) numberOfTests * Configuration.NANOS_PER_SECOND
                / durationNanos;
    }

    public long getTotalNanos() {
        return totalNanos;
    }

    public double getTotalMillis() {
        return (double) getTotalNanos() / Configuration.NANOS_PER_MILLI;
    }

    public double getBytesPerSecond() {
        double bytes = (double) bytesSent + (double) bytesReceived;
        double secs = (double) durationNanos
                / (double) Configuration.NANOS_PER_SECOND;
        return bytes / secs;
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