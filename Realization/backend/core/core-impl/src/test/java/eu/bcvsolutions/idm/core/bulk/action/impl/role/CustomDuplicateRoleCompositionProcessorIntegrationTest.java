package eu.bcvsolutions.idm.core.bulk.action.impl.role;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Sets;

import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleCompositionDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.service.IdmRoleCompositionService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.bulk.action.impl.role.RoleDuplicateBulkAction;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleComposition_;
import eu.bcvsolutions.idm.core.model.event.processor.role.DuplicateRoleCompositionProcessor;
import eu.bcvsolutions.idm.test.api.AbstractBulkActionTest;

/**
 * Test project specific duplication of role composition.
 * 
 * @author Radek Tomiška
 *
 */
public class CustomDuplicateRoleCompositionProcessorIntegrationTest extends AbstractBulkActionTest {

	@Autowired private IdmRoleService roleService;;
	@Autowired private IdmRoleCompositionService roleCompositionService;
	
	@Before
	public void login() {
		loginAsAdmin();
		// switch usage of custom processor
		getHelper().disableProcessor(DuplicateRoleCompositionProcessor.PROCESSOR_NAME);
		getHelper().enableProcessor(CustomDuplicateRoleCompositionProcessor.PROCESSOR_NAME);
	}
	
	@After
	public void logout() {
		getHelper().disableProcessor(CustomDuplicateRoleCompositionProcessor.PROCESSOR_NAME);
		getHelper().enableProcessor(DuplicateRoleCompositionProcessor.PROCESSOR_NAME);
		//
		super.logout();
	}
	
	/**
	 * Sub roles has to have the same environment, 
	 * Application sub role is not duplicated automatically - has to exist on the target environment
	 */
	@Test
	public void testDuplicateRole() {
		String environment = getHelper().createName();
		String otherEnvironment = getHelper().createName();
		String targetEnvironment = getHelper().createName();
		IdmRoleDto role = getHelper().createRole(null, null, environment);
		IdmRoleDto roleSubOne = getHelper().createRole(null, null, environment);
		IdmRoleDto otherRoleSub = getHelper().createRole(null, null, otherEnvironment);
		IdmRoleDto roleSubTwo = getHelper().createRole(null, null, environment);
		IdmRoleDto roleSubOneSubOne = getHelper().createRole(null, null, environment);
		IdmRoleDto roleSubOneSubTwo = getHelper().createRole(null, null, environment);
		getHelper().createRoleComposition(role, roleSubOne);
		getHelper().createRoleComposition(role, otherRoleSub);
		getHelper().createRoleComposition(role, roleSubTwo);
		getHelper().createRoleComposition(roleSubOne, roleSubOneSubOne);
		getHelper().createRoleComposition(roleSubOne, roleSubOneSubTwo);
		// prepare some of application role on the target environmant
		IdmRoleDto targetRoleSubOneSubOne = getHelper().createRole(null, roleSubOneSubOne.getBaseCode(), targetEnvironment);
		
		IdmBulkActionDto bulkAction = findBulkAction(IdmRole.class, RoleDuplicateBulkAction.NAME);
		bulkAction.setIdentifiers(Sets.newHashSet(role.getId()));
		bulkAction.getProperties().put(RoleDuplicateBulkAction.PROPERTY_ENVIRONMENT, targetEnvironment);
		bulkAction.getProperties().put(DuplicateRoleCompositionProcessor.PARAMETER_INCLUDE_ROLE_COMPOSITION, true);
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		//
		checkResultLrt(processAction, 1l, null, null);
		//
		IdmRoleDto duplicate = roleService.getByBaseCodeAndEnvironment(role.getBaseCode(), targetEnvironment);
		List<IdmRoleCompositionDto> directSubRoles = roleCompositionService.findDirectSubRoles(duplicate.getId());
		Assert.assertEquals(1, directSubRoles.size());
		IdmRoleDto subRole = DtoUtils.getEmbedded(directSubRoles.get(0), IdmRoleComposition_.sub);
		Assert.assertEquals(roleSubOne.getBaseCode(), subRole.getBaseCode());
		directSubRoles = roleCompositionService.findDirectSubRoles(subRole.getId());
		Assert.assertEquals(1, directSubRoles.size());
		Assert.assertEquals(targetRoleSubOneSubOne.getId(), directSubRoles.get(0).getSub());
	}	
}
