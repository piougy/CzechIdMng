package eu.bcvsolutions.idm.core.workflow.hr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.spy;

import java.util.UUID;

import org.joda.time.LocalDate;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.utils.AutowireHelper;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleService;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmProcessedTaskItemDto;
import eu.bcvsolutions.idm.core.scheduler.task.impl.hr.HrEndContractProcess;
import eu.bcvsolutions.idm.test.api.utils.SchedulerTestUtils;

/**
 * Test for HR contract exclusion process.
 * @author Jan Helbich
 *
 */
public class HrEndContractProcessTest extends AbstractHrProcessTest<IdmIdentityContractDto> {
	
	@Autowired
	private IdmRoleService roleService;
	
	@Before
	public void init() {
		super.loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
		executor = new HrEndContractProcess();
		AutowireHelper.autowire(executor);
		executor = spy(executor);
		scheduledTask = createIdmScheduledTask(UUID.randomUUID().toString());
		lrt = createIdmLongRunningTask(scheduledTask, HrEndContractProcess.class);
		executor.setLongRunningTaskId(lrt.getId());
	}
	
	@After
	public void logout() {
		super.logout();
	}
	
	@Test
	public void testExclusion1() {
		IdmIdentityContractDto dto = prepareTestData1();
		assertEquals(false, identityService.get(dto.getIdentity()).isDisabled());
		assertNotEquals(0, identityRoleService.findAllByContract(dto.getId()).size());
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
		assertEquals(0, identityRoleService.findAllByContract(dto.getId()).size());
	}

	@Test
	public void testEnd2() {
		IdmIdentityContractDto dto = prepareTestData2();
		assertEquals(false, identityService.get(dto.getIdentity()).isDisabled());
		assertNotEquals(0, identityRoleService.findAllByContract(dto.getId()).size());
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
		assertEquals(0, identityRoleService.findAllByContract(dto.getId()).size());
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
		assertEquals(originalCount + 1, currentCount);
		//
		contract = getTestContract(createTestIdentity(UUID.randomUUID().toString()), false);
		contract.setValidTill(LocalDate.now().minusDays(1));
		contract = identityContractService.save(contract);
		//
		currentCount = executor.getItemsToProcess(null).getTotalElements();
		//
		assertEquals(originalCount + 2, currentCount);
		//
		contract = getTestContract(createTestIdentity(UUID.randomUUID().toString()), true);
		contract.setValidFrom(LocalDate.now().minusDays(100));
		contract.setValidTill(LocalDate.now().minusDays(1));
		contract = identityContractService.save(contract);
		//
		currentCount = executor.getItemsToProcess(null).getTotalElements();
		//
		assertEquals(originalCount + 3, currentCount);
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
		assertEquals(originalCount, currentCount);
		//
		contract.setValidTill(null);
		contract.setValidFrom(LocalDate.now().minusDays(10));
		contract = identityContractService.save(contract);
		//
		currentCount = executor.getItemsToProcess(null).getTotalElements();
		//
		assertEquals(originalCount, currentCount);
	}

	// only one non-default and disable contract -> must disable identity
	private IdmIdentityContractDto prepareTestData1() {
		IdmIdentityContractDto contract = getTestContract(createTestIdentity(UUID.randomUUID().toString(),false), true);
		contract.setValidTill(LocalDate.now().minusDays(1));
		contract = identityContractService.save(contract);
		addRolesToContract(contract);
		return contract;
	}

	private void addRolesToContract(IdmIdentityContractDto contract) {
		roleService.find(null).forEach(role -> {
			IdmIdentityRoleDto d = new IdmIdentityRoleDto();
			d.setRole(role.getId());
			d.setAutomaticRole(false);
			d.setIdentityContract(contract.getId());
			identityRoleService.save(d);
		});
	}
	
	// two non-default and contracts, one is valid-> must NOT disable identity
	private IdmIdentityContractDto prepareTestData2() {
		IdmIdentityDto identity = createTestIdentity(UUID.randomUUID().toString());
		createTestContract(identity, false);
		IdmIdentityContractDto contract = getTestContract(identity, false);
		contract.setValidTill(LocalDate.now().minusDays(1));
		contract = identityContractService.save(contract);
		addRolesToContract(contract);
		return contract;
	}

}
