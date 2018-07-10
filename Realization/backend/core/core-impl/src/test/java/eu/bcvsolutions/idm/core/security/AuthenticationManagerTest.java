package eu.bcvsolutions.idm.core.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.joda.time.DateTime;
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
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
public class AuthenticationManagerTest extends AbstractIntegrationTest {
	
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
		DateTime blockLoginDate = identity.getBlockLoginDate();
		assertNotNull(blockLoginDate);
		
		// try success but login is blocked
		tryLoginExceptFail(identity.getUsername(), testPassword);
		
		identity = identityService.get(identity.getId());
		assertNotNull(identity.getBlockLoginDate());
		assertEquals(blockLoginDate, identity.getBlockLoginDate()); // date is same
		
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
