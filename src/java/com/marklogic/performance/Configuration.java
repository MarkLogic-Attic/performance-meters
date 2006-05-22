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

import java.lang.Boolean;
import java.util.Properties;
import java.util.Random;

import com.marklogic.xdmp.XDMPConnection;

public class Configuration {

    public static final boolean DEFAULT_RECORDRESULTS = false;

    private static final boolean DEFAULT_SHARED = false;

    public static final String DEFAULT_HOST = "localhost";

    public static final int DEFAULT_PORT = 8003;

    public static final String DEFAULT_USER = "admin";

    public static final String DEFAULT_PASSWORD = "admin";

    public static final long DEFAULT_RANDOMSEED = 0;

    private static final long DEFAULT_TESTTIME = Long.MAX_VALUE;

    public static final boolean DEFAULT_CHECKRESULTS = false;

    public static final boolean DEFAULT_REPORTTIME = true;

    private static final String TESTLISTCLASS_KEY = "testListClass";

    private static final String DEFAULT_TESTLISTCLASS = XMLFileTestList.class.getName();

    private static final String ELEMENTQNAME_KEY = "elementQName";

    private String[] host;

    private int port;

    private String user;

    private String password;

    private String inputPath;

    private String outputPath;

    private int numThreads;

    private boolean reportTime;

    // output results even if reporting time
    private boolean recordResults;

    private String testType;

    // shared
    // if true, then threads share the input file tests
    // if false, then each thread will process all tests, starting an different
    // offsets
    private boolean shared;

    private String reporterClassName;

    private long randomSeed = DEFAULT_RANDOMSEED;

    private boolean isRandomTest = false;

    private boolean isTimedTest = false;

    private int readSize;

    private long testTime;

    private int reportPercentileDuration;

    private boolean checkResults = DEFAULT_CHECKRESULTS;

    private Random random;

    private int hostIndex = 0;

    private Properties props;

    private String testListClassName;

    private String elementQName;

    Configuration() {
        // set up the initial object using system properties
        // if the user wants to supply more properties,
        // the load() method is public.
        props = new Properties();
        load(System.getProperties());
    }

    /**
     * @param props
     */
    public void load(Properties _props) {
        // fill in the config from the supplied properties object:
        // allow it to override any existing properties 
        props.putAll(_props);
        
        // we support multiple hosts, but they must all be on the same port:
        // this would be easier with multiple connection strings...
        String hostString = props.getProperty("host", DEFAULT_HOST);
        host = hostString.split("\\s+");

        // TODO wouldn't a connection-string be simpler? support both?
        port = Integer.parseInt(props.getProperty("port", "" + DEFAULT_PORT));

        user = props.getProperty("user", DEFAULT_USER);

        password = props.getProperty("password", DEFAULT_PASSWORD);

        readSize = Integer.parseInt(props.getProperty("readSize", "0"));

        inputPath = props.getProperty("inputPath");

        // default to cwd for output files
        outputPath = props.getProperty("outputPath", "");

        numThreads = Integer.parseInt(props.getProperty("numThreads", "1"));

        reportTime = Boolean.valueOf(props.getProperty("reportTime", "" + DEFAULT_REPORTTIME))
                .booleanValue();

        reportPercentileDuration = Integer.parseInt(props.getProperty(
                "reportPercentileDuration", "0"));

        testTime = Long.parseLong(props.getProperty("testTime", ""
                + DEFAULT_TESTTIME));

        isTimedTest = (testTime != DEFAULT_TESTTIME);

        isRandomTest = Boolean.valueOf(
                props.getProperty("isRandomTest", "false")).booleanValue();

        // for backward compatibility
        recordResults = Boolean.valueOf(
                props.getProperty("recordResults", props.getProperty(
                        "forceResults", "" + DEFAULT_RECORDRESULTS)))
                .booleanValue();

        checkResults = Boolean.valueOf(
                props.getProperty("checkResults", "" + DEFAULT_CHECKRESULTS))
                .booleanValue();

        if (checkResults && !recordResults) {
            // this doesn't make any sense, so don't honor it
            System.err
                    .println("WARNING: checkResults=true, recordResults=false!\n"
                            + "WARNING: turning off checkResults!");
            checkResults = false;
        }

        shared = Boolean.valueOf(
                props.getProperty("shared", "" + DEFAULT_SHARED))
                .booleanValue();

        testType = props.getProperty("testType", "XDBC");
        if (isXDBC()
                && numThreads > XDMPConnection.getMaxOpenDocInsertStreams()) {
            XDMPConnection.setMaxOpenDocInsertStreams(numThreads);
        }
        if (isXDBC() && numThreads > XDMPConnection.getMaxOpenResultSequences()) {
            XDMPConnection.setMaxOpenResultSequences(numThreads);
        }

        reporterClassName = props.getProperty("reporter", "XMLReporter");
        // System.err.println("reporterClassName = " + reporterClassName);

        if (reporterClassName.indexOf('.') < 1) {
            // prepend this class's package name
            reporterClassName = Configuration.class.getPackage().getName()
                    + "." + reporterClassName;
        }
        
        testListClassName = props.getProperty(TESTLISTCLASS_KEY, DEFAULT_TESTLISTCLASS);
        // System.err.println("testListClassName = " + testListClassName);
        if (testListClassName.indexOf('.') < 1) {
            // prepend this class's package name
            testListClassName = Configuration.class.getPackage().getName()
                    + "." + testListClassName;
        }

        randomSeed = Long.parseLong(props.getProperty("randomSeed", ""
                + DEFAULT_RANDOMSEED));
        if (randomSeed != DEFAULT_RANDOMSEED && !(isTimedTest && isRandomTest)) {
            // this doesn't make any sense, so don't honor it
            System.err
                    .println("WARNING: tried to set randomSeed with timedTest="
                            + isTimedTest + ", randomTest=" + isRandomTest
                            + "!\n" + "WARNING: turning off randomSeed!");
            randomSeed = DEFAULT_RANDOMSEED;
        }

        if (randomSeed != DEFAULT_RANDOMSEED) {
            System.err.println("using base random seed: " + randomSeed);
            // random tests only make sense as timed tests
            random = new Random();
            random.setSeed(randomSeed);
        }
        
        // no default value
        elementQName = props.getProperty(ELEMENTQNAME_KEY);
    }

