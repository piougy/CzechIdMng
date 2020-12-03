package eu.bcvsolutions.idm.core.security.service.impl;

import java.time.ZonedDateTime;
import java.util.UUID;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmPasswordDto;
import eu.bcvsolutions.idm.core.api.dto.IdmProfileDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTokenDto;
import eu.bcvsolutions.idm.core.api.exception.EntityNotFoundException;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.IdmPasswordService;
import eu.bcvsolutions.idm.core.api.service.IdmProfileService;
import eu.bcvsolutions.idm.core.security.api.authentication.AuthenticationManager;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.security.api.domain.IdmJwtAuthentication;
import eu.bcvsolutions.idm.core.security.api.domain.TwoFactorAuthenticationType;
import eu.bcvsolutions.idm.core.security.api.dto.LoginDto;
import eu.bcvsolutions.idm.core.security.api.dto.TwoFactorRegistrationConfirmDto;
import eu.bcvsolutions.idm.core.security.api.dto.TwoFactorRegistrationResponseDto;
import eu.bcvsolutions.idm.core.security.api.exception.MustChangePasswordException;
import eu.bcvsolutions.idm.core.security.api.exception.TwoFactorAuthenticationRequiredException;
import eu.bcvsolutions.idm.core.security.api.service.TokenManager;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Two factor authentization manager test.
 * 
 * @author Radek Tomiška
 */
public class DefaultTwoFactorAuthenticationManagerIntegrationTest extends AbstractIntegrationTest {
	
	@Autowired private ApplicationContext context;
	@Autowired private TokenManager tokenManager;
	@Autowired private IdmProfileService profileService;
	@Autowired private AuthenticationManager authenticationManager;
	@Autowired private IdmPasswordService passwordService;
	@Autowired private JwtAuthenticationMapper jwtAuthenticationMapper;
	//
	private DefaultTwoFactorAuthenticationManager manager;
	
	@Before
	public void init() {		
		manager = context.getAutowireCapableBeanFactory().createBean(DefaultTwoFactorAuthenticationManager.class);
		getHelper().setConfigurationValue("idm.pub.app.stage", "development");
	}
	
	@After
	public void after() {
		getHelper().setConfigurationValue("idm.pub.app.stage", null);
	}
	
	@Test
	public void testRegistrationApplication() {
		IdmIdentityDto identity = getHelper().createIdentity(); // password is needed
		//
		TwoFactorRegistrationResponseDto initResponse = manager.init(identity.getId(), TwoFactorAuthenticationType.APPLICATION);
		Assert.assertNotNull(initResponse);
		Assert.assertNotNull(initResponse.getVerificationSecret());
		Assert.assertEquals(identity.getUsername(), initResponse.getUsername());
		Assert.assertNotNull(initResponse.getQrcode());
		//
		// confirm
		TwoFactorRegistrationConfirmDto confirm = new TwoFactorRegistrationConfirmDto();
		confirm.setVerificationSecret(new GuardedString(initResponse.getVerificationSecret()));
		confirm.setVerificationCode(manager.generateCode(new GuardedString(initResponse.getVerificationSecret())));
		confirm.setTwoFactorAuthenticationType(TwoFactorAuthenticationType.APPLICATION);
		Assert.assertTrue(manager.confirm(identity.getId(), confirm));
		Assert.assertEquals(initResponse.getVerificationSecret(), getHelper().getPassword(identity).getVerificationSecret());
		Assert.assertEquals(TwoFactorAuthenticationType.APPLICATION, manager.getTwoFactorAuthenticationType(identity.getId()));
	}
	
