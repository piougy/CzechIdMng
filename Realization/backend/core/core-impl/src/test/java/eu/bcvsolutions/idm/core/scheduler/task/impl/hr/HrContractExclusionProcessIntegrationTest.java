package eu.bcvsolutions.idm.core.scheduler.task.impl.hr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.spy;

import java.util.UUID;

import org.joda.time.LocalDate;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.data.domain.Page;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.utils.AutowireHelper;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmProcessedTaskItemDto;
import eu.bcvsolutions.idm.test.api.utils.SchedulerTestUtils;

/**
 * Test for HR contract exclusion process.
 * @author Jan Helbich
 *
 */
public class HrContractExclusionProcessIntegrationTest extends AbstractHrProcessIntegrationTest<IdmIdentityContractDto> {
	
	@Before
	public void init() {
		super.before();
		executor = new HrContractExclusionProcess();
		AutowireHelper.autowire(executor);
		executor = spy(executor);
		scheduledTask = createIdmScheduledTask(UUID.randomUUID().toString());
		lrt = createIdmLongRunningTask(scheduledTask, HrContractExclusionProcess.class);
		executor.setLongRunningTaskId(lrt.getId());
	}

	@After
	public void logout() {
		super.after();
	}
	
	/**
	 * One contract to be disabled with manually added roles attached.
	 * The end of contract process must disable the identity and remove
	 * added roles.
	 */
	@Test
	public void testEnd1() {
		IdmIdentityContractDto dto = prepareTestData1();
		assertEquals(false, identityService.get(dto.getIdentity()).isDisabled());
		//
		process(lrt, dto);
		//
		Page<IdmProcessedTaskItemDto> queueItems = itemService.findQueueItems(scheduledTask, null);
		Page<IdmProcessedTaskItemDto> logItems = itemService.findLogItems(lrt, null);
		//
		assertEquals(true, identityService.get(dto.getIdentity()).isDisabled());
		assertEquals(1, queueItems.getTotalElements());
		assertEquals(1, logItems.getTotalElements());
		SchedulerTestUtils.checkLogItems(lrt, IdmIdentityContractDto.class, logItems);
		SchedulerTestUtils.checkQueueItems(scheduledTask, IdmIdentityContractDto.class, queueItems);
		identityRoleService.findAllByContract(dto.getId()).forEach(r -> assertTrue(r.isAutomaticRole()));
	}

	@Test
	public void testExclusion2() {
		IdmIdentityContractDto dto = prepareTestData2();
		assertEquals(false, identityService.get(dto.getIdentity()).isDisabled());
		//
		process(lrt, dto);
		//
		Page<IdmProcessedTaskItemDto> queueItems = itemService.findQueueItems(scheduledTask, null);
		Page<IdmProcessedTaskItemDto> logItems = itemService.findLogItems(lrt, null);
		//
		assertEquals(false, identityService.get(dto.getIdentity()).isDisabled());
		assertEquals(1, queueItems.getTotalElements());
		assertEquals(1, logItems.getTotalElements());
		SchedulerTestUtils.checkLogItems(lrt, IdmIdentityContractDto.class, logItems);
		SchedulerTestUtils.checkQueueItems(scheduledTask, IdmIdentityContractDto.class, queueItems);
		identityRoleService.findAllByContract(dto.getId()).forEach(r -> assertTrue(r.isAutomaticRole()));
	}
	
	@Test
	public void testExclusionWithProcessorsEnabled() {
		enableAllProcessors();
		//
		IdmIdentityContractDto dto = prepareTestData2();
		assertEquals(false, identityService.get(dto.getIdentity()).isDisabled());
		//
		process(lrt, dto);
		//
		Page<IdmProcessedTaskItemDto> queueItems = itemService.findQueueItems(scheduledTask, null);
		Page<IdmProcessedTaskItemDto> logItems = itemService.findLogItems(lrt, null);
		//
		assertEquals(false, identityService.get(dto.getIdentity()).isDisabled());
		assertEquals(1, queueItems.getTotalElements());
		assertEquals(1, logItems.getTotalElements());
		SchedulerTestUtils.checkLogItems(lrt, IdmIdentityContractDto.class, logItems);
		SchedulerTestUtils.checkQueueItems(scheduledTask, IdmIdentityContractDto.class, queueItems);
		identityRoleService.findAllByContract(dto.getId()).forEach(r -> assertTrue(r.isAutomaticRole()));
	}
	
