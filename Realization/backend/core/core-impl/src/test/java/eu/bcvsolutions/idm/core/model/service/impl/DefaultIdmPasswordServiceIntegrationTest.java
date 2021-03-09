package eu.bcvsolutions.idm.core.model.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.api.domain.IdentityState;
import eu.bcvsolutions.idm.core.api.domain.IdmPasswordPolicyType;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmPasswordDto;
import eu.bcvsolutions.idm.core.api.dto.IdmPasswordPolicyDto;
import eu.bcvsolutions.idm.core.api.dto.PasswordChangeDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmPasswordFilter;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.IdmPasswordPolicyService;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.security.api.dto.LoginDto;
import eu.bcvsolutions.idm.core.security.api.exception.IdmAuthenticationException;
import eu.bcvsolutions.idm.core.security.rest.impl.LoginController;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Password service integration test.
 * 
 * @author Jan Helbich
 * @author Petr Hanák
 * @author Radek Tomiška
 */
@Transactional
public class DefaultIdmPasswordServiceIntegrationTest extends AbstractIntegrationTest {

	@Autowired private ApplicationContext context;
	@Autowired private IdmPasswordPolicyService policyService;
	@Autowired private IdmIdentityService identityService;
	@Autowired private LoginController loginController;
	//
	private DefaultIdmPasswordService passwordService;

	@Before
	public void before() {
		passwordService = context.getAutowireCapableBeanFactory().createBean(DefaultIdmPasswordService.class);
		removeDefaultPolicy();
	}

	@Test
	public void testCreatePasswordNoPolicy() {
		IdmIdentityDto identity = getHelper().createIdentity();
		IdmPasswordDto password = passwordService.findOneByIdentity(identity.getId());
		Assert.assertNull(password.getValidFrom());
		assertEquals(identity.getId(), password.getIdentity());
		assertNull(password.getValidTill());
	}

	@Test
	public void testCreatePasswordNonDefaultPolicy() {
		IdmPasswordPolicyDto policy = getTestPolicy(false);
		assertNotNull(policy);
		IdmIdentityDto identity = getHelper().createIdentity();
		//
		IdmPasswordDto password = passwordService.findOneByIdentity(identity.getId());
		Assert.assertNull(password.getValidFrom());
		Assert.assertEquals(identity.getId(), password.getIdentity());
		// when not exists default validation policy valid till be null
		Assert.assertNull(password.getValidTill());
	}

	@Test
	public void testCreatePasswordDefaultPolicy() {
		IdmPasswordPolicyDto policy = getTestPolicy(true);
		IdmIdentityDto identity = getHelper().createIdentity();
		//
		IdmPasswordDto password = passwordService.findOneByIdentity(identity.getId());
		Assert.assertNull(password.getValidFrom());
		assertEquals(identity.getId(), password.getIdentity());
		assertEquals(LocalDate.now().plusDays(policy.getMaxPasswordAge()), password.getValidTill());
	}

	@Test
	public void testCreatePasswordMultiplePolicies() {
		IdmPasswordPolicyDto policy1 = getTestPolicy(true, IdmPasswordPolicyType.VALIDATE, 365);
		assertNotNull(policy1);
		IdmPasswordPolicyDto policy2 = getTestPolicy(true, IdmPasswordPolicyType.VALIDATE, 5);
		IdmIdentityDto identity = getHelper().createIdentity();
		//
		IdmPasswordDto password = passwordService.findOneByIdentity(identity.getId());
		Assert.assertNull(password.getValidFrom());
		Assert.assertEquals(identity.getId(), password.getIdentity());
		// default password policy may be only one
		Assert.assertEquals(LocalDate.now().plusDays(policy2.getMaxPasswordAge()), password.getValidTill());
	}
	
