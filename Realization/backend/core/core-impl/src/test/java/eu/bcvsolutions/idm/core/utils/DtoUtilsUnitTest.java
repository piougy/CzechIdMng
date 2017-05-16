package eu.bcvsolutions.idm.core.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.UUID;

import org.junit.Test;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.test.api.AbstractUnitTest;

/**
 * Common dto helpers test
 * 
 * @author Radek Tomiška
 *
 */
public class DtoUtilsUnitTest extends AbstractUnitTest {
	
	private static final String PROPERTY = "identityContract"; // IdmIdentityRole_.identityContract could not be used in unit test (see DtoUtilsIntegrationTest)

	@Test
	public void testEmbedded() {
		IdmIdentityRoleDto dto = new IdmIdentityRoleDto();
		IdmIdentityContractDto contract = new IdmIdentityContractDto(UUID.randomUUID());
		dto.getEmbedded().put(PROPERTY, contract);
		//
		IdmIdentityContractDto embedded = DtoUtils.getEmbedded(dto, PROPERTY, IdmIdentityContractDto.class);
		assertEquals(contract, embedded);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testEmptyEmbeddedWIthoutDefault() {
		IdmIdentityRoleDto dto = new IdmIdentityRoleDto();
		//
		DtoUtils.getEmbedded(dto, PROPERTY, IdmIdentityContractDto.class);
	}
	
	@Test
	public void testEmbeddedWithtDefault() {
		IdmIdentityRoleDto dto = new IdmIdentityRoleDto();
		//
		assertNull(DtoUtils.getEmbedded(dto, PROPERTY, IdmIdentityContractDto.class, null));
	}
}
