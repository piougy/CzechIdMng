package eu.bcvsolutions.idm.core.bulk.action.impl.event;

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
import eu.bcvsolutions.idm.core.api.domain.PriorityType;
import eu.bcvsolutions.idm.core.api.dto.IdmEntityEventDto;
import eu.bcvsolutions.idm.core.api.dto.OperationResultDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmEntityEventFilter;
import eu.bcvsolutions.idm.core.api.service.IdmEntityEventService;
import eu.bcvsolutions.idm.core.model.entity.IdmEntityEvent;
import eu.bcvsolutions.idm.test.api.AbstractBulkActionTest;

/**
 * Delete entity events from queue integration test
 * 
 * @author Radek Tomiška
 *
 */
public class EntityEventDeleteBulkActionIntegrationTest extends AbstractBulkActionTest {

	@Autowired private IdmEntityEventService service;
	
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
		List<IdmEntityEventDto> events = createEvents(5);
		
		IdmBulkActionDto bulkAction = findBulkAction(IdmEntityEvent.class, EntityEventDeleteBulkAction.NAME);
		
		Set<UUID> ids = this.getIdFromList(events);
		bulkAction.setIdentifiers(ids);
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		
		checkResultLrt(processAction, 5l, null, null);
		
		for (UUID id : ids) {
			Assert.assertNull(service.get(id));
		}
	}
	
	@Test
	public void processBulkActionByFilter() {
		List<IdmEntityEventDto> events = createEvents(5);
		
		IdmEntityEventFilter filter = new IdmEntityEventFilter();
		filter.setOwnerId(events.get(2).getOwnerId());

		List<IdmEntityEventDto> checkEvents = service.find(filter, null).getContent();
		Assert.assertEquals(1, checkEvents.size());

		IdmBulkActionDto bulkAction = findBulkAction(IdmEntityEvent.class, EntityEventDeleteBulkAction.NAME);
		bulkAction.setTransformedFilter(filter);
		bulkAction.setFilter(toMap(filter));
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		checkResultLrt(processAction, 1l, null, null);
	
		Assert.assertNull(service.get(events.get(2)));
		Assert.assertNotNull(service.get(events.get(1)));
		Assert.assertNotNull(service.get(events.get(3)));
	}
	
	private List<IdmEntityEventDto> createEvents(int count) {
		List<IdmEntityEventDto> results = new ArrayList<>();
		
		for (int i = 0; i < count; i++) {
			IdmEntityEventDto dto = new IdmEntityEventDto();
			dto.setOwnerId(UUID.randomUUID());
			dto.setOwnerType("mock");
			dto.setInstanceId("mock");
			dto.setPriority(PriorityType.NORMAL);
			dto.setResult(new OperationResultDto(OperationState.CREATED));
			results.add(service.save(dto));
		}
		
		return results;
	}
}
