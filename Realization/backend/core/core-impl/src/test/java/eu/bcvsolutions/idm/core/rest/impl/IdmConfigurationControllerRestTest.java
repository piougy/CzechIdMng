package eu.bcvsolutions.idm.core.rest.impl;

import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.dto.IdmConfigurationDto;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoControllerRestTest;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;

/**
 * Identity controller tests
 * - TODO: move filters here
 * 
 * @author Radek Tomiška
 *
 */
public class IdmConfigurationControllerRestTest extends AbstractReadWriteDtoControllerRestTest<IdmConfigurationDto> {

	@Autowired private IdmConfigurationController controller;
	
	@Override
	protected AbstractReadWriteDtoController<IdmConfigurationDto, ?> getController() {
		return controller;
	}
	
	@Override
	protected boolean supportsPatch() {
		return false;
	}
	
	@Override
	protected boolean supportsAutocomplete() {
		return false;
	}

	@Override
	protected IdmConfigurationDto prepareDto() {
		IdmConfigurationDto dto = new IdmConfigurationDto();
		dto.setName(ConfigurationService.IDM_PUBLIC_PROPERTY_PREFIX + getHelper().createName());
		dto.setValue(getHelper().createName());
		return dto;
	}
	
	// TODO: secured property cannot be read by REST 
}