	@Test
	public void testRegistrationNotification() {
		IdmIdentityDto identity = getHelper().createIdentity(); // password is needed
		Assert.assertNull(manager.getTwoFactorAuthenticationType(identity.getId()));
		//
		TwoFactorRegistrationResponseDto initResponse = manager.init(identity.getId(), TwoFactorAuthenticationType.NOTIFICATION);
		Assert.assertNotNull(initResponse);
		Assert.assertNotNull(initResponse.getVerificationSecret());
		Assert.assertEquals(identity.getUsername(), initResponse.getUsername());
		Assert.assertNull(initResponse.getQrcode());
		//
		// confirm
		TwoFactorRegistrationConfirmDto confirm = new TwoFactorRegistrationConfirmDto();
		confirm.setVerificationSecret(new GuardedString(initResponse.getVerificationSecret()));
		confirm.setVerificationCode(manager.generateCode(new GuardedString(initResponse.getVerificationSecret())));
		confirm.setTwoFactorAuthenticationType(TwoFactorAuthenticationType.NOTIFICATION);
		Assert.assertTrue(manager.confirm(identity.getId(), confirm));
		Assert.assertEquals(initResponse.getVerificationSecret(), getHelper().getPassword(identity).getVerificationSecret());
		Assert.assertEquals(TwoFactorAuthenticationType.NOTIFICATION, manager.getTwoFactorAuthenticationType(identity.getId()));
	}
	
	@Test(expected = EntityNotFoundException.class)
	public void testConfirmRegistrationWithoutPassword() {
		manager.confirm(UUID.randomUUID(), new TwoFactorRegistrationConfirmDto());
	}
	
	@Test(expected = ResultCodeException.class)
	public void testConfirmRegistrationWithWrongCode() {
		IdmIdentityDto identity = getHelper().createIdentity(); // password is needed
		TwoFactorRegistrationConfirmDto confirm = new TwoFactorRegistrationConfirmDto();
		confirm.setVerificationSecret(new GuardedString("mock"));
		confirm.setVerificationCode(new GuardedString("mock"));
		//
		manager.confirm(identity.getId(), confirm);
	}
	
	@Test
	public void testGenerateCode() {
		IdmIdentityDto identity = getHelper().createIdentity(); // password is needed
		//
		TwoFactorRegistrationResponseDto initResponse = manager.init(identity.getId(), TwoFactorAuthenticationType.APPLICATION);
		Assert.assertNotNull(initResponse);
		Assert.assertNotNull(initResponse.getVerificationSecret());
		Assert.assertEquals(identity.getUsername(), initResponse.getUsername());
		Assert.assertNotNull(initResponse.getQrcode());
		//
		// confirm
		TwoFactorRegistrationConfirmDto confirm = new TwoFactorRegistrationConfirmDto();
		confirm.setVerificationSecret(new GuardedString(initResponse.getVerificationSecret()));
		confirm.setVerificationCode(manager.generateCode(new GuardedString(initResponse.getVerificationSecret())));
		Assert.assertTrue(manager.confirm(identity.getId(), confirm));
		Assert.assertEquals(initResponse.getVerificationSecret(), getHelper().getPassword(identity).getVerificationSecret());
		//
		GuardedString generateCode = manager.generateCode(identity.getId());
		Assert.assertNotNull(generateCode);
		Assert.assertFalse(generateCode.asString().isEmpty());
		Assert.assertTrue(manager.verifyCode(identity.getId(), generateCode));
		Assert.assertFalse(manager.verifyCode(identity.getId(), new GuardedString("xxxxxx")));
	}
	
	@Test(expected = EntityNotFoundException.class)
	public void testGenerateCodeWithoutPassword() {
		manager.generateCode(UUID.randomUUID());
	}
	
	@Test(expected = EntityNotFoundException.class)
	public void testVerifyCodeWithoutPassword() {
		manager.verifyCode(UUID.randomUUID(), new GuardedString("mock"));
	}
	
