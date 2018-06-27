package eu.bcvsolutions.idm.acc.service.impl;

import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.acc.TestHelper;
import eu.bcvsolutions.idm.acc.domain.ProvisioningContext;
import eu.bcvsolutions.idm.acc.domain.ProvisioningEventType;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningBatchDto;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningOperationDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemEntityDto;
import eu.bcvsolutions.idm.acc.entity.TestResource;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningBatchService;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningOperationService;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.filter.EmptyFilter;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Searching entities, using filters
 *
 * @author Petr Hanák
 * @author Radek Tomiška
 *
 */
@Transactional
public class DefaultSysProvisioningBatchServiceTest extends AbstractIntegrationTest {

	@Autowired private SysProvisioningOperationService operationService;
	@Autowired private SysProvisioningBatchService batchService;

	@Test
	public void emptyFilterTest() {
		IdmBasePermission permission = IdmBasePermission.ADMIN;
		SysSystemDto system = getHelper().createSystem(TestResource.TABLE_NAME);
		SysSystemEntityDto systemEntity = getHelper().createSystemEntity(system);
		//
		SysProvisioningBatchDto provisioningBatch = new SysProvisioningBatchDto();
		provisioningBatch.setId(UUID.randomUUID());
		provisioningBatch.setSystemEntity(systemEntity.getId());
		batchService.save(provisioningBatch);

		EmptyFilter filter = new EmptyFilter();

		Page<SysProvisioningBatchDto> result = batchService.find(filter, null, permission);
		assertTrue(result.getContent().contains(provisioningBatch));
	}
	
	@Test
	public void testMergeBatches() {
		// create two batch for the same system entity
		SysSystemDto system = getHelper().createSystem(TestResource.TABLE_NAME);
		SysSystemEntityDto systemEntity = getHelper().createSystemEntity(system);
		//
		SysProvisioningBatchDto batchOne = new SysProvisioningBatchDto();
		batchOne.setId(UUID.randomUUID());
		batchOne.setSystemEntity(systemEntity.getId());
		batchOne = batchService.save(batchOne);
		SysProvisioningOperationDto operationOne = createOperation(systemEntity, batchOne);
		//
		SysProvisioningBatchDto batchTwo = new SysProvisioningBatchDto();
		batchTwo.setId(UUID.randomUUID());
		batchTwo.setSystemEntity(systemEntity.getId());
		batchTwo = batchService.save(batchTwo);
		SysProvisioningOperationDto operationTwo = createOperation(systemEntity, batchTwo);
		//
		Assert.assertNotEquals(batchOne.getId(), batchTwo.getId());
		//
		SysProvisioningBatchDto mergedBatch = batchService.findBatch(systemEntity.getId());
		//
		Assert.assertEquals(batchOne.getId(), mergedBatch.getId());
		//
		Assert.assertNull(batchService.get(batchTwo));
		operationOne = operationService.get(operationOne.getId());
		operationTwo = operationService.get(operationTwo.getId());
		Assert.assertEquals(batchOne.getId(), operationOne.getBatch());
		Assert.assertEquals(batchOne.getId(), operationTwo.getBatch());
	}
	
	@Override
	protected TestHelper getHelper() {
		return (TestHelper) super.getHelper();
	}
	
	private SysProvisioningOperationDto createOperation(SysSystemEntityDto systemEntity, SysProvisioningBatchDto batch) {
		SysProvisioningOperationDto operation = new SysProvisioningOperationDto();
		operation.setBatch(batch.getId());
		operation.setProvisioningContext(new ProvisioningContext());
		operation.setOperationType(ProvisioningEventType.CREATE);
		operation.setSystem(systemEntity.getSystem());
		operation.setEntityType(SystemEntityType.IDENTITY);
		operation.setSystemEntity(systemEntity.getId());
		operation.setResult(new OperationResult(OperationState.CREATED));
		//
		return operationService.save(operation);
	}
}
