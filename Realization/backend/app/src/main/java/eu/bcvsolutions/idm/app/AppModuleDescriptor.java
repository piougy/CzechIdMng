package eu.bcvsolutions.idm.app;

import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.domain.AbstractModuleDescriptor;

@Component
public class AppModuleDescriptor extends AbstractModuleDescriptor {

	public static final String MODULE_ID = "app";
	
	@Override
	public String getId() {
		return MODULE_ID;
	}
	
	@Override
	public boolean isDisableable() {
		return false;
	}
}
