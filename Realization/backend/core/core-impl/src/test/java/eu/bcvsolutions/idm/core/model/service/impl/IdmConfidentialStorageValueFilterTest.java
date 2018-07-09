package eu.bcvsolutions.idm.core.model.service.impl;

import static org.junit.Assert.assertEquals;

import java.io.Serializable;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import eu.bcvsolutions.idm.core.api.domain.Identifiable;
import eu.bcvsolutions.idm.core.api.dto.IdmConfidentialStorageValueDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmConfidentialStorageValueFilter;
import eu.bcvsolutions.idm.core.api.service.IdmConfidentialStorageValueService;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;
import eu.bcvsolutions.idm.test.api.TestHelper;

/**
 * Filter tests for Confidential Storage Value Filter
 * 
 * @author Patrik Stloukal
 *
 */
public class IdmConfidentialStorageValueFilterTest extends AbstractIntegrationTest {

	@Autowired
	private IdmConfidentialStorageValueService idmConfidentialValueService;
	@Autowired
	private DefaultIdmConfidentialStorage defaultStorageService;
	@Autowired
	private TestHelper testHelper;

	@Before
	public void login() {
		loginAsAdmin();
	}

	@After
	public void logout() {
		super.logout();
	}

	@Test
	public void testOwnerIdFilter() {
		IdmIdentityDto human = testHelper.createIdentity();
		String key = "test001";
		createValue(human, IdmIdentity.class, key, UUID.randomUUID());
		IdmConfidentialStorageValueFilter filter = new IdmConfidentialStorageValueFilter();
		filter.setOwnerId(human.getId());
		filter.setKey(key);
		Page<IdmConfidentialStorageValueDto> result = idmConfidentialValueService.find(filter, null);
		Serializable serializable = defaultStorageService.get(human.getId(), IdmIdentity.class, key);
		assertEquals(1, result.getTotalElements());
		assertEquals(serializable, result.getContent().get(0).getSerializableValue());
	}

	@Test
	public void testOwnerTypeFilter() {
		IdmIdentityDto human = testHelper.createIdentity();
		String key = "test002";
		createValue(human, IdmIdentity.class, key, UUID.randomUUID());
		IdmConfidentialStorageValueFilter filter = new IdmConfidentialStorageValueFilter();
		filter.setOwnerType(IdmIdentity.class.getName());
		filter.setKey(key);
		Page<IdmConfidentialStorageValueDto> result = idmConfidentialValueService.find(filter, null);
		Serializable serializable = defaultStorageService.get(human.getId(), IdmIdentity.class, key);
		assertEquals(1, result.getTotalElements());
		assertEquals(serializable, result.getContent().get(0).getSerializableValue());
	}

	@Test
	public void testKeyFilter() {
		IdmIdentityDto human = testHelper.createIdentity();
		String key = "test003 " + System.currentTimeMillis();
		createValue(human, IdmIdentity.class, key, UUID.randomUUID());
		IdmConfidentialStorageValueFilter filter = new IdmConfidentialStorageValueFilter();
		filter.setKey(key);
		Page<IdmConfidentialStorageValueDto> result = idmConfidentialValueService.find(filter, null);
		Serializable serializable = defaultStorageService.get(human.getId(), IdmIdentity.class, key);
		assertEquals(1, result.getTotalElements());
		assertEquals(serializable, result.getContent().get(0).getSerializableValue());
	}

	@Test
	public void testTextFilter() {
		IdmIdentityDto human = testHelper.createIdentity();
		String key = "test004 " + System.currentTimeMillis();
		createValue(human, IdmIdentity.class, key, UUID.randomUUID());
		IdmConfidentialStorageValueFilter filter = new IdmConfidentialStorageValueFilter();
		filter.setText(key);
		Page<IdmConfidentialStorageValueDto> result = idmConfidentialValueService.find(filter, null);
		Serializable serializable = defaultStorageService.get(human.getId(), IdmIdentity.class, key);
		assertEquals(1, result.getTotalElements());
		assertEquals(serializable, result.getContent().get(0).getSerializableValue());
	}

	/**
	 * Creates record in confidential storage repository
	 * 
	 * @param owner, ownerType, key, value
	 */
	private void createValue(Identifiable owner, Class<? extends Identifiable> ownerType, String key,
			Serializable value) {
		defaultStorageService.save(UUID.fromString(owner.getId().toString()), ownerType, key, value);
	}
}
