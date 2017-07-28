package eu.bcvsolutions.idm.core.model.service.impl;

import static org.junit.Assert.*;

import java.util.UUID;

import org.joda.time.LocalDate;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.api.domain.IdmPasswordPolicyType;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmPasswordDto;
import eu.bcvsolutions.idm.core.model.entity.IdmPasswordPolicy;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityService;
import eu.bcvsolutions.idm.core.model.service.api.IdmPasswordPolicyService;
import eu.bcvsolutions.idm.core.model.service.api.IdmPasswordService;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Password service integration test.
 * 
 * @author Jan Helbich
 */
@Transactional // we need rollback after each test
public class DefaultIdmPasswordServiceIntegrationTest extends AbstractIntegrationTest {

	@Autowired private IdmPasswordService passwordService;
	@Autowired private IdmPasswordPolicyService policyService;
	@Autowired private IdmIdentityService identityService;

	@Before
	public void before() {
		this.loginAsAdmin("[DefaultIdmPasswordServiceIntegrationTest]");
		removeDefaultPolicy();
	}

	@After
	public void after() {
		this.logout();
	}

	@Test
	public void testCreatePasswordNoPolicy() {
		IdmIdentityDto i = getTestIdentity();
		IdmPasswordDto password = passwordService.findOneByIdentity(i.getId());
		assertEquals(LocalDate.now(), password.getValidFrom());
		assertEquals(i.getId(), password.getIdentity());
		assertNull(password.getValidTill());
	}

	@Test
	public void testCreatePasswordNonDefaultPolicy() {
		IdmPasswordPolicy p = getTestPolicy(false);
		IdmIdentityDto i = getTestIdentity();
		//
		IdmPasswordDto password = passwordService.findOneByIdentity(i.getId());
		assertEquals(LocalDate.now(), password.getValidFrom());
		assertEquals(i.getId(), password.getIdentity());
		assertEquals(LocalDate.now().plusDays(p.getMaxPasswordAge()), password.getValidTill());
	}

	@Test
	public void testCreatePasswordDefaultPolicy() {
		IdmPasswordPolicy p = getTestPolicy(true);
		IdmIdentityDto i = getTestIdentity();
		//
		IdmPasswordDto password = passwordService.findOneByIdentity(i.getId());
		assertEquals(LocalDate.now(), password.getValidFrom());
		assertEquals(i.getId(), password.getIdentity());
		assertEquals(LocalDate.now().plusDays(p.getMaxPasswordAge()), password.getValidTill());
	}

	@Test
	public void testCreatePasswordMultiplePolicies() {
		IdmPasswordPolicy p1 = getTestPolicy(false, IdmPasswordPolicyType.GENERATE, 365);
		IdmPasswordPolicy p2 = getTestPolicy(false, IdmPasswordPolicyType.GENERATE, 5);
		IdmIdentityDto i = getTestIdentity();
		//
		IdmPasswordDto password = passwordService.findOneByIdentity(i.getId());
		assertEquals(LocalDate.now(), password.getValidFrom());
		assertEquals(i.getId(), password.getIdentity());
		if (p1.getName().compareTo(p2.getName()) < 0) {
			assertEquals(LocalDate.now().plusDays(p1.getMaxPasswordAge()), password.getValidTill());
		} else {
			assertEquals(LocalDate.now().plusDays(p2.getMaxPasswordAge()), password.getValidTill());
		}
	}

	@Test
	public void testCreatePasswordValidationPolicy() {
		IdmPasswordPolicy p = getTestPolicy(false, IdmPasswordPolicyType.VALIDATE, 365);
		IdmIdentityDto i = getTestIdentity();
		//
		IdmPasswordDto password = passwordService.findOneByIdentity(i.getId());
		assertEquals(LocalDate.now(), password.getValidFrom());
		assertEquals(i.getId(), password.getIdentity());
		assertNull(password.getValidTill());
	}

	private IdmPasswordPolicy getTestPolicy(boolean isDefault, IdmPasswordPolicyType type, int maxAge) {
		IdmPasswordPolicy p = new IdmPasswordPolicy();
		p.setName(UUID.randomUUID().toString());
		p.setType(type);
		p.setMaxPasswordAge(maxAge);
		p.setDefaultPolicy(isDefault);
		return policyService.save(p);
	}

	private IdmPasswordPolicy getTestPolicy(boolean isDefault) {
		return getTestPolicy(isDefault, IdmPasswordPolicyType.GENERATE, 365);
	}

	private IdmIdentityDto getTestIdentity() {
		IdmIdentityDto i = new IdmIdentityDto();
		i.setUsername(UUID.randomUUID().toString());
		i.setLastName("last name");
		i.setPassword(new GuardedString("test"));
		return identityService.save(i);
	}

	private void removeDefaultPolicy() {
		// I need to get rid of default policy defined in init test data
		policyService.find(null).forEach(p -> policyService.delete(p));
	}

}