package eu.bcvsolutions.idm.core.workflow.hr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.spy;

import java.util.UUID;

import org.joda.time.LocalDate;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.data.domain.Page;

import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.utils.AutowireHelper;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmProcessedTaskItemDto;
import eu.bcvsolutions.idm.core.scheduler.task.impl.hr.HrEnableContractProcess;
import eu.bcvsolutions.idm.test.api.utils.SchedulerTestUtils;

/**
 * Test for HR enable contract process.
 * @author Jan Helbich
 *
 */
public class HrEnableContractProcessTest extends AbstractHrProcessTest<IdmIdentityContractDto> {
	
	@Before
	public void init() {
		super.loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
		executor = new HrEnableContractProcess();
		AutowireHelper.autowire(executor);
		executor = spy(executor); // because we want to stub getItemsToProcess
		scheduledTask = createIdmScheduledTask(UUID.randomUUID().toString());
		lrt = createIdmLongRunningTask(scheduledTask, HrEnableContractProcess.class);
		executor.setLongRunningTaskId(lrt.getId());
	}
	
	@After
	public void logout() {
		super.logout();
	}
	
	/**
	 * Disabled identity, one contract gets enabled => must enable identity.
	 */
	@Test
	public void testEnable1() {
		IdmIdentityContractDto dto = prepareTestData1();
		assertEquals(true, identityService.get(dto.getIdentity()).isDisabled());
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
	}

	/**
	 * Disabled identity, two contracts, one gets enabled => must enable identity.
	 */
	@Test
	public void testEnable2() {
		IdmIdentityContractDto dto = prepareTestData2();
		assertEquals(true, identityService.get(dto.getIdentity()).isDisabled());
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
	}
	
	@Test
	public void testGetItemsToProcessIgnoreNonValidContracts() {
		final long originalCount = executor.getItemsToProcess(null).getTotalElements();
		//
		IdmIdentityContractDto contract = getTestContract(createTestIdentity(UUID.randomUUID().toString()), false);
		contract.setValidFrom(LocalDate.now().plusDays(1));
		contract = identityContractService.save(contract);
		assertFalse(contract.isValid());
		//
		long currentCount = executor.getItemsToProcess(null).getTotalElements();
		//
		assertEquals(originalCount, currentCount);
		//
		contract = getTestContract(createTestIdentity(UUID.randomUUID().toString()), false);
		contract.setValidTill(LocalDate.now().minusDays(1));
		contract = identityContractService.save(contract);
		assertFalse(contract.isValid());
		//
		currentCount = executor.getItemsToProcess(null).getTotalElements();
		//
		assertEquals(originalCount, currentCount);
		//
		contract = getTestContract(createTestIdentity(UUID.randomUUID().toString()), false);
		contract.setValidFrom(LocalDate.now().minusDays(100));
		contract.setValidTill(LocalDate.now().minusDays(1));
		contract = identityContractService.save(contract);
		assertFalse(contract.isValid());
		//
		currentCount = executor.getItemsToProcess(null).getTotalElements();
		//
		assertEquals(originalCount, currentCount);
	}
	
	@Test
	public void testGetItemsToProcessAddValidContracts() {
		final long originalCount = executor.getItemsToProcess(null).getTotalElements();
		//
		IdmIdentityContractDto contract = getTestContract(createTestIdentity(UUID.randomUUID().toString()), false);
		contract.setValidTill(LocalDate.now().plusDays(1));
		contract = identityContractService.save(contract);
		assertTrue(contract.isValid());
		//
		long currentCount = executor.getItemsToProcess(null).getTotalElements();
		//
		assertEquals(originalCount + 1, currentCount);
		//
		contract.setValidTill(null);
		contract.setValidFrom(LocalDate.now().minusDays(10));
		contract = identityContractService.save(contract);
		assertTrue(contract.isValid());
		//
		currentCount = executor.getItemsToProcess(null).getTotalElements();
		//
		assertEquals(originalCount + 1, currentCount);
	}

	private IdmIdentityContractDto prepareTestData1() {
		return createTestContract(createTestIdentity(UUID.randomUUID().toString(), true), false);
	}
	
	private IdmIdentityContractDto prepareTestData2() {
		IdmIdentityDto identity = createTestIdentity(UUID.randomUUID().toString(), true);
		createTestContract(identity, true);
		return createTestContract(identity, false);
	}
	
}
