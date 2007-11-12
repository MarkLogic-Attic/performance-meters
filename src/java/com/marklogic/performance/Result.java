/*
 * Copyright (c)2005-2007 Mark Logic Corporation
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

/**
 * @author Ron Avnur, ron.avnur@marklogic.com
 * @author Michael Blakeley, michael.blakeley@marklogic.com
 * 
 */
public class Result implements ResultInterface {

    /**
     * 
     */
    private static final String BYTES_RECEIVED = "bytes-received";

    /**
     * 
     */
    private static final String BYTES_SENT = "bytes-sent";

    /**
     * 
     */
    private static final String END_MILLIS = "end-millis";

    /**
     * 
     */
    private static final String START_MILLIS = "start-millis";

    /**
     * 
     */
    private static final String DURATION_MILLIS = "duration-millis";

    /**
     * 
     */
    private static final String ERROR = "got-error";

    /**
     * 
     */
    private static final String QUERY_RESULT = "result-text";

    /**
     * 
     */
    private static final String COMMENT = "comment-expected-result";

    /**
     * 
     */
    private static final String TEST_NAME = "name";

    private String testName, comment, queryResult;

    private long startNanos, endNanos, bytesSent, bytesReceived;

    private boolean error;

    public Result(String _testName, String _comment) {
        testName = _testName;
        comment = _comment;
        error = false;
        bytesSent = 0;
        bytesReceived = 0;
    }

    public static String[] getFieldNames(boolean withDetails) {
        if (withDetails) {
            return new String[] { TEST_NAME, COMMENT, QUERY_RESULT,
                    ERROR, START_MILLIS, END_MILLIS, BYTES_SENT,
                    BYTES_RECEIVED, DURATION_MILLIS };
        }
        return new String[] { TEST_NAME, COMMENT, QUERY_RESULT, ERROR };
    }

    public static String[] getFieldNames() {
        return getFieldNames(false);
    }
    
    /* (non-Javadoc)
     * @see com.marklogic.performance.ResultInterface#getFieldValue(java.lang.String)
     */
    public String getFieldValue(String _field)
            throws UnknownResultFieldException {
        // TODO change data structure to hash, to simplify this code?
        if (_field.equals(TEST_NAME))
            return getTestName();

        if (_field.equals(COMMENT))
            return getComment();

        if (_field.equals(QUERY_RESULT))
            return getQueryResult();

        if (_field.equals(ERROR))
            return "" + isError();

        if (_field.equals(START_MILLIS))
            return "" + getStartMillis();

        if (_field.equals(END_MILLIS))
            return "" + getEndMillis();

        if (_field.equals(DURATION_MILLIS))
            return "" + getDurationMillis();
        
        if (_field.equals(BYTES_SENT))
            return "" + getBytesSent();

        if (_field.equals(BYTES_RECEIVED))
            return "" + getBytesReceived();

        throw new UnknownResultFieldException("unknown result field: "
                + _field);
    }

    public void setBegin(long _nanos) {
        startNanos = _nanos;
    }

    public void setStart(long _nanos) {
        startNanos = _nanos;
    }

    public void setStop(long _nanos) {
        endNanos = _nanos;
    }

    public void setEnd(long _nanos) {
        endNanos = _nanos;
    }

    public void setError(boolean err) {
        error = err;
    }

    public void setQueryResult(String res) {
        queryResult = res;
        bytesReceived += res.length();
    }

    public void setQueryResult(String res, boolean e) {
        queryResult = res;
        error = e;
        bytesReceived += res.length();
    }

    void print() {
        System.out.println(queryResult);
    }

    public String getTestName() {
        return testName;
    }

    public String getComment() {
        return comment;
    }

    public String getQueryResult() {
        return queryResult;
    }

    public boolean isError() {
        return error;
    }

    public long getStartNanos() {
        return startNanos;
    }

    public long getEndNanos() {
        return endNanos;
    }

    public double getStartMillis() {
        return (double) startNanos / Configuration.NANOS_PER_MILLI;
    }

    public double getEndMillis() {
        return (double) endNanos / Configuration.NANOS_PER_MILLI;
    }

    /**
     * @return
     */
    public long getDurationNanos() {
        return endNanos - startNanos;
    }

    /**
     * @return
     */
    public double getDurationMillis() {
        return (double) getDurationNanos()
                / Configuration.NANOS_PER_MILLI;
    }

    /**
     * @return
     */
    public long getBytesSent() {
        return bytesSent;
    }

    /**
     * @return
     */
    public long getBytesReceived() {
        return bytesReceived;
    }

    /**
     * @param bytes
     */
    public void incrementBytesReceived(long bytes) {
        bytesReceived += bytes;
    }

    /**
     * @param query
     */
    public void incrementBytesSent(long bytes) {
        bytesSent += bytes;
    }

    /**
     * 
     */
    public void setStart() {
        setStart(System.nanoTime());
    }

    /**
     * 
     */
    public void setEnd() {
        setEnd(System.nanoTime());
    }

}