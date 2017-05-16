package eu.bcvsolutions.idm.core.utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.joda.time.LocalDate;
import org.junit.Test;

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
}
