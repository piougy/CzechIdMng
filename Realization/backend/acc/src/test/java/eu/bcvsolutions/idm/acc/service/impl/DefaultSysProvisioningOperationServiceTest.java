package eu.bcvsolutions.idm.acc.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.acc.domain.ProvisioningContext;
import eu.bcvsolutions.idm.acc.domain.ProvisioningEventType;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningBatchDto;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningOperationDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysProvisioningOperationFilter;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningBatchService;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningOperationService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;
import java.util.UUID;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

/**
 * Searching entities, using filters
 *
 * @author Petr Hanák
 *
 */
public class DefaultSysProvisioningOperationServiceTest extends AbstractIntegrationTest {

	@Autowired private SysProvisioningOperationService operationService;
	@Autowired private SysSystemService systemService;
	@Autowired private SysProvisioningBatchService batchService;

	@Before
	public void init() {
		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
	}

	@After
	public void logout() {
		super.logout();
	}

	@Test
	public void typeFilterTest() {
		IdmBasePermission permission = IdmBasePermission.ADMIN;
		SysSystemDto system = createRoleSystem();

		createProvisioningOperation(SystemEntityType.CONTRACT, system);
		SysProvisioningOperationDto provisioningOperation2 = createProvisioningOperation(SystemEntityType.IDENTITY, system);
		SysProvisioningOperationDto provisioningOperation3 = createProvisioningOperation(SystemEntityType.CONTRACT, system);

		SysProvisioningOperationFilter filter = new SysProvisioningOperationFilter();
		filter.setEntityType(SystemEntityType.CONTRACT);

		Page<SysProvisioningOperationDto> result = operationService.find(filter, null, permission);
		assertEquals(2, result.getTotalElements());
		assertTrue(result.getContent().contains(provisioningOperation3));
		assertFalse(result.getContent().contains(provisioningOperation2));
	}

	@Test
	public void operationTypeFilterTest() {
		IdmBasePermission permission = IdmBasePermission.ADMIN;
		SystemEntityType entityType = SystemEntityType.IDENTITY;

		SysSystemDto system = createRoleSystem();

		SysProvisioningOperationDto provisioningOperation1 = createProvisioningOperation(entityType, system);
		SysProvisioningOperationDto provisioningOperation2 = createProvisioningOperation(entityType, system);
		provisioningOperation2.setOperationType(ProvisioningEventType.UPDATE);
		operationService.save(provisioningOperation2);
		SysProvisioningOperationDto provisioningOperation3 = createProvisioningOperation(entityType, system);
		provisioningOperation3.setOperationType(ProvisioningEventType.UPDATE);
		operationService.save(provisioningOperation3);

		SysProvisioningOperationFilter filter = new SysProvisioningOperationFilter();
		filter.setOperationType(ProvisioningEventType.UPDATE);

		Page<SysProvisioningOperationDto> result = operationService.find(filter, null, permission);
		assertEquals(2, result.getTotalElements());
		assertTrue(result.getContent().contains(provisioningOperation2));
		assertFalse(result.getContent().contains(provisioningOperation1));
	}

	@Test
	public void systemIdFilterTest() {
		IdmBasePermission permission = IdmBasePermission.ADMIN;
		SystemEntityType entityType = SystemEntityType.IDENTITY;

		SysSystemDto system2 = createRoleSystem();
		SysSystemDto system1 = createRoleSystem();

		createProvisioningOperation(entityType, system2);
		SysProvisioningOperationDto provisioningOperation1 = createProvisioningOperation(entityType, system1);
		SysProvisioningOperationDto provisioningOperation2 = createProvisioningOperation(entityType, system2);

		SysProvisioningOperationFilter filter = new SysProvisioningOperationFilter();
		filter.setSystemId(system2.getId());

		Page<SysProvisioningOperationDto> result = operationService.find(filter, null, permission);
		assertEquals(2, result.getTotalElements());
		assertTrue(result.getContent().contains(provisioningOperation2));
		assertFalse(result.getContent().contains(provisioningOperation1));
	}

	@Test
	public void systemEntityUidFilterTest() {
		IdmBasePermission permission = IdmBasePermission.ADMIN;
		SystemEntityType entityType = SystemEntityType.IDENTITY;

		SysSystemDto system = createRoleSystem();

		SysProvisioningOperationDto provisioningOperation1 = createProvisioningOperation(entityType, system);
		SysProvisioningOperationDto provisioningOperation2 = createProvisioningOperation(entityType, system);
		SysProvisioningOperationDto provisioningOperation3 = createProvisioningOperation(entityType, system);

		SysProvisioningOperationFilter filter = new SysProvisioningOperationFilter();
		filter.setSystemEntityUid(provisioningOperation1.getSystemEntityUid());

		Page<SysProvisioningOperationDto> result = operationService.find(filter, null, permission);
		assertEquals(1, result.getTotalElements());
		assertTrue(result.getContent().contains(provisioningOperation1));
		assertFalse(result.getContent().contains(provisioningOperation2));
		assertFalse(result.getContent().contains(provisioningOperation3));
	}

