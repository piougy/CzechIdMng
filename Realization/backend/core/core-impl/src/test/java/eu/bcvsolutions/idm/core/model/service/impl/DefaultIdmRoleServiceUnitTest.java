package eu.bcvsolutions.idm.core.model.service.impl;

import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import eu.bcvsolutions.idm.core.api.config.domain.RoleConfiguration;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.CoreEvent.CoreEventType;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleCatalogueRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleCompositionService;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.model.event.processor.role.RoleCodeEnvironmentProcessor;
import eu.bcvsolutions.idm.core.model.repository.IdmRoleRepository;
import eu.bcvsolutions.idm.test.api.AbstractUnitTest;

/**
 * Unit test for role service
 * - append environment suffix
 * 
 * @author Radek Tomiška 
 *
 */
public class DefaultIdmRoleServiceUnitTest extends AbstractUnitTest {

	@Mock private EntityEventManager entityEventManager;
	@Mock private FormService formService;
	@Mock private RoleConfiguration roleConfiguration;
	@Mock private IdmRoleRepository repository;
	@Mock private ConfigurationService configurationService;
	@Mock private IdmRoleCatalogueRoleService roleCatalogueRoleService;
	@Mock private IdmRoleCompositionService roleCompositionService;
	@Mock private IdmIdentityService identityService;
	//
	@InjectMocks private DefaultIdmRoleService service;


	@Test
	public void testGetCodeWithoutEnvironment() {
		when(roleConfiguration.getCodeEnvironmentSeperator()).thenReturn(RoleConfiguration.DEFAULT_CODE_ENVIRONMENT_SEPARATOR);
		//
		String code = "code";
		String env = "env";
		IdmRoleDto role = new IdmRoleDto();
		role.setBaseCode(code);
		//
		Assert.assertEquals(code, service.getCodeWithoutEnvironment(role));
		//
		role.setEnvironment(env);
		//
		Assert.assertEquals(code, service.getCodeWithoutEnvironment(role));
		//
		//
		role.setBaseCode(code + RoleConfiguration.DEFAULT_CODE_ENVIRONMENT_SEPARATOR + env);
		role.setEnvironment(env);
		//
		Assert.assertEquals(code + RoleConfiguration.DEFAULT_CODE_ENVIRONMENT_SEPARATOR + env, service.getCodeWithoutEnvironment(role));
	}
	
	@Test
	public void testGetCodeWithEnvironment() {
		when(roleConfiguration.getCodeEnvironmentSeperator()).thenReturn(RoleConfiguration.DEFAULT_CODE_ENVIRONMENT_SEPARATOR);
		//
		String code = "code";
		String env = "env";
		IdmRoleDto role = new IdmRoleDto();
		role.setBaseCode(code);
		//
		Assert.assertEquals(code, service.getCodeWithEnvironment(role));
		//
		role.setEnvironment(env);
		//
		Assert.assertEquals(code + RoleConfiguration.DEFAULT_CODE_ENVIRONMENT_SEPARATOR + env, service.getCodeWithEnvironment(role));
	}
	
