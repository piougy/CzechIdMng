package eu.bcvsolutions.idm.core.eav.service.impl;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
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

public class IdmFormValueControllerRestTest extends AbstractRestTest {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(IdmFormValueControllerRestTest.class);

	@Autowired
	private IdmIdentityService identityService;
	@Autowired
	private FormService formService;
	@Autowired
	private ApplicationContext context;

	private Authentication getAuthentication() {
		return new IdmJwtAuthentication(identityService.getByUsername(TestHelper.ADMIN_USERNAME), null, Lists.newArrayList(IdmAuthorityUtils.getAdminAuthority()), "core");
	}

	@Before
	public void init() {
		formService = context.getAutowireCapableBeanFactory().createBean(DefaultFormService.class);
		getHelper().loginAdmin();
	}

	@After
	public void logout() {
		super.logout();
	}

	@Test
	public void findValues() throws Exception {
		IdmIdentityDto ownerIdentity = getHelper().createIdentity((GuardedString) null);
		IdmRoleDto ownerRole = getHelper().createRole();
		IdmTreeNodeDto ownerTreeNode = getHelper().createTreeNode();
		IdmIdentityContractDto ownerIdentityContract = getHelper().createIdentityContact(ownerIdentity);

		Assert.assertEquals(1, prepareDataAndSearch(IdmIdentity.class, ownerIdentity));
		Assert.assertEquals(1, prepareDataAndSearch(IdmRole.class, ownerRole));
		Assert.assertEquals(1, prepareDataAndSearch(IdmTreeNode.class, ownerTreeNode));
		Assert.assertEquals(1, prepareDataAndSearch(IdmIdentityContract.class, ownerIdentityContract));
	}

	private int prepareDataAndSearch(Class<? extends AbstractEntity> type, AbstractDto owner) throws Exception {
		//create attribute
		IdmFormAttributeDto attribute = new IdmFormAttributeDto();
		String attributeName = "name_" + System.currentTimeMillis();
		attribute.setCode(attributeName);
		attribute.setName(attribute.getCode());
		attribute.setPersistentType(PersistentType.SHORTTEXT);

		//create definition
		IdmFormDefinitionDto definition = formService.createDefinition(type, getHelper().createName(), Lists.newArrayList(attribute));
		attribute = definition.getMappedAttributeByCode(attribute.getCode());

		//save value
		formService.saveValues(owner.getId(), type, attribute, Lists.newArrayList("one"));
		String response = getJsonAsString("/form-values", "definitionId=" + definition.getId(), 20l, 0l, null, null, getAuthentication());
		return getEmbeddedList("formValues", response).size();
	}
}
