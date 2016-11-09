package eu.bcvsolutions.idm.icf;

import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.domain.AbstractModuleDescriptor;

@Component
public class IcfModuleDescriptor extends AbstractModuleDescriptor {

	public static final String MODULE_ID = "icf";
	
	@Override
	public String getId() {
		return MODULE_ID;
	}
}
