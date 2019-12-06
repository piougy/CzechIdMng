package eu.bcvsolutions.idm.example.bulk.action.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventContext;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.bulk.action.impl.role.RoleDuplicateBulkAction;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.model.event.RoleEvent;
import eu.bcvsolutions.idm.core.model.event.RoleEvent.RoleEventType;
import eu.bcvsolutions.idm.test.api.AbstractBulkActionTest;

/**
 * Test prevent to remove account on target system, when role is duplicated and changed.
 * 
 * @author Radek Tomiška
 *
 */
public class DuplicateRoleLogProcessorIntegrationTest extends AbstractBulkActionTest {

	@Autowired private IdmRoleService roleService;
	@Autowired private RoleDuplicateBulkAction action;
	
	@Before
	public void login() {
		loginAsAdmin();
	}
	
	@After
	public void logout() {
		super.logout();
	}
	
	@Test
	public void testDuplicateLogProcessorIsRegistered() {
		List<IdmFormAttributeDto> formAttributes = action.getFormAttributes();
		//
		Assert.assertTrue(formAttributes.stream().anyMatch(a -> a.getCode().equals(DuplicateRoleLogProcessor.PARAMETER_INCLUDE_LOG)));
	}
	
	@Test
	public void testDuplicateLogIsCreated() {
		IdmRoleDto role = getHelper().createRole(null, null, getHelper().createName());
		IdmRoleDto targetRole = new IdmRoleDto();
		targetRole.setBaseCode(role.getBaseCode());
		targetRole.setEnvironment(getHelper().createName());
		//
		// publish duplicate event
		EntityEvent<IdmRoleDto> event = new RoleEvent(RoleEventType.DUPLICATE, targetRole);
		event.setOriginalSource(role); // original source is the cloned role
		EventContext<IdmRoleDto> context = roleService.publish(event);
		//
		// processor conditional is not match
		Assert.assertFalse(context.getResults().stream().anyMatch(r -> r.getProcessor().getClass().equals(DuplicateRoleLogProcessor.class)));
		//
		targetRole = new IdmRoleDto();
		targetRole.setBaseCode(role.getBaseCode());
		targetRole.setEnvironment(getHelper().createName());
		Map<String, Serializable> props = new HashMap<>();
		props.put(DuplicateRoleLogProcessor.PARAMETER_INCLUDE_LOG, true);
		event = new RoleEvent(RoleEventType.DUPLICATE, targetRole, props);
		event.setOriginalSource(role); // original source is the cloned role
		context = roleService.publish(event);
		//
		// processor will be evaluated
		Assert.assertTrue(context.getResults().stream().anyMatch(r -> r.getProcessor().getClass().equals(DuplicateRoleLogProcessor.class)));
		
	}
}
