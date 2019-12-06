package eu.bcvsolutions.idm.core.security.evaluator.eav;

import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmCodeListDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.service.CodeListManager;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.eav.api.service.IdmFormAttributeService;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormAttribute;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Authorization policy evaluator test.
 * 
 * @author Radek Tomiška
 */
@Transactional
public class FormAttributteByCodeListEvaluatorIntegrationTest extends AbstractIntegrationTest {

	@Autowired private CodeListManager codeListManager;
	@Autowired private FormService formService;
	@Autowired private IdmFormAttributeService formAttributeService;
	
	@Test
	public void testPermissions() {
		// create codelist and items
		IdmIdentityDto identity = getHelper().createIdentity();
		IdmCodeListDto codeListOne = codeListManager.create(getHelper().createName());
		IdmFormAttributeDto attributeOne = new IdmFormAttributeDto();
		attributeOne.setCode(getHelper().createName());
		attributeOne.setName(attributeOne.getCode());
		attributeOne.setPersistentType(PersistentType.SHORTTEXT);
		attributeOne.setFormDefinition(codeListOne.getFormDefinition().getId());
		attributeOne = formService.saveAttribute(attributeOne);
		IdmCodeListDto codeListTwo = codeListManager.create(getHelper().createName());
		IdmFormAttributeDto attributeTwo = new IdmFormAttributeDto();
		attributeTwo.setCode(getHelper().createName());
		attributeTwo.setName(attributeTwo.getCode());
		attributeTwo.setPersistentType(PersistentType.SHORTTEXT);
		attributeTwo.setFormDefinition(codeListTwo.getFormDefinition().getId());
		formService.saveAttribute(attributeTwo);
		//
		List<IdmFormAttributeDto> attributes = null;
		IdmRoleDto roleOne = getHelper().createRole();
		//
		getHelper().createIdentityRole(identity, roleOne);
		//
		// check - read without policy
		try {			
			getHelper().login(identity.getUsername(), identity.getPassword());
			//
			attributes = formAttributeService.find(null, IdmBasePermission.READ).getContent();
			Assert.assertTrue(attributes.isEmpty());	
		} finally {
			logout();
		}
		//
		// without login
		attributes = formAttributeService.find(null, IdmBasePermission.READ).getContent();
		Assert.assertTrue(attributes.isEmpty());
		//
		// create authorization policies - assign to role
		getHelper().createUuidPolicy(roleOne.getId(), codeListOne.getId(), IdmBasePermission.READ);
		getHelper().createAuthorizationPolicy(
				roleOne.getId(),
				CoreGroupPermission.FORMATTRIBUTE,
				IdmFormAttribute.class,
				FormAttributteByCodeListEvaluator.class);
		//
		try {
			getHelper().login(identity.getUsername(), identity.getPassword());
			//
			// without update permission
			attributes = formAttributeService.find(null, IdmBasePermission.UPDATE).getContent();
			Assert.assertTrue(attributes.isEmpty());
			//
			// evaluate	access
			attributes = formAttributeService.find(null, IdmBasePermission.READ).getContent();
			Assert.assertEquals(1, attributes.size());	
			Assert.assertEquals(attributeOne.getId(), attributes.get(0).getId());
			//
			Set<String> permissions = formAttributeService.getPermissions(attributeOne);
			Assert.assertEquals(1, permissions.size());
			Assert.assertTrue(permissions.stream().anyMatch(p -> p.equals(IdmBasePermission.READ.name())));
		} finally {
			logout();
		}
		//
		getHelper().createUuidPolicy(roleOne.getId(), codeListOne.getId(), IdmBasePermission.UPDATE);
		//
		try {
			getHelper().login(identity.getUsername(), identity.getPassword());
			//
			Set<String> permissions = formAttributeService.getPermissions(attributeOne);
			Assert.assertEquals(2, permissions.size());
			Assert.assertTrue(permissions.stream().anyMatch(p -> p.equals(IdmBasePermission.READ.name())));
			Assert.assertTrue(permissions.stream().anyMatch(p -> p.equals(IdmBasePermission.UPDATE.name())));
		} finally {
			logout();
		}
	}

}
