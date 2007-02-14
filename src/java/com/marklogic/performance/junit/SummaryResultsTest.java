/**
 * Copyright (c) 2006 Mark Logic Corporation. All rights reserved.
 */
package com.marklogic.performance.junit;

import java.io.IOException;

import junit.framework.TestCase;

import com.marklogic.performance.Configuration;
import com.marklogic.performance.Result;
import com.marklogic.performance.SummaryResults;

/**
 * @author Michael Blakeley, michael.blakeley@marklogic.com
 * 
 */
public class SummaryResultsTest extends TestCase {

    public void testStandardDeviation2() throws IOException {
        Configuration config = new Configuration(new String[0], false);
        TestSampler[] samplers = new TestSampler[1];
        samplers[0] = new TestSampler(null, config);
        Result r1 = new Result("test1", null);
        r1.setStart(0);
        r1.setEnd(4);
        samplers[0].add(r1);
        Result r2 = new Result("test2", null);
        r2.setStart(0);
        r2.setEnd(8);
        samplers[0].add(r2);
        SummaryResults sr = new SummaryResults(config, 1000, 2000,
                samplers);
        double stdev = sr.getStandardDeviationNanos();
        double mean = sr.getAvgNanos();
        System.err.println("mean=" + mean + ", stdev=" + stdev);
        assertEquals(6.0, mean);
        assertEquals(2.0, stdev);
    }

    public void testStandardDeviation() throws IOException {
        double expected = 1.5811;
        long[] population = new long[] { 5L, 6L, 8L, 9L };
        Configuration config = new Configuration(new String[0], false);
        TestSampler[] samplers = new TestSampler[1];
        samplers[0] = new TestSampler(null, config);

        for (int i = 0; i < population.length; i++) {
            Result r = new Result("test" + i, null);
            r.setStart(0);
            r.setEnd(population[i]);
            samplers[0].add(r);
        }

        SummaryResults sr = new SummaryResults(config, 1000, 2000,
                samplers);
        double stdev = (double) (Math.round(10000 * sr
                .getStandardDeviationNanos())) / 10000;
        double mean = sr.getAvgNanos();
        System.err.println("mean=" + mean + ", stdev=" + stdev);
        assertEquals(7.0, mean);
        assertEquals(expected, stdev);
    }

}
