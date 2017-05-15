package eu.bcvsolutions.idm.core.scheduler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.utils.AutowireHelper;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityService;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmLongRunningTaskDto;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmProcessedTaskItemDto;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmScheduledTaskDto;
import eu.bcvsolutions.idm.core.scheduler.api.service.SchedulableTaskExecutor;
import eu.bcvsolutions.idm.core.scheduler.service.api.IdmLongRunningTaskService;
import eu.bcvsolutions.idm.core.scheduler.service.api.IdmProcessedTaskItemService;
import eu.bcvsolutions.idm.core.scheduler.service.api.IdmScheduledTaskService;
import eu.bcvsolutions.idm.core.scheduler.service.impl.AbstractSchedulableStatefulExecutor;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;
import eu.bcvsolutions.idm.test.api.utils.SchedulerTestUtils;

/**
 * Stateful tasks test.
 * @author Jan Helbich
 *
 */
public class AbstractSchedulableStatefulExecutorIntegrationTest extends AbstractIntegrationTest {

	private TestIdenityIntegrationExecutor executor;
	
	@Autowired
	private IdmIdentityService identityService;
	
	@Autowired
	private IdmProcessedTaskItemService itemService;
	
	@Autowired
	private IdmScheduledTaskService scheduledTaskService;
	
	@Autowired
	private IdmLongRunningTaskService longRunningService;
	
	@Before
	public void init() {
		executor = new TestIdenityIntegrationExecutor();
		AutowireHelper.autowire(executor);
	}
	
	/**
	 * Tests the stateful execution method of the task by the following
	 * scenario:
	 *   1. find all identities to process
	 *   2. process retrieved identities
	 *   3. persist processing log
	 *   4. save processed identities into processed queue
	 * 
	 * Second execution run:
	 *   1. find all identities to process - same as first run
	 *   2. call process retrieved identities - all have been processed already
	 *   3. check processing log - nothing new was processed, therefore must be empty
	 *   4. check processed items queue did not change
	 *   
	 * Third run:
	 *   1. find all identities to process - returns empty list
	 *   2. check processing log - nothing was processed
	 *   3. check processed items queue - must be empty
	 * @throws Exception 
	 */
	@Test
	public void testExecute() throws Exception {
		// manually prepare control entities - normally scheduler will take care of it itself
		IdmScheduledTaskDto scheduledTask = createIdmScheduledTask(UUID.randomUUID().toString());
		IdmLongRunningTaskDto longRunningTask = createIdmLongRunningTask(scheduledTask, TestIdenityIntegrationExecutor.class);
		executor.setLongRunningTaskId(longRunningTask.getId());
		// first run
		List<IdmIdentityDto> itemsToProcess = getTestIdentities();
		// set executor data
		executor.dtos = itemsToProcess;
		//
		Boolean result = executor.process();
		Page<IdmProcessedTaskItemDto> queueItems = itemService.findQueueItems(scheduledTask, null);
		Page<IdmProcessedTaskItemDto> logItems = itemService.findLogItems(longRunningTask, null);
		//
		assertTrue(result);
		assertEquals(longRunningTask.getScheduledTask(), scheduledTask.getId());
		assertEquals(itemsToProcess.size(), queueItems.getTotalElements());
		assertEquals(itemsToProcess.size(), logItems.getTotalElements());
		SchedulerTestUtils.checkLogItems(longRunningTask, IdmIdentityDto.class, logItems);
		SchedulerTestUtils.checkQueueItems(scheduledTask, IdmIdentityDto.class, queueItems);
		//
		// second run
		//
		longRunningTask = createIdmLongRunningTask(scheduledTask, TestIdenityIntegrationExecutor.class);
		executor.setLongRunningTaskId(longRunningTask.getId());
		executor.dtos = itemsToProcess;
		//
		result = executor.process();
		queueItems = itemService.findQueueItems(scheduledTask, null);
		logItems = itemService.findLogItems(longRunningTask, null);
		//
		assertTrue(result);
		assertEquals(itemsToProcess.size(), queueItems.getTotalElements());
		assertEquals(0, logItems.getTotalElements());
		SchedulerTestUtils.checkQueueItems(scheduledTask, IdmIdentityDto.class, queueItems);
		//
		// third run
		//
		longRunningTask = createIdmLongRunningTask(scheduledTask, TestIdenityIntegrationExecutor.class);
		executor.setLongRunningTaskId(longRunningTask.getId());
		//
		result = executor.process();
		queueItems = itemService.findQueueItems(scheduledTask, null);
		logItems = itemService.findLogItems(longRunningTask, null);
		//
		assertTrue(result);
		assertEquals(0, queueItems.getTotalElements());
		assertEquals(0, logItems.getTotalElements());
	}

	public static class TestIdenityIntegrationExecutor extends AbstractSchedulableStatefulExecutor<IdmIdentityDto> {
		
		private List<IdmIdentityDto> dtos = null;
		
		@Override
		public Page<IdmIdentityDto> getItemsToProcess(Pageable pageable) {
			PageImpl<IdmIdentityDto> res = new PageImpl<>(dtos);
			dtos = new ArrayList<>();
			return res;
		}

		@Override
		public Optional<OperationResult> processItem(IdmIdentityDto dto) {
			return Optional.of(new OperationResult.Builder(OperationState.EXECUTED).build());
		}
		
	}
	
	private List<IdmIdentityDto> getTestIdentities() {
		List<IdmIdentityDto> dtos = new ArrayList<>();
		identityService.find(null).forEach(identity -> dtos.add(identity));
		return dtos;
	}

	private IdmScheduledTaskDto createIdmScheduledTask(String taskName) {
		return scheduledTaskService.save(SchedulerTestUtils.createIdmScheduledTask(taskName));
	}

	private IdmLongRunningTaskDto createIdmLongRunningTask(IdmScheduledTaskDto taskDto,
			Class<? extends SchedulableTaskExecutor<Boolean>> clazz) {
		return longRunningService.save(SchedulerTestUtils.createIdmLongRunningTask(taskDto, clazz));
	}
	
}
