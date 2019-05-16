package eu.bcvsolutions.idm.acc.bulk.action.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Sets;

import eu.bcvsolutions.idm.InitApplicationData;
import eu.bcvsolutions.idm.acc.TestHelper;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningOperationDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysProvisioningOperationFilter;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningOperation;
import eu.bcvsolutions.idm.acc.rest.impl.SysProvisioningOperationController;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningOperationService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.test.api.AbstractBulkActionTest;

/**
 * Tests for provisioning operation bulk action {@link ProvisioningOperationCancelBulkAction}
 *
 * @author Ondrej Kopr
 *
 */
public class ProvisioningOperationCancelBulkActionTest extends AbstractBulkActionTest {

	@Autowired
	private TestHelper helper;
	@Autowired
	private SysSystemService systemService;
	@Autowired
	private SysProvisioningOperationService provisioningOperationService;
	@Autowired
	private IdmIdentityService identityService;
	@Autowired
	private SysProvisioningOperationController provisioningOperationController;

	@Before
	public void init() {
		loginAsAdmin(InitApplicationData.ADMIN_USERNAME);
	}

	@After
	public void logout() {
		super.logout();
	}

	@Test
	public void testCancelOneItem() {
		IdmRoleDto role = getHelper().createRole();
		IdmIdentityDto identity = getHelper().createIdentity();

		SysSystemDto system = helper.createTestResourceSystem(true);
		system.setDisabled(true);
		system = systemService.save(system);

		helper.createRoleSystem(role, system);
		getHelper().createIdentityRole(identity, role);

		identityService.save(identity);
		identityService.save(identity);
		identityService.save(identity);
		identityService.save(identity);

		SysProvisioningOperationFilter filter = new SysProvisioningOperationFilter();
		filter.setEntityIdentifier(identity.getId());
		filter.setEntityType(SystemEntityType.IDENTITY);
		List<SysProvisioningOperationDto> operations = provisioningOperationService.find(filter, null).getContent();
		assertFalse(operations.isEmpty());
		assertTrue(operations.size() > 1);
		
		IdmBulkActionDto bulkAction = this.findBulkAction(SysProvisioningOperation.class, ProvisioningOperationCancelBulkAction.NAME);

		// We found just one
		SysProvisioningOperationDto provisioningOperationDto = operations.stream().findAny().get();
		bulkAction.setIdentifiers(Sets.newHashSet(provisioningOperationDto.getId()));
		
		Map<String, Object> properties = new HashMap<>();
		properties.put(ProvisioningOperationCancelBulkAction.RETRY_WHOLE_BATCH_CODE, Boolean.FALSE.toString());
		bulkAction.setProperties(properties);

		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		checkResultLrt(processAction, 1l, null, null);

		List<SysProvisioningOperationDto> newOperations = provisioningOperationService.find(filter, null).getContent();
		assertEquals(operations.size() - 1, newOperations.size());

		SysProvisioningOperationDto orElse = newOperations.stream().filter(opr -> {
			return opr.getId().equals(provisioningOperationDto.getId());
		}).findAny().orElse(null);
		assertNull(orElse);
	}

	@Test
	public void testCancelWithFilter() {
		IdmRoleDto role = getHelper().createRole();
		IdmIdentityDto identity = getHelper().createIdentity();

		SysSystemDto system = helper.createTestResourceSystem(true);
		system.setDisabled(true);
		system = systemService.save(system);

		helper.createRoleSystem(role, system);
		getHelper().createIdentityRole(identity, role);

		identityService.save(identity);
		identityService.save(identity);
		identityService.save(identity);
		identityService.save(identity);

		SysProvisioningOperationFilter filter = new SysProvisioningOperationFilter();
		filter.setEntityIdentifier(identity.getId());
		filter.setEntityType(SystemEntityType.IDENTITY);
		List<SysProvisioningOperationDto> operations = provisioningOperationService.find(filter, null).getContent();
		assertFalse(operations.isEmpty());
		assertTrue(operations.size() > 1);

		IdmBulkActionDto bulkAction = this.findBulkAction(SysProvisioningOperation.class, ProvisioningOperationCancelBulkAction.NAME);

		bulkAction.setTransformedFilter(filter);
		bulkAction.setFilter(toMap(filter));

		Map<String, Object> properties = new HashMap<>();
		properties.put(ProvisioningOperationCancelBulkAction.RETRY_WHOLE_BATCH_CODE, Boolean.FALSE.toString());
		bulkAction.setProperties(properties);

		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		checkResultLrt(processAction, (long)operations.size(), null, null);

		List<SysProvisioningOperationDto> newOperations = provisioningOperationService.find(filter, null).getContent();
		assertEquals(0, newOperations.size());
	}

