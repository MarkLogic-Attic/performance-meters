/**
 * Copyright (c) 2006-2007 Mark Logic Corporation. All rights reserved.
 */
package com.marklogic.performance.junit;

import java.io.IOException;

import com.marklogic.performance.Configuration;
import com.marklogic.performance.Result;
import com.marklogic.performance.sampler.Sampler;
import com.marklogic.performance.TestInterface;
import com.marklogic.performance.TestIterator;

/**
 * @author Michael Blakeley, michael.blakeley@marklogic.com
 * 
 */
public class TestSampler extends Sampler {

    /**
     * @param ti
     * @param cfg
     */
    TestSampler(TestIterator ti, Configuration cfg) {
        super(ti, cfg);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.marklogic.performance.Sampler#sample(com.marklogic.performance.TestInterface)
     */
    public Result sample(TestInterface test) throws IOException {
        throw new IOException("unimplemented");
    }

    public void add(Result r) {
        results.add(r);
    }

}
