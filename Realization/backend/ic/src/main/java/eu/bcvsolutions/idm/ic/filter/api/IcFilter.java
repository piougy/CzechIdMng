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
package eu.bcvsolutions.idm.ic.filter.api;

import eu.bcvsolutions.idm.ic.api.IcConnectorObject;

/**
 * Basic interface to match a {@link IcConnectorObject}.
 *
 * @author Will Droste
 * @since 1.0
 */
public interface IcFilter {
    /**
     * Determines whether the specified {@link IcConnectorObject} matches this
     * filter.
     *
     * @param obj
     *            - The specified IcConnectorObject.
     * @return {@code true} if the object matches (that is, satisfies all
     *         selection criteria of) this filter; otherwise {@code false}.
     */
    boolean accept(IcConnectorObject obj);

    /**
     * Applies a {@code IcFilterVisitor} to this {@code IcFilter}.
     *
     * @param <R>
     *            The return type of the visitor's methods.
     * @param <P>
     *            The type of the additional parameters to the visitor's
     *            methods.
     * @param v
     *            The filter visitor.
     * @param p
     *            Optional additional visitor parameter.
     * @return A result as specified by the visitor.
     * @since 1.4
     */
    <R, P> R accept(IcFilterVisitor<R, P> v, P p);
}
