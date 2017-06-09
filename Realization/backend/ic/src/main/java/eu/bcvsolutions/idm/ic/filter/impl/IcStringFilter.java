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
import eu.bcvsolutions.idm.ic.api.IcConnectorObject;

/**
 * Filter based on strings.
 */
public abstract class IcStringFilter extends IcSingleValueAttributeFilter {

    /**
     * Attempts to get a string from the attribute.
     */
    IcStringFilter(IcAttribute attr) {
        super(attr);
        Object val = super.getValue();
        if (!(val instanceof String)) {
            final String msg = "Value must be a string!";
            throw new IllegalArgumentException(msg);
        }
    }

    /**
     * Get the string value from the afore mentioned attribute.
     *
     * @see IcSingleValueAttributeFilter#getValue()
     */
    @Override
    public String getValue() {
        return (String) super.getValue();
    }

    /**
     * @throws ClassCastException
     *             if the value from the {@link IcConnectorObject}'s attribute of
     *             the same name as provided is not a string.
     */
    @Override
    public boolean accept(IcConnectorObject obj) {
        boolean ret = false;
        IcAttribute attr = obj.getAttributeByName(getName());
        if (attr != null) {
            ret = accept((String) attr.getValues().get(0));
        }
        return ret;
    }

    public abstract boolean accept(String value);
}
