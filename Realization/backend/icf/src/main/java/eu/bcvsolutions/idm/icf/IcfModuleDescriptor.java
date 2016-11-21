package eu.bcvsolutions.idm.icf;

import eu.bcvsolutions.idm.core.api.domain.AbstractModuleDescriptor;

// TODO: icf module is "standard" java dependency for now. 
// @Component
public class IcfModuleDescriptor extends AbstractModuleDescriptor {

	public static final String MODULE_ID = "icf";
	
	@Override
	public String getId() {
		return MODULE_ID;
	}
}
