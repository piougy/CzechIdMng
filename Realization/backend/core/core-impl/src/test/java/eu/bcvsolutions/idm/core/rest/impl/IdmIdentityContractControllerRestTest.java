package eu.bcvsolutions.idm.core.rest.impl;

import org.junit.After;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.config.domain.IdentityConfiguration;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoControllerRestTest;

/**
 * Controller tests
 * - TODO: move filters here
 * 
 * @author Radek Tomiška
 *
 */
public class IdmIdentityContractControllerRestTest extends AbstractReadWriteDtoControllerRestTest<IdmIdentityContractDto> {

	@Autowired private IdmIdentityContractController controller;
	
	@Override
	protected AbstractReadWriteDtoController<IdmIdentityContractDto, ?> getController() {
		return controller;
	}
	
	@Before
	@Override
	public void setup() throws Exception {
		super.setup();
		//
		getHelper().setConfigurationValue(IdentityConfiguration.PROPERTY_IDENTITY_CREATE_DEFAULT_CONTRACT, Boolean.FALSE);
	}

	@After
	@Override
	public void logout() {
		super.logout();
		//
		getHelper().setConfigurationValue(IdentityConfiguration.PROPERTY_IDENTITY_CREATE_DEFAULT_CONTRACT, Boolean.TRUE);
	}
	
	@Override
	protected boolean supportsPatch() {
		return false;
	}

	@Override
	protected IdmIdentityContractDto prepareDto() {
		IdmIdentityContractDto dto = new IdmIdentityContractDto();
		dto.setIdentity(getHelper().createIdentity().getId());
		//
		return dto;
	}
}
