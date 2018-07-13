package eu.bcvsolutions.idm.core.security.evaluator.impl.identity;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.domain.ConfigurationMap;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormDefinition;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.security.evaluator.eav.FormDefinitionUuidEvaluator;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Share definition by uuid
 * 
 * @author Radek Tomiška
 *
 */
public class FormDefinitionUuidEvaluatorIntegrationTest extends AbstractIntegrationTest {

	@Autowired private FormService formService;
	
	@Test
	public void testAutocompleteFormDefinition() {
		IdmIdentityDto identity = getHelper().createIdentity();
		//
		// check created identity doesn't hava form definitions available
		try {			
			getHelper().login(identity.getUsername(), identity.getPassword());
			List<IdmFormDefinitionDto> definitions = formService.getDefinitions(IdmIdentityDto.class, IdmBasePermission.AUTOCOMPLETE);
			Assert.assertTrue(definitions.isEmpty());			
		} finally {
			logout();
		}
		// 
		IdmFormDefinitionDto definition = formService.getDefinition(IdmIdentityDto.class);
		IdmRoleDto role = getHelper().createRole();
		ConfigurationMap properties = new ConfigurationMap();
		properties.put(FormDefinitionUuidEvaluator.PARAMETER_UUID, definition.getId());
		getHelper().createAuthorizationPolicy(
				role.getId(),
				CoreGroupPermission.FORMDEFINITION,
				IdmFormDefinition.class,
				FormDefinitionUuidEvaluator.class,
				properties,
				IdmBasePermission.AUTOCOMPLETE);
		//
		// assign role
		getHelper().createIdentityRole(identity, role);
		//
		try {
			getHelper().login(identity.getUsername(), identity.getPassword());
			//
			// evaluate	access
			getHelper().login(identity.getUsername(), identity.getPassword());
			List<IdmFormDefinitionDto> definitions = formService.getDefinitions(IdmIdentityDto.class, IdmBasePermission.AUTOCOMPLETE);
			Assert.assertEquals(1, definitions.size());	
			Assert.assertTrue(definitions.stream().anyMatch(d -> d.getId().equals(definition.getId())));
		} finally {
			logout();
		}
	}
	
}
