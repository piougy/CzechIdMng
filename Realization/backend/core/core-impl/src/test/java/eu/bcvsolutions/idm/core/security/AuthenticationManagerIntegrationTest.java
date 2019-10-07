package eu.bcvsolutions.idm.core.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.CoreModuleDescriptor;
import eu.bcvsolutions.idm.core.api.domain.IdmPasswordPolicyType;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmPasswordDto;
import eu.bcvsolutions.idm.core.api.dto.IdmPasswordPolicyDto;
import eu.bcvsolutions.idm.core.api.dto.PasswordChangeDto;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.IdmPasswordPolicyService;
import eu.bcvsolutions.idm.core.api.service.IdmPasswordService;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmMessageDto;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationLogDto;
import eu.bcvsolutions.idm.core.notification.api.dto.filter.IdmNotificationFilter;
import eu.bcvsolutions.idm.core.notification.api.service.IdmNotificationLogService;
import eu.bcvsolutions.idm.core.notification.entity.IdmEmailLog;
import eu.bcvsolutions.idm.core.security.api.authentication.AuthenticationManager;
import eu.bcvsolutions.idm.core.security.api.authentication.Authenticator;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.security.api.dto.LoginDto;
import eu.bcvsolutions.idm.core.security.exception.IdmAuthenticationException;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;
import eu.bcvsolutions.idm.test.api.TestHelper;

/**
 * Default test for {@link AuthenticationManager} and core {@link Authenticator}.
 * 
 * @author Ondrej Kopr
 *
 */
public class AuthenticationManagerIntegrationTest extends AbstractIntegrationTest {
	
	@Autowired
	private AuthenticationManager authenticationManager;
	@Autowired
	private IdmIdentityService identityService;
	@Autowired
	private IdmPasswordPolicyService passwordPolicyService;
	@Autowired
	private IdmNotificationLogService notificationLogService;
	@Autowired
	private TestHelper testHelper;
	@Autowired
	private IdmPasswordService passwordService;
	
	@Before
	public void login() {
		loginAsAdmin();
	}
	
	@After
	@Override
	public void logout() {
		super.logout();
	}
	
	@Transactional
	@Test(expected = AuthenticationException.class)
	public void loginViaManagerBadCredentials() {
		IdmIdentityDto identity = new IdmIdentityDto();
		identity.setUsername("test_login_1");
		identity.setLastName("test_login_1");
		identity.setPassword(new GuardedString("test1234"));
		identity = this.identityService.save(identity);
		
		LoginDto loginDto = new LoginDto();
		loginDto.setPassword(new GuardedString("test12345"));
		loginDto.setUsername("test_login_1");
		
		authenticationManager.authenticate(loginDto);
		fail();
	}
	
	@Test
	@Transactional
	public void loginViaManagerSuccesful() {
		IdmIdentityDto identity = new IdmIdentityDto();
		identity.setUsername("test_login_2");
		identity.setLastName("test_login_2");
		identity.setPassword(new GuardedString("test1234"));
		identity = this.identityService.save(identity);
		
		LoginDto loginDto = new LoginDto();
		loginDto.setPassword(new GuardedString("test1234"));
		loginDto.setUsername("test_login_2");
		
		loginDto = authenticationManager.authenticate(loginDto);
		assertNotNull(loginDto);
		assertNotNull(loginDto.getAuthentication());
		assertEquals("core", loginDto.getAuthenticationModule());
	}
	
