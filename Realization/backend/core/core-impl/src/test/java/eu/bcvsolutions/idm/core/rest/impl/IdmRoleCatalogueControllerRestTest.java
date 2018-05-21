package eu.bcvsolutions.idm.core.rest.impl;

import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.dto.IdmRoleCatalogueDto;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoControllerRestTest;

/**
 * Controller tests
 * - TODO: move filters here
 * 
 * @author Radek Tomiška
 *
 */
public class IdmRoleCatalogueControllerRestTest extends AbstractReadWriteDtoControllerRestTest<IdmRoleCatalogueDto> {

	@Autowired private IdmRoleCatalogueController controller;
	
	@Override
	protected AbstractReadWriteDtoController<IdmRoleCatalogueDto, ?> getController() {
		return controller;
	}

	@Override
	protected IdmRoleCatalogueDto prepareDto() {
		IdmRoleCatalogueDto dto = new IdmRoleCatalogueDto();
		dto.setName(getHelper().createName());
		dto.setCode(getHelper().createName());
		return dto;
	}
}