	@Test
	public void batchIdFilterTest() {
		IdmBasePermission permission = IdmBasePermission.ADMIN;
		SystemEntityType entityType = SystemEntityType.IDENTITY;
		SysSystemDto system = createRoleSystem();

		SysProvisioningBatchDto provisioningBatch = new SysProvisioningBatchDto();
		provisioningBatch = batchService.save(provisioningBatch);

		SysProvisioningOperationDto provisioningOperation1 = createProvisioningOperation(entityType, system);
		provisioningOperation1.setBatch(provisioningBatch.getId());
		provisioningOperation1 = operationService.save(provisioningOperation1);
		SysProvisioningOperationDto provisioningOperation2 = createProvisioningOperation(entityType, system);
		provisioningOperation2.setBatch(provisioningBatch.getId());
		provisioningOperation2 = operationService.save(provisioningOperation2);
		SysProvisioningOperationDto provisioningOperation3 = createProvisioningOperation(entityType, system);

		SysProvisioningOperationFilter filter = new SysProvisioningOperationFilter();
		filter.setBatchId(provisioningBatch.getId());

		Page<SysProvisioningOperationDto> result = operationService.find(filter, null, permission);
		assertEquals(2, result.getTotalElements());
		assertTrue(result.getContent().contains(provisioningOperation1));
		assertTrue(result.getContent().contains(provisioningOperation2));
		assertFalse(result.getContent().contains(provisioningOperation3));
	}

	@Test
	public void entityIdentifierFilterTest() {
		IdmBasePermission permission = IdmBasePermission.ADMIN;
		SystemEntityType entityType = SystemEntityType.IDENTITY;
		SysSystemDto system = createRoleSystem();

		createProvisioningOperation(entityType, system);

		SysProvisioningOperationDto provisioningOperation1 = createProvisioningOperation(entityType, system);
		provisioningOperation1.setEntityIdentifier(UUID.randomUUID());
		operationService.save(provisioningOperation1);
		SysProvisioningOperationDto provisioningOperation2 = createProvisioningOperation(entityType, system);

		SysProvisioningOperationFilter filter = new SysProvisioningOperationFilter();
		filter.setEntityIdentifier(provisioningOperation1.getEntityIdentifier());

		Page<SysProvisioningOperationDto> result = operationService.find(filter, null, permission);
		assertEquals(1, result.getTotalElements());
		assertTrue(result.getContent().contains(provisioningOperation1));
		assertFalse(result.getContent().contains(provisioningOperation2));
	}

	@Test
	public void resultStateFilterTest() {
		IdmBasePermission permission = IdmBasePermission.ADMIN;
		SystemEntityType entityType = SystemEntityType.IDENTITY;
		SysSystemDto system = createRoleSystem();

		OperationResult resultState = new OperationResult();
		resultState.setState(OperationState.CREATED);

		SysProvisioningOperationDto provisioningOperation1 = createProvisioningOperation(entityType, system);
		SysProvisioningOperationDto provisioningOperation2 = createProvisioningOperation(entityType, system);
		provisioningOperation2.setResult(resultState);
		operationService.save(provisioningOperation2);

		SysProvisioningOperationFilter filter = new SysProvisioningOperationFilter();
		filter.setResultState(OperationState.CREATED);

		Page<SysProvisioningOperationDto> result = operationService.find(filter, null, permission);
		assertEquals(1, result.getTotalElements());
		assertTrue(result.getContent().contains(provisioningOperation2));
		assertFalse(result.getContent().contains(provisioningOperation1));
	}

	@Test
	public void dateTimeFilterTest() {
		IdmBasePermission permission = IdmBasePermission.ADMIN;
		SystemEntityType entityType = SystemEntityType.IDENTITY;
		SysSystemDto system = createRoleSystem();

		SysProvisioningOperationDto provisioningOperation1 = createProvisioningOperation(entityType, system);

		DateTime dateTime = DateTime.now();

		SysProvisioningOperationDto provisioningOperation2 = createProvisioningOperation(entityType, system);

		SysProvisioningOperationFilter filter = new SysProvisioningOperationFilter();
		filter.setFrom(dateTime);

		Page<SysProvisioningOperationDto> result = operationService.find(filter, null, permission);
		assertEquals(1, result.getTotalElements());
		assertTrue(result.getContent().contains(provisioningOperation2));

		dateTime = dateTime.minusHours(1);
		SysProvisioningOperationFilter filter2 = new SysProvisioningOperationFilter();
		filter2.setTill(dateTime);

		Page<SysProvisioningOperationDto> result2 = operationService.find(filter2, null, permission);
		assertEquals(0, result2.getTotalElements());
	}

	private SysSystemDto createRoleSystem() {
		SysSystemDto system = new SysSystemDto();
		system.setName("system_" + UUID.randomUUID());
		return systemService.save(system);
	}

	private SysProvisioningOperationDto createProvisioningOperation (SystemEntityType type, SysSystemDto system) {
		SysProvisioningOperationDto provisioningOperation = new SysProvisioningOperationDto();
		provisioningOperation.setEntityType(type);
		provisioningOperation.setOperationType(ProvisioningEventType.CREATE);
		provisioningOperation.setProvisioningContext(new ProvisioningContext());
		provisioningOperation.setSystem(system.getId());
		provisioningOperation.setSystemEntityUid("SomeEntityUID" + UUID.randomUUID());

		OperationResult result = new OperationResult();
		result.setState(OperationState.RUNNING);

		provisioningOperation.setResult(result);

		return operationService.save(provisioningOperation);
	}

}
