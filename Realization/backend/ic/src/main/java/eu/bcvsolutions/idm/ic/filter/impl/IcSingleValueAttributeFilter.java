/*
 * ====================
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008-2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License("CDDL") (the "License").  You may not use this file
 * except in compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://opensource.org/licenses/cddl1.php
 * See the License for the specific language governing permissions and limitations
 * under the License.
 *
 * When distributing the Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://opensource.org/licenses/cddl1.php.
 * If applicable, add the following below this CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * ====================
 */
package eu.bcvsolutions.idm.ic.filter.impl;

import eu.bcvsolutions.idm.ic.api.IcAttribute;
import eu.bcvsolutions.idm.ic.api.IcUidAttribute;

/**
 * Get a single value out of the attribute to test w/.
 */
public abstract class IcSingleValueAttributeFilter extends IcAttributeFilter {

    /**
     * Attempt to single out the value for comparison.
     */
    IcSingleValueAttributeFilter(IcAttribute attr) {
        super(attr);
        // make sure this is not a Uid..
        if (IcUidAttribute.NAME.equals(attr.getName())) {
            throw new IllegalArgumentException("Uid can only be used for equals comparison.");
        }
        // actual runtime..
        if (attr.isMultiValue()) {
            throw new IllegalArgumentException("Must only be one value!");
        }
    }

    /**
     * Value to test against.
     */
    public Object getValue() {
        return getAttribute().getValue();
    }
}
