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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import eu.bcvsolutions.idm.ic.api.IcConnectorObject;
import eu.bcvsolutions.idm.ic.filter.api.IcFilter;
import eu.bcvsolutions.idm.ic.filter.api.IcFilterVisitor;

public final class IcOrFilter extends IcCompositeFilter {

    /**
     * Left side of a composite based filter.
     */
    private LinkedList<IcFilter> subFilters;

    /**
     * Takes the result of the left and right filter and ORs them.
     */
    public IcOrFilter(final IcFilter left, final IcFilter right) {
        this(newList(left, right));
    }

    public IcOrFilter(final Collection<IcFilter> filters) {
        super(null, null);
        subFilters = new LinkedList<IcFilter>(filters);
    }

    /**
     * Takes the result from the left and ORs it w/ the right filter.
     *
     * @see IcFilter#accept(IcConnectorObject)
     */
    @Override
    public boolean accept(final IcConnectorObject obj) {
        boolean result = false;
        for (final IcFilter subFilter : subFilters) {
            result = subFilter.accept(obj);
            if (result) {
                break;
            }
        }
        return result;
    }

    public <R, P> R accept(IcFilterVisitor<R, P> v, P p) {
        return v.visitOrFilter(p, this);
    }

    @Override
    public IcFilter getLeft() {
        return subFilters.getFirst();
    }

    @Override
    public IcFilter getRight() {
        if (subFilters.size() > 2) {
            final LinkedList<IcFilter> right = new LinkedList<IcFilter>(subFilters);
            right.removeFirst();
            return new IcAndFilter(right);
        } else if (subFilters.size() == 2 ){
            return subFilters.getLast();
        } else {
            return null;
        }
    }

    public Collection<IcFilter> getFilters() {
    	 if (subFilters == null) {
    		 subFilters = new LinkedList<IcFilter>();
         }
         return Collections.unmodifiableList(subFilters);
    }
    
    private static List<IcFilter> newList(IcFilter... arr) {
        List<IcFilter> ret = new ArrayList<IcFilter>();
        if (arr != null && arr.length != 0) {
            for (IcFilter t : arr) {
                ret.add(t);
            }
        }
        return ret;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder().append('(');
        boolean isFirst = true;
        for (final IcFilter subFilter : subFilters) {
            if (isFirst) {
                isFirst = false;
            } else {
                builder.append(" or ");
            }
            builder.append(subFilter);
        }
        return builder.append(')').toString();
    }
}
