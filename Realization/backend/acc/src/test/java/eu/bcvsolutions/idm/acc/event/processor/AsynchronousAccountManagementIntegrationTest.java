package eu.bcvsolutions.idm.acc.event.processor;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.acc.TestHelper;
import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemAttributeMappingDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.config.domain.EventConfiguration;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.IdmEntityEventDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmEntityEventFilter;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.service.IdmEntityEventService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Asynchronous account management
 * - asynchronous event processing is enabled - check account management and provisioning
 * - asynchronous event processing is enabled - account managment fails.
 * 
 * @author Radek Tomiška
 *
 */
public class AsynchronousAccountManagementIntegrationTest extends AbstractIntegrationTest {

	@Autowired private TestHelper helper;
	@Autowired private IdmIdentityService identityService;
	@Autowired private AccAccountService accountService;
	@Autowired private SysSystemService systemService;
	@Autowired private SysSystemAttributeMappingService schemaAttributeHandlingService;
	@Autowired private IdmEntityEventService entityEventService;
	@Autowired private ConfigurationService configurationService;
	
	@Before
	public void init() {
		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
		helper.setConfigurationValue(EventConfiguration.PROPERTY_EVENT_ASYNCHRONOUS_ENABLED, true);
	}

	@After
	public void logout() {
		helper.setConfigurationValue(EventConfiguration.PROPERTY_EVENT_ASYNCHRONOUS_ENABLED, false);
		super.logout();
	}
	
	@Test
	public void testAsynchronousAccountManagementGreenLine() {
		IdmIdentityDto identity = helper.createIdentity();
		SysSystemDto system = helper.createTestResourceSystem(true);
		IdmRoleDto role = helper.createRole();
		helper.createRoleSystem(role, system);
		helper.createIdentityRole(identity, role);
		try {			
			helper.waitForResult(res -> {
				return !(entityEventService.findByState(configurationService.getInstanceId(), OperationState.CREATED).isEmpty()
						&& entityEventService.findByState(configurationService.getInstanceId(), OperationState.RUNNING).isEmpty());
			});
			AccAccountDto account = accountService.getAccount(identity.getUsername(), system.getId());
			Assert.assertNotNull(account);
			Assert.assertNotNull(helper.findResource(account.getRealUid()));
		} finally {
			identityService.delete(identity);
			systemService.delete(system);
		}
	}
	
	@Test
	public void testAsynchronousAccountManagementError() {
		// add error to some script
		SysSystemDto system = helper.createTestResourceSystem(true);
		SysSystemMappingDto mapping = helper.getDefaultMapping(system);
		SysSystemAttributeMappingDto attributeHandlingUserName = schemaAttributeHandlingService
				.findBySystemMappingAndName(mapping.getId(), TestHelper.ATTRIBUTE_MAPPING_NAME);
		// username is transformed with error
		attributeHandlingUserName.setTransformToResourceScript("returan \"" + "error" + "\";");
		attributeHandlingUserName = schemaAttributeHandlingService.save(attributeHandlingUserName);
		
		IdmIdentityDto identity = helper.createIdentity();
		IdmRoleDto role = helper.createRole();
		helper.createRoleSystem(role, system);
		IdmIdentityRoleDto identityRole = helper.createIdentityRole(identity, role);
		try {
			helper.waitForResult(res -> {
				return !(entityEventService.findByState(configurationService.getInstanceId(), OperationState.CREATED).isEmpty()
						&& entityEventService.findByState(configurationService.getInstanceId(), OperationState.RUNNING).isEmpty());
			});
			
			AccAccountDto account = accountService.getAccount(identity.getUsername(), system.getId());
			Assert.assertNull(account);
			//
			// find event result with exception
			IdmEntityEventFilter eventFilter = new IdmEntityEventFilter();
			eventFilter.setOwnerId(identityRole.getId());
			eventFilter.setStates(Lists.newArrayList(OperationState.EXCEPTION));
			List<IdmEntityEventDto> failedEvents = entityEventService.find(eventFilter, null).getContent();
			//
			Assert.assertEquals(1, failedEvents.size());
			Assert.assertEquals(CoreResultCode.GROOVY_SCRIPT_EXCEPTION.getCode(), failedEvents.get(0).getResult().getCode());
			
		} finally {
			identityService.delete(identity);
			systemService.delete(system);
		}
	}	
}
