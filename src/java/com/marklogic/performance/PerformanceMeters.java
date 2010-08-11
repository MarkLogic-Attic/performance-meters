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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.marklogic.performance.reporter.Reporter;
import com.marklogic.performance.reporter.ReporterException;
import com.marklogic.performance.sampler.Sampler;
import com.marklogic.performance.sampler.XCCSampler;
import com.marklogic.xcc.Version;

/**
 * @author Ron Avnur, ron.avnur@marklogic.com
 * @author Michael Blakeley, michael.blakeley@marklogic.com
 * @author Wayne Feick, wayne.feick@marklogic.com
 *
 */
public class PerformanceMeters {

    static final boolean debug = false;

    private static final String NAME = PerformanceMeters.class.getName();

    private static final String VERSION = "2010-08-11.1";

    private Configuration config;

    List<Sampler> samplers;

    TestList tests;

    long startTime, endTime;

    static Reporter reporter = null;

    public static void main(String args[]) throws Exception {
        // start with getting the configuration parameters
        // if the user supplied any arguments,
        // assume they are properties files
        Configuration config = new Configuration(args, true);

        showProgress(NAME + " starting, version " + VERSION);

        if (debug) {
            showProgress(config.configString());
        }

        // use reflection to create the reporter, for output
        Class<? extends Reporter> reporterClass = Class.forName(
                config.getReporterClassName()).asSubclass(Reporter.class);
        Constructor<? extends Reporter> reporterConstructor = reporterClass
                .getConstructor(new Class[0]);
        reporter = reporterConstructor.newInstance(new Object[0]);

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
        samplers = new ArrayList<Sampler>();
    }

    void initializeTests() throws Exception {
        // this needs to allow for other test types too!
        // use reflection to create a TestList subclass constructor
        Class<? extends TestList> testListClass = Class.forName(
                config.getTestListClassName()).asSubclass(TestList.class);
        Constructor<? extends TestList> testListConstructor = testListClass
                .getConstructor(new Class[0]);
        tests = testListConstructor.newInstance(new Object[0]);
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

        // use reflection to create a sampler constructor object
        Class<? extends Sampler> samplerClass = Class.forName(
                config.getSamplerClassName()).asSubclass(Sampler.class);
        // constructor with test iterator and configuration arguments
        Constructor<? extends Sampler> samplerConstructor = samplerClass
                .getConstructor(new Class[] { TestIterator.class,
                        Configuration.class });
        if (XCCSampler.class == samplerClass) {
            showProgress("XCC " + Version.getVersionString());
        }

        Sampler sampler;
        for (int i = 0; i < numThreads; i++) {
            if (offsetPerThread != -1) {
                // new test iterator for each thread
                ti = new OffsetTestIterator(tests, i * offsetPerThread);
            }

            sampler = samplerConstructor.newInstance(ti, config);
            sampler.setIndex(i);
            samplers.add(sampler);
        }

        // with really large numbers of threads, creation time is significant
        showProgress("starting...");
        startTime = System.nanoTime();
        for (int i = 0; i < numThreads; i++) {
            samplers.get(i).start();
        }

        // wait for all to finish
        for (int i = 0; i < samplers.size(); i++) {
            try {
                (samplers.get(i)).join();
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
            System.out.println(String.format(
                    "Completed %d tests in %.0f ms, with %d errors.",
                    summaryResults.getNumberOfTests(), summaryResults
                            .getDurationMillis(), summaryResults
                            .getNumberOfErrors()));

            // report min, max, avg response times
            System.out.println(String.format(
                    "Response times (min/max/avg): %.0f/%.0f/%.0f ms",
                    summaryResults.getMinMillis(), summaryResults
                            .getMaxMillis(), summaryResults
                            .getAvgMillis()));
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
            System.out.println(String.format("Tests per second: %.0f",
                    summaryResults.getTestsPerSecond()));
            System.out.println(String.format(
                    "Average throughput: %.0f B/s", summaryResults
                            .getBytesPerSecond()));
        }

    }

    void printTests() throws Exception {
        TestIterator ti = new SimpleTestIterator(tests);
        int i = 0;
        while (ti.hasNext()) {
            TestInterface t = ti.next();
            System.out.println(t.getQuery());
            System.out.println(i++);
        }
    }

}
