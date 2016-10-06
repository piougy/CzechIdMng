package eu.bcvsolutions.idm.example;

import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.domain.AbstractModuleDescriptor;

@Component
public class ExampleModuleDescriptor extends AbstractModuleDescriptor {

	public static final String MODULE_ID = "example";
	
	@Override
	public String getId() {
		return MODULE_ID;
	}
}
