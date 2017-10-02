package eu.bcvsolutions.idm.ic.impl;

import java.util.ArrayList;

import eu.bcvsolutions.idm.ic.api.IcUidAttribute;

public class IcUidAttributeImpl extends IcAttributeImpl implements IcUidAttribute {

	private static final long serialVersionUID = 7514965769990064116L;
	private final String revision;

	public IcUidAttributeImpl(String name, String uid, String revision) {
		super();
		this.name = name;
		this.values = new ArrayList<>();
		if (uid != null) {
			this.values.add(uid);
		}
		multiValue = false;
		this.revision = revision;
	}

	 /**
     * Obtain a string representation of the value.
     *
     * @return value that uniquely identifies an object.
     */
	@Override
	public String getUidValue() {
		if (this.multiValue || (this.values != null && this.values.size() > 1)) {
			throw new IllegalArgumentException("Attribute [" + name + "] must be a single value.");
		}
		if (this.values == null || this.values.isEmpty()) {
			return null;
		}
		if (!(this.values.get(0) instanceof String)) {
			throw new IllegalArgumentException("Attribute [" + name + "] must be a String value.");
		}
		return (String) this.values.get(0);

	}

	@Override
	public String getRevision() {
		return this.revision;
	}

}
