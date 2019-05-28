package eu.bcvsolutions.idm.acc.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.acc.DefaultAccTestHelper;
import eu.bcvsolutions.idm.acc.domain.ProvisioningContext;
import eu.bcvsolutions.idm.acc.domain.ProvisioningEventType;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningArchiveDto;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningOperationDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysProvisioningOperationFilter;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningArchiveService;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningOperationService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Searching entities, using filters
 *
 * @author Petr Hanák
 * @author Radek Tomiška
 */
public class DefaultSysProvisioningArchiveServiceTest extends AbstractIntegrationTest {

	@Autowired private SysProvisioningOperationService operationService;
	@Autowired private SysProvisioningArchiveService archiveService;
	@Autowired private SysSystemService systemService;

	@Test
	@Transactional
	public void typeFilterTest() {
		SysSystemDto system = createRoleSystem();

		SysProvisioningArchiveDto provisioningOperation1 = createProvisioningArchive(SystemEntityType.CONTRACT, system);
		SysProvisioningArchiveDto provisioningOperation2 = createProvisioningArchive(SystemEntityType.IDENTITY, system);
		SysProvisioningArchiveDto provisioningOperation3 = createProvisioningArchive(SystemEntityType.CONTRACT, system);

		SysProvisioningOperationFilter filter = new SysProvisioningOperationFilter();
		filter.setSystemId(system.getId());
		filter.setEntityType(SystemEntityType.CONTRACT);

		Page<SysProvisioningArchiveDto> result = archiveService.find(filter, null);
		assertEquals(2, result.getTotalElements());
		assertTrue(result.getContent().contains(provisioningOperation1));
		assertTrue(result.getContent().contains(provisioningOperation3));
		assertFalse(result.getContent().contains(provisioningOperation2));
	}

	@Test
	@Transactional
	public void operationTypeFilterTest() {
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

		Page<SysProvisioningArchiveDto> result = archiveService.find(filter, null);
		assertEquals(2, result.getTotalElements());
		assertFalse(result.getContent().contains(provisioningArchive1));
		assertTrue(result.getContent().contains(provisioningArchive2));
		assertTrue(result.getContent().contains(provisioningArchive3));
	}

	@Test
	@Transactional
	public void systemIdFilterTest() {
		SystemEntityType entityType = SystemEntityType.IDENTITY;

		SysSystemDto system1 = createRoleSystem();
		SysSystemDto system2 = createRoleSystem();

		SysProvisioningArchiveDto provisioningArchive1 = createProvisioningArchive(entityType, system1);
		SysProvisioningArchiveDto provisioningArchive2 = createProvisioningArchive(entityType, system1);
		SysProvisioningArchiveDto provisioningArchive3 = createProvisioningArchive(entityType, system2);

		SysProvisioningOperationFilter filter = new SysProvisioningOperationFilter();
		filter.setSystemId(system1.getId());

		Page<SysProvisioningArchiveDto> result = archiveService.find(filter, null);
		assertEquals(2, result.getTotalElements());
		assertTrue(result.getContent().contains(provisioningArchive1));
		assertTrue(result.getContent().contains(provisioningArchive2));
		assertFalse(result.getContent().contains(provisioningArchive3));
	}

	@Test
	@Transactional
	public void systemEntityUidFilterTest() {
		SystemEntityType entityType = SystemEntityType.IDENTITY;

		SysSystemDto system = createRoleSystem();

		SysProvisioningArchiveDto provisioningArchive1 = createProvisioningArchive(entityType, system);
		SysProvisioningArchiveDto provisioningArchive2 = createProvisioningArchive(entityType, system);
		SysProvisioningArchiveDto provisioningArchive3 = createProvisioningArchive(entityType, system);

		SysProvisioningOperationFilter filter = new SysProvisioningOperationFilter();
		filter.setSystemEntityUid(provisioningArchive1.getSystemEntityUid());

		Page<SysProvisioningArchiveDto> result = archiveService.find(filter, null);
		assertEquals(1, result.getTotalElements());
		assertTrue(result.getContent().contains(provisioningArchive1));
		assertFalse(result.getContent().contains(provisioningArchive2));
		assertFalse(result.getContent().contains(provisioningArchive3));
	}