	@Test
	public void testTwoPoliciesSecondValidTillNull() {
		IdmPasswordPolicyDto policy1 = getTestPolicy(false, IdmPasswordPolicyType.VALIDATE, null);
		IdmPasswordPolicyDto policy2 = getTestPolicy(true, IdmPasswordPolicyType.VALIDATE, 5);
		IdmIdentityDto identity = getHelper().createIdentity();
		//
		IdmPasswordDto password = passwordService.findOneByIdentity(identity.getId());
		Assert.assertNull(password.getValidFrom());
		assertEquals(identity.getId(), password.getIdentity());
		assertEquals(LocalDate.now().plusDays(policy2.getMaxPasswordAge()), password.getValidTill());
		//
		policy1.setDefaultPolicy(true);
		policy1 = policyService.save(policy1);
		PasswordChangeDto passwordChangeDto = new PasswordChangeDto();
		passwordChangeDto.setAll(true);
		passwordChangeDto.setIdm(true);
		passwordChangeDto.setOldPassword(identity.getPassword());
		passwordChangeDto.setNewPassword(new GuardedString("testPassword"));
		//
		try {
			getHelper().login(identity);
			identityService.passwordChange(identity, passwordChangeDto);
			password = passwordService.findOneByIdentity(identity.getId());
			Assert.assertNotNull(password.getValidFrom());
			Assert.assertNull(password.getValidTill());
		} finally {
			logout();
		}
	}

	@Test
	public void testCreatePasswordValidationPolicy() {
		getTestPolicy(false, IdmPasswordPolicyType.VALIDATE, 365);
		IdmIdentityDto identity = getHelper().createIdentity();
		//
		IdmPasswordDto password = passwordService.findOneByIdentity(identity.getId());
		Assert.assertNull(password.getValidFrom());
		Assert.assertEquals(identity.getId(), password.getIdentity());
		Assert.assertNull(password.getValidTill());
	}

	@Test
	@Transactional
	public void testIncreaseUnsuccessfulAttempts() {
		IdmIdentityDto identity = getHelper().createIdentity();
		passwordService.increaseUnsuccessfulAttempts(identity.getUsername());
		passwordService.increaseUnsuccessfulAttempts(identity.getUsername());
		//
		assertEquals(2, passwordService.findOneByIdentity(identity.getId()).getUnsuccessfulAttempts());
	}

	@Test
	@Transactional
	public void testSetLastSuccessfulLogin() {
		IdmIdentityDto identity = getHelper().createIdentity();
		passwordService.setLastSuccessfulLogin(identity.getUsername());
		//
		assertNotNull(passwordService.findOneByIdentity(identity.getId()).getLastSuccessfulLogin());
		assertTrue(ZonedDateTime.now().plusNanos(1).isAfter(passwordService.findOneByIdentity(identity.getId()).getLastSuccessfulLogin()));
	}

	@Test
	@Transactional
	public void testSuccessfulLoginTimestamp() {
		IdmIdentityDto identity = getHelper().createIdentity();
		identity.setPassword(new GuardedString("SomePasswd"));
		identity = identityService.save(identity);

		// first login
		LoginDto loginDto = new LoginDto();
		loginDto.setUsername(identity.getUsername());
		loginDto.setPassword(new GuardedString("SomePasswd"));
		loginController.login(loginDto);
		ZonedDateTime timestamp = passwordService.findOneByIdentity(identity.getUsername()).getLastSuccessfulLogin();

		assertNotNull(passwordService.findOneByIdentity(identity.getUsername()).getLastSuccessfulLogin());

		// second login
		loginDto = new LoginDto();
		loginDto.setUsername(identity.getUsername());
		loginDto.setPassword(new GuardedString("SomePasswd"));
		loginController.login(loginDto);
		ZonedDateTime timestamp2 = passwordService.findOneByIdentity(identity.getUsername()).getLastSuccessfulLogin();

		assertTrue(timestamp2.isAfter(timestamp));
	}
	
