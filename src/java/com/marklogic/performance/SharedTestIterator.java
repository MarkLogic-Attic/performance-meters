/*
 * Copyright (c)2005-2009 Mark Logic Corporation
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

class SharedTestIterator extends AbstractTestIterator {

    /**
     * @param _tests
     */
    SharedTestIterator(TestList _tests) {
        super(_tests);
    }

    public boolean hasNext() {
        // TODO bottleneck for lots of threads?
        synchronized (this) {
            return super.hasNext();
        }
    }

    public TestInterface next() {
        // TODO bottleneck for lots of threads?
        synchronized (this) {
            if (cursor >= super.tests.size()) {
                super.reset();
            }
            return super.next();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.marklogic.performance.TestIterator#shuffle()
     */
    public void shuffle(Random random) {
        // TODO bottleneck for lots of threads?
        synchronized (this) {
            super.shuffle(random);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.marklogic.performance.TestIterator#reset()
     */
    public void reset() {
        // TODO bottleneck for lots of threads?
        synchronized (this) {
            super.reset();
        }
    }
}