	@Test
	public void testCancelBatchOneItem() {
		IdmRoleDto role = getHelper().createRole();
		IdmIdentityDto identity = getHelper().createIdentity();

		SysSystemDto system = helper.createTestResourceSystem(true);
		system.setDisabled(true);
		system = systemService.save(system);

		helper.createRoleSystem(role, system);
		getHelper().createIdentityRole(identity, role);

		identityService.save(identity);
		identityService.save(identity);
		identityService.save(identity);
		identityService.save(identity);
		identityService.save(identity);
		identityService.save(identity);

		SysProvisioningOperationFilter filter = new SysProvisioningOperationFilter();
		filter.setEntityIdentifier(identity.getId());
		filter.setEntityType(SystemEntityType.IDENTITY);
		List<SysProvisioningOperationDto> operations = provisioningOperationService.find(filter, null).getContent();
		assertFalse(operations.isEmpty());
		assertTrue(operations.size() > 1);
		
		IdmBulkActionDto bulkAction = this.findBulkAction(SysProvisioningOperation.class, ProvisioningOperationCancelBulkAction.NAME);

		// We found just one
		SysProvisioningOperationDto provisioningOperationDto = operations.stream().findAny().get();
		bulkAction.setIdentifiers(Sets.newHashSet(provisioningOperationDto.getId()));
		
		Map<String, Object> properties = new HashMap<>();
		properties.put(ProvisioningOperationCancelBulkAction.RETRY_WHOLE_BATCH_CODE, Boolean.TRUE.toString());
		bulkAction.setProperties(properties);

		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		checkResultLrt(processAction, 1l, null, null);

		List<SysProvisioningOperationDto> newOperations = provisioningOperationService.find(filter, null).getContent();
		assertEquals(0, newOperations.size()); // Must be zero
	}

	@Test
	public void testCancelBatchAllItems() {
		IdmRoleDto role = getHelper().createRole();
		IdmIdentityDto identity = getHelper().createIdentity();

		SysSystemDto system = helper.createTestResourceSystem(true);
		system.setDisabled(true);
		system = systemService.save(system);

		helper.createRoleSystem(role, system);
		getHelper().createIdentityRole(identity, role);

		identityService.save(identity);
		identityService.save(identity);
		identityService.save(identity);
		identityService.save(identity);
		identityService.save(identity);
		identityService.save(identity);
		// 7 operations

		SysProvisioningOperationFilter filter = new SysProvisioningOperationFilter();
		filter.setEntityIdentifier(identity.getId());
		filter.setEntityType(SystemEntityType.IDENTITY);
		List<SysProvisioningOperationDto> operations = provisioningOperationService.find(filter, null).getContent();
		assertFalse(operations.isEmpty());
		assertTrue(operations.size() > 1);
		
		IdmBulkActionDto bulkAction = this.findBulkAction(SysProvisioningOperation.class, ProvisioningOperationCancelBulkAction.NAME);

		int allOperations = operations.size();
		
		// We found just one
		bulkAction.setIdentifiers(getIdFromList(operations));
		
		Map<String, Object> properties = new HashMap<>();
		properties.put(ProvisioningOperationCancelBulkAction.RETRY_WHOLE_BATCH_CODE, Boolean.TRUE.toString());
		bulkAction.setProperties(properties);

		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		checkResultLrt(processAction, 7l, null, null); // Filtering by filter = 7 operations
		checkProcessItemsCount(processAction, allOperations);

		List<SysProvisioningOperationDto> newOperations = provisioningOperationService.find(filter, null).getContent();
		assertEquals(0, newOperations.size()); // Must be zero
	}