	@Test
	public void testBlockLogin() throws InterruptedException {
		loginAsAdmin();
		String testPassword = "testPassword" + System.currentTimeMillis();

		IdmPasswordPolicyDto passwordPolicy = new IdmPasswordPolicyDto();
		passwordPolicy.setName(testHelper.createName());
		passwordPolicy.setDefaultPolicy(true);
		passwordPolicy.setType(IdmPasswordPolicyType.VALIDATE);
		passwordPolicy.setBlockLoginTime(2);
		passwordPolicy.setMaxUnsuccessfulAttempts(4);
		passwordPolicy = passwordPolicyService.save(passwordPolicy);

		IdmIdentityDto identity = testHelper.createIdentity(new GuardedString(testPassword));
		logout();
		
		LoginDto loginDto = tryLogin(identity.getUsername(), testPassword);
		
		assertNotNull(loginDto.getToken());
		assertEquals(CoreModuleDescriptor.MODULE_ID, loginDto.getAuthenticationModule());
		
		// try fail - 1#
		tryLoginExceptFail(identity.getUsername(), "badPassword" + System.currentTimeMillis());
		
		identity = identityService.get(identity.getId());
		assertNull(identity.getBlockLoginDate());
		
		// try fail - 2#
		tryLoginExceptFail(identity.getUsername(), "badPassword" + System.currentTimeMillis());
		
		identity = identityService.get(identity.getId());
		assertNull(identity.getBlockLoginDate());
		
		// try fail - 3#
		tryLoginExceptFail(identity.getUsername(), "badPassword" + System.currentTimeMillis());
		
		identity = identityService.get(identity.getId());
		assertNull(identity.getBlockLoginDate());
		
		// try fail - 4# (block)
		tryLoginExceptFail(identity.getUsername(), "badPassword" + System.currentTimeMillis());
		
		identity = identityService.get(identity.getId());
		ZonedDateTime blockLoginDate = identity.getBlockLoginDate();
		assertNull(blockLoginDate); // blockLoginDate isn't filled by service more
		IdmPasswordDto password = passwordService.findOneByIdentity(identity.getId());
		assertNotNull(password);
		blockLoginDate = password.getBlockLoginDate();
		assertNotNull(blockLoginDate);
		
		// try success but login is blocked
		tryLoginExceptFail(identity.getUsername(), testPassword);
		
		identity = identityService.get(identity.getId());
		password = passwordService.findOneByIdentity(identity.getId());
		assertNotNull(password);
		assertNotNull(password.getBlockLoginDate());
		assertEquals(blockLoginDate, password.getBlockLoginDate()); // date is same
		
		// wait for 2 sec
		Thread.sleep(2000);
		
		loginDto = tryLogin(identity.getUsername(), testPassword);
		
		assertNotNull(loginDto.getToken());
		assertEquals(CoreModuleDescriptor.MODULE_ID, loginDto.getAuthenticationModule());
		
		passwordPolicyService.delete(passwordPolicy);
	}

	@Test
	public void testLoginWithoutPasswordPolicy() {
		// remove all policies
		for (IdmPasswordPolicyDto passwordPolicy : passwordPolicyService.find(null)) {
			passwordPolicyService.delete(passwordPolicy);
		}

		String testPassword = "testPassword" + System.currentTimeMillis();
		IdmIdentityDto identity = testHelper.createIdentity(new GuardedString(testPassword));
		logout();

		LoginDto loginDto = tryLogin(identity.getUsername(), testPassword);
		checkLoginDto(loginDto);
		
		String wrongPassword = "badPassword" + System.currentTimeMillis();
		tryLoginExceptFail(identity.getUsername(), wrongPassword);
		tryLoginExceptFail(identity.getUsername(), wrongPassword);
		tryLoginExceptFail(identity.getUsername(), wrongPassword);
		tryLoginExceptFail(identity.getUsername(), wrongPassword);
		
		IdmPasswordDto passwordDto = passwordService.findOneByIdentity(identity.getId());
		assertNotNull(passwordDto);
		assertNull(passwordDto.getBlockLoginDate());
		
		identity = identityService.get(identity.getId());
		assertNull(identity.getBlockLoginDate());
	}
	
