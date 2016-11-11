package eu.bcvsolutions.idm.icf.dto;

import java.util.ArrayList;
import java.util.List;

import eu.bcvsolutions.idm.icf.api.IcfAttribute;
import eu.bcvsolutions.idm.icf.api.IcfConnectorObject;
import eu.bcvsolutions.idm.icf.api.IcfObjectClass;

/**
 * Instance of connector object
 * @author svandav
 *
 */
public class IcfConnectorObjectDto implements IcfConnectorObject {
	private IcfObjectClass objectClass;
	private List<IcfAttribute> attributes;

	
	public IcfConnectorObjectDto() {
	}

	public IcfConnectorObjectDto(IcfObjectClass objectClass, List<IcfAttribute> attributes) {
		super();
		this.objectClass = objectClass;
		this.attributes = attributes;
	}

	public List<IcfAttribute> getAttributes() {
		if(attributes == null){
			this.attributes = new ArrayList<>();
		}
		return attributes;
	}

	public void setAttributes(List<IcfAttribute> attributes) {
		this.attributes = attributes;
	}

	@Override
	public IcfObjectClass getObjectClass() {
		return objectClass;
	}

	public void setObjectClass(IcfObjectClass objectClass) {
		this.objectClass = objectClass;
	}

	@Override
	public String toString() {
		return "IcfConnectorObjectDto [objectClass=" + objectClass + ", attributes=" + attributes + "]";
	}

}
