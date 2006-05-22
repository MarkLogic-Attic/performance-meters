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

class OffsetTestIterator extends AbstractTestIterator {

    // this iterator wraps around, so it needs some extra glue
    private int totalTests;

    private int testsCount;

    private int start = 0;

    OffsetTestIterator(TestList _tests, int start) {
        super(_tests);
        totalTests = tests.size();
        this.start = start;
        cursor = start;
        testsCount = 0;
    }

    public boolean hasNext() {
        return testsCount < totalTests;
    }

    public TestInterface next() {
        if (cursor == totalTests) {
            cursor = 0;
            testsCount++;
            return (TestInterface) (tests.get(cursor));
        }
        testsCount++;
        return (TestInterface) (tests.get(cursor++));
    }
    
    public void reset() {
        cursor = start;
        testsCount = 0;
    }
    
}