package eu.bcvsolutions.idm.core.security.service.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.context.ApplicationContext;

import eu.bcvsolutions.idm.core.api.domain.ConfigurationMap;
import eu.bcvsolutions.idm.core.api.dto.IdmAuthorizationPolicyDto;
import eu.bcvsolutions.idm.core.api.service.IdmAuthorizationPolicyService;
import eu.bcvsolutions.idm.core.api.service.IdmCacheManager;
import eu.bcvsolutions.idm.core.api.service.ModuleService;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;
import eu.bcvsolutions.idm.core.security.evaluator.BasePermissionEvaluator;
import eu.bcvsolutions.idm.core.security.evaluator.UuidEvaluator;
import eu.bcvsolutions.idm.test.api.AbstractUnitTest;

/**
 * Test for authorities evaluation
 * 
 * @author Radek Tomiška
 *
 */
public class DefaultAuthorizationManagerUnitTest extends AbstractUnitTest {

	@Mock private ApplicationContext context;
	@Mock private IdmAuthorizationPolicyService service;
	@Mock private SecurityService securityService;
	@Mock private ModuleService moduleService;
	@Mock private IdmCacheManager idmCacheManager;
	//
	private List<IdmAuthorizationPolicyDto> enabledPolicies;
	private final BasePermissionEvaluator evaluator = new BasePermissionEvaluator();
	//
	@InjectMocks
	private DefaultAuthorizationManager manager;
	
	@Before
	public void init() {		
		enabledPolicies = new ArrayList<>();
		//
		IdmAuthorizationPolicyDto policyOne = new  IdmAuthorizationPolicyDto(UUID.randomUUID());
		policyOne.setPermissions(IdmBasePermission.READ);
		policyOne.setEvaluator(evaluator.getClass());
		enabledPolicies.add(policyOne);
		//
		IdmAuthorizationPolicyDto policyTwo = new  IdmAuthorizationPolicyDto(UUID.randomUUID());
		policyTwo.setPermissions(IdmBasePermission.UPDATE);
		policyTwo.setEvaluator(evaluator.getClass());
		enabledPolicies.add(policyTwo);
	}
	
	@Test
	public void testGetPermissions() {
		when(service.getEnabledPolicies(any(), any())).thenReturn(enabledPolicies);
		when(context.getBean(BasePermissionEvaluator.class)).thenReturn(evaluator);
		when(securityService.getCurrentId()).thenReturn(UUID.randomUUID());
		//
		Set<String> basePermissions = manager.getPermissions(new IdmRole());
		Assert.assertEquals(2, basePermissions.size());
		Assert.assertTrue(basePermissions.contains(IdmBasePermission.READ.getName()));
		Assert.assertTrue(basePermissions.contains(IdmBasePermission.UPDATE.getName()));
	}
	
	@Test
	public void testEvaluate() {
		when(service.getEnabledPolicies(any(), any())).thenReturn(enabledPolicies);
		when(context.getBean(BasePermissionEvaluator.class)).thenReturn(evaluator);
		//
		Assert.assertTrue(manager.evaluate(new IdmRole(), IdmBasePermission.READ));
		Assert.assertTrue(manager.evaluate(new IdmRole(), IdmBasePermission.UPDATE));
		Assert.assertFalse(manager.evaluate(new IdmRole(), IdmBasePermission.ADMIN));
		Assert.assertFalse(manager.evaluate(new IdmRole(), IdmBasePermission.AUTOCOMPLETE));
	}
	
	@Test
	public void testIsDuplicate() {
		IdmAuthorizationPolicyDto one = new IdmAuthorizationPolicyDto(UUID.randomUUID());
		IdmAuthorizationPolicyDto two = new IdmAuthorizationPolicyDto(UUID.randomUUID());
		// different id, but no configuration => same 
		Assert.assertTrue(manager.isDuplicate(one, two));
		// same id => same every time
		two.setId(one.getId());
		two.setAuthorizableType("mockTwo");
		Assert.assertTrue(manager.isDuplicate(one, two));
		// different authorization type
		two.setId(UUID.randomUUID());
		Assert.assertFalse(manager.isDuplicate(one, two));
		//
		one.setAuthorizableType("mockOne");
		Assert.assertFalse(manager.isDuplicate(one, two));
		//
		one.setAuthorizableType("mockTwo");
		Assert.assertTrue(manager.isDuplicate(one, two));
		//
		one.setEvaluator(BasePermissionEvaluator.class);
		Assert.assertFalse(manager.isDuplicate(one, two));
		//
		two.setEvaluator(UuidEvaluator.class);
		Assert.assertFalse(manager.isDuplicate(one, two));
		//
		one.setEvaluator(UuidEvaluator.class);
		Assert.assertTrue(manager.isDuplicate(one, two));
		//
		one.setGroupPermission("groupOne");
		Assert.assertFalse(manager.isDuplicate(one, two));
		//
		two.setGroupPermission("groupTwo");
		Assert.assertFalse(manager.isDuplicate(one, two));
		//
		two.setGroupPermission("groupOne");
		Assert.assertTrue(manager.isDuplicate(one, two));
		//
		one.setPermissions(IdmBasePermission.READ, IdmBasePermission.UPDATE, IdmBasePermission.DELETE);
		Assert.assertFalse(manager.isDuplicate(one, two));
		//
		two.setPermissions(IdmBasePermission.READ, IdmBasePermission.UPDATE);
		Assert.assertFalse(manager.isDuplicate(one, two));
		//
		two.setPermissions(IdmBasePermission.READ, IdmBasePermission.UPDATE, IdmBasePermission.DELETE);
		Assert.assertTrue(manager.isDuplicate(one, two));
		//
		two.setPermissions(IdmBasePermission.UPDATE, IdmBasePermission.DELETE, IdmBasePermission.READ);
		Assert.assertTrue(manager.isDuplicate(one, two));
		//
		ConfigurationMap propsOne = new ConfigurationMap();
		propsOne.put("one", "valueOne");
		propsOne.put("two", "valueTwo");
		one.setEvaluatorProperties(propsOne);
		Assert.assertFalse(manager.isDuplicate(one, two));
		//
		ConfigurationMap propsTwo = new ConfigurationMap();
		propsTwo.put("two", "valueTwo");
		propsTwo.put("one", "valueOneU");		
		two.setEvaluatorProperties(propsTwo);
		Assert.assertFalse(manager.isDuplicate(one, two));
		//
		propsTwo.put("one", "valueOne");		
		two.setEvaluatorProperties(propsTwo);
		Assert.assertTrue(manager.isDuplicate(one, two));
		//
		propsTwo.remove("one");
		two.setEvaluatorProperties(propsTwo);
		Assert.assertFalse(manager.isDuplicate(one, two));
	}
	
}