	@Test
	public void testRequireTwoFactorAuthenticationApplication() {
		IdmIdentityDto identity = getHelper().createIdentity(); // password is needed
		//
		TwoFactorRegistrationResponseDto initResponse = manager.init(identity.getId(), TwoFactorAuthenticationType.APPLICATION);
		Assert.assertNotNull(initResponse);
		Assert.assertNotNull(initResponse.getVerificationSecret());
		Assert.assertEquals(identity.getUsername(), initResponse.getUsername());
		Assert.assertNotNull(initResponse.getQrcode());
		//
		// confirm
		TwoFactorRegistrationConfirmDto confirm = new TwoFactorRegistrationConfirmDto();
		confirm.setVerificationSecret(new GuardedString(initResponse.getVerificationSecret()));
		confirm.setVerificationCode(manager.generateCode(new GuardedString(initResponse.getVerificationSecret())));
		confirm.setTwoFactorAuthenticationType(TwoFactorAuthenticationType.APPLICATION);
		Assert.assertTrue(manager.confirm(identity.getId(), confirm));
		Assert.assertEquals(initResponse.getVerificationSecret(), getHelper().getPassword(identity).getVerificationSecret());
		//
		IdmTokenDto token = createToken(identity, false);
		//
		Assert.assertTrue(manager.requireTwoFactorAuthentication(identity.getId(), token.getId()));
	}
	
	@Test
	public void testRequireTwoFactorAuthenticationNotification() {
		IdmIdentityDto identity = getHelper().createIdentity(); // password is needed
		//
		TwoFactorRegistrationResponseDto initResponse = manager.init(identity.getId(), TwoFactorAuthenticationType.NOTIFICATION);
		Assert.assertNotNull(initResponse);
		Assert.assertNotNull(initResponse.getVerificationSecret());
		Assert.assertEquals(identity.getUsername(), initResponse.getUsername());
		Assert.assertNull(initResponse.getQrcode());
		//
		// confirm
		TwoFactorRegistrationConfirmDto confirm = new TwoFactorRegistrationConfirmDto();
		confirm.setVerificationSecret(new GuardedString(initResponse.getVerificationSecret()));
		confirm.setVerificationCode(manager.generateCode(new GuardedString(initResponse.getVerificationSecret())));
		confirm.setTwoFactorAuthenticationType(TwoFactorAuthenticationType.NOTIFICATION);
		Assert.assertTrue(manager.confirm(identity.getId(), confirm));
		Assert.assertEquals(initResponse.getVerificationSecret(), getHelper().getPassword(identity).getVerificationSecret());
		//
		IdmTokenDto token = createToken(identity, false);
		//
		Assert.assertTrue(manager.requireTwoFactorAuthentication(identity.getId(), token.getId()));
	}
	
	@Test
	public void testNotRequireTwoFactorAuthenticationWithoutProfile() {
		IdmIdentityDto identity = getHelper().createIdentity(); // password is needed
		//
		Assert.assertFalse(manager.requireTwoFactorAuthentication(UUID.randomUUID(), null));
		Assert.assertFalse(manager.requireTwoFactorAuthentication(identity.getId(), null));
	}
	
