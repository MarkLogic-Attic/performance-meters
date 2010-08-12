/**
 * Copyright (c) 2007-2010 Mark Logic Corporation. All rights reserved.
 */
package com.marklogic.performance;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

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

    private int valuesIndex = -1;

    private boolean isSequential = false;

    private Object valuesMutex = new Object();

    /**
     * @param name
     * @param namespace
     * @param type
     * @param values
     * @param sequential
     */
    public ListVariable(String name, String namespace, String type,
            String[] values, boolean sequential) {
        xname = new XName(namespace, name);
        xvalues = new XdmValue[values.length];
        // copy the input values
        for (int i = 0; i < values.length; i++) {
            xvalues[i] = XMLFileTest.newValue(type, values[i]);
        }
        isSequential = sequential;
    }

    /**
     * @param name
     * @param namespace
     * @param type
     * @param valuesFile
     * @param sequential
     * @throws IOException
     */
    public ListVariable(String name, String namespace, String type,
            File valuesFile, boolean sequential) throws IOException {
        xname = new XName(namespace, name);
        List<XdmValue> values = new LinkedList<XdmValue>();
        BufferedReader br = new BufferedReader(new FileReader(valuesFile));
        String line;
        XdmValue v;
        do {
            line = br.readLine();
            if (null != line) {
                v = XMLFileTest.newValue(type, line.trim());
                values.add(v);
            }
        } while (null != line);
        br.close();
        xvalues = values.toArray(new XdmValue[0]);
        isSequential = sequential;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.marklogic.xcc.types.XdmVariable#getName()
     */
    public XName getName() {
        return xname;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.marklogic.xcc.types.XdmVariable#getValue()
     */
    public XdmValue getValue() {
        // sequential or random?
        if (isSequential) {
            synchronized (valuesMutex) {
                valuesIndex++;
                if (valuesIndex >= xvalues.length) {
                    valuesIndex = 0;
                }
                return xvalues[valuesIndex];
            }
        }
        // return a random value from the sequence
        return xvalues[(int) (Math.random() * xvalues.length)];
    }

}
