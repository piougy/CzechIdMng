package eu.bcvsolutions.idm.core.rest.impl;

import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.dto.IdmRoleCatalogueRoleDto;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoControllerRestTest;

/**
 * Controller tests
 * - TODO: move filters here
 * 
 * @author Radek Tomiška
 *
 */
public class IdmRoleCatalogueRoleControllerRestTest extends AbstractReadWriteDtoControllerRestTest<IdmRoleCatalogueRoleDto> {

	@Autowired private IdmRoleCatalogueRoleController controller;
	
	@Override
	protected AbstractReadWriteDtoController<IdmRoleCatalogueRoleDto, ?> getController() {
		return controller;
	}

	@Override
	protected IdmRoleCatalogueRoleDto prepareDto() {
		IdmRoleCatalogueRoleDto dto = new IdmRoleCatalogueRoleDto();
		try {
			getHelper().loginAdmin(); // role catalogue create using repository directly - authentication is needed
			dto.setRole(getHelper().createRole().getId());
			dto.setRoleCatalogue(getHelper().createRoleCatalogue().getId());
		} finally {
			getHelper().logout();
		}
		//
		return dto;
	}
}
