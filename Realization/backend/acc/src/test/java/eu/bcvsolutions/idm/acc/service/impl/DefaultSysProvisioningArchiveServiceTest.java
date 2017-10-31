package eu.bcvsolutions.idm.acc.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.acc.domain.ProvisioningContext;
import eu.bcvsolutions.idm.acc.domain.ProvisioningEventType;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningArchiveDto;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningBatchDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysProvisioningOperationFilter;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningArchiveService;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningBatchService;
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
public class DefaultSysProvisioningArchiveServiceTest extends AbstractIntegrationTest {

	@Autowired private SysProvisioningArchiveService archiveService;
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

		SysProvisioningArchiveDto provisioningOperation1 = createProvisioningArchive(SystemEntityType.CONTRACT, system);
		SysProvisioningArchiveDto provisioningOperation2 = createProvisioningArchive(SystemEntityType.IDENTITY, system);
		SysProvisioningArchiveDto provisioningOperation3 = createProvisioningArchive(SystemEntityType.CONTRACT, system);

		SysProvisioningOperationFilter filter = new SysProvisioningOperationFilter();
		filter.setEntityType(SystemEntityType.CONTRACT);

		Page<SysProvisioningArchiveDto> result = archiveService.find(filter, null, permission);
		assertEquals(2, result.getTotalElements());
		assertTrue(result.getContent().contains(provisioningOperation1));
		assertTrue(result.getContent().contains(provisioningOperation3));
		assertFalse(result.getContent().contains(provisioningOperation2));
	}

	@Test
	public void operationTypeFilterTest() {
		IdmBasePermission permission = IdmBasePermission.ADMIN;
		SystemEntityType entityType = SystemEntityType.IDENTITY;

		SysSystemDto system = createRoleSystem();

		SysProvisioningArchiveDto provisioningArchive1 = createProvisioningArchive(entityType, system);
		SysProvisioningArchiveDto provisioningArchive2 = createProvisioningArchive(entityType, system);
		provisioningArchive2.setOperationType(ProvisioningEventType.UPDATE);
		archiveService.save(provisioningArchive2);
		SysProvisioningArchiveDto provisioningArchive3 = createProvisioningArchive(entityType, system);
		provisioningArchive3.setOperationType(ProvisioningEventType.UPDATE);
		archiveService.save(provisioningArchive3);

		SysProvisioningOperationFilter filter = new SysProvisioningOperationFilter();
		filter.setOperationType(ProvisioningEventType.UPDATE);
		filter.setSystemId(system.getId());

		Page<SysProvisioningArchiveDto> result = archiveService.find(filter, null, permission);
		assertEquals(2, result.getTotalElements());
		assertFalse(result.getContent().contains(provisioningArchive1));
		assertTrue(result.getContent().contains(provisioningArchive2));
		assertTrue(result.getContent().contains(provisioningArchive3));
	}

	@Test
	public void systemIdFilterTest() {
		IdmBasePermission permission = IdmBasePermission.ADMIN;
		SystemEntityType entityType = SystemEntityType.IDENTITY;

		SysSystemDto system1 = createRoleSystem();
		SysSystemDto system2 = createRoleSystem();

		SysProvisioningArchiveDto provisioningArchive1 = createProvisioningArchive(entityType, system1);
		SysProvisioningArchiveDto provisioningArchive2 = createProvisioningArchive(entityType, system1);
		SysProvisioningArchiveDto provisioningArchive3 = createProvisioningArchive(entityType, system2);

		SysProvisioningOperationFilter filter = new SysProvisioningOperationFilter();
		filter.setSystemId(system1.getId());

		Page<SysProvisioningArchiveDto> result = archiveService.find(filter, null, permission);
		assertEquals(2, result.getTotalElements());
		assertTrue(result.getContent().contains(provisioningArchive1));
		assertTrue(result.getContent().contains(provisioningArchive2));
		assertFalse(result.getContent().contains(provisioningArchive3));
	}

	@Test
	public void systemEntityUidFilterTest() {
		IdmBasePermission permission = IdmBasePermission.ADMIN;
		SystemEntityType entityType = SystemEntityType.IDENTITY;

		SysSystemDto system = createRoleSystem();

		SysProvisioningArchiveDto provisioningArchive1 = createProvisioningArchive(entityType, system);
		SysProvisioningArchiveDto provisioningArchive2 = createProvisioningArchive(entityType, system);
		SysProvisioningArchiveDto provisioningArchive3 = createProvisioningArchive(entityType, system);

		SysProvisioningOperationFilter filter = new SysProvisioningOperationFilter();
		filter.setSystemEntityUid(provisioningArchive1.getSystemEntityUid());

		Page<SysProvisioningArchiveDto> result = archiveService.find(filter, null, permission);
		assertEquals(1, result.getTotalElements());
		assertTrue(result.getContent().contains(provisioningArchive1));
		assertFalse(result.getContent().contains(provisioningArchive2));
		assertFalse(result.getContent().contains(provisioningArchive3));
	}

	@Test
	public void entityIdentifierFilterTest() {
		IdmBasePermission permission = IdmBasePermission.ADMIN;
		SystemEntityType entityType = SystemEntityType.IDENTITY;
		SysSystemDto system = createRoleSystem();

		createProvisioningArchive(entityType, system);

		SysProvisioningArchiveDto provisioningArchive1 = createProvisioningArchive(entityType, system);
		provisioningArchive1.setEntityIdentifier(UUID.randomUUID());
		provisioningArchive1 = archiveService.save(provisioningArchive1);
		SysProvisioningArchiveDto provisioningArchive2 = createProvisioningArchive(entityType, system);

		SysProvisioningOperationFilter filter = new SysProvisioningOperationFilter();
		filter.setEntityIdentifier(provisioningArchive1.getEntityIdentifier());

		Page<SysProvisioningArchiveDto> result = archiveService.find(filter, null, permission);
		assertEquals(1, result.getTotalElements());
		assertTrue(result.getContent().contains(provisioningArchive1));
		assertFalse(result.getContent().contains(provisioningArchive2));
	}

	@Test
	public void resultStateFilterTest() {
		IdmBasePermission permission = IdmBasePermission.ADMIN;
		SystemEntityType entityType = SystemEntityType.IDENTITY;
		SysSystemDto system = createRoleSystem();

		OperationResult resultState = new OperationResult();
		resultState.setState(OperationState.CREATED);

		SysProvisioningArchiveDto provisioningArchive1 = createProvisioningArchive(entityType, system);

		SysProvisioningArchiveDto provisioningArchive2 = createProvisioningArchive(entityType, system);
		provisioningArchive2.setResult(resultState);
		archiveService.save(provisioningArchive2);

		SysProvisioningOperationFilter filter = new SysProvisioningOperationFilter();
		filter.setResultState(OperationState.CREATED);

		Page<SysProvisioningArchiveDto> result = archiveService.find(filter, null, permission);
		assertEquals(1, result.getTotalElements());
		assertFalse(result.getContent().contains(provisioningArchive1));
		assertTrue(result.getContent().contains(provisioningArchive2));
	}

	private SysSystemDto createRoleSystem() {
		SysSystemDto system = new SysSystemDto();
		system.setName("system_" + UUID.randomUUID());
		return systemService.save(system);
	}

	private SysProvisioningArchiveDto createProvisioningArchive(SystemEntityType type, SysSystemDto system) {
		SysProvisioningArchiveDto provisioningArchive = new SysProvisioningArchiveDto();
		provisioningArchive.setEntityType(type);
		provisioningArchive.setOperationType(ProvisioningEventType.CREATE);
		provisioningArchive.setProvisioningContext(new ProvisioningContext());
		provisioningArchive.setSystem(system.getId());
		provisioningArchive.setSystemEntityUid("SomeEntityUID" + UUID.randomUUID());

		OperationResult result = new OperationResult();
		result.setState(OperationState.RUNNING);
		provisioningArchive.setResult(result);

		return archiveService.save(provisioningArchive);
	}

}
