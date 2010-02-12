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

import java.io.IOException;

import com.marklogic.xcc.types.XdmVariable;

/**
 * @author Michael Blakeley, michael.blakeley@marklogic.com
 * 
 */
public abstract class AbstractTest implements TestInterface {
    protected String name;

    protected String commentExpectedResult;

    protected String query;

    protected String user;

    protected String password;

    protected XdmVariable[] variables = null;

    /*
     * (non-Javadoc)
     * 
     * @see com.marklogic.performance.TestInterface#getName()
     */
    public String getName() {
        return name;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.marklogic.performance.TestInterface#getPassword()
     */
    public String getPassword() {
        return password;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.marklogic.performance.TestInterface#getUser()
     */
    public String getUser() {
        return user;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.marklogic.performance.TestInterface#getQuery()
     */
    @SuppressWarnings("unused")
    public String getQuery() throws IOException {
        return query;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.marklogic.performance.TestInterface#getCommentExpectedResult()
     */
    public String getCommentExpectedResult() {
        return commentExpectedResult;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.marklogic.performance.TestInterface#hasVariables()
     */
    public boolean hasVariables() {
        if (null == variables || 0 == variables.length) {
            return false;
        }
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.marklogic.performance.TestInterface#getVariables()
     */
    public XdmVariable[] getVariables() {
        return variables;
    }
}