	@Test
	public void testCancelBatchSystem() {
		IdmRoleDto role = getHelper().createRole();
		IdmIdentityDto identity = getHelper().createIdentity();
		IdmIdentityDto identityTwo = getHelper().createIdentity();

		SysSystemDto system = helper.createTestResourceSystem(true);
		system.setDisabled(true);
		system = systemService.save(system);

		helper.createRoleSystem(role, system);
		getHelper().createIdentityRole(identity, role);
		getHelper().createIdentityRole(identityTwo, role);

		identityService.save(identity);
		identityService.save(identity);
		identityService.save(identity);
		identityService.save(identity);
		identityService.save(identity);
		identityService.save(identity);
		// 7 Operations
		
		identityService.save(identityTwo);
		identityService.save(identityTwo);
		identityService.save(identityTwo);
		identityService.save(identityTwo);
		// 5 Operations

		SysProvisioningOperationFilter filter = new SysProvisioningOperationFilter();
		filter.setSystemId(system.getId());
		filter.setEntityType(SystemEntityType.IDENTITY);
		List<SysProvisioningOperationDto> operations = provisioningOperationService.find(filter, null).getContent();
		assertFalse(operations.isEmpty());
		assertTrue(operations.size() > 1);
		
		IdmBulkActionDto bulkAction = this.findBulkAction(SysProvisioningOperation.class, ProvisioningOperationCancelBulkAction.NAME);

		int allOperations = operations.size();

		bulkAction.setTransformedFilter(filter);
		bulkAction.setFilter(toMap(filter));
		
		Map<String, Object> properties = new HashMap<>();
		properties.put(ProvisioningOperationCancelBulkAction.RETRY_WHOLE_BATCH_CODE, Boolean.TRUE.toString());
		bulkAction.setProperties(properties);

		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		checkResultLrt(processAction, 12l, null, null); // Filtering by filter = 12 operations
		checkProcessItemsCount(processAction, allOperations);

		List<SysProvisioningOperationDto> newOperations = provisioningOperationService.find(filter, null).getContent();
		assertEquals(0, newOperations.size()); // Must be zero
	}

	@Test
	public void testCancelItemsSystem() {
		IdmRoleDto role = getHelper().createRole();
		IdmIdentityDto identity = getHelper().createIdentity();
		IdmIdentityDto identityTwo = getHelper().createIdentity();

		SysSystemDto system = helper.createTestResourceSystem(true);
		system.setDisabled(true);
		system = systemService.save(system);

		helper.createRoleSystem(role, system);
		getHelper().createIdentityRole(identity, role);
		getHelper().createIdentityRole(identityTwo, role);

		identityService.save(identity);
		identityService.save(identity);
		identityService.save(identity);
		identityService.save(identity);
		identityService.save(identity);
		identityService.save(identity);
		
		identityService.save(identityTwo);
		identityService.save(identityTwo);
		identityService.save(identityTwo);
		identityService.save(identityTwo);

		SysProvisioningOperationFilter filter = new SysProvisioningOperationFilter();
		filter.setSystemId(system.getId());
		filter.setEntityType(SystemEntityType.IDENTITY);
		List<SysProvisioningOperationDto> operations = provisioningOperationService.find(filter, null).getContent();
		assertFalse(operations.isEmpty());
		assertTrue(operations.size() > 1);
		
		IdmBulkActionDto bulkAction = this.findBulkAction(SysProvisioningOperation.class, ProvisioningOperationCancelBulkAction.NAME);

		int allOperations = operations.size();
		
		// We found just one
		bulkAction.setIdentifiers(getIdFromList(operations));
		
		Map<String, Object> properties = new HashMap<>();
		properties.put(ProvisioningOperationCancelBulkAction.RETRY_WHOLE_BATCH_CODE, Boolean.FALSE.toString());
		bulkAction.setProperties(properties);

		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		checkResultLrt(processAction, Long.valueOf(allOperations), null, null);

		List<SysProvisioningOperationDto> newOperations = provisioningOperationService.find(filter, null).getContent();
		assertEquals(0, newOperations.size()); // Must be zero
	}

