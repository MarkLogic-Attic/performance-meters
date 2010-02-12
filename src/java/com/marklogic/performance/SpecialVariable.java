/*
 * Copyright (c)2010 Mark Logic Corporation
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

import com.marklogic.performance.sampler.Sampler;
import com.marklogic.xcc.ValueFactory;
import com.marklogic.xcc.exceptions.UnimplementedFeatureException;
import com.marklogic.xcc.types.XName;
import com.marklogic.xcc.types.XdmValue;
import com.marklogic.xcc.types.XdmVariable;

/**
 * @author Michael Blakeley, Mark Logic Corporation
 * 
 */
public class SpecialVariable implements XdmVariable {

    protected XName xname;

    protected SPECIAL special = null;
    
    protected static enum SPECIAL {
        INDEX, NAME
    };

    /**
     * @param _name
     * @param _namespace
     * @param _special
     */
    public SpecialVariable(String _name, String _namespace,
            String _special) {
        xname = new XName(_namespace, _name);
        SPECIAL[] values = SPECIAL.values();
        for (int i = 0; i < values.length; i++) {
            if (values[i].toString().equalsIgnoreCase(_special)) {
                special = values[i];
            }
        }
        if (null == special) {
            throw new UnimplementedFeatureException(
                    "Undefined special variable: " + _special);
        }
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
        // process special value according to whatever rules we define
        XdmValue value = null;
        switch (special) {
        case NAME:
            // name of current thread
            value = ValueFactory.newXSString(Thread.currentThread()
                    .getName());
            break;
        case INDEX:
            // index of current Sampler
            // TODO: how to organize this so we know?
            value = ValueFactory.newXSInteger(((Sampler)Thread.currentThread()).getIndex());
            break;
        default:
            throw new UnimplementedFeatureException(
                    "Unknown special type: " + special);
        }
        return value;
    }

}