	@Test
	@Transactional
	public void testResetUsuccessfulAttemptsAfterPasswordChange() {
		IdmIdentityDto identity = getHelper().createIdentity();

		// login
		LoginDto loginDto = new LoginDto();
		loginDto.setUsername(identity.getUsername());
		loginDto.setPassword(new GuardedString("wrong"));
		try {
			loginController.login(loginDto);
		} catch (IdmAuthenticationException ex) {
			// nothing
		}
		try {
			loginController.login(loginDto);
		} catch (IdmAuthenticationException ex) {
			// nothing
		}
		IdmPasswordDto password = passwordService.findOneByIdentity(identity.getId());
		//
		Assert.assertEquals(2, password.getUnsuccessfulAttempts());
		//
		// password change
		PasswordChangeDto passwordChange = new PasswordChangeDto();
		passwordChange.setIdm(true);
		passwordChange.setNewPassword(new GuardedString("new"));
		passwordService.save(identity, passwordChange);
		//
		password = passwordService.findOneByIdentity(identity.getId());
		//
		Assert.assertEquals(0, password.getUnsuccessfulAttempts());
	}

	@Test
	public void checkPasswordByPersisIdentity() {
		GuardedString password = new GuardedString("password-" + System.currentTimeMillis());
		
		IdmIdentityDto identity = new IdmIdentityDto();
		identity.setUsername(getHelper().createName());
		identity.setPassword(password);

		IdmIdentityDto saveIdentity = identityService.save(identity);
		assertNull(saveIdentity.getPassword());
	}
	
	@Test
	public void checkNullValueNewPassword() {
		GuardedString passwordForCheck = new GuardedString("password");
		IdmPasswordDto newPassword = new IdmPasswordDto();
		assertFalse(passwordService.checkPassword(passwordForCheck, newPassword));
	}

	@Test
	public void checkNullValuePasswordForCheck() {
		GuardedString passwordForCheck = new GuardedString();
		IdmPasswordDto newPassword = new IdmPasswordDto();
		newPassword.setPassword(generateHash("password"));
		assertFalse(passwordService.checkPassword(passwordForCheck, newPassword));
	}
	
	@Test
	public void checkNullBothPasswords() {
		GuardedString passwordForCheck = new GuardedString();
		IdmPasswordDto newPassword = new IdmPasswordDto();
		assertFalse(passwordService.checkPassword(passwordForCheck, newPassword));
	}

	@Test
	public void checkCorrectBehaviorTrue() {
		String password = "password" + System.currentTimeMillis();
		GuardedString passwordForCheck = new GuardedString(password);
		IdmPasswordDto newPassword = new IdmPasswordDto();
		newPassword.setPassword(generateHash(password));
		assertTrue(passwordService.checkPassword(passwordForCheck, newPassword));
	}

	@Test
	public void checkCorrectBehaviorFalse() {
		String password = "password" + System.currentTimeMillis();
		GuardedString passwordForCheck = new GuardedString(password + "2");
		IdmPasswordDto newPassword = new IdmPasswordDto();
		newPassword.setPassword(generateHash(password));
		assertFalse(passwordService.checkPassword(passwordForCheck, newPassword));
	}

	@Test
	public void testFilterIdentityUsername() {
		IdmIdentityDto identity = getHelper().createIdentity(new GuardedString("test" + System.currentTimeMillis()));

		IdmPasswordFilter filter = new IdmPasswordFilter();
		filter.setIdentityUsername(identity.getUsername());
		List<IdmPasswordDto> passwords = passwordService.find(filter, null).getContent();

		assertEquals(1, passwords.size());
		IdmPasswordDto passwordDto = passwords.get(0);
		assertEquals(identity.getId(), passwordDto.getIdentity());
	}

