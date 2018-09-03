package eu.bcvsolutions.idm.core.eav.service.impl;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeNodeDto;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.security.api.domain.IdmJwtAuthentication;
import eu.bcvsolutions.idm.core.security.api.utils.IdmAuthorityUtils;
import eu.bcvsolutions.idm.test.api.AbstractRestTest;
import eu.bcvsolutions.idm.test.api.TestHelper;

/**
 * For values agenda tests 
 * 
 * @author Roman Kučera
 * @author Radek Tomiška
 */
public class IdmFormValueControllerRestTest extends AbstractRestTest {

	@Autowired private IdmIdentityService identityService;
	@Autowired private FormService formService;

	private Authentication getAuthentication() {
		return new IdmJwtAuthentication(identityService.getByUsername(TestHelper.ADMIN_USERNAME), null, Lists.newArrayList(IdmAuthorityUtils.getAdminAuthority()), "core");
	}

	@Test
	public void findValues() throws Exception {
		IdmIdentityDto ownerIdentity = getHelper().createIdentity((GuardedString) null);
		IdmRoleDto ownerRole = getHelper().createRole();
		IdmIdentityContractDto ownerIdentityContract = getHelper().createIdentityContact(ownerIdentity);
		IdmTreeNodeDto ownerTreeNode = null;
		try {
			getHelper().loginAdmin();
			ownerTreeNode = getHelper().createTreeNode();
		} finally {
			logout();
		}
		//
		Assert.assertEquals(1, prepareDataAndSearch(IdmIdentity.class, ownerIdentity));
		Assert.assertEquals(1, prepareDataAndSearch(IdmRole.class, ownerRole));
		Assert.assertEquals(1, prepareDataAndSearch(IdmTreeNode.class, ownerTreeNode));
		Assert.assertEquals(1, prepareDataAndSearch(IdmIdentityContract.class, ownerIdentityContract));
	}

	private int prepareDataAndSearch(Class<? extends AbstractEntity> type, AbstractDto owner) throws Exception {
		// create attribute
		IdmFormAttributeDto attribute = new IdmFormAttributeDto();
		String attributeName = "name_" + System.currentTimeMillis();
		attribute.setCode(attributeName);
		attribute.setName(attribute.getCode());
		attribute.setPersistentType(PersistentType.SHORTTEXT);

		// create definition
		IdmFormDefinitionDto definition = formService.createDefinition(type, getHelper().createName(), Lists.newArrayList(attribute));
		attribute = definition.getMappedAttributeByCode(attribute.getCode());

		// save value
		formService.saveValues(owner.getId(), type, attribute, Lists.newArrayList("one"));
		String response = getJsonAsString("/form-values", "definitionId=" + definition.getId(), 20l, 0l, null, null, getAuthentication());
		return getEmbeddedList("formValues", response).size();
	}
}
