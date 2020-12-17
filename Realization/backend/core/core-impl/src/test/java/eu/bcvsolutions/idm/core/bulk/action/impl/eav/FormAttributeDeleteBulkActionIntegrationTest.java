package eu.bcvsolutions.idm.core.bulk.action.impl.eav;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.bulk.action.impl.policy.AuthorizationPolicyDeleteBulkAction;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.filter.IdmFormAttributeFilter;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.eav.api.service.IdmFormAttributeService;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormAttribute;
import eu.bcvsolutions.idm.core.model.entity.IdmAuthorizationPolicy;
import eu.bcvsolutions.idm.test.api.AbstractBulkActionTest;

/**
 * Delete form attributes integration test.
 * 
 * @author Radek Tomiška
 *
 */
public class FormAttributeDeleteBulkActionIntegrationTest extends AbstractBulkActionTest {

	@Autowired private IdmFormAttributeService service;
	@Autowired private FormService formService;
	
	@Before
	public void login() {
		loginAsAdmin();
	}
	
	@After
	public void logout() {
		super.logout();
	}
	
	@Test
	public void processBulkActionByIds() {
		List<IdmFormAttributeDto> attributes = createAttributes(5);
		
		IdmBulkActionDto bulkAction = findBulkAction(IdmFormAttribute.class, FormAttributeDeleteBulkAction.NAME);
		
		Set<UUID> ids = this.getIdFromList(attributes);
		bulkAction.setIdentifiers(ids);
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		
		checkResultLrt(processAction, 5l, null, null);
		
		for (UUID id : ids) {
			Assert.assertNull(service.get(id));
		}
	}
	
	@Test
	public void processBulkActionByFilter() {
		List<IdmFormAttributeDto> attributes = createAttributes(5);
		
		IdmFormAttributeFilter filter = new IdmFormAttributeFilter();
		filter.setId(attributes.get(2).getId());

		List<IdmFormAttributeDto> checkAttributes = service.find(filter, null).getContent();
		Assert.assertEquals(1, checkAttributes.size());

		IdmBulkActionDto bulkAction = findBulkAction(IdmFormAttribute.class, FormAttributeDeleteBulkAction.NAME);
		bulkAction.setTransformedFilter(filter);
		bulkAction.setFilter(toMap(filter));
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		checkResultLrt(processAction, 1l, null, null);
	
		Assert.assertNull(service.get(attributes.get(2)));
		Assert.assertNotNull(service.get(attributes.get(1)));
		Assert.assertNotNull(service.get(attributes.get(3)));
	}
	
	private List<IdmFormAttributeDto> createAttributes(int count) {
		List<IdmFormAttributeDto> results = new ArrayList<>();
		IdmFormDefinitionDto formDefinition = formService.createDefinition(IdmRoleDto.class, getHelper().createName(), null);
		//
		for (int i = 0; i < count; i++) {
			IdmFormAttributeDto attribute = new IdmFormAttributeDto(getHelper().createName());
			attribute.setFormDefinition(formDefinition.getId());
			results.add(service.save(attribute));
		}
		//
		return results;
	}
}
