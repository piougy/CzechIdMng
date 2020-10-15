package eu.bcvsolutions.idm.core.scheduler.task.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.domain.PriorityType;
import eu.bcvsolutions.idm.core.api.dto.IdmEntityEventDto;
import eu.bcvsolutions.idm.core.api.dto.OperationResultDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmEntityEventFilter;
import eu.bcvsolutions.idm.core.api.service.IdmEntityEventService;
import eu.bcvsolutions.idm.core.api.utils.AutowireHelper;
import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskManager;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * LRT integration test.
 * 
 * @author Radek Tomiška
 *
 */
public class DeleteExecutedEventTaskExecutorIntegrationTest extends AbstractIntegrationTest {
	
	@Autowired private IdmEntityEventService service;
	@Autowired private LongRunningTaskManager longRunningTaskManager;
	
	@Test
	public void testDeleteOldExecutedEvents() {
		// prepare provisioning operations
		ZonedDateTime createdOne = ZonedDateTime.now().minusDays(2);
		UUID ownerId = UUID.randomUUID();
		IdmEntityEventDto operationOne = createDto(ownerId, createdOne, OperationState.EXECUTED);
		// all other variants for not removal
		createDto(ownerId, LocalDate.now().atStartOfDay(ZoneId.systemDefault()).plusMinutes(1), OperationState.EXECUTED);
		createDto(ownerId, LocalDate.now().atStartOfDay(ZoneId.systemDefault()).plusMinutes(1), OperationState.CREATED);
		createDto(ownerId, LocalDate.now().atStartOfDay(ZoneId.systemDefault()).plusMinutes(1), OperationState.EXECUTED);
		createDto(ownerId, ZonedDateTime.now().minusDays(2), OperationState.EXCEPTION);
		createDto(ownerId, LocalDate.now().atStartOfDay(ZoneId.systemDefault()).minusHours(23), OperationState.EXECUTED);
		//
		Assert.assertEquals(createdOne, operationOne.getCreated());
		IdmEntityEventFilter filter = new IdmEntityEventFilter();
		filter.setOwnerId(ownerId);
		List<IdmEntityEventDto> events = service.find(filter, null).getContent();
		Assert.assertEquals(6, events.size());
		//
		DeleteExecutedEventTaskExecutor taskExecutor = new DeleteExecutedEventTaskExecutor();
		Map<String, Object> properties = new HashMap<>();
		properties.put(DeleteExecutedEventTaskExecutor.PARAMETER_NUMBER_OF_DAYS, 1);
		AutowireHelper.autowire(taskExecutor);
		taskExecutor.init(properties);
		//
		longRunningTaskManager.executeSync(taskExecutor);
		//
		events = service.find(filter, null).getContent();
		Assert.assertEquals(5, events.size());
		Assert.assertTrue(events.stream().allMatch(a -> !a.getId().equals(operationOne.getId())));		
	}
	
	private IdmEntityEventDto createDto(UUID ownerId, ZonedDateTime created, OperationState state) {
		IdmEntityEventDto dto = new IdmEntityEventDto();
		dto.setCreated(created);
		dto.setResult(new OperationResultDto(state));
		dto.setOwnerType("mock");
		dto.setOwnerId(ownerId);
		dto.setPriority(PriorityType.IMMEDIATE);
		dto.setInstanceId("mock");
		//
		return service.save(dto);
	}
}
