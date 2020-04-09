package eu.bcvsolutions.idm.core.bulk.action.impl.eav;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;


import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.filter.IdmFormDefinitionFilter;
import eu.bcvsolutions.idm.core.eav.api.service.IdmFormDefinitionService;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormDefinition;
import eu.bcvsolutions.idm.test.api.AbstractBulkActionTest;

/**
 * Delete form definition
 * - by id / filter
 *
 * 
 * @author Ondrej Husnik
 *
 */
public class FormDefinitionDeleteBulkActionIntegrationTest extends AbstractBulkActionTest {
	
	private final int TEST_COUNT = 10;
	
	@Autowired private IdmFormDefinitionService formDefService;
	
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
		String type = getHelper().createName();
		getHelper().createFormDefinition(type, true); // creates Main definition which won't be deleted
		List<IdmFormDefinitionDto> defDtos = new ArrayList<IdmFormDefinitionDto>();
		for (int i = 0; i < TEST_COUNT; ++i) {
			getHelper().createFormDefinition(type);
		}

		IdmFormDefinitionFilter filter = new IdmFormDefinitionFilter();
		filter.setType(type);
		filter.setMain(false);
		defDtos = formDefService.find(filter, null).getContent();

		Set<UUID> defIds = defDtos.stream().map(IdmFormDefinitionDto::getId).collect(Collectors.toSet());
		Assert.assertTrue(defIds.size() == TEST_COUNT);

		IdmBulkActionDto bulkAction = findBulkAction(IdmFormDefinition.class, FormDefinitionDeleteBulkAction.NAME);
		bulkAction.setIdentifiers(defIds);
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		checkResultLrt(processAction, new Long(TEST_COUNT), null, null);

		filter.setMain(null);
		List<IdmFormDefinitionDto> defsRemain = formDefService.find(filter, null).getContent();
		Assert.assertEquals(1, defsRemain.size());

		Set<UUID> result = defsRemain.stream().map(IdmFormDefinitionDto::getId).collect(Collectors.toSet());
		result.retainAll(defIds);
		Assert.assertTrue(result.isEmpty());
	}
	
	@Test
	public void processBulkActionByFilter() {
		String type = getHelper().createName();
		getHelper().createFormDefinition(type, true); // creates Main definition which won't be deleted
		List<IdmFormDefinitionDto> defDtos = new ArrayList<IdmFormDefinitionDto>();
		for (int i = 0; i < TEST_COUNT; ++i) {
			getHelper().createFormDefinition(type);
		}

		IdmFormDefinitionFilter filter = new IdmFormDefinitionFilter();
		filter.setType(type);
		filter.setMain(false);
		defDtos = formDefService.find(filter, null).getContent();

		Set<UUID> defIds = defDtos.stream().map(IdmFormDefinitionDto::getId).collect(Collectors.toSet());
		Assert.assertTrue(defIds.size() == TEST_COUNT);

		IdmBulkActionDto bulkAction = findBulkAction(IdmFormDefinition.class, FormDefinitionDeleteBulkAction.NAME);
		bulkAction.setTransformedFilter(filter);
		bulkAction.setFilter(toMap(filter));
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		checkResultLrt(processAction, new Long(TEST_COUNT), null, null);

		filter.setMain(null);
		List<IdmFormDefinitionDto> defsRemain = formDefService.find(filter, null).getContent();
		Assert.assertEquals(1, defsRemain.size());

		Set<UUID> result = defsRemain.stream().map(IdmFormDefinitionDto::getId).collect(Collectors.toSet());
		result.retainAll(defIds);
		Assert.assertTrue(result.isEmpty());
	}
}
