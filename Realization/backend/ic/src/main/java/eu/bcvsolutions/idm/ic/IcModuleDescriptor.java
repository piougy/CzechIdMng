package eu.bcvsolutions.idm.ic;

import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.domain.AbstractModuleDescriptor;

@Component
public class IcModuleDescriptor extends AbstractModuleDescriptor {

	public static final String MODULE_ID = "ic";
	
	@Override
	public String getId() {
		return MODULE_ID;
	}
	
	/**
	 * Its lib "only" now
	 */
	@Override
	public boolean isDisableable() {
		return false;
	}
}
