package eu.bcvsolutions.idm.acc;

import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.model.domain.AbstractModuleDescriptor;

@Component
public class AccModuleDescriptor extends AbstractModuleDescriptor {

	public static final String MODULE_ID = "acc";
	
	@Override
	public String getId() {
		return MODULE_ID;
	}
}