	@Test(expected = EntityNotFoundException.class)
	public void testRequireTwoFactorAuthenticationNotificationWithoutPassword() {
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null); // password is needed
		IdmProfileDto profile = getHelper().createProfile(identity);
		profile.setTwoFactorAuthenticationType(TwoFactorAuthenticationType.NOTIFICATION); // set without confirm, secret etc.
		profileService.save(profile);
		//
		IdmTokenDto token = createToken(identity, false);
		//
		manager.requireTwoFactorAuthentication(identity.getId(), token.getId());
	}
	
	@Test
	public void testNotRequireTwoFactorAuthenticationWithVerifiedToken() {
		IdmIdentityDto identity = getHelper().createIdentity(); // password is needed
		//
		TwoFactorRegistrationResponseDto initResponse = manager.init(identity.getId(), TwoFactorAuthenticationType.NOTIFICATION);
		Assert.assertNotNull(initResponse);
		Assert.assertNotNull(initResponse.getVerificationSecret());
		Assert.assertEquals(identity.getUsername(), initResponse.getUsername());
		Assert.assertNull(initResponse.getQrcode());
		//
		// confirm
		TwoFactorRegistrationConfirmDto confirm = new TwoFactorRegistrationConfirmDto();
		confirm.setVerificationSecret(new GuardedString(initResponse.getVerificationSecret()));
		confirm.setVerificationCode(manager.generateCode(new GuardedString(initResponse.getVerificationSecret())));
		confirm.setTwoFactorAuthenticationType(TwoFactorAuthenticationType.NOTIFICATION);
		Assert.assertTrue(manager.confirm(identity.getId(), confirm));
		Assert.assertEquals(initResponse.getVerificationSecret(), getHelper().getPassword(identity).getVerificationSecret());
		//
		IdmTokenDto token = createToken(identity, true);
		//
		Assert.assertFalse(manager.requireTwoFactorAuthentication(identity.getId(), token.getId()));
	}
	
	@Test
	public void testAuthenticate() {
		IdmIdentityDto identity = getHelper().createIdentity(); // password is needed
		//
		TwoFactorRegistrationResponseDto initResponse = manager.init(identity.getId(), TwoFactorAuthenticationType.NOTIFICATION);
		Assert.assertNotNull(initResponse);
		Assert.assertNotNull(initResponse.getVerificationSecret());
		Assert.assertEquals(identity.getUsername(), initResponse.getUsername());
		Assert.assertNull(initResponse.getQrcode());
		//
		// confirm
		TwoFactorRegistrationConfirmDto confirm = new TwoFactorRegistrationConfirmDto();
		confirm.setVerificationSecret(new GuardedString(initResponse.getVerificationSecret()));
		confirm.setVerificationCode(manager.generateCode(new GuardedString(initResponse.getVerificationSecret())));
		confirm.setTwoFactorAuthenticationType(TwoFactorAuthenticationType.NOTIFICATION);
		Assert.assertTrue(manager.confirm(identity.getId(), confirm));
		Assert.assertEquals(initResponse.getVerificationSecret(), getHelper().getPassword(identity).getVerificationSecret());
		//
		LoginDto loginDto = new LoginDto();
		loginDto.setUsername(identity.getUsername());
		loginDto.setPassword(identity.getPassword());
		// creadentials are valid
		Assert.assertTrue(authenticationManager.validate(loginDto));
		// but two factor authentication is required
		String token = null;
		try {
			authenticationManager.authenticate(loginDto);
		} catch (TwoFactorAuthenticationRequiredException ex) {
			token = ex.getToken();
		}
		Assert.assertNotNull(token);
		//
		loginDto.setToken(token);
		loginDto.setPassword(manager.generateCode(identity.getId()));
		LoginDto authenticated = manager.authenticate(loginDto);
		//
		Assert.assertNotNull(authenticated);
		Assert.assertNotNull(authenticated.getAuthentication());
		Assert.assertTrue(tokenManager.getToken(authenticated.getAuthentication().getId()).isSecretVerified());
	}
	
	@Test(expected = ResultCodeException.class)
	public void testAuthenticateTokenNotFound() {
		IdmIdentityDto identity = getHelper().createIdentity(); // password is needed
		//
		TwoFactorRegistrationResponseDto initResponse = manager.init(identity.getId(), TwoFactorAuthenticationType.NOTIFICATION);
		Assert.assertNotNull(initResponse);
		Assert.assertNotNull(initResponse.getVerificationSecret());
		Assert.assertEquals(identity.getUsername(), initResponse.getUsername());
		Assert.assertNull(initResponse.getQrcode());
		//
		// confirm
		TwoFactorRegistrationConfirmDto confirm = new TwoFactorRegistrationConfirmDto();
		confirm.setVerificationSecret(new GuardedString(initResponse.getVerificationSecret()));
		confirm.setVerificationCode(manager.generateCode(new GuardedString(initResponse.getVerificationSecret())));
		confirm.setTwoFactorAuthenticationType(TwoFactorAuthenticationType.NOTIFICATION);
		Assert.assertTrue(manager.confirm(identity.getId(), confirm));
		Assert.assertEquals(initResponse.getVerificationSecret(), getHelper().getPassword(identity).getVerificationSecret());
		//
		LoginDto loginDto = new LoginDto();
		loginDto.setUsername(identity.getUsername());
		loginDto.setPassword(manager.generateCode(identity.getId()));
		//
		manager.authenticate(loginDto);
	}
	
	@Test(expected = ResultCodeException.class)
	public void testAuthenticateWrongVerificationCode() {
		IdmIdentityDto identity = getHelper().createIdentity(); // password is needed
		//
		TwoFactorRegistrationResponseDto initResponse = manager.init(identity.getId(), TwoFactorAuthenticationType.NOTIFICATION);
		Assert.assertNotNull(initResponse);
		Assert.assertNotNull(initResponse.getVerificationSecret());
		Assert.assertEquals(identity.getUsername(), initResponse.getUsername());
		Assert.assertNull(initResponse.getQrcode());
		//
		// confirm
		TwoFactorRegistrationConfirmDto confirm = new TwoFactorRegistrationConfirmDto();
		confirm.setVerificationSecret(new GuardedString(initResponse.getVerificationSecret()));
		confirm.setVerificationCode(manager.generateCode(new GuardedString(initResponse.getVerificationSecret())));
		confirm.setTwoFactorAuthenticationType(TwoFactorAuthenticationType.NOTIFICATION);
		Assert.assertTrue(manager.confirm(identity.getId(), confirm));
		Assert.assertEquals(initResponse.getVerificationSecret(), getHelper().getPassword(identity).getVerificationSecret());
		//
		LoginDto loginDto = new LoginDto();
		loginDto.setUsername(identity.getUsername());
		loginDto.setPassword(identity.getPassword());
		// creadentials are valid
		Assert.assertTrue(authenticationManager.validate(loginDto));
		// but two factor authentication is required
		String token = null;
		try {
			authenticationManager.authenticate(loginDto);
		} catch (TwoFactorAuthenticationRequiredException ex) {
			token = ex.getToken();
		}
		Assert.assertNotNull(token);
		//
		loginDto.setToken(token);
		loginDto.setPassword(new GuardedString("mock"));
		//
		manager.authenticate(loginDto);
	}
	
	@Test(expected = MustChangePasswordException.class)
	public void testAuthenticateMustChangePassword() {
		IdmIdentityDto identity = getHelper().createIdentity(); // password is needed
		IdmPasswordDto password = passwordService.findOneByIdentity(identity.getId());
		password.setMustChange(true);
		passwordService.save(password);
		//
		TwoFactorRegistrationResponseDto initResponse = manager.init(identity.getId(), TwoFactorAuthenticationType.NOTIFICATION);
		Assert.assertNotNull(initResponse);
		Assert.assertNotNull(initResponse.getVerificationSecret());
		Assert.assertEquals(identity.getUsername(), initResponse.getUsername());
		Assert.assertNull(initResponse.getQrcode());
		//
		// confirm
		TwoFactorRegistrationConfirmDto confirm = new TwoFactorRegistrationConfirmDto();
		confirm.setVerificationSecret(new GuardedString(initResponse.getVerificationSecret()));
		confirm.setVerificationCode(manager.generateCode(new GuardedString(initResponse.getVerificationSecret())));
		confirm.setTwoFactorAuthenticationType(TwoFactorAuthenticationType.NOTIFICATION);
		Assert.assertTrue(manager.confirm(identity.getId(), confirm));
		Assert.assertEquals(initResponse.getVerificationSecret(), getHelper().getPassword(identity).getVerificationSecret());
		//
		LoginDto loginDto = new LoginDto();
		loginDto.setUsername(identity.getUsername());
		loginDto.setPassword(identity.getPassword());
		// creadentials are valid
		Assert.assertTrue(authenticationManager.validate(loginDto));
		// but two factor authentication is required
		String token = null;
		try {
			authenticationManager.authenticate(loginDto);
		} catch (TwoFactorAuthenticationRequiredException ex) {
			token = ex.getToken();
		}
		Assert.assertNotNull(token);
		//
		loginDto.setToken(token);
		loginDto.setPassword(manager.generateCode(identity.getId()));
		manager.authenticate(loginDto);
	}
	
	@Test
	public void testAuthenticateMustChangePasswordIsSkipped() {
		IdmIdentityDto identity = getHelper().createIdentity(); // password is needed
		IdmPasswordDto password = passwordService.findOneByIdentity(identity.getId());
		password.setMustChange(true);
		passwordService.save(password);
		//
		TwoFactorRegistrationResponseDto initResponse = manager.init(identity.getId(), TwoFactorAuthenticationType.NOTIFICATION);
		Assert.assertNotNull(initResponse);
		Assert.assertNotNull(initResponse.getVerificationSecret());
		Assert.assertEquals(identity.getUsername(), initResponse.getUsername());
		Assert.assertNull(initResponse.getQrcode());
		//
		// confirm
		TwoFactorRegistrationConfirmDto confirm = new TwoFactorRegistrationConfirmDto();
		confirm.setVerificationSecret(new GuardedString(initResponse.getVerificationSecret()));
		confirm.setVerificationCode(manager.generateCode(new GuardedString(initResponse.getVerificationSecret())));
		confirm.setTwoFactorAuthenticationType(TwoFactorAuthenticationType.NOTIFICATION);
		Assert.assertTrue(manager.confirm(identity.getId(), confirm));
		Assert.assertEquals(initResponse.getVerificationSecret(), getHelper().getPassword(identity).getVerificationSecret());
		//
		LoginDto loginDto = new LoginDto();
		loginDto.setUsername(identity.getUsername());
		loginDto.setPassword(identity.getPassword());
		// creadentials are valid
		Assert.assertTrue(authenticationManager.validate(loginDto));
		// but two factor authentication is required
		String token = null;
		try {
			authenticationManager.authenticate(loginDto);
		} catch (TwoFactorAuthenticationRequiredException ex) {
			token = ex.getToken();
		}
		Assert.assertNotNull(token);
		//
		loginDto.setToken(token);
		loginDto.setPassword(manager.generateCode(identity.getId()));
		loginDto.setSkipMustChange(true);
		LoginDto authenticated = manager.authenticate(loginDto);
		//
		Assert.assertNotNull(authenticated);
		Assert.assertNotNull(authenticated.getAuthentication());
		Assert.assertTrue(tokenManager.getToken(authenticated.getAuthentication().getId()).isSecretVerified());
	}
	
	@Test(expected = EntityNotFoundException.class)
	public void testAuthenticatePasswordIsDeleted() {
		IdmIdentityDto identity = getHelper().createIdentity(); // password is needed
		IdmPasswordDto password = passwordService.findOneByIdentity(identity.getId());
		//
		TwoFactorRegistrationResponseDto initResponse = manager.init(identity.getId(), TwoFactorAuthenticationType.NOTIFICATION);
		Assert.assertNotNull(initResponse);
		Assert.assertNotNull(initResponse.getVerificationSecret());
		Assert.assertEquals(identity.getUsername(), initResponse.getUsername());
		Assert.assertNull(initResponse.getQrcode());
		//
		// confirm
		TwoFactorRegistrationConfirmDto confirm = new TwoFactorRegistrationConfirmDto();
		confirm.setVerificationSecret(new GuardedString(initResponse.getVerificationSecret()));
		confirm.setVerificationCode(manager.generateCode(new GuardedString(initResponse.getVerificationSecret())));
		confirm.setTwoFactorAuthenticationType(TwoFactorAuthenticationType.NOTIFICATION);
		Assert.assertTrue(manager.confirm(identity.getId(), confirm));
		Assert.assertEquals(initResponse.getVerificationSecret(), getHelper().getPassword(identity).getVerificationSecret());
		//
		LoginDto loginDto = new LoginDto();
		loginDto.setUsername(identity.getUsername());
		loginDto.setPassword(identity.getPassword());
		// creadentials are valid
		Assert.assertTrue(authenticationManager.validate(loginDto));
		// but two factor authentication is required
		String token = null;
		try {
			authenticationManager.authenticate(loginDto);
		} catch (TwoFactorAuthenticationRequiredException ex) {
			token = ex.getToken();
		}
		Assert.assertNotNull(token);
		//
		loginDto.setToken(token);
		loginDto.setPassword(manager.generateCode(identity.getId()));
		//
		// delete password
		passwordService.delete(password);
		//
		manager.authenticate(loginDto);
	}
	
	@Test(expected = EntityNotFoundException.class)
	public void testAuthenticateIdentityIsDeleted() {
		IdmIdentityDto identity = getHelper().createIdentity(); // password is needed
		//
		TwoFactorRegistrationResponseDto initResponse = manager.init(identity.getId(), TwoFactorAuthenticationType.NOTIFICATION);
		Assert.assertNotNull(initResponse);
		Assert.assertNotNull(initResponse.getVerificationSecret());
		Assert.assertEquals(identity.getUsername(), initResponse.getUsername());
		Assert.assertNull(initResponse.getQrcode());
		//
		// confirm
		TwoFactorRegistrationConfirmDto confirm = new TwoFactorRegistrationConfirmDto();
		confirm.setVerificationSecret(new GuardedString(initResponse.getVerificationSecret()));
		confirm.setVerificationCode(manager.generateCode(new GuardedString(initResponse.getVerificationSecret())));
		confirm.setTwoFactorAuthenticationType(TwoFactorAuthenticationType.NOTIFICATION);
		Assert.assertTrue(manager.confirm(identity.getId(), confirm));
		Assert.assertEquals(initResponse.getVerificationSecret(), getHelper().getPassword(identity).getVerificationSecret());
		//
		LoginDto loginDto = new LoginDto();
		loginDto.setUsername(identity.getUsername());
		loginDto.setPassword(identity.getPassword());
		// creadentials are valid
		Assert.assertTrue(authenticationManager.validate(loginDto));
		// but two factor authentication is required
		String token = null;
		try {
			authenticationManager.authenticate(loginDto);
		} catch (TwoFactorAuthenticationRequiredException ex) {
			token = ex.getToken();
		}
		Assert.assertNotNull(token);
		//
		loginDto.setToken(token);
		loginDto.setPassword(manager.generateCode(identity.getId()));
		//
		// delete password
		getHelper().deleteIdentity(identity.getId());
		//
		manager.authenticate(loginDto);
	}
	
	@Test(expected = ResultCodeException.class)
	public void testAuthenticateTokenExpired() throws Exception {
		IdmIdentityDto identity = getHelper().createIdentity(); // password is needed
		//
		TwoFactorRegistrationResponseDto initResponse = manager.init(identity.getId(), TwoFactorAuthenticationType.NOTIFICATION);
		Assert.assertNotNull(initResponse);
		Assert.assertNotNull(initResponse.getVerificationSecret());
		Assert.assertEquals(identity.getUsername(), initResponse.getUsername());
		Assert.assertNull(initResponse.getQrcode());
		//
		// confirm
		TwoFactorRegistrationConfirmDto confirm = new TwoFactorRegistrationConfirmDto();
		confirm.setVerificationSecret(new GuardedString(initResponse.getVerificationSecret()));
		confirm.setVerificationCode(manager.generateCode(new GuardedString(initResponse.getVerificationSecret())));
		confirm.setTwoFactorAuthenticationType(TwoFactorAuthenticationType.NOTIFICATION);
		Assert.assertTrue(manager.confirm(identity.getId(), confirm));
		Assert.assertEquals(initResponse.getVerificationSecret(), getHelper().getPassword(identity).getVerificationSecret());
		//
		LoginDto loginDto = new LoginDto();
		loginDto.setUsername(identity.getUsername());
		loginDto.setPassword(identity.getPassword());
		// creadentials are valid
		Assert.assertTrue(authenticationManager.validate(loginDto));
		// but two factor authentication is required
		String token = null;
		try {
			authenticationManager.authenticate(loginDto);
		} catch (TwoFactorAuthenticationRequiredException ex) {
			token = ex.getToken();
		}
		Assert.assertNotNull(token);
		//
		// set token expiration
		IdmJwtAuthentication jwt = jwtAuthenticationMapper.readToken(token);
		jwt.setExpiration(ZonedDateTime.now().minusDays(1));
		token = jwtAuthenticationMapper.writeToken(jwt);
		//
		loginDto.setToken(token);
		loginDto.setPassword(manager.generateCode(identity.getId()));
		//
		manager.authenticate(loginDto);
	}
	
	@Test
	public void testAuthenticateTokenNotExpired() throws Exception {
		IdmIdentityDto identity = getHelper().createIdentity(); // password is needed
		//
		TwoFactorRegistrationResponseDto initResponse = manager.init(identity.getId(), TwoFactorAuthenticationType.NOTIFICATION);
		Assert.assertNotNull(initResponse);
		Assert.assertNotNull(initResponse.getVerificationSecret());
		Assert.assertEquals(identity.getUsername(), initResponse.getUsername());
		Assert.assertNull(initResponse.getQrcode());
		//
		// confirm
		TwoFactorRegistrationConfirmDto confirm = new TwoFactorRegistrationConfirmDto();
		confirm.setVerificationSecret(new GuardedString(initResponse.getVerificationSecret()));
		confirm.setVerificationCode(manager.generateCode(new GuardedString(initResponse.getVerificationSecret())));
		confirm.setTwoFactorAuthenticationType(TwoFactorAuthenticationType.NOTIFICATION);
		Assert.assertTrue(manager.confirm(identity.getId(), confirm));
		Assert.assertEquals(initResponse.getVerificationSecret(), getHelper().getPassword(identity).getVerificationSecret());
		//
		LoginDto loginDto = new LoginDto();
		loginDto.setUsername(identity.getUsername());
		loginDto.setPassword(identity.getPassword());
		// creadentials are valid
		Assert.assertTrue(authenticationManager.validate(loginDto));
		// but two factor authentication is required
		String token = null;
		try {
			authenticationManager.authenticate(loginDto);
		} catch (TwoFactorAuthenticationRequiredException ex) {
			token = ex.getToken();
		}
		Assert.assertNotNull(token);
		//
		// set token expiration
		IdmJwtAuthentication jwt = jwtAuthenticationMapper.readToken(token);
		jwt.setExpiration(ZonedDateTime.now().plusDays(1));
		token = jwtAuthenticationMapper.writeToken(jwt);
		//
		loginDto.setToken(token);
		loginDto.setPassword(manager.generateCode(identity.getId()));
		//
		LoginDto authenticated = manager.authenticate(loginDto);
		//
		Assert.assertNotNull(authenticated);
		Assert.assertNotNull(authenticated.getAuthentication());
		Assert.assertTrue(tokenManager.getToken(authenticated.getAuthentication().getId()).isSecretVerified());
	}
	
	private IdmTokenDto createToken(IdmIdentityDto owner, boolean secretVerified) {
		IdmTokenDto dto = new IdmTokenDto();
		dto.setTokenType("mock");
		dto.setToken("mock");
		dto.setIssuedAt(ZonedDateTime.now());
		dto.setSecretVerified(secretVerified);
		//
		return tokenManager.saveToken(owner, dto);
	}
}
