package eu.bcvsolutions.idm.ic.impl;

import java.util.ArrayList;

import eu.bcvsolutions.idm.core.security.api.domain.ConfidentialString;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.ic.api.IcPasswordAttribute;


public class IcPasswordAttributeImpl extends IcAttributeImpl implements IcPasswordAttribute {

	private static final long serialVersionUID = -4667649003440978002L;
	private boolean password = false;

	public IcPasswordAttributeImpl(String name, GuardedString value) {
		super();
		this.name = name;
		this.values = new ArrayList<>();
		if (value != null) {
			this.values.add(value);
		}
		multiValue = false;
		this.password = true;
	}

	/**
	 * Return confidential single value. Attribute have to set multiValue on
	 * false and confidential to true.
	 * 
	 * @return
	 */
	@Override
	public GuardedString getPasswordValue() {
		if (this.multiValue || (this.values != null && this.values.size() > 1)) {
			throw new IllegalArgumentException("Must be a single value.");
		}
		if (!this.password) {
			throw new IllegalArgumentException("Must be a password value.");
		}
		if (this.values == null || this.values.isEmpty()) {
			return null;
		}
		if (this.values.get(0) instanceof ConfidentialString) {
			return new GuardedString(GuardedString.SECRED_PROXY_STRING);
		}
		if (!(this.values.get(0) instanceof GuardedString)) {
			throw new IllegalArgumentException("Must be a GuardedString value.");
		}
		return (GuardedString) this.values.get(0);

	}

	/**
	 * If is true, then attribute contains single value with
	 * {@link GuardedString} type.
	 * 
	 * @return
	 */
	public boolean isPassword() {
		return password;
	}

}
