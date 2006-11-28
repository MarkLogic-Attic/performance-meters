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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Ron Avnur, ron.avnur@marklogic.com
 * @author Michael Blakeley, michael.blakeley@marklogic.com
 * 
 */
public class PerformanceMeters {

    static final boolean debug = false;

    private static final String NAME = PerformanceMeters.class.getName();

    private static final String VERSION = "2006-11-27.1";

    private Configuration config;

    List<Thread> threads;

    List<Sampler> samplers;

    TestList tests;

    long startTime, endTime;

    static Reporter reporter = null;

    public static void main(String args[]) throws Exception {
        // start with getting the config parameters
        // if the user supplied any args, assume they are properties files
        Configuration config = new Configuration(args, true);

        showProgress(NAME + " starting, version " + VERSION);

        if (debug) {
            showProgress(config.configString());
        }

        // use reflection to create the reporter, for output
        Class reporterClass = Class
                .forName(config.getReporterClassName());
        // System.err.println("reporter class: " + reporterClass.getName());
        Constructor reporterConstructor = reporterClass
                .getConstructor(new Class[0]);
        reporter = (Reporter) reporterConstructor
                .newInstance(new Object[0]);

        try {
            PerformanceMeters pm = new PerformanceMeters(config);
            pm.initializeTests();
            if (debug)
                pm.printTests();

            pm.run();

            pm.reportResults();
        } catch (Exception e) {
            showProgress("configuration: " + config.configString());
            e.printStackTrace();
        }
    }

    PerformanceMeters(Configuration _config) {
        config = _config;
        threads = new ArrayList<Thread>();
        samplers = new ArrayList<Sampler>();
    }

    void initializeTests() throws Exception {
        // this needs to allow for other test types too!
        // use reflection to create a TestList subclass constructor
        Class testListClass = Class
                .forName(config.getTestListClassName());
        Constructor testListConstructor = testListClass
                .getConstructor(new Class[0]);
        tests = (TestList) testListConstructor.newInstance(new Object[0]);
        tests.initialize(config);

        if (debug) {
            showProgress("number of tests: " + tests.size());
        }
    }

    void run() throws Exception {
        // launch threads
        int numThreads = config.getNumThreads();
        showProgress("creating " + numThreads + " threads...");
        TestIterator ti = null;
        int offsetPerThread = -1;
        if (config.isShared()) {
            ti = new SharedTestIterator(tests);
        } else {
            offsetPerThread = tests.size() / numThreads;
        }

        Sampler sampler;
        for (int i = 0; i < numThreads; i++) {
            if (offsetPerThread != -1) {
                // need a new ti every time
                ti = new OffsetTestIterator(tests, i * offsetPerThread);
            }

            if (config.isHTTP()) {
                sampler = new HTTPSampler(ti, config);
            } else if (config.isURI()) {
                sampler = new URISampler(ti, config);
            } else if (config.isXDBC()) {
                sampler = new XDBCSampler(ti, config);
            } else {
                // default to XCC
                sampler = new XCCSampler(ti, config);
            }
            sampler.setIndex(i);

            samplers.add(sampler);
            Thread th = new Thread(sampler, "sampler #" + i);
            threads.add(th);
        }

        // with really large numbers of threads, creation time is significant
        // TODO can we use java.util.concurrent? with a BlockingQueue?
        showProgress("starting...");
        startTime = System.nanoTime();
        for (int i = 0; i < numThreads; i++) {
            threads.get(i).start();
        }

        // wait for all to finish
        for (int i = 0; i < threads.size(); i++) {
            try {
                (threads.get(i)).join();
            } catch (InterruptedException e) {
                /* should not happen */
                continue;
            }
        }

        endTime = System.nanoTime();
    }

    /**
     * @param message
     */
    private static void showProgress(String message) {
        System.err.println(new Date() + ": " + message);
    }

    void reportResults() throws IOException, ReporterException {
        showProgress("Reporting results...");

        String outputPath = config.getOutputPath();
        if (outputPath == null || outputPath.equals("")) {
            // generate a unique path, based on time
            outputPath = NAME + "-" + System.currentTimeMillis()
                    + reporter.getPreferredFileExtension();
        }
        File outputFile = new File(outputPath);
        if (outputFile.exists() && outputFile.isDirectory()) {
            // generate a unique file, in the specified dir
            outputFile = new File(outputFile, NAME + "-"
                    + System.currentTimeMillis()
                    + reporter.getPreferredFileExtension());
        }
        showProgress("Writing results to "
                + outputFile.getCanonicalPath());
        FileWriter resultDocument = new FileWriter(outputFile);
        Sampler[] samplerArray = samplers.toArray(new Sampler[0]);
        SummaryResults summaryResults = new SummaryResults(config,
                startTime, endTime, samplerArray);

        reporter.setSummaryResults(summaryResults);
        reporter.report(resultDocument, config.isReportTime());
        resultDocument.flush();
        resultDocument.close();

        // report some generic information
        if (config.isReportTime()) {
            System.out.println("Completed "
                    + summaryResults.getNumberOfTests() + " tests in "
                    + summaryResults.getDurationMillis()
                    + " milliseconds, with "
                    + summaryResults.getNumberOfErrors() + " errors.");

            // report min, max, avg response times
            System.out.println("Response times (min/max/avg): "
                    + summaryResults.getMinMillis() + "/"
                    + summaryResults.getMaxMillis() + "/"
                    + summaryResults.getAvgMillis() + " ms");
            if (config.isReportStandardDeviation()) {
                // report standard deviation
                System.out.println("Standard deviation: "
                        + summaryResults.getStandardDeviationMillis());
            }
            // report multiple percentiles
            if (config.hasReportPercentileDuration()) {
                // report N% response times
                int[] percentiles = config.getReportPercentileDuration();
                int reportPercentile;
                for (int i = 0; i < percentiles.length; i++) {
                    reportPercentile = percentiles[i];
                    System.out
                            .println("Response time ("
                                    + reportPercentile
                                    + "th percentile): "
                                    + summaryResults
                                            .getPercentileDurationMillis(reportPercentile));
                }
            }

            // report bytes sent, received
            System.out.println("Bytes (sent/received): "
                    + summaryResults.getBytesSent() + "/"
                    + summaryResults.getBytesReceived() + " B");

            // report throughput nicely
            // be sure to use toString, to work around this issue:
            // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4863883
            double tput = summaryResults.getTestsPerSecond();
            int scale = 2;
            System.out.println("Tests per second: "
                    + new BigDecimal(Double.toString(tput)).setScale(
                            scale, BigDecimal.ROUND_HALF_EVEN));
            tput = summaryResults.getBytesPerSecond();
            System.out.println("Average throughput: "
                    + new BigDecimal(Double.toString(tput)).setScale(
                            scale, BigDecimal.ROUND_HALF_EVEN) + " B/s");
        }

    }

    void printTests() throws IOException {
        TestIterator ti = new SimpleTestIterator(tests);
        int i = 0;
        while (ti.hasNext()) {
            TestInterface t = ti.next();
            System.out.println(t.getQuery());
            System.out.println(i++);
        }
    }

}
