package eu.bcvsolutions.idm.acc.bulk.action.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import eu.bcvsolutions.idm.test.api.AbstractBulkActionTest;

/**
 * Test for provisioning operation bulk action {@link ProvisioningOperationRetryBulkAction}
 *
 * @author Ondrej Kopr
 *
 */
public class ProvisioningOperationRetryBulkActionTest extends AbstractBulkActionTest {

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
	public void testRetryOneItemWithReturnToQueue() {
		IdmRoleDto role = getHelper().createRole();
		IdmIdentityDto identity = getHelper().createIdentity();

		SysSystemDto system = helper.createTestResourceSystem(true);
		system.setReadonly(true);
		system = systemService.save(system);

		helper.createRoleSystem(role, system);
		getHelper().createIdentityRole(identity, role);

		identityService.save(identity);
		identityService.save(identity);
		identityService.save(identity);

		SysProvisioningOperationFilter filter = new SysProvisioningOperationFilter();
		filter.setEntityIdentifier(identity.getId());
		filter.setEntityType(SystemEntityType.IDENTITY);
		List<SysProvisioningOperationDto> operations = provisioningOperationService.find(filter, null).getContent();
		assertFalse(operations.isEmpty());
		assertTrue(operations.size() > 1);

		IdmBulkActionDto bulkAction = this.findBulkAction(SysProvisioningOperation.class, ProvisioningOperationRetryBulkAction.NAME);

		// We found just one
		SysProvisioningOperationDto provisioningOperationDto = operations.stream().findAny().get();
		bulkAction.setIdentifiers(Sets.newHashSet(provisioningOperationDto.getId()));
		
		Map<String, Object> properties = new HashMap<>();
		properties.put(ProvisioningOperationRetryBulkAction.RETRY_WHOLE_BATCH_CODE, Boolean.FALSE.toString());
		bulkAction.setProperties(properties);

		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		checkResultLrt(processAction, 1l, null, null);

		List<SysProvisioningOperationDto> newOperations = provisioningOperationService.find(filter, null).getContent();
		assertEquals(operations.size(), newOperations.size());
	}

	@Test
	public void testRetryWithFilterAndReturnToQueue() {
		IdmRoleDto role = getHelper().createRole();
		IdmIdentityDto identity = getHelper().createIdentity();

		SysSystemDto system = helper.createTestResourceSystem(true);
		system.setReadonly(true);
		system = systemService.save(system);

		helper.createRoleSystem(role, system);
		getHelper().createIdentityRole(identity, role);

		identityService.save(identity);
		identityService.save(identity);
		identityService.save(identity);

		SysProvisioningOperationFilter filter = new SysProvisioningOperationFilter();
		filter.setEntityIdentifier(identity.getId());
		filter.setEntityType(SystemEntityType.IDENTITY);
		List<SysProvisioningOperationDto> operations = provisioningOperationService.find(filter, null).getContent();
		assertFalse(operations.isEmpty());
		assertTrue(operations.size() > 1);

		IdmBulkActionDto bulkAction = this.findBulkAction(SysProvisioningOperation.class, ProvisioningOperationRetryBulkAction.NAME);
		bulkAction.setFilter(toMap(filter));
		bulkAction.setTransformedFilter(filter);

		Map<String, Object> properties = new HashMap<>();
		properties.put(ProvisioningOperationRetryBulkAction.RETRY_WHOLE_BATCH_CODE, Boolean.FALSE.toString());
		bulkAction.setProperties(properties);

		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		checkResultLrt(processAction, (long)operations.size(), null, null); // Multipled by 2
		checkProcessItemsCount(processAction, operations.size());

		List<SysProvisioningOperationDto> newOperations = provisioningOperationService.find(filter, null).getContent();
		assertEquals(operations.size(), newOperations.size());
	}

	@Test
	public void testRetryOneItem() {
		IdmRoleDto role = getHelper().createRole();
		IdmIdentityDto identity = getHelper().createIdentity();

		SysSystemDto system = helper.createTestResourceSystem(true);
		system.setReadonly(true);
		system = systemService.save(system);

		helper.createRoleSystem(role, system);
		getHelper().createIdentityRole(identity, role);

		identityService.save(identity);
		identityService.save(identity);
		identityService.save(identity);

		SysProvisioningOperationFilter filter = new SysProvisioningOperationFilter();
		filter.setEntityIdentifier(identity.getId());
		filter.setEntityType(SystemEntityType.IDENTITY);
		List<SysProvisioningOperationDto> operations = provisioningOperationService.find(filter, null).getContent();
		assertFalse(operations.isEmpty());
		assertTrue(operations.size() > 1);

		system.setReadonly(false);
		system = systemService.save(system);
		
		IdmBulkActionDto bulkAction = this.findBulkAction(SysProvisioningOperation.class, ProvisioningOperationRetryBulkAction.NAME);

		// We found just one
		SysProvisioningOperationDto provisioningOperationDto = operations.stream().findAny().get();
		bulkAction.setIdentifiers(Sets.newHashSet(provisioningOperationDto.getId()));
		
		Map<String, Object> properties = new HashMap<>();
		properties.put(ProvisioningOperationRetryBulkAction.RETRY_WHOLE_BATCH_CODE, Boolean.FALSE.toString());
		bulkAction.setProperties(properties);

		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		checkResultLrt(processAction, 1l, null, null);

		List<SysProvisioningOperationDto> newOperations = provisioningOperationService.find(filter, null).getContent();
		assertEquals(operations.size() - 1, newOperations.size());
	}

