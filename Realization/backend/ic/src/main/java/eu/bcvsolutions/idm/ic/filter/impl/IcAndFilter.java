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

import java.util.Collection;
import java.util.LinkedList;

import org.identityconnectors.common.CollectionUtil;
import org.identityconnectors.framework.common.objects.ConnectorObject;

import eu.bcvsolutions.idm.ic.api.IcConnectorObject;
import eu.bcvsolutions.idm.ic.filter.api.IcFilter;
import eu.bcvsolutions.idm.ic.filter.api.IcFilterVisitor;

public final class IcAndFilter extends IcCompositeFilter {

    /**
     * Left side of a composite based IcFilter.
     */
    private LinkedList<IcFilter> subIcFilters;

    /**
     * And the the left and right IcFilters.
     */
    public IcAndFilter(IcFilter left, IcFilter right) {
        this(CollectionUtil.newList(left, right));
    }

    public IcAndFilter(final Collection<IcFilter> IcFilters) {
        super(null, null);
        subIcFilters = new LinkedList<IcFilter>(IcFilters);
    }

    /**
     * Ands the left and right IcFilters.
     *
     * @see IcFilter#accept(ConnectorObject)
     */
    @Override
    public boolean accept(final IcConnectorObject obj) {
        boolean result = true;
        for (final IcFilter subIcFilter : subIcFilters) {
            result = subIcFilter.accept(obj);
            if (!result) {
                break;
            }
        }
        return result;
    }


    public <R, P> R accept(IcFilterVisitor<R, P> v, P p) {
        return v.visitAndFilter(p, this);
    }

    @Override
    public IcFilter getLeft() {
        return subIcFilters.getFirst();
    }

    @Override
    public IcFilter getRight() {
        if (subIcFilters.size() > 2) {
            LinkedList<IcFilter> right = new LinkedList<IcFilter>(subIcFilters);
            right.removeFirst();
            return new IcAndFilter(right);
        } else if (subIcFilters.size() == 2 ){
           return subIcFilters.getLast();
        } else {
            return null;
        }
    }

    @Override
    public Collection<IcFilter> getFilters() {
        return CollectionUtil.asReadOnlyList(subIcFilters);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder().append('(');
        boolean isFirst = true;
        for (final IcFilter subIcFilter : subIcFilters) {
            if (isFirst) {
                isFirst = false;
            } else {
                builder.append(" and ");
            }
            builder.append(subIcFilter);
        }
        return builder.append(')').toString();
    }
}