	@Test
	public void testBlockLoginCheckNotification() {
		IdmPasswordPolicyDto passwordPolicy = new IdmPasswordPolicyDto();
		passwordPolicy.setName(testHelper.createName());
		passwordPolicy.setDefaultPolicy(true);
		passwordPolicy.setType(IdmPasswordPolicyType.VALIDATE);
		passwordPolicy.setBlockLoginTime(2);
		passwordPolicy.setMaxUnsuccessfulAttempts(2);
		passwordPolicy = passwordPolicyService.save(passwordPolicy);

		String testPassword = "testPassword" + System.currentTimeMillis();
		IdmIdentityDto identity = testHelper.createIdentity(new GuardedString(testPassword));
		
		LoginDto loginDto = tryLogin(identity.getUsername(), testPassword);
		checkLoginDto(loginDto);
		
		String wrongPassword = "badPassword" + System.currentTimeMillis();
		tryLoginExceptFail(identity.getUsername(), wrongPassword);
		tryLoginExceptFail(identity.getUsername(), wrongPassword);
		tryLoginExceptFail(identity.getUsername(), wrongPassword); // block
		
		IdmNotificationFilter filter = new IdmNotificationFilter();
		filter.setRecipient(identity.getUsername());
		filter.setNotificationType(IdmEmailLog.class);
		List<IdmNotificationLogDto> notifications = notificationLogService.find(filter, null).getContent();

		assertEquals(1, notifications.size());
		IdmNotificationLogDto notification = notifications.get(0);
		IdmMessageDto message = notification.getMessage();
		assertNotNull(message);

		// hardcode text form template
		assertTrue(message.getSubject().contains("login blocked"));
		assertTrue(message.getHtmlMessage().contains("has been exceeded the number of unsuccessful logon attempts"));
		assertTrue(message.getHtmlMessage().contains(identity.getUsername()));
		
		passwordPolicyService.delete(passwordPolicy);
	}
	
	@Test
	public void testNonExistingPassword() {
		IdmPasswordPolicyDto passwordPolicy = new IdmPasswordPolicyDto();
		passwordPolicy.setName(testHelper.createName());
		passwordPolicy.setDefaultPolicy(true);
		passwordPolicy.setType(IdmPasswordPolicyType.VALIDATE);
		passwordPolicy.setBlockLoginTime(2);
		passwordPolicy.setMaxUnsuccessfulAttempts(2);
		passwordPolicy = passwordPolicyService.save(passwordPolicy);

		IdmIdentityDto identity = testHelper.createIdentity(null, null);
		IdmPasswordDto passwordDto = passwordService.findOneByIdentity(identity.getId());

		assertNull(passwordDto);
		
		String wrongPassword = "badPassword" + System.currentTimeMillis();
		tryLoginExceptFail(identity.getUsername(), wrongPassword);
		
		passwordDto = passwordService.findOneByIdentity(identity.getId());
		assertNotNull(passwordDto); // password was created
		assertNull(passwordDto.getPassword());
		assertNull(passwordDto.getBlockLoginDate());
		
		tryLoginExceptFail(identity.getUsername(), wrongPassword);
		tryLoginExceptFail(identity.getUsername(), wrongPassword); // block
		
		passwordDto = passwordService.findOneByIdentity(identity.getId());
		assertNotNull(passwordDto);
		assertNull(passwordDto.getPassword());
		assertNotNull(passwordDto.getBlockLoginDate());
		
		passwordPolicyService.delete(passwordPolicy);
	}

