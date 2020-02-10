package eu.bcvsolutions.idm.acc.bulk.action.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.text.MessageFormat;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.ImmutableSet;

import eu.bcvsolutions.idm.acc.TestHelper;
import eu.bcvsolutions.idm.acc.domain.SystemOperationType;
import eu.bcvsolutions.idm.acc.dto.SysSystemAttributeMappingDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemMappingFilter;
import eu.bcvsolutions.idm.acc.entity.SysSystemMapping;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemMappingService;
import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.test.api.AbstractBulkActionTest;

/**
 * Integration tests for {@link SystemMappingDuplicateBulkAction}
 *
 * @author Ondrej Husnik
 *
 */

public class SystemMappingDuplicateBulkActionTest extends AbstractBulkActionTest {

	@Autowired
	private TestHelper helper;
	@Autowired
	private SysSystemMappingService mappingService;
	@Autowired
	private SysSystemAttributeMappingService attrMappingService;

	@Before
	public void login() {
		IdmIdentityDto adminIdentity = this.createUserWithAuthorities(IdmBasePermission.UPDATE, IdmBasePermission.READ);
		loginAsNoAdmin(adminIdentity.getUsername());
	}

	@After
	public void logout() {
		super.logout();
	}

	@Test
	public void processBulkActionByIds() {
		SysSystemDto system = helper.createTestResourceSystem(true, getHelper().createName());

		SysSystemMappingFilter filter = new SysSystemMappingFilter();
		filter.setSystemId(system.getId());
		List<SysSystemMappingDto> mappings = mappingService.find(filter, null).getContent();

		// Contains only one created system mapping
		assertEquals(1, mappings.size());

		SysSystemMappingDto origMapping = mappings.get(0);
		IdmBulkActionDto bulkAction = this.findBulkAction(SysSystemMapping.class,
				SystemMappingDuplicateBulkAction.NAME);
		bulkAction.setIdentifiers(ImmutableSet.of(origMapping.getId()));
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		checkResultLrt(processAction, 1l, null, null);

		// Some new cloned mappings were created
		mappings = mappingService.find(filter, null).getContent();
		assertTrue(mappings.size() > 1);

		compareCloningResult(origMapping, mappings);
	}

	@Test
	public void processBulkActionByFilter() {
		SysSystemDto system = helper.createTestResourceSystem(true, getHelper().createName());

		SysSystemMappingFilter filter = new SysSystemMappingFilter();
		filter.setSystemId(system.getId());
		List<SysSystemMappingDto> mappings = mappingService.find(filter, null).getContent();

		// Contains only one created system mapping
		assertEquals(1, mappings.size());

		SysSystemMappingDto origMapping = mappings.get(0);
		IdmBulkActionDto bulkAction = this.findBulkAction(SysSystemMapping.class,
				SystemMappingDuplicateBulkAction.NAME);
		bulkAction.setTransformedFilter(filter);
		bulkAction.setFilter(toMap(filter));
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		checkResultLrt(processAction, 1l, null, null);

		// Some new cloned mappings were created
		mappings = mappingService.find(filter, null).getContent();
		assertTrue(mappings.size() > 1);

		compareCloningResult(origMapping, mappings);
	}

	private void compareCloningResult(SysSystemMappingDto origMapping, List<SysSystemMappingDto> otherMappings) {
		List<SysSystemMappingDto> clonedMappings = otherMappings //
				.stream() //
				.filter(mapping -> {
					return !mapping.getId().equals(origMapping.getId())
							&& mapping.getName().contains(origMapping.getName());
				}).collect(Collectors.toList()); //

		assertEquals(1, clonedMappings.size());

		SysSystemMappingDto clonedMapping = clonedMappings.get(0);

		// Expected name matches
		assertTrue(clonedMapping.getName().equals(MessageFormat.format("{0}{1}", "Copy-of-", origMapping.getName())));

		// Test that all cloned mappings are turned to synchronization type -> same
		// provisioning types are not allowed
		assertTrue(SystemOperationType.SYNCHRONIZATION.equals(clonedMapping.getOperationType()));

		// UUID of both mappings has to be different
		assertNotEquals(origMapping.getId(), clonedMapping.getId());

		// Original and cloned mappings contain same count of mapped attributes
		List<SysSystemAttributeMappingDto> attrMappingOrig = attrMappingService.findBySystemMapping(clonedMapping);
		List<SysSystemAttributeMappingDto> attrMappingCloned = attrMappingService.findBySystemMapping(origMapping);
		assertEquals(attrMappingCloned.size(), attrMappingOrig.size());
	}
}
