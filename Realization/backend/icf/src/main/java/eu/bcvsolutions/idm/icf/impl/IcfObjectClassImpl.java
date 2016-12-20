package eu.bcvsolutions.idm.icf.impl;

import eu.bcvsolutions.idm.icf.api.IcfObjectClass;

/**
 * Implementation of object class. Object class defined type or category of connector object.
 * @author svandav
 *
 */
public class IcfObjectClassImpl implements IcfObjectClass {
	
	private String type;
	private String displayName;
	
	public IcfObjectClassImpl(String type) {
		super();
		this.type = type;
		this.displayName = type;
	}

	/**
	 * Return type or category of connector object
	 * @return
	 */
	@Override
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	/**
	 * Return display name for this type of object class
	 * @return
	 */
	@Override
	public String getDisplayName() {
		return displayName;
	}

	@Override
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		IcfObjectClassImpl other = (IcfObjectClassImpl) obj;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}
}
