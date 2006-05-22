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

import java.util.Random;

class AbstractTestIterator implements TestIterator {

    protected TestList tests;

    protected int cursor;

    AbstractTestIterator(TestList _tests) {
        tests = _tests;
        cursor = 0;
    }

    public boolean hasNext() {
        return cursor < tests.size();
    }

    public TestInterface next() {
        return tests.get(cursor++);
    }

    /* (non-Javadoc)
     * @see com.marklogic.performance.TestIterator#shuffle()
     */
    public void shuffle(Random random) {
        tests.shuffle(random);
    }

    /* (non-Javadoc)
     * @see com.marklogic.performance.TestIterator#reset()
     */
    public void reset() {
        cursor = 0;
    }
    
}