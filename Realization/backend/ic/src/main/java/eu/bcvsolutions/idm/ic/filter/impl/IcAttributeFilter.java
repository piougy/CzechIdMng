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

import java.util.Optional;

import eu.bcvsolutions.idm.ic.api.IcAttribute;
import eu.bcvsolutions.idm.ic.api.IcConnectorObject;
import eu.bcvsolutions.idm.ic.filter.api.IcFilter;

public abstract class IcAttributeFilter implements IcFilter {

	private final IcAttribute attr;

	/**
	 * Root filter for Attribute testing..
	 */
	IcAttributeFilter(IcAttribute attr) {
		this.attr = attr;
		if (attr == null) {
			throw new IllegalArgumentException("Attribute not be null!");
		}
	}

	/**
	 * Get the internal attribute.
	 */
	public IcAttribute getAttribute() {
		return this.attr;
	}

	/**
	 * Name of the attribute to find in the {@link IcConnectorObject}.
	 */
	public String getName() {
		return getAttribute().getName();
	}

	/**
	 * Determines if the attribute provided is present in the
	 * {@link IcConnectorObject}.
	 */
	public boolean isPresent(IcConnectorObject obj) {
		Optional<IcAttribute> optionalAttr = obj.getAttributes().stream().filter(attribute -> {
			return getName().equals(this.attr.getName());
		}).findFirst();
		IcAttribute attr = optionalAttr.isPresent() ? optionalAttr.get() : null;
		return attr != null;
	}
}
