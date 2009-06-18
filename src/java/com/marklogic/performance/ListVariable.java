/**
 * Copyright (c) 2007-2008 Mark Logic Corporation. All rights reserved.
 */
package com.marklogic.performance;

import com.marklogic.xcc.types.XName;
import com.marklogic.xcc.types.XdmValue;
import com.marklogic.xcc.types.XdmVariable;

/**
 * @author Michael Blakeley, michael.blakeley@marklogic.com
 *
 */
public class ListVariable implements XdmVariable {

    private XName xname;
    private XdmValue[] xvalues;

    /**
     * @param name
     * @param namespace
     * @param type
     * @param values
     */
    public ListVariable(String name, String namespace, String type,
            String[] values) {
        xname = new XName(namespace, name);
        xvalues = new XdmValue[values.length];
        // copy the input values
        for (int i = 0; i < values.length; i++) {
            xvalues[i] = XMLFileTest.newValue(type, values[i]);
        }
    }

    /* (non-Javadoc)
     * @see com.marklogic.xcc.types.XdmVariable#getName()
     */
    public XName getName() {
        return xname;
    }

    /* (non-Javadoc)
     * @see com.marklogic.xcc.types.XdmVariable#getValue()
     */
    public XdmValue getValue() {
        // return a random value from the sequence
        // TODO also allow sequential values?
        return xvalues[(int)(Math.random() * xvalues.length)];
    }

}
