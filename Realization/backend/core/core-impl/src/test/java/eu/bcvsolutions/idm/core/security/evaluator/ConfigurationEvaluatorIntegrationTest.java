package eu.bcvsolutions.idm.core.security.evaluator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.core.api.dto.IdmAuthorizationPolicyDto;
import eu.bcvsolutions.idm.core.api.dto.IdmConfigurationDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.exception.ForbiddenEntityException;
import eu.bcvsolutions.idm.core.api.service.IdmAuthorizationPolicyService;
import eu.bcvsolutions.idm.core.api.service.IdmConfigurationService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmConfiguration;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.security.api.dto.LoginDto;
import eu.bcvsolutions.idm.core.security.api.service.LoginService;
import eu.bcvsolutions.idm.core.security.evaluator.configuration.ConfigurationEvaluator;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;
import eu.bcvsolutions.idm.test.api.TestHelper;

/**
 * Configuration evaluator tests
 * 
 * @author Radek Tomiška
 *
 */
public class ConfigurationEvaluatorIntegrationTest extends AbstractIntegrationTest {
	
	@Autowired private TestHelper helper;
	@Autowired private IdmIdentityService identityService;
	@Autowired private IdmAuthorizationPolicyService authorizationPolicyService;
	@Autowired private LoginService loginService;
	@Autowired private IdmConfigurationService configurationService;
	//
	private IdmConfigurationDto publicConfig = null;
	private IdmConfigurationDto privateConfig = null;
	
	@Before
	public void prepareProperties() {
		publicConfig = configurationService.save(new IdmConfigurationDto("idm.pub.core.test.config.one." + System.currentTimeMillis(), "one"));
		privateConfig = configurationService.save(new IdmConfigurationDto("idm.sec.core.test.config.one." + System.currentTimeMillis(), "one"));
	}
	
	@Test
	public void testReadWithoutPermissions() {
		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
		// prepare identity
		IdmIdentityDto identity = helper.createIdentity();
		identity.setPassword(new GuardedString("heslo"));
		identityService.save(identity);
		//
		logout();
		//
		try {
			loginService.login(new LoginDto(identity.getUsername(), identity.getPassword()));
			//
			// evaluate	access
			assertEquals(0, configurationService.find(null, IdmBasePermission.READ).getTotalElements());
		} finally {
			logout();
		}
	}
	
	@Test
	public void testReadWithPermissions() {
		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
		// prepare role
		IdmRoleDto role = helper.createRole();
		//
		IdmAuthorizationPolicyDto dto = new IdmAuthorizationPolicyDto();
		dto.setRole(role.getId());
		dto.setEvaluator(ConfigurationEvaluator.class);
		dto.setGroupPermission(CoreGroupPermission.CONFIGURATION.getName());
		dto.setAuthorizableType(IdmConfiguration.class.getCanonicalName());
		dto.setPermissions(IdmBasePermission.READ);
		authorizationPolicyService.save(dto);
		// prepare identity
		IdmIdentityDto identity = helper.createIdentity();
		identity.setPassword(new GuardedString("heslo"));
		identityService.save(identity);
		// assign role
		helper.createIdentityRole(identity, role);
		logout();
		//
		try {
			loginService.login(new LoginDto(identity.getUsername(), identity.getPassword()));
			//
			// evaluate	access
			List<IdmConfigurationDto> configs = configurationService.find(null, IdmBasePermission.READ).getContent();
			assertTrue(configs.contains(publicConfig));
			assertFalse(configs.contains(privateConfig));
		} finally {
			logout();
		}
	}
	
	@Test
	public void testReadSecuredWithPermissions() {
		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
		// prepare role
		IdmRoleDto role = helper.createRole();
		//
		IdmAuthorizationPolicyDto dto = new IdmAuthorizationPolicyDto();
		dto.setRole(role.getId());
		dto.setEvaluator(ConfigurationEvaluator.class);
		dto.setGroupPermission(CoreGroupPermission.CONFIGURATION.getName());
		dto.setAuthorizableType(IdmConfiguration.class.getCanonicalName());
		dto.getEvaluatorProperties().put(ConfigurationEvaluator.PARAMETER_SECURED, Boolean.TRUE.toString());
		dto.setPermissions(IdmBasePermission.READ);
		authorizationPolicyService.save(dto);
		// prepare identity
		IdmIdentityDto identity = helper.createIdentity();
		identity.setPassword(new GuardedString("heslo"));
		identityService.save(identity);
		// assign role
		helper.createIdentityRole(identity, role);
		logout();
		//
		try {
			loginService.login(new LoginDto(identity.getUsername(), identity.getPassword()));
			//
			// evaluate	access
			List<IdmConfigurationDto> configs = configurationService.find(null, IdmBasePermission.READ).getContent();
			assertTrue(configs.contains(publicConfig));
			assertTrue(configs.contains(privateConfig));
		} finally {
			logout();
		}
	}
	
	@Test(expected = ForbiddenEntityException.class)
	public void testUpdateWithoutPermissions() {
		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
		// prepare role
		IdmRoleDto role = helper.createRole();
		//
		IdmAuthorizationPolicyDto dto = new IdmAuthorizationPolicyDto();
		dto.setRole(role.getId());
		dto.setEvaluator(ConfigurationEvaluator.class);
		dto.setGroupPermission(CoreGroupPermission.CONFIGURATION.getName());
		dto.setAuthorizableType(IdmConfiguration.class.getCanonicalName());
		dto.setPermissions(IdmBasePermission.READ);
		authorizationPolicyService.save(dto);
		// prepare identity
		IdmIdentityDto identity = helper.createIdentity();
		identity.setPassword(new GuardedString("heslo"));
		identityService.save(identity);
		// assign role
		helper.createIdentityRole(identity, role);
		logout();
		//
		try {
			loginService.login(new LoginDto(identity.getUsername(), identity.getPassword()));
			//
			publicConfig.setValue("update");
			configurationService.save(publicConfig, IdmBasePermission.UPDATE);
		} finally {
			logout();
		}
	}
	
	@Test(expected = ForbiddenEntityException.class)
	public void testUpdateSecuredWithoutPermissions() {
		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
		// prepare role
		IdmRoleDto role = helper.createRole();
		//
		IdmAuthorizationPolicyDto dto = new IdmAuthorizationPolicyDto();
		dto.setRole(role.getId());
		dto.setEvaluator(ConfigurationEvaluator.class);
		dto.setGroupPermission(CoreGroupPermission.CONFIGURATION.getName());
		dto.setAuthorizableType(IdmConfiguration.class.getCanonicalName());
		dto.setPermissions(IdmBasePermission.READ, IdmBasePermission.UPDATE);
		authorizationPolicyService.save(dto);
		// prepare identity
		IdmIdentityDto identity = helper.createIdentity();
		identity.setPassword(new GuardedString("heslo"));
		identityService.save(identity);
		// assign role
		helper.createIdentityRole(identity, role);
		logout();
		//
		try {
			loginService.login(new LoginDto(identity.getUsername(), identity.getPassword()));
			//
			privateConfig.setValue("update");
			configurationService.save(privateConfig, IdmBasePermission.UPDATE);
		} finally {
			logout();
		}
	}
}
