package eu.bcvsolutions.idm.ic.impl;

import java.util.ArrayList;
import java.util.List;

import eu.bcvsolutions.idm.ic.api.IcAttribute;
import eu.bcvsolutions.idm.ic.api.IcConnectorObject;
import eu.bcvsolutions.idm.ic.api.IcObjectClass;

/**
 * Instance of connector object
 * @author svandav
 *
 */
public class IcConnectorObjectImpl implements IcConnectorObject {

	private static final long serialVersionUID = 7115318820292735486L;
	private String uidValue;
	private IcObjectClass objectClass;
	private List<IcAttribute> attributes;

	public IcConnectorObjectImpl() {
	}

	public IcConnectorObjectImpl(String uidValue, IcObjectClass objectClass, List<IcAttribute> attributes) {
		this.uidValue = uidValue;
		this.objectClass = objectClass;
		this.attributes = attributes;
	}
	
	@Override
	public String getUidValue() {
		return uidValue;
	}
	
	public void setUidValue(String uidValue) {
		this.uidValue = uidValue;
	}

	public List<IcAttribute> getAttributes() {
		if(attributes == null){
			this.attributes = new ArrayList<>();
		}
		return attributes;
	}

	public void setAttributes(List<IcAttribute> attributes) {
		this.attributes = attributes;
	}

	@Override
	public IcObjectClass getObjectClass() {
		return objectClass;
	}

	public void setObjectClass(IcObjectClass objectClass) {
		this.objectClass = objectClass;
	}

	@Override
	public String toString() {
		return "IcConnectorObjectImpl [objectClass=" + objectClass + ", attributes=" + attributes + "]";
	}

}