	@Test
	public void testProcessBulkActionWithoutPermissions() {		
		IdmRoleDto role = getHelper().createRole();
		IdmIdentityDto identity = getHelper().createIdentity();

		SysSystemDto system = helper.createTestResourceSystem(true);
		system.setDisabled(true);
		system = systemService.save(system);

		helper.createRoleSystem(role, system);
		getHelper().createIdentityRole(identity, role); // create provisioning

		identityService.save(identity); // update provisioning

		SysProvisioningOperationFilter filter = new SysProvisioningOperationFilter();
		filter.setEntityIdentifier(identity.getId());
		filter.setEntityType(SystemEntityType.IDENTITY);
		List<SysProvisioningOperationDto> operations = provisioningOperationService.find(filter, null).getContent();
		assertFalse(operations.isEmpty());
		assertTrue(operations.size() > 1);

		IdmIdentityDto adminIdentity = this.createUserWithAuthorities(IdmBasePermission.READ);
		loginAsNoAdmin(adminIdentity.getUsername());

		IdmBulkActionDto bulkAction = this.findBulkAction(SysProvisioningOperation.class, ProvisioningOperationCancelBulkAction.NAME);
		bulkAction.setIdentifiers(operations.stream().map(SysProvisioningOperationDto::getId).collect(Collectors.toSet()));
		int allOperations = operations.size();

		Map<String, Object> properties = new HashMap<>();
		properties.put(ProvisioningOperationCancelBulkAction.RETRY_WHOLE_BATCH_CODE, Boolean.TRUE.toString());
		bulkAction.setProperties(properties);

		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		checkResultLrt(processAction, null, null, Long.valueOf(allOperations));

		List<SysProvisioningOperationDto> newOperations = provisioningOperationService.find(filter, null).getContent();
		assertEquals(allOperations, newOperations.size()); // Must be same
	}

	@Test
	public void testEmptyFilterWithController() {
		// Delete operation from before
		provisioningOperationService.deleteAllOperations();
		SysProvisioningOperationFilter filter = new SysProvisioningOperationFilter();
		List<SysProvisioningOperationDto> operations = provisioningOperationService.find(filter, null).getContent();
		assertTrue(operations.isEmpty());

		IdmRoleDto role = getHelper().createRole();
		IdmIdentityDto identity = getHelper().createIdentity();

		SysSystemDto system = helper.createTestResourceSystem(true);
		system.setDisabled(true);
		system = systemService.save(system);

		helper.createRoleSystem(role, system);
		getHelper().createIdentityRole(identity, role);

		identityService.save(identity);

		operations = provisioningOperationService.find(filter, null).getContent();
		assertFalse(operations.isEmpty());

		IdmBulkActionDto bulkAction = this.findBulkAction(SysProvisioningOperation.class, ProvisioningOperationCancelBulkAction.NAME);
		bulkAction.setTransformedFilter(null); // There must be null
		bulkAction.setFilter(toMap(filter)); // Empty filter

		int allOperations = operations.size();

		Map<String, Object> properties = new HashMap<>();
		properties.put(ProvisioningOperationCancelBulkAction.RETRY_WHOLE_BATCH_CODE, Boolean.TRUE.toString());
		bulkAction.setProperties(properties);

		IdmBulkActionDto processAction = provisioningOperationController.bulkAction(bulkAction).getBody();
		checkResultLrt(processAction, Long.valueOf(allOperations), null, null);

		operations = provisioningOperationService.find(filter, null).getContent();
		assertTrue(operations.isEmpty());
	}
}