	@Test
	@Transactional
	public void entityIdentifierFilterTest() {
		SystemEntityType entityType = SystemEntityType.IDENTITY;
		SysSystemDto system = createRoleSystem();

		createProvisioningArchive(entityType, system);

		SysProvisioningArchiveDto provisioningArchive1 = createProvisioningArchive(entityType, system);
		provisioningArchive1.setEntityIdentifier(UUID.randomUUID());
		provisioningArchive1 = archiveService.save(provisioningArchive1);
		SysProvisioningArchiveDto provisioningArchive2 = createProvisioningArchive(entityType, system);

		SysProvisioningOperationFilter filter = new SysProvisioningOperationFilter();
		filter.setEntityIdentifier(provisioningArchive1.getEntityIdentifier());

		Page<SysProvisioningArchiveDto> result = archiveService.find(filter, null);
		assertEquals(1, result.getTotalElements());
		assertTrue(result.getContent().contains(provisioningArchive1));
		assertFalse(result.getContent().contains(provisioningArchive2));
	}

	@Test
	@Transactional
	public void resultStateFilterTest() {
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

		Page<SysProvisioningArchiveDto> result = archiveService.find(filter, null);
		assertEquals(1, result.getTotalElements());
		assertFalse(result.getContent().contains(provisioningArchive1));
		assertTrue(result.getContent().contains(provisioningArchive2));
	}
	
	@Test
	public void testOperationArchivate() {
		SysSystemDto system = getHelper().createTestResourceSystem(false);
		SysProvisioningOperationDto operation = new SysProvisioningOperationDto();
		UUID mockIdentityId = UUID.randomUUID();
		operation.setSystem(system.getId());
		operation.setEntityIdentifier(UUID.randomUUID());
		operation.setOperationType(ProvisioningEventType.CANCEL);
		operation.setEntityType(SystemEntityType.CONTRACT);
		operation.setProvisioningContext(new ProvisioningContext());
		operation.setResult(new OperationResult(OperationState.CANCELED));
		operation.setSystemEntity(getHelper().createSystemEntity(system).getId());
		operation.setCreator(mockIdentityId.toString());
		operation.setCreatorId(mockIdentityId);
		operation.setOriginalCreator(mockIdentityId.toString());
		operation.setOriginalCreatorId(mockIdentityId);
		//
		operation = operationService.save(operation);
		//
		Assert.assertNotNull(operation.getSystem());
		Assert.assertNotNull(operation.getCreated());
		Assert.assertEquals(mockIdentityId.toString(), operation.getCreator());
		Assert.assertEquals(mockIdentityId, operation.getCreatorId());
		Assert.assertEquals(mockIdentityId.toString(), operation.getOriginalCreator());
		Assert.assertEquals(mockIdentityId, operation.getOriginalCreatorId());
		//
		getHelper().waitForResult(null, 30, 1);
		SysProvisioningArchiveDto archive = archiveService.archive(operation);
		//
		Assert.assertEquals(operation.getCreated(), archive.getCreated());
		Assert.assertNotNull(archive.getModified());
		Assert.assertNotEquals(operation.getCreated(), archive.getModified());
		Assert.assertEquals(mockIdentityId.toString(), archive.getCreator());
		Assert.assertEquals(mockIdentityId.toString(), archive.getCreator());
		Assert.assertEquals(mockIdentityId, archive.getCreatorId());
		Assert.assertEquals(mockIdentityId.toString(), archive.getOriginalCreator());
		Assert.assertEquals(mockIdentityId, archive.getOriginalCreatorId());
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
	
	@Override
	protected DefaultAccTestHelper getHelper() {
		return (DefaultAccTestHelper) super.getHelper();
	}

}