	@Test
	public void testChangeRoleEnvironment() {
		when(roleConfiguration.getCodeEnvironmentSeperator()).thenReturn(RoleConfiguration.DEFAULT_CODE_ENVIRONMENT_SEPARATOR);
		//
		String code = "code";
		String env = "env";
		IdmRoleDto role = new IdmRoleDto();
		role.setBaseCode(code);
		role.setEnvironment(env);
		//
		RoleCodeEnvironmentProcessor roleCodeEnvironmentProcessor = new RoleCodeEnvironmentProcessor(service);
		IdmRoleDto result = roleCodeEnvironmentProcessor.process(new CoreEvent<IdmRoleDto>(CoreEventType.CREATE, role)).getEvent().getContent();
		//
		Assert.assertEquals(service.getCodeWithEnvironment(role), result.getCode());
		//
		role.setBaseCode(code);
		role.setEnvironment(env);
		role.setCode(service.getCodeWithEnvironment(role));
		//
		result = roleCodeEnvironmentProcessor.process(new CoreEvent<IdmRoleDto>(CoreEventType.CREATE, role)).getEvent().getContent();
		//
		Assert.assertEquals(service.getCodeWithEnvironment(role), result.getCode());
		//
		// after environment change
		IdmRoleDto roleUpdate = new IdmRoleDto();
		roleUpdate.setBaseCode(code);
		String envUpdate = "envU";
		roleUpdate.setEnvironment(envUpdate);
		//
		CoreEvent<IdmRoleDto> updateEvent = new CoreEvent<IdmRoleDto>(CoreEventType.CREATE, roleUpdate);
		updateEvent.setOriginalSource(result);
		IdmRoleDto resultUpdate = roleCodeEnvironmentProcessor.process(updateEvent).getEvent().getContent();
		//
		Assert.assertEquals(service.getCodeWithEnvironment(roleUpdate), resultUpdate.getCode());
		Assert.assertEquals(code + RoleConfiguration.DEFAULT_CODE_ENVIRONMENT_SEPARATOR + envUpdate, resultUpdate.getCode());
	}
	
	@Test(expected = ResultCodeException.class)
	public void testCreateRoleWitCodeAndEnvironmentConflict() {
		when(roleConfiguration.getCodeEnvironmentSeperator()).thenReturn(RoleConfiguration.DEFAULT_CODE_ENVIRONMENT_SEPARATOR);
		//
		IdmRoleDto role = new IdmRoleDto();
		role.setCode("code");
		role.setBaseCode("code");
		role.setEnvironment("env");
		//
		RoleCodeEnvironmentProcessor roleCodeEnvironmentProcessor = new RoleCodeEnvironmentProcessor(service);
		roleCodeEnvironmentProcessor.process(new CoreEvent<IdmRoleDto>(CoreEventType.CREATE, role));
	}
	
	@Test(expected = ResultCodeException.class)
	public void testUpdateRoleWitCodeAndEnvironmentConflict() {
		when(roleConfiguration.getCodeEnvironmentSeperator()).thenReturn(RoleConfiguration.DEFAULT_CODE_ENVIRONMENT_SEPARATOR);
		//
		IdmRoleDto role = new IdmRoleDto();
		role.setCode("code" + RoleConfiguration.DEFAULT_CODE_ENVIRONMENT_SEPARATOR + "env");
		role.setBaseCode("code");
		role.setEnvironment("env");
		//
		IdmRoleDto roleUpdate = new IdmRoleDto();
		roleUpdate.setCode("code" + RoleConfiguration.DEFAULT_CODE_ENVIRONMENT_SEPARATOR + "env2");
		roleUpdate.setBaseCode("code");
		roleUpdate.setEnvironment("env2");
		//
		RoleCodeEnvironmentProcessor roleCodeEnvironmentProcessor = new RoleCodeEnvironmentProcessor(service);
		CoreEvent<IdmRoleDto> updateEvent = new CoreEvent<IdmRoleDto>(CoreEventType.CREATE, roleUpdate);
		updateEvent.setOriginalSource(role);
		roleCodeEnvironmentProcessor.process(updateEvent);
	}
	
	@Test
	public void testCreateSuccess() {
		when(roleConfiguration.getCodeEnvironmentSeperator()).thenReturn(RoleConfiguration.DEFAULT_CODE_ENVIRONMENT_SEPARATOR);
		//
		IdmRoleDto role = new IdmRoleDto();
		role.setBaseCode("code");
		role.setEnvironment("env");
		//
		RoleCodeEnvironmentProcessor roleCodeEnvironmentProcessor = new RoleCodeEnvironmentProcessor(service);
		IdmRoleDto content = roleCodeEnvironmentProcessor.process(new CoreEvent<IdmRoleDto>(CoreEventType.CREATE, role)).getEvent().getContent();
		//
		Assert.assertEquals("code" + RoleConfiguration.DEFAULT_CODE_ENVIRONMENT_SEPARATOR + "env", content.getCode());
	}
	
