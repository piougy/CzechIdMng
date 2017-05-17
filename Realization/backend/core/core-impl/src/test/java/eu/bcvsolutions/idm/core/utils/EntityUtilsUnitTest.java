package eu.bcvsolutions.idm.core.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.joda.time.LocalDate;
import org.junit.Test;

import eu.bcvsolutions.idm.core.CoreModuleDescriptor;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.utils.EntityUtils;
import eu.bcvsolutions.idm.test.api.AbstractUnitTest;

/**
 * Common entity helpers tests
 * 
 * @author Radek Tomiška
 *
 */
public class EntityUtilsUnitTest extends AbstractUnitTest {

	@Test
	public void testIsValidWithoutDates() {
		IdmIdentityRoleDto role = new IdmIdentityRoleDto();
		//
		assertTrue(EntityUtils.isValid(role));
	}
	
	@Test
	public void testIsNotValid_TillInHistory() {
		IdmIdentityRoleDto role = new IdmIdentityRoleDto();
		role.setValidTill(new LocalDate().minusDays(1));
		//
		assertFalse(EntityUtils.isValid(role));
	}
	
	@Test
	public void testIsNotValid_ValidInFuture() {
		IdmIdentityRoleDto role = new IdmIdentityRoleDto();
		role.setValidFrom(new LocalDate().plusDays(1));
		role.setValidTill(new LocalDate().plusDays(2));
		//
		assertFalse(EntityUtils.isValid(role));
	}
	
	@Test
	public void testIsValidInFuture() {
		IdmIdentityRoleDto role = new IdmIdentityRoleDto();
		role.setValidFrom(new LocalDate().plusDays(1));
		role.setValidTill(new LocalDate().plusDays(2));
		//
		assertTrue(EntityUtils.isValidInFuture(role));
	}
	
	@Test
	public void testIsValidNowOrInFuture() {
		IdmIdentityRoleDto role = new IdmIdentityRoleDto();
		role.setValidFrom(new LocalDate().plusDays(1));
		role.setValidTill(new LocalDate().plusDays(2));
		//
		assertTrue(EntityUtils.isValidNowOrInFuture(role));
	}
	
	@Test 
	public void testValidableChanged() {
		LocalDate now = new LocalDate();
		IdmIdentityRoleDto role = new IdmIdentityRoleDto();
		role.setValidFrom(now.plusDays(1));
		role.setValidTill(now.plusDays(2));
		//
		IdmIdentityRoleDto role2 = new IdmIdentityRoleDto();
		role.setValidFrom(now.plusDays(1));
		role.setValidTill(now.plusDays(3));
		//
		assertTrue(EntityUtils.validableChanged(role, role2));
	}
	
	@Test 
	public void testValidableNotChanged() {
		LocalDate now = new LocalDate();
		IdmIdentityRoleDto role = new IdmIdentityRoleDto();
		role.setValidFrom(now.plusDays(1));
		role.setValidTill(now.plusDays(2));
		//
		IdmIdentityRoleDto role2 = new IdmIdentityRoleDto();
		role.setValidFrom(now.plusDays(1));
		role.setValidTill(now.plusDays(2));
		//
		assertTrue(EntityUtils.validableChanged(role, role2));
	}
	
	@Test
	public void testGetModule() {
		assertEquals(CoreModuleDescriptor.MODULE_ID, EntityUtils.getModule(IdmIdentityRoleDto.class));
	}
	
	@Test
	public void testToUuidValid() {
		UUID uuid = UUID.randomUUID();
		assertNull(EntityUtils.toUuid(null));
		assertEquals(uuid, EntityUtils.toUuid(uuid));
		assertEquals(uuid, EntityUtils.toUuid(uuid.toString()));
		assertEquals(UUID.fromString("00000000-0000-0000-0000-000000000000"), EntityUtils.toUuid("00000000-0000-0000-0000-000000000000"));
	}
	
	@Test(expected = ClassCastException.class)
	public void testToUuidInvalid() {
		EntityUtils.toUuid("");
	}
	
	@Test(expected = ClassCastException.class)
	public void testToUuidInvalid2() {
		EntityUtils.toUuid("aaaa");
	}
}