	@Test
	public void testClearBlockLoginDate() {
		IdmPasswordPolicyDto validatePolicy = new IdmPasswordPolicyDto();
		validatePolicy.setName(getHelper().createName());
		validatePolicy.setBlockLoginTime(150);
		validatePolicy.setMaxUnsuccessfulAttempts(3);
		validatePolicy.setDefaultPolicy(true);
		validatePolicy.setType(IdmPasswordPolicyType.VALIDATE);
		validatePolicy = passwordPolicyService.save(validatePolicy);

		IdmIdentityDto identity = getHelper().createIdentity();
		IdmPasswordDto passwordDto = passwordService.findOneByIdentity(identity.getId());
		assertNotNull(passwordDto);
		assertNull(passwordDto.getBlockLoginDate());
		assertEquals(0, passwordDto.getUnsuccessfulAttempts());
		
		// first login
		LoginDto loginDto = new LoginDto();
		loginDto.setUsername(identity.getUsername());
		GuardedString oldPassword = new GuardedString(String.valueOf(System.currentTimeMillis()));
		loginDto.setPassword(oldPassword);

		try {
			authenticationManager.authenticate(loginDto);
			fail();
		} catch (IdmAuthenticationException ex) {
			// success
		}

		passwordDto = passwordService.findOneByIdentity(identity.getId());
		assertNotNull(passwordDto);
		assertNull(passwordDto.getBlockLoginDate());
		assertEquals(1, passwordDto.getUnsuccessfulAttempts());

		try {
			authenticationManager.authenticate(loginDto);
			fail();
		} catch (IdmAuthenticationException ex) {
			// success
		}

		passwordDto = passwordService.findOneByIdentity(identity.getId());
		assertNotNull(passwordDto);
		assertNull(passwordDto.getBlockLoginDate());
		assertEquals(2, passwordDto.getUnsuccessfulAttempts());

		try {
			authenticationManager.authenticate(loginDto);
			fail();
		} catch (ResultCodeException ex) { // Another exception
			// success
		}

		passwordDto = passwordService.findOneByIdentity(identity.getId());
		assertNotNull(passwordDto);
		assertNotNull(passwordDto.getBlockLoginDate());
		assertEquals(3, passwordDto.getUnsuccessfulAttempts());

		PasswordChangeDto passwordChangeDto = new PasswordChangeDto();
		passwordChangeDto.setAll(true);
		passwordChangeDto.setIdm(true);
		passwordChangeDto.setOldPassword(oldPassword);
		passwordChangeDto.setNewPassword(new GuardedString(String.valueOf(System.currentTimeMillis())));
		identityService.passwordChange(identity, passwordChangeDto);

		passwordDto = passwordService.findOneByIdentity(identity.getId());
		assertNotNull(passwordDto);
		assertNull(passwordDto.getBlockLoginDate());
		assertEquals(0, passwordDto.getUnsuccessfulAttempts());

		passwordPolicyService.delete(validatePolicy);
	}

	@Test
	public void testReachSecondBlockPeriod() throws InterruptedException {
		IdmPasswordPolicyDto validatePolicy = new IdmPasswordPolicyDto();
		validatePolicy.setName(getHelper().createName());
		validatePolicy.setBlockLoginTime(2);
		validatePolicy.setMaxUnsuccessfulAttempts(1);
		validatePolicy.setDefaultPolicy(true);
		validatePolicy.setType(IdmPasswordPolicyType.VALIDATE);
		validatePolicy = passwordPolicyService.save(validatePolicy);

		IdmIdentityDto identity = getHelper().createIdentity();
		IdmPasswordDto passwordDto = passwordService.findOneByIdentity(identity.getId());
		assertNotNull(passwordDto);
		assertNull(passwordDto.getBlockLoginDate());
		assertEquals(0, passwordDto.getUnsuccessfulAttempts());

		// first login
		LoginDto loginDto = new LoginDto();
		loginDto.setUsername(identity.getUsername());
		GuardedString oldPassword = new GuardedString(String.valueOf(System.currentTimeMillis()));
		loginDto.setPassword(oldPassword);

		ZonedDateTime start = ZonedDateTime.now();
		try {
			authenticationManager.authenticate(loginDto);
			fail();
		} catch (ResultCodeException ex) {
			// success
		}

		passwordDto = passwordService.findOneByIdentity(identity.getId());
		assertNotNull(passwordDto);
		assertNotNull(passwordDto.getBlockLoginDate());
		assertEquals(1, passwordDto.getUnsuccessfulAttempts());
		ZonedDateTime blockLoginDate = passwordDto.getBlockLoginDate();

		long seconds = ChronoUnit.SECONDS.between(start, blockLoginDate);
		if (seconds > 3) { // correct is 2 second but some machine can be slower
			fail("Diff between start and block date is more than 3 second. Current: " + seconds);
		}
		Thread.sleep(1000 * seconds);

		start = ZonedDateTime.now();
		try {
			authenticationManager.authenticate(loginDto);
			fail();
		} catch (ResultCodeException ex) {
			// success
		}

		passwordDto = passwordService.findOneByIdentity(identity.getId());
		assertNotNull(passwordDto);
		assertNotNull(passwordDto.getBlockLoginDate());
		assertEquals(2, passwordDto.getUnsuccessfulAttempts()); // Attempts are increased
		blockLoginDate = passwordDto.getBlockLoginDate();
		seconds = ChronoUnit.SECONDS.between(start, blockLoginDate);
		if (seconds > 5) { // correct is 4 second but some machine can be slower
			fail("Diff between start and block date is more than 5 second. Current: " + seconds);
		}
	}

