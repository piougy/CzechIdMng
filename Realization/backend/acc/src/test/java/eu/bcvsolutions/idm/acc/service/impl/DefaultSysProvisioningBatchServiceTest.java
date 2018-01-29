package eu.bcvsolutions.idm.acc.service.impl;

import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningBatchDto;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningBatchService;
import eu.bcvsolutions.idm.core.api.dto.filter.EmptyFilter;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Searching entities, using filters
 *
 * @author Petr Hanák
 *
 */
@Transactional
public class DefaultSysProvisioningBatchServiceTest extends AbstractIntegrationTest {

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
	public void emptyFilterTest() {
		IdmBasePermission permission = IdmBasePermission.ADMIN;

		SysProvisioningBatchDto provisioningBatch = new SysProvisioningBatchDto();
		provisioningBatch.setId(UUID.randomUUID());
		batchService.save(provisioningBatch);

		EmptyFilter filter = new EmptyFilter();

		Page<SysProvisioningBatchDto> result = batchService.find(filter, null, permission);
		assertTrue(result.getContent().contains(provisioningBatch));
	}

}
