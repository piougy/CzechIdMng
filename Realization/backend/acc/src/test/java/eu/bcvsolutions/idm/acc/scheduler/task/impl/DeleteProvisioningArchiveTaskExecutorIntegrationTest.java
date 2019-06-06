package eu.bcvsolutions.idm.acc.scheduler.task.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.acc.TestHelper;
import eu.bcvsolutions.idm.acc.domain.ProvisioningContext;
import eu.bcvsolutions.idm.acc.domain.ProvisioningEventType;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningArchiveDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysProvisioningOperationFilter;
import eu.bcvsolutions.idm.acc.scheduler.task.impl.DeleteProvisioningArchiveTaskExecutor;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningArchiveService;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.utils.AutowireHelper;
import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskManager;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * LRT integration test
 * 
 * @author Radek Tomiška
 *
 */
@Transactional
public class DeleteProvisioningArchiveTaskExecutorIntegrationTest extends AbstractIntegrationTest {
	
	@Autowired private SysProvisioningArchiveService service;
	@Autowired private LongRunningTaskManager longRunningTaskManager;
	
	@Test
	public void testDeleteOldProvisioningArchives() {
		// prepare provisioning operations
		SysSystemDto system = getHelper().createTestResourceSystem(false);
		SysSystemDto systemOther = getHelper().createTestResourceSystem(false);
		DateTime createdOne = DateTime.now().minusDays(2);
		SysProvisioningArchiveDto operationOne = createDto(system, createdOne, OperationState.EXECUTED);
		// all other variants for not removal
		createDto(system, DateTime.now().withTimeAtStartOfDay().plusMinutes(1), OperationState.EXECUTED);
		createDto(system, DateTime.now().withTimeAtStartOfDay().plusMinutes(1), OperationState.CREATED);
		createDto(system, DateTime.now().withTimeAtStartOfDay().plusMinutes(1), OperationState.EXECUTED);
		createDto(system, DateTime.now().minusDays(2), OperationState.EXCEPTION);
		createDto(system, DateTime.now().withTimeAtStartOfDay().minusHours(23), OperationState.EXECUTED);
		SysProvisioningArchiveDto operationOther = createDto(systemOther, DateTime.now().minusDays(2), OperationState.EXECUTED);
		//
		Assert.assertEquals(createdOne, operationOne.getCreated());
		SysProvisioningOperationFilter filter = new SysProvisioningOperationFilter();
		filter.setSystemId(system.getId());
		List<SysProvisioningArchiveDto> archives = service.find(filter, null).getContent();
		Assert.assertEquals(6, archives.size());
		//
		DeleteProvisioningArchiveTaskExecutor taskExecutor = new DeleteProvisioningArchiveTaskExecutor();
		Map<String, Object> properties = new HashMap<>();
		properties.put(DeleteProvisioningArchiveTaskExecutor.PARAMETER_NUMBER_OF_DAYS, 1);
		properties.put(DeleteProvisioningArchiveTaskExecutor.PARAMETER_OPERATION_STATE, OperationState.EXECUTED);
		properties.put(DeleteProvisioningArchiveTaskExecutor.PARAMETER_SYSTEM, system.getId());
		AutowireHelper.autowire(taskExecutor);
		taskExecutor.init(properties);
		//
		longRunningTaskManager.execute(taskExecutor);
		//
		archives = service.find(filter, null).getContent();
		Assert.assertEquals(5, archives.size());
		Assert.assertTrue(archives.stream().allMatch(a -> !a.getId().equals(operationOne.getId())));
		//
		filter.setSystemId(systemOther.getId());
		archives = service.find(filter, null).getContent();
		Assert.assertEquals(1, archives.size());
		Assert.assertTrue(archives.stream().allMatch(a -> a.getId().equals(operationOther.getId())));
		//
		taskExecutor = new DeleteProvisioningArchiveTaskExecutor();
		properties = new HashMap<>();
		properties.put(DeleteProvisioningArchiveTaskExecutor.PARAMETER_NUMBER_OF_DAYS, 1);
		properties.put(DeleteProvisioningArchiveTaskExecutor.PARAMETER_OPERATION_STATE, OperationState.EXECUTED);
		taskExecutor.init(properties);
		//
		filter.setSystemId(system.getId());
		longRunningTaskManager.execute(taskExecutor);
		archives = service.find(filter, null).getContent();
		Assert.assertEquals(5, archives.size());
		Assert.assertTrue(archives.stream().allMatch(a -> !a.getId().equals(operationOne.getId())));
		//
		filter.setSystemId(systemOther.getId());
		archives = service.find(filter, null).getContent();
		Assert.assertTrue(archives.isEmpty());
	}
	
	private SysProvisioningArchiveDto createDto(SysSystemDto system, DateTime created, OperationState state) {
		SysProvisioningArchiveDto dto = new SysProvisioningArchiveDto();
		dto.setCreated(created);
		dto.setSystem(system.getId());
		dto.setEntityIdentifier(UUID.randomUUID());
		dto.setOperationType(ProvisioningEventType.CANCEL);
		dto.setEntityType(SystemEntityType.CONTRACT);
		dto.setProvisioningContext(new ProvisioningContext());
		dto.setResult(new OperationResult(state));
		//
		return service.save(dto);
	}
	
	@Override
	protected TestHelper getHelper() {
		return (TestHelper) super.getHelper();
	}
}
