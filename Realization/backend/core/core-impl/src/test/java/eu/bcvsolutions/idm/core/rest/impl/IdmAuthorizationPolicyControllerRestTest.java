package eu.bcvsolutions.idm.core.rest.impl;

import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.dto.IdmAuthorizationPolicyDto;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoControllerRestTest;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmGroupPermission;
import eu.bcvsolutions.idm.core.security.evaluator.BasePermissionEvaluator;

/**
 * Controller tests
 * - TODO: move filters here
 * 
 * @author Radek Tomiška
 *
 */
public class IdmAuthorizationPolicyControllerRestTest extends AbstractReadWriteDtoControllerRestTest<IdmAuthorizationPolicyDto> {

	@Autowired private IdmAuthorizationPolicyController controller;
	
	@Override
	protected AbstractReadWriteDtoController<IdmAuthorizationPolicyDto, ?> getController() {
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
	protected IdmAuthorizationPolicyDto prepareDto() {
		IdmAuthorizationPolicyDto dto = new IdmAuthorizationPolicyDto();
		dto.setGroupPermission(IdmGroupPermission.APP.getName());
		dto.setPermissions(IdmBasePermission.AUTOCOMPLETE);
		dto.setRole(getHelper().createRole().getId());
		dto.setEvaluator(BasePermissionEvaluator.class);
		return dto;
	}
}
