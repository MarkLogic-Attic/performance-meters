/**
 * Copyright (c) 2007-2008 Mark Logic Corporation. All rights reserved.
 */
package com.marklogic.performance;

/**
 * @author Michael Blakeley, michael.blakeley@marklogic.com
 *
 */
public interface ResultInterface {

    /**
     * @param _field
     * @return
     * @throws UnknownResultFieldException
     */
    public abstract String getFieldValue(String _field)
            throws UnknownResultFieldException;

}