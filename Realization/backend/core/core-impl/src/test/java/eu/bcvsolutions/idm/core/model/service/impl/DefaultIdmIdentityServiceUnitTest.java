package eu.bcvsolutions.idm.core.model.service.impl;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import eu.bcvsolutions.idm.core.api.config.domain.RoleConfiguration;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.model.repository.IdmAuthorityChangeRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRepository;
import eu.bcvsolutions.idm.test.api.AbstractUnitTest;

/**
 * Identity service unit tests
 * - nice label
 * 
 * @author Radek Tomiška
 *
 */
public class DefaultIdmIdentityServiceUnitTest extends AbstractUnitTest {

	@Mock private IdmIdentityRepository repository;
	@Mock private IdmRoleService roleService;
	@Mock private IdmAuthorityChangeRepository authChangeRepository;
	@Mock private EntityEventManager entityEventManager;
	@Mock private RoleConfiguration roleConfiguration;
	@Mock private FormService formService;
	//
	@InjectMocks 
	private DefaultIdmIdentityService service;
	
	@Test
	public void testNiceLabelWithNull() {
		Assert.assertNull(service.getNiceLabel(null));
	}
	
	@Test
	public void testNiceLabelWithUsernameOnly() {
		IdmIdentityDto identity = new IdmIdentityDto();
		String username = "validation_test_" + System.currentTimeMillis();
		identity.setUsername(username);
		//
		Assert.assertEquals(username, service.getNiceLabel(identity));
	}
	
	@Test
	public void testNiceLabelWithTitlesAndFirstnameOnly() {
		IdmIdentityDto identity = new IdmIdentityDto();
		String username = "validation_test_" + System.currentTimeMillis();
		identity.setUsername(username);
		identity.setFirstName("firstname");
		identity.setTitleAfter("csc.");
		identity.setTitleBefore("Bc.");
		//
		Assert.assertEquals(username, service.getNiceLabel(identity));
	}
	
	@Test
	public void testNiceLabelWithLastnameOnly() {
		IdmIdentityDto identity = new IdmIdentityDto();
		identity.setLastName("lastName");
		//
		Assert.assertEquals(identity.getLastName(), service.getNiceLabel(identity));
	}
	
	@Test
	public void testNiceLabelWithFirstnameLastName() {
		IdmIdentityDto identity = new IdmIdentityDto();
		identity.setFirstName("firstname");
		identity.setLastName("lastName");
		//
		Assert.assertEquals(String.format("%s %s", 
				identity.getFirstName(),
				identity.getLastName()), 
				service.getNiceLabel(identity));
	}
	
	@Test
	public void testNiceLabelWithFullName() {
		IdmIdentityDto identity = new IdmIdentityDto();
		identity.setFirstName("firstname");
		identity.setLastName("lastName");
		identity.setTitleAfter("csc.");
		identity.setTitleBefore("Bc.");
		//
		Assert.assertEquals(String.format("%s %s %s, %s", 
				identity.getTitleBefore(),
				identity.getFirstName(),
				identity.getLastName(),
				identity.getTitleAfter()), 
				service.getNiceLabel(identity));
	}
}
