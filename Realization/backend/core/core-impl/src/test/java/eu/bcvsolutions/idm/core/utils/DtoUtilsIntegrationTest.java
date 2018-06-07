package eu.bcvsolutions.idm.core.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.UUID;

import org.junit.Test;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole_;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Common dto helpers integration test - jpa metamodel is used.
 * 
 * @author Radek Tomiška
 *
 */
public class DtoUtilsIntegrationTest extends AbstractIntegrationTest {

	@Test
	public void testEmbedded() {
		IdmIdentityRoleDto dto = new IdmIdentityRoleDto();
		IdmIdentityContractDto contract = new IdmIdentityContractDto(UUID.randomUUID());
		dto.getEmbedded().put(IdmIdentityRole_.identityContract.getName(), contract);
		//
		IdmIdentityContractDto embedded = DtoUtils.getEmbedded(dto, IdmIdentityRole_.identityContract);
		assertEquals(contract, embedded);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testEmptyEmbeddedWIthoutDefault() {
		IdmIdentityRoleDto dto = new IdmIdentityRoleDto();
		//
		DtoUtils.getEmbedded(dto, IdmIdentityRole_.identityContract);
	}
	
	@Test
	public void testEmbeddedWithtDefault() {
		IdmIdentityRoleDto dto = new IdmIdentityRoleDto();
		//
		assertNull(DtoUtils.getEmbedded(dto, IdmIdentityRole_.identityContract, (IdmIdentityContractDto) null));
	}
}
