package eu.bcvsolutions.idm.core.rest.impl;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.config.domain.IdentityConfiguration;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityContractFilter;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoControllerRestTest;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;

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
	
	@Test
	public void testFindByRole() {
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		//
		IdmIdentityContractDto contractOne = getHelper().createContract(identity);
		IdmIdentityContractDto contractTwo = getHelper().createContract(identity);
		IdmRoleDto roleOne = getHelper().createRole();
		IdmRoleDto roleTwo = getHelper().createRole();
		getHelper().createIdentityRole(contractOne, roleOne);
		getHelper().createIdentityRole(contractTwo, roleTwo);
		//
		IdmIdentityContractFilter filter = new IdmIdentityContractFilter();
		filter.setIdentity(identity.getId());
		filter.setRoleId(roleOne.getId());
		List<IdmIdentityContractDto> contracts = find(filter);
		Assert.assertEquals(1, contracts.size());
		Assert.assertEquals(contractOne.getId(), contracts.get(0).getId());
		//
		filter.setRoleId(roleTwo.getId());
		contracts = find(filter);
		Assert.assertEquals(1, contracts.size());
		Assert.assertEquals(contractTwo.getId(), contracts.get(0).getId());
	}
}
