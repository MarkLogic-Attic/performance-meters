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

import java.util.Comparator;

/**
 * @author Michael Blakeley, michael.blakeley@marklogic.com
 * 
 */
public class ResultDurationComparator implements Comparator<Result> {

    /**
     * 
     */
    public ResultDurationComparator() {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    public int compare(Result a, Result b) {
        long aValue = a.getDurationNanos();
        long bValue = b.getDurationNanos();
        return aValue > bValue ? 1 : aValue < bValue ? -1 : 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Comparator#equals(java.lang.Object, java.lang.Object)
     */
    public boolean equals(Result a, Result b) {
        return a.getDurationNanos() == b.getDurationNanos();
    }

}
