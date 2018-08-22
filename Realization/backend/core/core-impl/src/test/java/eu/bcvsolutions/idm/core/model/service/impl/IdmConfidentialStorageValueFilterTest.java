package eu.bcvsolutions.idm.core.model.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.Serializable;
import java.util.UUID;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.api.domain.Identifiable;
import eu.bcvsolutions.idm.core.api.dto.IdmConfidentialStorageValueDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmConfidentialStorageValueFilter;
import eu.bcvsolutions.idm.core.api.service.IdmConfidentialStorageValueService;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Filter tests for Confidential Storage Value Filter
 * 
 * @author Patrik Stloukal
 * @author Radek Tomiška
 */
@Transactional
public class IdmConfidentialStorageValueFilterTest extends AbstractIntegrationTest {

	@Autowired private IdmConfidentialStorageValueService confidentialValueService;
	@Autowired private DefaultIdmConfidentialStorage defaultStorageService;

	@Test
	public void testOwnerIdFilter() {
		IdmIdentityDto human = getHelper().createIdentity((GuardedString) null);
		String key = "test001";
		createValue(human, IdmIdentity.class, key, UUID.randomUUID());
		IdmConfidentialStorageValueFilter filter = new IdmConfidentialStorageValueFilter();
		filter.setOwnerId(human.getId());
		filter.setKey(key);
		Page<IdmConfidentialStorageValueDto> result = confidentialValueService.find(filter, null);
		assertEquals(1, result.getTotalElements());
		//
		checkTrimmedValue(GuardedString.SECRED_PROXY_STRING, result.getContent().get(0));
	}

	@Test
	public void testOwnerTypeFilter() {
		IdmIdentityDto human = getHelper().createIdentity((GuardedString) null);
		String key = "test002";
		createValue(human, IdmIdentity.class, key, UUID.randomUUID());
		IdmConfidentialStorageValueFilter filter = new IdmConfidentialStorageValueFilter();
		filter.setOwnerType(IdmIdentity.class.getName());
		filter.setKey(key);
		Page<IdmConfidentialStorageValueDto> result = confidentialValueService.find(filter, null);
		assertEquals(1, result.getTotalElements());
		//
		checkTrimmedValue(GuardedString.SECRED_PROXY_STRING, result.getContent().get(0));
	}

	@Test
	public void testKeyFilter() {
		IdmIdentityDto human = getHelper().createIdentity((GuardedString) null);
		String key = "test003 " + System.currentTimeMillis();
		createValue(human, IdmIdentity.class, key, UUID.randomUUID());
		IdmConfidentialStorageValueFilter filter = new IdmConfidentialStorageValueFilter();
		filter.setKey(key);
		Page<IdmConfidentialStorageValueDto> result = confidentialValueService.find(filter, null);
		assertEquals(1, result.getTotalElements());
		//
		checkTrimmedValue(GuardedString.SECRED_PROXY_STRING, result.getContent().get(0));
	}

	@Test
	public void testTextFilter() {
		IdmIdentityDto human = getHelper().createIdentity((GuardedString) null);
		String key = "test004 " + System.currentTimeMillis();
		createValue(human, IdmIdentity.class, key, UUID.randomUUID());
		IdmConfidentialStorageValueFilter filter = new IdmConfidentialStorageValueFilter();
		filter.setText(key);
		Page<IdmConfidentialStorageValueDto> result = confidentialValueService.find(filter, null);
		assertEquals(1, result.getTotalElements());
		//
		checkTrimmedValue(GuardedString.SECRED_PROXY_STRING, result.getContent().get(0));
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

	/**
	 * Get {@link IdmConfidentialStorageValueDto} as not trimmed and check if value is same as given in parameters.
	 *
	 * @param serializable
	 * @param trimmedValue
	 */
	private void checkTrimmedValue(Serializable serializable, IdmConfidentialStorageValueDto trimmedValue) {
		// for trimmed isn't value transformed
		IdmConfidentialStorageValueDto foundedValue = confidentialValueService.get(trimmedValue.getId());
		assertNotNull(foundedValue);
		assertEquals(serializable, foundedValue.getSerializableValue());
	}
}