	@Test
	public void testRetryFullBatch() {
		IdmRoleDto role = getHelper().createRole();
		IdmIdentityDto identity = getHelper().createIdentity();

		SysSystemDto system = helper.createTestResourceSystem(true);
		system.setReadonly(true);
		system = systemService.save(system);

		helper.createRoleSystem(role, system);
		getHelper().createIdentityRole(identity, role);

		identityService.save(identity);
		identityService.save(identity);
		identityService.save(identity);

		SysProvisioningOperationFilter filter = new SysProvisioningOperationFilter();
		filter.setEntityIdentifier(identity.getId());
		filter.setEntityType(SystemEntityType.IDENTITY);
		List<SysProvisioningOperationDto> operations = provisioningOperationService.find(filter, null).getContent();
		assertFalse(operations.isEmpty());
		assertTrue(operations.size() > 1);

		system.setReadonly(false);
		system = systemService.save(system);
		
		IdmBulkActionDto bulkAction = this.findBulkAction(SysProvisioningOperation.class, ProvisioningOperationRetryBulkAction.NAME);

		// We found just one
		SysProvisioningOperationDto provisioningOperationDto = operations.stream().findAny().get();
		bulkAction.setIdentifiers(Sets.newHashSet(provisioningOperationDto.getId()));
		
		Map<String, Object> properties = new HashMap<>();
		properties.put(ProvisioningOperationRetryBulkAction.RETRY_WHOLE_BATCH_CODE, Boolean.TRUE.toString());
		bulkAction.setProperties(properties);

		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		checkResultLrt(processAction, 1l, null, null);
		checkProcessItemsCount(processAction, 1);

		List<SysProvisioningOperationDto> newOperations = provisioningOperationService.find(filter, null).getContent();
		assertEquals(0, newOperations.size());
	}

	@Test
	public void testRetryFullBatchWithFilter() {
		IdmRoleDto role = getHelper().createRole();
		IdmIdentityDto identity = getHelper().createIdentity();

		SysSystemDto system = helper.createTestResourceSystem(true);
		system.setReadonly(true);
		system = systemService.save(system);

		helper.createRoleSystem(role, system);
		getHelper().createIdentityRole(identity, role);

		identityService.save(identity);
		identityService.save(identity);
		identityService.save(identity);

		SysProvisioningOperationFilter filter = new SysProvisioningOperationFilter();
		filter.setEntityIdentifier(identity.getId());
		filter.setEntityType(SystemEntityType.IDENTITY);
		List<SysProvisioningOperationDto> operations = provisioningOperationService.find(filter, null).getContent();
		assertFalse(operations.isEmpty());
		assertTrue(operations.size() > 1);

		system.setReadonly(false);
		system = systemService.save(system);
		
		IdmBulkActionDto bulkAction = this.findBulkAction(SysProvisioningOperation.class, ProvisioningOperationRetryBulkAction.NAME);

		bulkAction.setFilter(toMap(filter));
		bulkAction.setTransformedFilter(filter);

		Map<String, Object> properties = new HashMap<>();
		properties.put(ProvisioningOperationRetryBulkAction.RETRY_WHOLE_BATCH_CODE, Boolean.TRUE.toString());
		bulkAction.setProperties(properties);

		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		checkResultLrt(processAction, 1l, null, null);
		checkProcessItemsCount(processAction, operations.size());

		List<SysProvisioningOperationDto> newOperations = provisioningOperationService.find(filter, null).getContent();
		assertEquals(0, newOperations.size());
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

		system.setDisabled(false);
		system = systemService.save(system);

		operations = provisioningOperationService.find(filter, null).getContent();
		assertFalse(operations.isEmpty());

		IdmBulkActionDto bulkAction = this.findBulkAction(SysProvisioningOperation.class, ProvisioningOperationRetryBulkAction.NAME);
		bulkAction.setTransformedFilter(filter);
		bulkAction.setFilter(filter.getData().toSingleValueMap()); // FIXME: can be multivalued (attributes) ...

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
