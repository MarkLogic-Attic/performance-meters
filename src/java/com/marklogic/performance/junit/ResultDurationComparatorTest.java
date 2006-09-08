/*
 * Copyright (c)2006 Mark Logic Corporation
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
package com.marklogic.performance.junit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import junit.framework.TestCase;

import com.marklogic.performance.Result;
import com.marklogic.performance.ResultDurationComparator;

/**
 * @author Michael Blakeley, michael.blakeley@marklogic.com
 * 
 */
public class ResultDurationComparatorTest extends TestCase {

    public void testCompareOneLtTwo() {
        ResultDurationComparator comp = new ResultDurationComparator();
        Result a = new Result("a", null);
        a.setEnd(1);
        Result b = new Result("b", null);
        b.setEnd(2);
        // System.err.println("a=" + a.getDurationNanos());
        // System.err.println("b=" + b.getDurationNanos());
        assertTrue(comp.compare(a, b) < 0);
    }

    public void testCompareTwoGtOne() {
        ResultDurationComparator comp = new ResultDurationComparator();
        Result a = new Result("a", null);
        a.setEnd(2);
        Result b = new Result("b", null);
        b.setEnd(1);
        assertTrue(comp.compare(a, b) > 0);
    }

    public void testCompareOneEqOne() {
        ResultDurationComparator comp = new ResultDurationComparator();
        Result a = new Result("a", null);
        a.setEnd(1);
        Result b = new Result("b", null);
        b.setEnd(1);
        assertTrue(comp.compare(a, b) == 0);
    }

    public void testCompareBigLtBigger() {
        ResultDurationComparator comp = new ResultDurationComparator();
        Result a = new Result("a", null);
        a.setEnd(830774000L);
        Result b = new Result("b", null);
        b.setEnd(2159281000L);
//        System.err.println("a=" + a.getDurationNanos());
//        System.err.println("b=" + b.getDurationNanos());
        assertTrue(comp.compare(a, b) < 0);
    }

    public void testCompareBiggerGtBig() {
        ResultDurationComparator comp = new ResultDurationComparator();
        Result a = new Result("a", null);
        a.setEnd(2159281000L);
        Result b = new Result("b", null);
        b.setEnd(830774000L);
//        System.err.println("a=" + a.getDurationNanos());
//        System.err.println("b=" + b.getDurationNanos());
        assertTrue(comp.compare(a, b) > 0);
    }
    
    public void testSorting() {
        long[] nanos = new long[] {4299025000L, 630758000L, 830774000L, 2159281000L};
        ArrayList<Result> results = new ArrayList<Result>();
        Result r;
        for (int i = 0; i < nanos.length; i++) {
            r = new Result("" + i, null);
            r.setEnd(nanos[i]);
//            System.err.println("" + i + "=" + r.getDurationNanos());
            results.add(r);
        }
        
        Comparator<Result> c = new ResultDurationComparator();
        Collections.sort(results, c);
        
//        Iterator<Result> iter = results.iterator();
//        while (iter.hasNext()) {
//            System.err.println("sorted=" + iter.next().getDurationNanos());            
//        }
        
        assertEquals(results.get(0).getDurationNanos(), 630758000L);
        assertEquals(results.get(3).getDurationNanos(), 4299025000L);
    }

}