	@Test
	public void testFailWithouBlockTime() {
		IdmPasswordPolicyDto validatePolicy = new IdmPasswordPolicyDto();
		validatePolicy.setName(getHelper().createName());
		validatePolicy.setBlockLoginTime(null);
		validatePolicy.setMaxUnsuccessfulAttempts(3);
		validatePolicy.setDefaultPolicy(true);
		validatePolicy.setType(IdmPasswordPolicyType.VALIDATE);
		
		try {
			validatePolicy = passwordPolicyService.save(validatePolicy); // this state throw error
			fail();
		} catch (ResultCodeException e) {
			// success
		}
	}

	@Test
	public void testFailWithouMaxUnsuccessfulAttempts() {
		IdmPasswordPolicyDto validatePolicy = new IdmPasswordPolicyDto();
		validatePolicy.setName(getHelper().createName());
		validatePolicy.setBlockLoginTime(3);
		validatePolicy.setMaxUnsuccessfulAttempts(null);
		validatePolicy.setDefaultPolicy(true);
		validatePolicy.setType(IdmPasswordPolicyType.VALIDATE);
		validatePolicy = passwordPolicyService.save(validatePolicy);

		IdmIdentityDto identity = getHelper().createIdentity();
		IdmPasswordDto passwordDto = passwordService.findOneByIdentity(identity.getId());
		assertNotNull(passwordDto);
		assertNull(passwordDto.getBlockLoginDate());
		assertEquals(0, passwordDto.getUnsuccessfulAttempts());

		// first login
		LoginDto loginDto = new LoginDto();
		loginDto.setUsername(identity.getUsername());
		GuardedString oldPassword = new GuardedString(String.valueOf(System.currentTimeMillis()));
		loginDto.setPassword(oldPassword);
		
		try {
			authenticationManager.authenticate(loginDto);
			fail();
		} catch (IdmAuthenticationException ex) {
			// success
		}

		passwordDto = passwordService.findOneByIdentity(identity.getId());
		assertNotNull(passwordDto);
		assertNull(passwordDto.getBlockLoginDate());
		assertEquals(1, passwordDto.getUnsuccessfulAttempts());

		try {
			authenticationManager.authenticate(loginDto);
			fail();
		} catch (IdmAuthenticationException ex) {
			// success
		}

		passwordDto = passwordService.findOneByIdentity(identity.getId());
		assertNotNull(passwordDto);
		assertNull(passwordDto.getBlockLoginDate());
		assertEquals(2, passwordDto.getUnsuccessfulAttempts());

		try {
			authenticationManager.authenticate(loginDto);
			fail();
		} catch (IdmAuthenticationException ex) {
			// success
		}

		passwordDto = passwordService.findOneByIdentity(identity.getId());
		assertNotNull(passwordDto);
		assertNull(passwordDto.getBlockLoginDate());
		assertEquals(3, passwordDto.getUnsuccessfulAttempts());

		try {
			authenticationManager.authenticate(loginDto);
			fail();
		} catch (IdmAuthenticationException ex) {
			// success
		}

		passwordDto = passwordService.findOneByIdentity(identity.getId());
		assertNotNull(passwordDto);
		assertNull(passwordDto.getBlockLoginDate());
		assertEquals(4, passwordDto.getUnsuccessfulAttempts());
	}