    public String configString() {
        return "-Dhost=" + host + " -Dport=" + port + " -Duser=" + user
                + " -Dpassword=" + password + " -DinputPath=" + inputPath
                + " -DoutputPath=" + outputPath + " -DnumThreads=" + numThreads
                + " -Dshared=" + shared + " -DreportTime=" + reportTime
                + " -DrecordResults=" + recordResults + " -DtestType="
                + testType;
    }

    String getHost() {
        if (host.length < 2) {
            return host[0];
        }
        // round-robin across available hosts
        return host[hostIndex++ % host.length];
    }

    String getUser() {
        return user;
    }

    String getPassword() {
        return password;
    }

    String getInputPath() {
        return inputPath;
    }

    String getOutputPath() {
        return outputPath;
    }

    int getNumThreads() {
        return numThreads;
    }

    int getPort() {
        return port;
    }

    boolean getReportTime() {
        return reportTime;
    }

    long getRandomSeed() {
        return randomSeed;
    }

    boolean getRecordResults() {
        return recordResults;
    }

    boolean isShared() {
        return shared;
    }

    String getTestType() {
        return testType;
    }

    boolean isHTTP() {
        return testType.compareToIgnoreCase("HTTP") == 0;
    }

    boolean isURI() {
        return testType.compareToIgnoreCase("URI") == 0;
    }

    boolean isXDBC() {
        return testType.compareToIgnoreCase("XDBC") == 0;
    }

    /**
     * @return
     */
    public String getReporterClassName() {
        return reporterClassName;
    }

    /**
     * @return
     */
    public boolean isRandomTest() {
        return isRandomTest;
    }

    /**
     * @return
     */
    public boolean isTimedTest() {
        return isTimedTest;
    }

    /**
     * @return
     */
    public int getReadSize() {
        return readSize;
    }

    public long getTestTime() {
        return testTime;
    }

    public long getTestTimeMillis() {
        return 1000 * testTime;
    }

    /**
     * @return
     */
    public boolean hasReportPercentileDuration() {
        return reportPercentileDuration > 0;
    }

    /**
     * @return
     */
    public int getReportPercentileDuration() {
        return reportPercentileDuration;
    }

    /**
     * @return
     */
    public boolean checkResults() {
        return checkResults;
    }

    /**
     * @return
     */
    public String getTestListClassName() {
        return testListClassName;
    }

    /**
     * @return
     */
    public String getElementQName() {
        return elementQName;
    }

}