	@Test
	public void testCreateSuccessWithEnvironment() {
		when(roleConfiguration.getCodeEnvironmentSeperator()).thenReturn(RoleConfiguration.DEFAULT_CODE_ENVIRONMENT_SEPARATOR);
		//
		IdmRoleDto role = new IdmRoleDto();
		role.setCode("code" + RoleConfiguration.DEFAULT_CODE_ENVIRONMENT_SEPARATOR + "env");
		role.setBaseCode("code");
		role.setEnvironment("env");
		//
		RoleCodeEnvironmentProcessor roleCodeEnvironmentProcessor = new RoleCodeEnvironmentProcessor(service);
		IdmRoleDto content = roleCodeEnvironmentProcessor.process(new CoreEvent<IdmRoleDto>(CoreEventType.CREATE, role)).getEvent().getContent();
		//
		Assert.assertEquals("code" + RoleConfiguration.DEFAULT_CODE_ENVIRONMENT_SEPARATOR + "env", content.getCode());
	}
	
	@Test
	public void testCreateSuccessWithBaseCodeOnly() {
		when(roleConfiguration.getCodeEnvironmentSeperator()).thenReturn(RoleConfiguration.DEFAULT_CODE_ENVIRONMENT_SEPARATOR);
		//
		IdmRoleDto role = new IdmRoleDto();
		role.setBaseCode("code");
		//
		RoleCodeEnvironmentProcessor roleCodeEnvironmentProcessor = new RoleCodeEnvironmentProcessor(service);
		IdmRoleDto content = roleCodeEnvironmentProcessor.process(new CoreEvent<IdmRoleDto>(CoreEventType.CREATE, role)).getEvent().getContent();
		//
		Assert.assertEquals("code", content.getCode());
		Assert.assertEquals("code", content.getBaseCode());
	}
	
	@Test
	public void testCreateSuccessWithCodeOnly() {
		when(roleConfiguration.getCodeEnvironmentSeperator()).thenReturn(RoleConfiguration.DEFAULT_CODE_ENVIRONMENT_SEPARATOR);
		//
		IdmRoleDto role = new IdmRoleDto();
		role.setCode("code");
		//
		RoleCodeEnvironmentProcessor roleCodeEnvironmentProcessor = new RoleCodeEnvironmentProcessor(service);
		IdmRoleDto content = roleCodeEnvironmentProcessor.process(new CoreEvent<IdmRoleDto>(CoreEventType.CREATE, role)).getEvent().getContent();
		//
		Assert.assertEquals("code", content.getCode());
		Assert.assertEquals("code", content.getBaseCode());
	}
	
	@Test
	public void testUpdateSuccessWithBaseCodeEvironment() {
		when(roleConfiguration.getCodeEnvironmentSeperator()).thenReturn(RoleConfiguration.DEFAULT_CODE_ENVIRONMENT_SEPARATOR);
		//
		IdmRoleDto role = new IdmRoleDto();
		role.setCode("code" + RoleConfiguration.DEFAULT_CODE_ENVIRONMENT_SEPARATOR + "env");
		role.setBaseCode("code");
		role.setEnvironment("env");
		//
		IdmRoleDto roleUpdate = new IdmRoleDto();
		roleUpdate.setCode("code" + RoleConfiguration.DEFAULT_CODE_ENVIRONMENT_SEPARATOR + "env");
		roleUpdate.setBaseCode("code");
		roleUpdate.setEnvironment("env2");
		//
		RoleCodeEnvironmentProcessor roleCodeEnvironmentProcessor = new RoleCodeEnvironmentProcessor(service);
		CoreEvent<IdmRoleDto> updateEvent = new CoreEvent<IdmRoleDto>(CoreEventType.CREATE, roleUpdate);
		updateEvent.setOriginalSource(role);
		IdmRoleDto content = roleCodeEnvironmentProcessor.process(updateEvent).getEvent().getContent();
		//
		//
		Assert.assertEquals("code" + RoleConfiguration.DEFAULT_CODE_ENVIRONMENT_SEPARATOR + "env2", content.getCode());
		Assert.assertEquals("code", content.getBaseCode());
	}
	
