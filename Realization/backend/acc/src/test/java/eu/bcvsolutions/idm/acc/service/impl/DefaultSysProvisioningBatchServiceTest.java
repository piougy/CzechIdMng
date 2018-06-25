package eu.bcvsolutions.idm.acc.service.impl;

import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.acc.TestHelper;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningBatchDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemEntityDto;
import eu.bcvsolutions.idm.acc.entity.TestResource;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningBatchService;
import eu.bcvsolutions.idm.core.api.dto.filter.EmptyFilter;
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

	@Autowired private SysProvisioningBatchService batchService;

	@Test
	public void emptyFilterTest() {
		IdmBasePermission permission = IdmBasePermission.ADMIN;
		
		SysSystemEntityDto createSystemEntity = ((TestHelper) getHelper()).createSystemEntity(((TestHelper) getHelper()).createSystem(TestResource.TABLE_NAME));

		SysProvisioningBatchDto provisioningBatch = new SysProvisioningBatchDto();
		provisioningBatch.setId(UUID.randomUUID());
		provisioningBatch.setSystemEntity(createSystemEntity.getId());
		batchService.save(provisioningBatch);

		EmptyFilter filter = new EmptyFilter();

		Page<SysProvisioningBatchDto> result = batchService.find(filter, null, permission);
		assertTrue(result.getContent().contains(provisioningBatch));
	}

}
