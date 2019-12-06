package eu.bcvsolutions.idm.core.scheduler.bulk.impl;

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
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmLongRunningTaskDto;
import eu.bcvsolutions.idm.core.scheduler.api.dto.filter.IdmLongRunningTaskFilter;
import eu.bcvsolutions.idm.core.scheduler.api.service.IdmLongRunningTaskService;
import eu.bcvsolutions.idm.core.scheduler.entity.IdmLongRunningTask;
import eu.bcvsolutions.idm.test.api.AbstractBulkActionTest;

/**
 * Delete entity events from queue integration test.
 * 
 * @author Radek Tomiška
 *
 */
public class LongRunningTaskDeleteBulkActionIntegrationTest extends AbstractBulkActionTest {

	@Autowired private IdmLongRunningTaskService service;
	
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
		List<IdmLongRunningTaskDto> tasks = createTasks(5);
		
		IdmBulkActionDto bulkAction = findBulkAction(IdmLongRunningTask.class, LongRunningTaskDeleteBulkAction.NAME);
		
		Set<UUID> ids = this.getIdFromList(tasks);
		bulkAction.setIdentifiers(ids);
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		
		checkResultLrt(processAction, 5l, null, null);
		
		for (UUID id : ids) {
			Assert.assertNull(service.get(id));
		}
	}
	
	@Test
	public void processBulkActionByFilter() {
		List<IdmLongRunningTaskDto> tasks = createTasks(5);
		
		IdmLongRunningTaskFilter filter = new IdmLongRunningTaskFilter();
		filter.setId(tasks.get(2).getId());

		List<IdmLongRunningTaskDto> checkEvents = service.find(filter, null).getContent();
		Assert.assertEquals(1, checkEvents.size());

		IdmBulkActionDto bulkAction = findBulkAction(IdmLongRunningTask.class, LongRunningTaskDeleteBulkAction.NAME);
		bulkAction.setTransformedFilter(filter);
		bulkAction.setFilter(toMap(filter));
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		checkResultLrt(processAction, 1l, null, null);
	
		Assert.assertNull(service.get(tasks.get(2)));
		Assert.assertNotNull(service.get(tasks.get(1)));
		Assert.assertNotNull(service.get(tasks.get(3)));
	}
	
	private List<IdmLongRunningTaskDto> createTasks(int count) {
		List<IdmLongRunningTaskDto> results = new ArrayList<>();
		
		for (int i = 0; i < count; i++) {
			IdmLongRunningTaskDto dto = new IdmLongRunningTaskDto();
			dto.setTaskType("mock");
			dto.setInstanceId("mock");
			dto.setResult(new OperationResult(OperationState.BLOCKED));
			//
			results.add(service.save(dto));
		}
		
		return results;
	}
}