	@Test
	public void testUpdateSuccessCodeChange() {
		when(roleConfiguration.getCodeEnvironmentSeperator()).thenReturn(RoleConfiguration.DEFAULT_CODE_ENVIRONMENT_SEPARATOR);
		//
		IdmRoleDto role = new IdmRoleDto();
		role.setCode("code");
		role.setBaseCode("code");
		//
		IdmRoleDto roleUpdate = new IdmRoleDto();
		roleUpdate.setCode("code2");
		roleUpdate.setBaseCode("code");
		//
		RoleCodeEnvironmentProcessor roleCodeEnvironmentProcessor = new RoleCodeEnvironmentProcessor(service);
		CoreEvent<IdmRoleDto> updateEvent = new CoreEvent<IdmRoleDto>(CoreEventType.CREATE, roleUpdate);
		updateEvent.setOriginalSource(role);
		IdmRoleDto content = roleCodeEnvironmentProcessor.process(updateEvent).getEvent().getContent();
		//
		//
		Assert.assertEquals("code2", content.getCode());
		Assert.assertEquals("code2", content.getBaseCode());
	}
	
	@Test
	public void testUpdateSuccessCodeChangeWithEnvironment() {
		when(roleConfiguration.getCodeEnvironmentSeperator()).thenReturn(RoleConfiguration.DEFAULT_CODE_ENVIRONMENT_SEPARATOR);
		//
		IdmRoleDto role = new IdmRoleDto();
		role.setCode("code");
		role.setBaseCode("code");
		role.setEnvironment("env");
		//
		IdmRoleDto roleUpdate = new IdmRoleDto();
		roleUpdate.setCode("code2");
		roleUpdate.setBaseCode("code");
		roleUpdate.setEnvironment("env");
		//
		RoleCodeEnvironmentProcessor roleCodeEnvironmentProcessor = new RoleCodeEnvironmentProcessor(service);
		CoreEvent<IdmRoleDto> updateEvent = new CoreEvent<IdmRoleDto>(CoreEventType.CREATE, roleUpdate);
		updateEvent.setOriginalSource(role);
		IdmRoleDto content = roleCodeEnvironmentProcessor.process(updateEvent).getEvent().getContent();
		//
		//
		Assert.assertEquals("code2" + RoleConfiguration.DEFAULT_CODE_ENVIRONMENT_SEPARATOR + "env", content.getCode());
		Assert.assertEquals("code2", content.getBaseCode());
	}
	
	@Test(expected = ResultCodeException.class)
	public void testUpdateFailureCodeChangeWithEnvironment() {
		when(roleConfiguration.getCodeEnvironmentSeperator()).thenReturn(RoleConfiguration.DEFAULT_CODE_ENVIRONMENT_SEPARATOR);
		//
		IdmRoleDto role = new IdmRoleDto();
		role.setCode("code");
		role.setBaseCode("code");
		role.setEnvironment("env");
		//
		IdmRoleDto roleUpdate = new IdmRoleDto();
		roleUpdate.setCode("code2");
		roleUpdate.setBaseCode("code");
		role.setEnvironment("env2");
		//
		RoleCodeEnvironmentProcessor roleCodeEnvironmentProcessor = new RoleCodeEnvironmentProcessor(service);
		CoreEvent<IdmRoleDto> updateEvent = new CoreEvent<IdmRoleDto>(CoreEventType.CREATE, roleUpdate);
		updateEvent.setOriginalSource(role);
		roleCodeEnvironmentProcessor.process(updateEvent);
	}
}
