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
 * Portions Copyrighted 2014 ForgeRock AS.
 */
package eu.bcvsolutions.idm.ic.filter.impl;

import eu.bcvsolutions.idm.ic.api.IcConnectorObject;
import eu.bcvsolutions.idm.ic.filter.api.IcFilter;
import eu.bcvsolutions.idm.ic.filter.api.IcFilterVisitor;

/**
 * Proxy the filter to return the negative of the value.
 */
public final class IcNotFilter implements IcFilter {

    private final IcFilter filter;

    /**
     * Take the value returned from the internal filter and NOT it.
     */
    public IcNotFilter(IcFilter filter) {
        this.filter = filter;
    }

    /**
     * Get the internal filter that is being negated.
     */
    public IcFilter getFilter() {
        return filter;
    }

    /**
     * Return the opposite the internal filters return value.
     *
     * @see IcFilter#accept(IcConnectorObject)
     */
    @Override
    public boolean accept(IcConnectorObject obj) {
        return !this.filter.accept(obj);
    }

    public <R, P> R accept(IcFilterVisitor<R, P> v, P p) {
        return v.visitNotFilter(p, this);
    }

    @Override
    public String toString() {
        StringBuilder bld = new StringBuilder();
        bld.append("NOT: ").append(getFilter());
        return super.toString();
    }
}