	@Test
	public void testExclusion3() {
		IdmIdentityContractDto dto = prepareTestData3();
		assertEquals(false, identityService.get(dto.getIdentity()).isDisabled());
		//
		process(lrt, dto);
		//
		Page<IdmProcessedTaskItemDto> queueItems = itemService.findQueueItems(scheduledTask, null);
		Page<IdmProcessedTaskItemDto> logItems = itemService.findLogItems(lrt, null);
		//
		assertEquals(true, identityService.get(dto.getIdentity()).isDisabled());
		assertEquals(1, queueItems.getTotalElements());
		assertEquals(1, logItems.getTotalElements());
		SchedulerTestUtils.checkLogItems(lrt, IdmIdentityContractDto.class, logItems);
		SchedulerTestUtils.checkQueueItems(scheduledTask, IdmIdentityContractDto.class, queueItems);
	}
	
	@Test
	public void testGetItemsToProcessIgnoreNonValidContracts() {
		final long originalCount = executor.getItemsToProcess(null).getTotalElements();
		//
		IdmIdentityContractDto contract = getTestContract(createTestIdentity(UUID.randomUUID().toString()), true);
		contract.setValidFrom(LocalDate.now().plusDays(1));
		contract = identityContractService.save(contract);
		//
		long currentCount = executor.getItemsToProcess(null).getTotalElements();
		//
		assertEquals(originalCount, currentCount);
		//
		contract = getTestContract(createTestIdentity(UUID.randomUUID().toString()), true);
		contract.setValidTill(LocalDate.now().minusDays(1));
		contract = identityContractService.save(contract);
		//
		currentCount = executor.getItemsToProcess(null).getTotalElements();
		//
		assertEquals(originalCount, currentCount);
		//
		contract = getTestContract(createTestIdentity(UUID.randomUUID().toString()), true);
		contract.setValidFrom(LocalDate.now().minusDays(100));
		contract.setValidTill(LocalDate.now().minusDays(1));
		contract = identityContractService.save(contract);
		//
		currentCount = executor.getItemsToProcess(null).getTotalElements();
		//
		assertEquals(originalCount, currentCount);
	}
	
	@Test
	public void testGetItemsToProcessAddValidContracts() {
		final long originalCount = executor.getItemsToProcess(null).getTotalElements();
		//
		IdmIdentityContractDto contract = getTestContract(createTestIdentity(UUID.randomUUID().toString()), true);
		contract.setValidTill(LocalDate.now().plusDays(1));
		contract = identityContractService.save(contract);
		//
		long currentCount = executor.getItemsToProcess(null).getTotalElements();
		//
		assertEquals(originalCount + 1, currentCount);
		//
		contract.setValidTill(null);
		contract.setValidFrom(LocalDate.now().minusDays(10));
		contract = identityContractService.save(contract);
		//
		currentCount = executor.getItemsToProcess(null).getTotalElements();
		//
		assertEquals(originalCount + 1, currentCount);
	}

	// only one non-default and disable contract -> must disable identity
	private IdmIdentityContractDto prepareTestData1() {
		return createTestContract(createTestIdentity(UUID.randomUUID().toString()), true);
	}
	
	// two non-default and contracts, one is valid-> must NOT disable identity
	private IdmIdentityContractDto prepareTestData2() {
		IdmIdentityDto identity = createTestIdentity(UUID.randomUUID().toString());
		createTestContract(identity, false);
		return createTestContract(identity, true);
	}
	
	// two non-default and contracts, one is valid-> must NOT disable identity
	private IdmIdentityContractDto prepareTestData3() {
		IdmIdentityDto identity = createTestIdentity(UUID.randomUUID().toString());
		createTestContract(identity, true);
		return createTestContract(identity, true);
	}

}
