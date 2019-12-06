package eu.bcvsolutions.idm.acc.scheduler.task.impl;

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

import eu.bcvsolutions.idm.acc.TestHelper;
import eu.bcvsolutions.idm.acc.domain.ProvisioningContext;
import eu.bcvsolutions.idm.acc.domain.ProvisioningEventType;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningArchiveDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysProvisioningOperationFilter;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningAttribute;
import eu.bcvsolutions.idm.acc.repository.SysProvisioningAttributeRepository;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningArchiveService;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.utils.AutowireHelper;
import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskManager;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * LRT integration test.
 * 
 * @author Radek Tomiška
 *
 */
public class DeleteProvisioningArchiveTaskExecutorIntegrationTest extends AbstractIntegrationTest {
	
	@Autowired private SysProvisioningArchiveService service;
	@Autowired private LongRunningTaskManager longRunningTaskManager;
	@Autowired private SysProvisioningAttributeRepository provisioningAttributeRepository;
	
	@Test
	public void testDeleteOldProvisioningArchives() {
		// prepare provisioning operations
		SysSystemDto system = getHelper().createTestResourceSystem(false);
		SysSystemDto systemOther = getHelper().createTestResourceSystem(false);
		ZonedDateTime createdOne = ZonedDateTime.now().minusDays(2);
		SysProvisioningArchiveDto operationOne = createDto(system, createdOne, OperationState.EXECUTED, ProvisioningEventType.CANCEL);
		// all other variants for not removal
		createDto(system, createdOne, OperationState.EXECUTED, ProvisioningEventType.DELETE);
		SysProvisioningArchiveDto operationTwo = createDto(system, createdOne, OperationState.EXECUTED, ProvisioningEventType.CANCEL);
		createAttribute(operationTwo.getId(), getHelper().createName(), true);
		createDto(system, LocalDate.now().atStartOfDay(ZoneId.systemDefault()).plusMinutes(1), OperationState.EXECUTED, ProvisioningEventType.CANCEL);
		createDto(system, LocalDate.now().atStartOfDay(ZoneId.systemDefault()).plusMinutes(1), OperationState.CREATED, ProvisioningEventType.CANCEL);
		createDto(system, LocalDate.now().atStartOfDay(ZoneId.systemDefault()).plusMinutes(1), OperationState.EXECUTED, ProvisioningEventType.CANCEL);
		createDto(system, ZonedDateTime.now().minusDays(2), OperationState.EXCEPTION, ProvisioningEventType.CANCEL);
		createDto(system, LocalDate.now().atStartOfDay(ZoneId.systemDefault()).minusHours(23), OperationState.EXECUTED, ProvisioningEventType.CANCEL);
		SysProvisioningArchiveDto operationOther = createDto(systemOther, ZonedDateTime.now().minusDays(2), OperationState.EXECUTED, ProvisioningEventType.CANCEL);
		//
		Assert.assertEquals(createdOne, operationOne.getCreated());
		SysProvisioningOperationFilter filter = new SysProvisioningOperationFilter();
		filter.setSystemId(system.getId());
		List<SysProvisioningArchiveDto> archives = service.find(filter, null).getContent();
		Assert.assertEquals(8, archives.size());
		//
		DeleteProvisioningArchiveTaskExecutor taskExecutor = new DeleteProvisioningArchiveTaskExecutor();
		Map<String, Object> properties = new HashMap<>();
		properties.put(DeleteProvisioningArchiveTaskExecutor.PARAMETER_NUMBER_OF_DAYS, 1);
		properties.put(DeleteProvisioningArchiveTaskExecutor.PARAMETER_OPERATION_STATE, OperationState.EXECUTED);
		properties.put(DeleteProvisioningArchiveTaskExecutor.PARAMETER_SYSTEM, system.getId());
		properties.put(DeleteProvisioningArchiveTaskExecutor.PARAMETER_EMPTY_PROVISIONING, Boolean.TRUE);
		AutowireHelper.autowire(taskExecutor);
		taskExecutor.init(properties);
		//
		longRunningTaskManager.execute(taskExecutor);
		//
		archives = service.find(filter, null).getContent();
		Assert.assertEquals(7, archives.size());
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
	
	private SysProvisioningArchiveDto createDto(
			SysSystemDto system, 
			ZonedDateTime created,
			OperationState state, 
			ProvisioningEventType provisioningEventType) {
		SysProvisioningArchiveDto dto = new SysProvisioningArchiveDto();
		dto.setCreated(created);
		dto.setSystem(system.getId());
		dto.setEntityIdentifier(UUID.randomUUID());
		dto.setOperationType(provisioningEventType);
		dto.setEntityType(SystemEntityType.CONTRACT);
		dto.setProvisioningContext(new ProvisioningContext());
		dto.setResult(new OperationResult(state));
		//
		return service.save(dto);
	}
	
	private SysProvisioningAttribute createAttribute(UUID provisioningId, String name, boolean removed) {
		SysProvisioningAttribute attribute = new SysProvisioningAttribute(provisioningId, name);
		attribute.setRemoved(removed);
		//
		return provisioningAttributeRepository.save(attribute);
	}
	
	@Override
	protected TestHelper getHelper() {
		return (TestHelper) super.getHelper();
	}
}