	@Test
	public void testFilterIdentityId() {
		IdmIdentityDto identity = getHelper().createIdentity(new GuardedString("test" + System.currentTimeMillis()));

		IdmPasswordFilter filter = new IdmPasswordFilter();
		filter.setIdentityId(identity.getId());
		List<IdmPasswordDto> passwords = passwordService.find(filter, null).getContent();

		assertEquals(1, passwords.size());
		IdmPasswordDto passwordDto = passwords.get(0);
		assertEquals(identity.getId(), passwordDto.getIdentity());
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testFilterText() {
		getHelper().createIdentity(new GuardedString("test" + System.currentTimeMillis()));

		IdmPasswordFilter filter = new IdmPasswordFilter();
		filter.setText("text-" + System.currentTimeMillis());
		passwordService.find(filter, null).getContent();
		fail();
	}

	@Test
	public void testFilterByPassword() {
		IdmIdentityDto identity = getHelper().createIdentity(new GuardedString("test" + System.currentTimeMillis()));

		IdmPasswordFilter filter = new IdmPasswordFilter();
		filter.setIdentityId(identity.getId());
		List<IdmPasswordDto> passwords = passwordService.find(filter, null).getContent();

		assertEquals(1, passwords.size());
		IdmPasswordDto passwordDto = passwords.get(0);
		assertEquals(identity.getId(), passwordDto.getIdentity());
		
		filter = new IdmPasswordFilter();
		filter.setPassword(passwordDto.getPassword());
		
		passwords = passwordService.find(filter, null).getContent();

		assertEquals(1, passwords.size());
		IdmPasswordDto passwordDtoTwo = passwords.get(0);
		assertEquals(identity.getId(), passwordDtoTwo.getIdentity());
		assertEquals(passwordDto.getId(), passwordDtoTwo.getId());
		assertEquals(passwordDto.getPassword(), passwordDtoTwo.getPassword());
	}

	@Test
	public void testFilterMustChange() {
		IdmIdentityDto identity = getHelper().createIdentity(new GuardedString("test" + System.currentTimeMillis()));

		IdmPasswordFilter filter = new IdmPasswordFilter();
		filter.setIdentityId(identity.getId());
		List<IdmPasswordDto> passwords = passwordService.find(filter, null).getContent();

		assertEquals(1, passwords.size());
		IdmPasswordDto passwordDto = passwords.get(0);
		assertEquals(identity.getId(), passwordDto.getIdentity());

		passwordDto.setMustChange(true);
		passwordService.save(passwordDto);

		filter = new IdmPasswordFilter();
		filter.setMustChange(true);
		passwords = passwordService.find(filter, null).getContent();
		assertEquals(1, passwords.size());
		IdmPasswordDto passwordDtoTwo = passwords.get(0);
		assertEquals(identity.getId(), passwordDtoTwo.getIdentity());
		assertEquals(passwordDto.getId(), passwordDtoTwo.getId());
		assertEquals(passwordDto.getPassword(), passwordDtoTwo.getPassword());
	}

	@Test
	public void testFilterIdentityDisabled() {
		IdmIdentityDto identityOne = getHelper().createIdentity(new GuardedString("test" + System.currentTimeMillis()));
		IdmIdentityDto identityTwo = getHelper().createIdentity(new GuardedString("test" + System.currentTimeMillis()));
		IdmIdentityDto identityThree = getHelper().createIdentity(new GuardedString("test" + System.currentTimeMillis()));

		identityOne.setState(IdentityState.DISABLED);
		identityTwo.setState(IdentityState.DISABLED);
		identityThree.setState(IdentityState.DISABLED);

		identityService.save(identityOne);
		identityService.save(identityTwo);
		identityService.save(identityThree);

		IdmPasswordFilter filter = new IdmPasswordFilter();
		filter.setIdentityDisabled(true);
		List<IdmPasswordDto> passwords = passwordService.find(filter, null).getContent();

		assertTrue(passwords.size() >= 3);

		IdmPasswordDto passwordDto = passwords.stream().filter(pass -> {
			return pass.getIdentity().equals(identityOne.getId());
		}).findFirst().orElse(null);
		assertNotNull(passwordDto);

		passwordDto = passwords.stream().filter(pass -> {
			return pass.getIdentity().equals(identityTwo.getId());
		}).findFirst().orElse(null);
		assertNotNull(passwordDto);

		passwordDto = passwords.stream().filter(pass -> {
			return pass.getIdentity().equals(identityThree.getId());
		}).findFirst().orElse(null);
		assertNotNull(passwordDto);
	}

	@Test
	public void testFilterValidTill() {
		IdmIdentityDto identityOne = getHelper().createIdentity(new GuardedString("test" + System.currentTimeMillis()));
		IdmIdentityDto identityTwo = getHelper().createIdentity(new GuardedString("test" + System.currentTimeMillis()));
		IdmIdentityDto identityTree = getHelper().createIdentity(new GuardedString("test" + System.currentTimeMillis()));

		IdmPasswordFilter filter = new IdmPasswordFilter();
		filter.setIdentityId(identityOne.getId());
		IdmPasswordDto passwordOne = passwordService.find(filter, null).getContent().get(0);
		filter.setIdentityId(identityTwo.getId());
		IdmPasswordDto passwordTwo = passwordService.find(filter, null).getContent().get(0);
		filter.setIdentityId(identityTree.getId());
		IdmPasswordDto passwordThree = passwordService.find(filter, null).getContent().get(0);

		passwordOne.setValidTill(LocalDate.now().minusDays(1));
		passwordTwo.setValidTill(LocalDate.now().minusDays(10));
		passwordThree.setValidTill(LocalDate.now().minusDays(100));
		passwordService.save(passwordOne);
		passwordService.save(passwordTwo);
		passwordService.save(passwordThree);

		filter = new IdmPasswordFilter();
		filter.setValidTill(LocalDate.now().minusDays(99));
		List<IdmPasswordDto> passwords = passwordService.find(filter, null).getContent();
		assertTrue(passwords.size() >= 1);
		
		IdmPasswordDto passwordDto = passwords.stream().filter(pass -> {
			return pass.getIdentity().equals(identityOne.getId());
		}).findFirst().orElse(null);
		assertNull(passwordDto);
		
		passwordDto = passwords.stream().filter(pass -> {
			return pass.getIdentity().equals(identityTwo.getId());
		}).findFirst().orElse(null);
		assertNull(passwordDto);
		
		passwordDto = passwords.stream().filter(pass -> {
			return pass.getIdentity().equals(identityTree.getId());
		}).findFirst().orElse(null);
		assertNotNull(passwordDto);

		filter = new IdmPasswordFilter();
		filter.setValidTill(LocalDate.now().minusDays(9));
		passwords = passwordService.find(filter, null).getContent();
		assertTrue(passwords.size() >= 2);

		passwordDto = passwords.stream().filter(pass -> {
			return pass.getIdentity().equals(identityOne.getId());
		}).findFirst().orElse(null);
		assertNull(passwordDto);
		
		passwordDto = passwords.stream().filter(pass -> {
			return pass.getIdentity().equals(identityTwo.getId());
		}).findFirst().orElse(null);
		assertNotNull(passwordDto);
		
		passwordDto = passwords.stream().filter(pass -> {
			return pass.getIdentity().equals(identityTree.getId());
		}).findFirst().orElse(null);
		assertNotNull(passwordDto);

		filter = new IdmPasswordFilter();
		filter.setValidTill(LocalDate.now());
		passwords = passwordService.find(filter, null).getContent();
		assertTrue(passwords.size() >= 2);

		passwordDto = passwords.stream().filter(pass -> {
			return pass.getIdentity().equals(identityOne.getId());
		}).findFirst().orElse(null);
		assertNotNull(passwordDto);
		
		passwordDto = passwords.stream().filter(pass -> {
			return pass.getIdentity().equals(identityTwo.getId());
		}).findFirst().orElse(null);
		assertNotNull(passwordDto);
		
		passwordDto = passwords.stream().filter(pass -> {
			return pass.getIdentity().equals(identityTree.getId());
		}).findFirst().orElse(null);
		assertNotNull(passwordDto);
	}

	@Test
	public void testFilterValidFrom() {
		IdmIdentityDto identityOne = getHelper().createIdentity(new GuardedString("test" + System.currentTimeMillis()));
		IdmIdentityDto identityTwo = getHelper().createIdentity(new GuardedString("test" + System.currentTimeMillis()));
		IdmIdentityDto identityTree = getHelper().createIdentity(new GuardedString("test" + System.currentTimeMillis()));

		IdmPasswordFilter filter = new IdmPasswordFilter();
		filter.setIdentityId(identityOne.getId());
		IdmPasswordDto passwordOne = passwordService.find(filter, null).getContent().get(0);
		filter.setIdentityId(identityTwo.getId());
		IdmPasswordDto passwordTwo = passwordService.find(filter, null).getContent().get(0);
		filter.setIdentityId(identityTree.getId());
		IdmPasswordDto passwordThree = passwordService.find(filter, null).getContent().get(0);

		passwordOne.setValidFrom(LocalDate.now().minusDays(55));
		passwordTwo.setValidFrom(LocalDate.now().minusDays(1));
		passwordThree.setValidFrom(LocalDate.now().plusDays(100));
		passwordService.save(passwordOne);
		passwordService.save(passwordTwo);
		passwordService.save(passwordThree);

		filter = new IdmPasswordFilter();
		filter.setValidFrom(LocalDate.now().minusDays(99));
		List<IdmPasswordDto> passwords = passwordService.find(filter, null).getContent();
		assertTrue(passwords.size() >= 3);
		
		IdmPasswordDto passwordDto = passwords.stream().filter(pass -> {
			return pass.getIdentity().equals(identityOne.getId());
		}).findFirst().orElse(null);
		assertNotNull(passwordDto);
		
		passwordDto = passwords.stream().filter(pass -> {
			return pass.getIdentity().equals(identityTwo.getId());
		}).findFirst().orElse(null);
		assertNotNull(passwordDto);
		
		passwordDto = passwords.stream().filter(pass -> {
			return pass.getIdentity().equals(identityTree.getId());
		}).findFirst().orElse(null);
		assertNotNull(passwordDto);

		filter = new IdmPasswordFilter();
		filter.setValidFrom(LocalDate.now().minusDays(9));
		passwords = passwordService.find(filter, null).getContent();
		assertTrue(passwords.size() >= 2);

		passwordDto = passwords.stream().filter(pass -> {
			return pass.getIdentity().equals(identityOne.getId());
		}).findFirst().orElse(null);
		assertNull(passwordDto);
		
		passwordDto = passwords.stream().filter(pass -> {
			return pass.getIdentity().equals(identityTwo.getId());
		}).findFirst().orElse(null);
		assertNotNull(passwordDto);
		
		passwordDto = passwords.stream().filter(pass -> {
			return pass.getIdentity().equals(identityTree.getId());
		}).findFirst().orElse(null);
		assertNotNull(passwordDto);

		filter = new IdmPasswordFilter();
		filter.setValidFrom(LocalDate.now().plusDays(50));
		passwords = passwordService.find(filter, null).getContent();
		assertTrue(passwords.size() >= 1);

		passwordDto = passwords.stream().filter(pass -> {
			return pass.getIdentity().equals(identityOne.getId());
		}).findFirst().orElse(null);
		assertNull(passwordDto);
		
		passwordDto = passwords.stream().filter(pass -> {
			return pass.getIdentity().equals(identityTwo.getId());
		}).findFirst().orElse(null);
		assertNull(passwordDto);
		
		passwordDto = passwords.stream().filter(pass -> {
			return pass.getIdentity().equals(identityTree.getId());
		}).findFirst().orElse(null);
		assertNotNull(passwordDto);
	}
	
	@Test
	public void testSetFalidFromUnderSameUser() {
		IdmIdentityDto identity = getHelper().createIdentity();
		//
		IdmPasswordDto password = passwordService.findOneByIdentity(identity.getId());
		Assert.assertNull(password.getValidFrom());
		Assert.assertEquals(identity.getId(), password.getIdentity());
		//
		PasswordChangeDto passwordChangeDto = new PasswordChangeDto();
		passwordChangeDto.setAll(true);
		passwordChangeDto.setIdm(true);
		passwordChangeDto.setOldPassword(identity.getPassword());
		passwordChangeDto.setNewPassword(new GuardedString("testPassword"));
		//
		try {
			getHelper().login(identity);
			identityService.passwordChange(identity, passwordChangeDto);
			password = passwordService.findOneByIdentity(identity.getId());
			Assert.assertNotNull(password.getValidFrom());
			Assert.assertNull(password.getValidTill());
		} finally {
			logout();
		}
	}
	
	@Test
	public void testPreserveValidFromUnderSameIdentity() {
		IdmIdentityDto identity = getHelper().createIdentity();
		//
		IdmPasswordDto password = passwordService.findOneByIdentity(identity.getId());
		Assert.assertNull(password.getValidFrom());
		Assert.assertEquals(identity.getId(), password.getIdentity());
		//
		PasswordChangeDto passwordChangeDto = new PasswordChangeDto();
		passwordChangeDto.setAll(true);
		passwordChangeDto.setIdm(true);
		passwordChangeDto.setOldPassword(identity.getPassword());
		passwordChangeDto.setNewPassword(new GuardedString("testPassword"));
		passwordChangeDto.setSkipResetValidFrom(true);
		//
		try {
			getHelper().login(identity);
			identityService.passwordChange(identity, passwordChangeDto);
			password = passwordService.findOneByIdentity(identity.getId());
			Assert.assertNotNull(password.getValidFrom());
			Assert.assertNull(password.getValidTill());
		} finally {
			logout();
		}
	}
	
	@Test
	public void testPreserveValidFromUnderDifferentIdentity() {
		IdmIdentityDto manager = getHelper().createIdentity();
		IdmIdentityDto identity = getHelper().createIdentity();
		//
		IdmPasswordDto password = passwordService.findOneByIdentity(identity.getId());
		Assert.assertNull(password.getValidFrom());
		Assert.assertEquals(identity.getId(), password.getIdentity());
		//
		PasswordChangeDto passwordChangeDto = new PasswordChangeDto();
		passwordChangeDto.setAll(true);
		passwordChangeDto.setIdm(true);
		passwordChangeDto.setOldPassword(identity.getPassword());
		passwordChangeDto.setNewPassword(new GuardedString("testPassword"));
		passwordChangeDto.setSkipResetValidFrom(true);
		//
		try {
			getHelper().login(manager);
			identityService.passwordChange(identity, passwordChangeDto);
			password = passwordService.findOneByIdentity(identity.getId());
			Assert.assertNotNull(password.getValidFrom());
			Assert.assertNull(password.getValidTill());
		} finally {
			logout();
		}
	}
	
	@Test
	public void testResetValidFromUnderDifferentIdentity() {
		IdmIdentityDto manager = getHelper().createIdentity();
		IdmIdentityDto identity = getHelper().createIdentity();
		//
		IdmPasswordDto password = passwordService.findOneByIdentity(identity.getId());
		Assert.assertNull(password.getValidFrom());
		Assert.assertEquals(identity.getId(), password.getIdentity());
		//
		PasswordChangeDto passwordChangeDto = new PasswordChangeDto();
		passwordChangeDto.setAll(true);
		passwordChangeDto.setIdm(true);
		passwordChangeDto.setNewPassword(new GuardedString("testPassword"));
		//
		try {
			getHelper().login(manager);
			identityService.passwordChange(identity, passwordChangeDto);
			password = passwordService.findOneByIdentity(identity.getId());
			Assert.assertNull(password.getValidFrom());
			Assert.assertNull(password.getValidTill());
		} finally {
			logout();
		}
	}

	private IdmPasswordPolicyDto getTestPolicy(boolean isDefault, IdmPasswordPolicyType type, Integer maxAge) {
		IdmPasswordPolicyDto policy = new IdmPasswordPolicyDto();
		policy.setName(UUID.randomUUID().toString());
		policy.setType(type);
		policy.setMaxPasswordAge(maxAge);
		policy.setDefaultPolicy(isDefault);
		return policyService.save(policy);
	}

	private IdmPasswordPolicyDto getTestPolicy(boolean isDefault) {
		return getTestPolicy(isDefault, IdmPasswordPolicyType.VALIDATE, 365);
	}

	private void removeDefaultPolicy() {
		// I need to get rid of default policy defined in init test data
		policyService.find(null).forEach(p -> policyService.delete(p));
	}

	private String generateHash(String password) {
		return passwordService.generateHash(new GuardedString(password), passwordService.getSalt());
	}
}