	@Test
	public void testSavePasswordNeverExpires() {
		String password = "pass-" + System.currentTimeMillis();
		IdmIdentityDto identityDto = this.getHelper().createIdentity(new GuardedString(password));
		IdmPasswordDto passwordDto = passwordService.findOneByIdentity(identityDto.getId());
		
		assertFalse(passwordDto.isPasswordNeverExpires());
		passwordDto.setPasswordNeverExpires(true);

		IdmPasswordDto newlySaved = passwordService.save(passwordDto);

		assertTrue(newlySaved.isPasswordNeverExpires());
	}

	@Test
	public void testSavePasswordNeverExpiresWithSetValidTill() {
		String password = "pass-" + System.currentTimeMillis();
		IdmIdentityDto identityDto = this.getHelper().createIdentity(new GuardedString(password));
		IdmPasswordDto passwordDto = passwordService.findOneByIdentity(identityDto.getId());
		
		assertFalse(passwordDto.isPasswordNeverExpires());
		passwordDto.setValidTill(LocalDate.now().plusDays(10));
		passwordDto = passwordService.save(passwordDto);
		assertFalse(passwordDto.isPasswordNeverExpires());
		assertEquals(LocalDate.now().plusDays(10), passwordDto.getValidTill());
		
		passwordDto.setPasswordNeverExpires(true);

		IdmPasswordDto newlySaved = passwordService.save(passwordDto);

		assertTrue(newlySaved.isPasswordNeverExpires());
		assertNull(passwordDto.getValidTill());
	}

	@Test
	public void testChangPasswordWithNeverExpiresAndValidTill() {
		IdmPasswordPolicyDto validatePolicy = new IdmPasswordPolicyDto();
		validatePolicy.setName(getHelper().createName());
		validatePolicy.setMaxPasswordAge(10);
		validatePolicy.setDefaultPolicy(true);
		validatePolicy.setType(IdmPasswordPolicyType.VALIDATE);
		validatePolicy = passwordPolicyService.save(validatePolicy);
		
		String password = "pass-" + System.currentTimeMillis();
		IdmIdentityDto identityDto = this.getHelper().createIdentity(new GuardedString(password));
		IdmPasswordDto passwordDto = passwordService.findOneByIdentity(identityDto.getId());

		assertEquals(LocalDate.now().plusDays(10), passwordDto.getValidTill());
		
		
		PasswordChangeDto passwordChange = new PasswordChangeDto();
		passwordChange.setOldPassword(new GuardedString(password));
		passwordChange.setNewPassword(new GuardedString(password + "2"));
		passwordService.save(identityDto, passwordChange);
		
		assertFalse(passwordDto.isPasswordNeverExpires());
		passwordDto.setPasswordNeverExpires(true);
		IdmPasswordDto newlySaved = passwordService.save(passwordDto);

		assertTrue(newlySaved.isPasswordNeverExpires());
		assertNull(passwordDto.getValidTill());
	}

	private LoginDto tryLogin(String username, String password) {
		LoginDto loginDto = new LoginDto();
		loginDto.setUsername(username);
		loginDto.setPassword(new GuardedString(password));
		return authenticationManager.authenticate(loginDto);
	}
	
	private void checkLoginDto(LoginDto loginDto) {
		assertNotNull(loginDto.getAuthentication());
		assertEquals(CoreModuleDescriptor.MODULE_ID, loginDto.getAuthenticationModule());
	}
	
	private void tryLoginExceptFail(String username, String password) {
		try {
			this.tryLogin(username, password);
			fail();
		} catch (ResultCodeException e) {
			assertEquals(HttpStatus.UNAUTHORIZED, e.getStatus());
		} catch (IdmAuthenticationException authEx) {
			assertTrue(authEx.getMessage().contains("Check identity password"));
			assertTrue(authEx.getMessage().contains("because the password digests differ"));
		} catch (Exception ex) {
			fail("Unxcepted exception: " + ex.getMessage());
		}
	}
}
