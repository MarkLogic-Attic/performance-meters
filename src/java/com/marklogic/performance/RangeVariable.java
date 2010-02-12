/**
 * Copyright (c) 2007-2010 Mark Logic Corporation. All rights reserved.
 */
package com.marklogic.performance;

import com.marklogic.xcc.ValueFactory;
import com.marklogic.xcc.exceptions.UnimplementedFeatureException;
import com.marklogic.xcc.types.ValueType;
import com.marklogic.xcc.types.XName;
import com.marklogic.xcc.types.XSDouble;
import com.marklogic.xcc.types.XSInteger;
import com.marklogic.xcc.types.XdmValue;
import com.marklogic.xcc.types.XdmVariable;

/**
 * @author Michael Blakeley, michael.blakeley@marklogic.com
 *
 */
public class RangeVariable implements XdmVariable {

    private XName xname;

    private XdmValue xMin;

    private XdmValue xMax;

    private String type;

    /**
     * @param name
     * @param namespace
     * @param type
     * @param minValue
     * @param maxValue
     */
    public RangeVariable(String name, String namespace, String type,
            String minValue, String maxValue) {
        xname = new XName(namespace, name);
        // prepare min, max values
        xMin = XMLFileTest.newValue(type, minValue);
        xMax = XMLFileTest.newValue(type, maxValue);
        this.type = type;
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
        if (xMin instanceof XSInteger) {
            long min = ((XSInteger) xMin).asPrimitiveLong();
            long range = ((XSInteger) xMax).asPrimitiveLong() - min;
            return ValueFactory.newXSInteger(min
                    + (long) (Math.random() * range));
        }

        if (xMin instanceof XSDouble) {
            double min = ((XSDouble) xMin).asPrimitiveDouble();
            double range = ((XSDouble) xMax).asPrimitiveDouble() - min;
            return ValueFactory.newValue(ValueType.XS_DOUBLE, min
                    + (Math.random() * range));
        }

        // TODO implement types as needed

        throw new UnimplementedFeatureException(
                "cannot use ranges of type " + type);
    }
}
