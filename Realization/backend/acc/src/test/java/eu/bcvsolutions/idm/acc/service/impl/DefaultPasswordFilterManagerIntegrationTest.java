package eu.bcvsolutions.idm.acc.service.impl;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.PageImpl;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.acc.AbstractPasswordFilterIntegrationTest;
import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.dto.AccPasswordFilterRequestDto;
import eu.bcvsolutions.idm.acc.dto.AccUniformPasswordDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemAttributeMappingDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.IdmPasswordPolicyGenerateType;
import eu.bcvsolutions.idm.core.api.domain.IdmPasswordPolicyType;
import eu.bcvsolutions.idm.core.api.domain.IdmScriptCategory;
import eu.bcvsolutions.idm.core.api.domain.ScriptAuthorityType;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmPasswordDto;
import eu.bcvsolutions.idm.core.api.dto.IdmPasswordPolicyDto;
import eu.bcvsolutions.idm.core.api.dto.IdmScriptDto;
import eu.bcvsolutions.idm.core.api.dto.PasswordChangeDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityFilter;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.exception.PasswordChangeException;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;

/**
 * Test for {@link DefaultPasswordFilterManager} and combination with {@link DefaultAccUniformPasswordService}
 *
 * @author Ondrej Kopr
 * @author Radek Tomiška
 */
public class DefaultPasswordFilterManagerIntegrationTest extends AbstractPasswordFilterIntegrationTest {

	
	@Autowired private ApplicationContext context;
	//
	private DefaultPasswordFilterManager passwordFilterManager;
	
	@Before
	@Override
	public void before() {
		super.before();
		//
		passwordFilterManager = context.getAutowireCapableBeanFactory().createBean(DefaultPasswordFilterManager.class);
	}
	
	@Test
	public void testGreenLine() {
		SysSystemDto system = createSystem(false);
		IdmIdentityDto identity = createIdentity(system);
		setPasswordFilter(system, true);
		String password = getHelper().createName();
		PasswordRequest request = prepareRequest(identity.getUsername(), system.getCode(), password);

		checkEcho(identity, system, EchoCheck.DOESNT_EXIST);

		processValidate(request, true);

		checkEcho(identity, system, EchoCheck.VALIDATE);
		checkPassword(prepareUid(identity, system), password, false);

		processChange(request, true);

		checkEcho(identity, system, EchoCheck.VALIDATE_AND_CHANGE);
		checkPassword(prepareUid(identity, system), password, false); // Password will not be same!
	}
	
	@Test
	public void testValidateWithMinPasswordAgeGreenLine() {
		SysSystemDto system = createSystem(false);
		IdmPasswordPolicyDto policy = new IdmPasswordPolicyDto();
		policy.setName(getHelper().createName());
		policy.setType(IdmPasswordPolicyType.VALIDATE);
		policy.setMinPasswordAge(1); 
		policy = passwordPolicyService.save(policy);
		system.setPasswordPolicyValidate(policy.getId());
		system = systemService.save(system);
		IdmIdentityDto identity = createIdentity(system);
		setPasswordFilter(system, true);

		assignSystem(createUniformDefinition(true), system);

		cleanProvivisioning(identity, system);
		checkChangeInIdm(identity, 0);

		String password = getHelper().createName();
		PasswordRequest request = prepareRequest(identity.getUsername(), system.getCode(), password);

		IdmPasswordDto idmPassword = passwordService.findOneByIdentity(identity.getId());
		Assert.assertNull(idmPassword);
		
		checkEmptyProvisioning(identity, system);
		checkEcho(identity, system, EchoCheck.DOESNT_EXIST);
		checkChangeInIdm(identity, 0);

		processValidate(request, true);

		checkEcho(identity, system, EchoCheck.VALIDATE);
		checkChangeInIdm(identity, 0);
		checkEmptyProvisioning(identity, system);

		processChange(request, true);
		
		checkEcho(identity, system, EchoCheck.VALIDATE_AND_CHANGE);
		checkChangeInIdm(identity, 1);
		checkEmptyProvisioning(identity, system);
		
		idmPassword = passwordService.findOneByIdentity(identity.getId());
		Assert.assertNotNull(idmPassword);
		Assert.assertNotNull(idmPassword.getValidFrom()); // under different user, but set
	}
	
	@Test(expected = PasswordChangeException.class)
	public void testValidateWithMinPasswordAgeFailed() {
		SysSystemDto system = createSystem(false);
		IdmPasswordPolicyDto policy = new IdmPasswordPolicyDto();
		policy.setName(getHelper().createName());
		policy.setType(IdmPasswordPolicyType.VALIDATE);
		policy.setMinPasswordAge(1); 
		policy = passwordPolicyService.save(policy);
		system.setPasswordPolicyValidate(policy.getId());
		system = systemService.save(system);
		IdmIdentityDto identity = createIdentity(system);
		setPasswordFilter(system, true);

		assignSystem(createUniformDefinition(true), system);

		cleanProvivisioning(identity, system);
		checkChangeInIdm(identity, 0);

		String password = getHelper().createName();
		PasswordRequest request = prepareRequest(identity.getUsername(), system.getCode(), password);

		IdmPasswordDto idmPassword = passwordService.findOneByIdentity(identity.getId());
		Assert.assertNull(idmPassword);
		
		checkEmptyProvisioning(identity, system);
		checkEcho(identity, system, EchoCheck.DOESNT_EXIST);
		checkChangeInIdm(identity, 0);

		processValidate(request, true);

		checkEcho(identity, system, EchoCheck.VALIDATE);
		checkChangeInIdm(identity, 0);
		checkEmptyProvisioning(identity, system);

		processChange(request, true);
		
		checkEcho(identity, system, EchoCheck.VALIDATE_AND_CHANGE);
		checkChangeInIdm(identity, 1);
		checkEmptyProvisioning(identity, system);
		
		idmPassword = passwordService.findOneByIdentity(identity.getId());
		Assert.assertNotNull(idmPassword);
		Assert.assertNotNull(idmPassword.getValidFrom()); // under different user, but set
		//
		// fail => cannot be changed again by policy
		try {
			request.setPassword(getHelper().createName());
			IdmIdentityDto manager = getHelper().createIdentity();
			getHelper().login(manager);
			//
			AccPasswordFilterRequestDto passwordFilterRequest = new AccPasswordFilterRequestDto();
			passwordFilterRequest.setUsername(identity.getUsername());
			passwordFilterRequest.setPassword(new GuardedString(getHelper().createName())); // different password => new validate without cache
			passwordFilterRequest.setResource(system.getCode());
			//
			passwordFilterManager.validate(passwordFilterRequest);
		} finally {
			getHelper().logout();
		}
	}
	
	@Test
	public void testValidateWithMinPasswordAgeUnderAdmin() {
		SysSystemDto system = createSystem(false);
		IdmPasswordPolicyDto policy = new IdmPasswordPolicyDto();
		policy.setName(getHelper().createName());
		policy.setType(IdmPasswordPolicyType.VALIDATE);
		policy.setMinPasswordAge(1); 
		policy = passwordPolicyService.save(policy);
		system.setPasswordPolicyValidate(policy.getId());
		system = systemService.save(system);
		IdmIdentityDto identity = createIdentity(system);
		setPasswordFilter(system, true);

		assignSystem(createUniformDefinition(true), system);

		cleanProvivisioning(identity, system);
		checkChangeInIdm(identity, 0);

		String password = getHelper().createName();
		PasswordRequest request = prepareRequest(identity.getUsername(), system.getCode(), password);

		IdmPasswordDto idmPassword = passwordService.findOneByIdentity(identity.getId());
		Assert.assertNull(idmPassword);
		
		checkEmptyProvisioning(identity, system);
		checkEcho(identity, system, EchoCheck.DOESNT_EXIST);
		checkChangeInIdm(identity, 0);

		processValidate(request, true);

		checkEcho(identity, system, EchoCheck.VALIDATE);
		checkChangeInIdm(identity, 0);
		checkEmptyProvisioning(identity, system);

		processChange(request, true);
		
		checkEcho(identity, system, EchoCheck.VALIDATE_AND_CHANGE);
		checkChangeInIdm(identity, 1);
		checkEmptyProvisioning(identity, system);
		
		idmPassword = passwordService.findOneByIdentity(identity.getId());
		Assert.assertNotNull(idmPassword);
		Assert.assertNotNull(idmPassword.getValidFrom()); // under different user, but set
		//
		// success => admin can change password
		try {
			request.setPassword(getHelper().createName());
			getHelper().loginAdmin();
			//
			AccPasswordFilterRequestDto passwordFilterRequest = new AccPasswordFilterRequestDto();
			passwordFilterRequest.setUsername(identity.getUsername());
			passwordFilterRequest.setPassword(new GuardedString(getHelper().createName())); // different password => new validate without cache
			passwordFilterRequest.setResource(system.getCode());
			//
			passwordFilterManager.validate(passwordFilterRequest);
		} finally {
			getHelper().logout();
		}
	}

	@Test
	public void testReadonlySystemPasswordFilterChange() {
		SysSystemDto system = createSystem(false);
		SysSystemDto systemTwo = createSystem(false);
		IdmIdentityDto identity = createIdentity(system);
		assignSystem(identity, systemTwo);
		setPasswordFilter(system, true);
		setPasswordFilter(systemTwo, true);
		assignSystem(createUniformDefinition(false), system, systemTwo);

		cleanProvivisioning(identity, system, systemTwo);

		systemTwo.setReadonly(true);
		systemTwo.setQueue(true);
		systemService.save(systemTwo);

		String password = getHelper().createName();
		
		PasswordRequest request = prepareRequest(identity.getUsername(), system.getCode(), password);

		checkEcho(identity, system, EchoCheck.DOESNT_EXIST);
		checkEcho(identity, systemTwo, EchoCheck.DOESNT_EXIST);

		processValidate(request, true);

		checkEcho(identity, system, EchoCheck.VALIDATE);
		checkEcho(identity, systemTwo, EchoCheck.VALIDATE);
		checkPassword(prepareUid(identity, system), password, false);

		processChange(request, true);

		checkEcho(identity, system, EchoCheck.VALIDATE_AND_CHANGE);
		checkEcho(identity, systemTwo, EchoCheck.VALIDATE); // Readonly system
		checkPassword(prepareUid(identity, system), password, false); // Password will not be same!
		checkPassword(prepareUid(identity, systemTwo), password, false);

		checkActivePasswordOperation(identity, systemTwo, 1, password);
	}

	@Test
	public void testChangeFromIdmWithoutOneManagedSystem() {
		SysSystemDto system = createSystem(false);
		SysSystemDto systemTwo = createSystem(false);
		IdmIdentityDto identity = createIdentity(system);
		assignSystem(identity, systemTwo);
		assignSystem(createUniformDefinition(false), system, systemTwo);

		setPasswordFilter(system, true);
		setPasswordFilter(systemTwo, false);

		cleanProvivisioning(identity, system, systemTwo);

		checkEcho(identity, system, EchoCheck.DOESNT_EXIST);
		checkEcho(identity, systemTwo, EchoCheck.DOESNT_EXIST);

		String password = getHelper().createName();

		PasswordChangeDto passwordChangeDto = new PasswordChangeDto();
		passwordChangeDto.setAll(true);
		passwordChangeDto.setNewPassword(new GuardedString(password));

		List<OperationResult> results = identityService.passwordChange(identity, passwordChangeDto);
		assertEquals(2, results.size());

		checkEcho(identity, system, EchoCheck.VALIDATE_AND_CHANGE);
		checkEcho(identity, systemTwo, EchoCheck.DOESNT_EXIST);

		checkPassword(prepareUid(identity, system), password, true);
		checkPassword(prepareUid(identity, systemTwo), password, true);

		checkActivePasswordOperation(identity, systemTwo, 0, password);
		checkActivePasswordOperation(identity, system, 0, password);
	}

	@Test
	public void testReadonlySystemChangeFromIdm() {
		SysSystemDto system = createSystem(false);
		SysSystemDto systemTwo = createSystem(false);
		IdmIdentityDto identity = createIdentity(system);
		assignSystem(identity, systemTwo);
		assignSystem(createUniformDefinition(false), system, systemTwo);

		setPasswordFilter(system, true);
		setPasswordFilter(systemTwo, true);

		cleanProvivisioning(identity, system, systemTwo);

		systemTwo.setReadonly(true);
		systemTwo.setQueue(true);
		systemService.save(systemTwo);

		checkEcho(identity, system, EchoCheck.DOESNT_EXIST);
		checkEcho(identity, systemTwo, EchoCheck.DOESNT_EXIST);

		String password = getHelper().createName();

		PasswordChangeDto passwordChangeDto = new PasswordChangeDto();
		passwordChangeDto.setAll(true);
		passwordChangeDto.setNewPassword(new GuardedString(password));

		List<OperationResult> results = identityService.passwordChange(identity, passwordChangeDto);
		assertEquals(2, results.size());

		checkEcho(identity, system, EchoCheck.VALIDATE_AND_CHANGE);
		checkEcho(identity, systemTwo, EchoCheck.VALIDATE);

		checkPassword(prepareUid(identity, system), password, true);
		checkPassword(prepareUid(identity, systemTwo), password, false);

		checkActivePasswordOperation(identity, systemTwo, 1, password);
		checkActivePasswordOperation(identity, system, 0, password);
	}

	@Test
	public void testChangeAllFromIdm() {
		SysSystemDto system = createSystem(false);
		SysSystemDto systemTwo = createSystem(false);
		IdmIdentityDto identity = createIdentity(system);
		assignSystem(identity, systemTwo);
		assignSystem(createUniformDefinition(true), system, systemTwo);

		setPasswordFilter(system, true);
		setPasswordFilter(systemTwo, true);

		cleanProvivisioning(identity, system, systemTwo);

		checkEcho(identity, system, EchoCheck.DOESNT_EXIST);
		checkEcho(identity, systemTwo, EchoCheck.DOESNT_EXIST);

		String password = getHelper().createName();

		PasswordChangeDto passwordChangeDto = new PasswordChangeDto();
		passwordChangeDto.setAll(true);
		passwordChangeDto.setNewPassword(new GuardedString(password));

		List<OperationResult> results = identityService.passwordChange(identity, passwordChangeDto);
		assertEquals(3, results.size());

		checkEcho(identity, system, EchoCheck.VALIDATE_AND_CHANGE);
		checkEcho(identity, systemTwo, EchoCheck.VALIDATE_AND_CHANGE);

		checkPassword(prepareUid(identity, system), password, true);
		checkPassword(prepareUid(identity, systemTwo), password, true);

		checkActivePasswordOperation(identity, systemTwo, 0, password);
		checkActivePasswordOperation(identity, system, 0, password);
		checkPassword(prepareUid(identity, system), password, true);
		checkPassword(prepareUid(identity, systemTwo), password, true);
		loginToIdm(identity, password, true);
	}

	@Test
	public void testChangeAllFromIdmWithoutPasswordFiler() {
		SysSystemDto system = createSystem(false);
		SysSystemDto systemTwo = createSystem(false);
		IdmIdentityDto identity = createIdentity(system);
		assignSystem(identity, systemTwo);
		assignSystem(createUniformDefinition(true), system, systemTwo);

		setPasswordFilter(system, false);
		setPasswordFilter(systemTwo, false);

		cleanProvivisioning(identity, system, systemTwo);

		checkEcho(identity, system, EchoCheck.DOESNT_EXIST);
		checkEcho(identity, systemTwo, EchoCheck.DOESNT_EXIST);

		String password = getHelper().createName();

		PasswordChangeDto passwordChangeDto = new PasswordChangeDto();
		passwordChangeDto.setAll(true);
		passwordChangeDto.setNewPassword(new GuardedString(password));

		List<OperationResult> results = identityService.passwordChange(identity, passwordChangeDto);
		assertEquals(3, results.size());

		checkEcho(identity, system, EchoCheck.DOESNT_EXIST);
		checkEcho(identity, systemTwo, EchoCheck.DOESNT_EXIST);

		checkPassword(prepareUid(identity, system), password, true);
		checkPassword(prepareUid(identity, systemTwo), password, true);

		checkActivePasswordOperation(identity, systemTwo, 0, password);
		checkActivePasswordOperation(identity, system, 0, password);
		checkPassword(prepareUid(identity, system), password, true);
		checkPassword(prepareUid(identity, systemTwo), password, true);
		loginToIdm(identity, password, true);
	}

	@Test
	public void testChangeAllFromIdmWithoutUniformPassword() {
		SysSystemDto system = createSystem(false);
		SysSystemDto systemTwo = createSystem(false);
		IdmIdentityDto identity = createIdentity(system);
		assignSystem(identity, systemTwo);

		setPasswordFilter(system, true);
		setPasswordFilter(systemTwo, true);

		cleanProvivisioning(identity, system, systemTwo);

		checkEcho(identity, system, EchoCheck.DOESNT_EXIST);
		checkEcho(identity, systemTwo, EchoCheck.DOESNT_EXIST);

		String password = getHelper().createName();

		PasswordChangeDto passwordChangeDto = new PasswordChangeDto();
		passwordChangeDto.setAll(true);
		passwordChangeDto.setNewPassword(new GuardedString(password));

		List<OperationResult> results = identityService.passwordChange(identity, passwordChangeDto);
		assertEquals(2, results.size());

		checkEcho(identity, system, EchoCheck.VALIDATE_AND_CHANGE);
		checkEcho(identity, systemTwo, EchoCheck.VALIDATE_AND_CHANGE);

		checkPassword(prepareUid(identity, system), password, true);
		checkPassword(prepareUid(identity, systemTwo), password, true);

		checkActivePasswordOperation(identity, systemTwo, 0, password);
		checkActivePasswordOperation(identity, system, 0, password);
		checkPassword(prepareUid(identity, system), password, true);
		checkPassword(prepareUid(identity, systemTwo), password, true);
		loginToIdm(identity, password, false);
	}

	@Test
	public void testChangeAllAndIdmFromIdmWithoutUniformPassword() {
		SysSystemDto system = createSystem(false);
		SysSystemDto systemTwo = createSystem(false);
		IdmIdentityDto identity = createIdentity(system);
		assignSystem(identity, systemTwo);

		setPasswordFilter(system, true);
		setPasswordFilter(systemTwo, true);

		cleanProvivisioning(identity, system, systemTwo);

		checkEcho(identity, system, EchoCheck.DOESNT_EXIST);
		checkEcho(identity, systemTwo, EchoCheck.DOESNT_EXIST);

		String password = getHelper().createName();

		PasswordChangeDto passwordChangeDto = new PasswordChangeDto();
		passwordChangeDto.setAll(true);
		passwordChangeDto.setIdm(true);
		passwordChangeDto.setNewPassword(new GuardedString(password));

		List<OperationResult> results = identityService.passwordChange(identity, passwordChangeDto);
		assertEquals(3, results.size());

		checkEcho(identity, system, EchoCheck.VALIDATE_AND_CHANGE);
		checkEcho(identity, systemTwo, EchoCheck.VALIDATE_AND_CHANGE);

		checkPassword(prepareUid(identity, system), password, true);
		checkPassword(prepareUid(identity, systemTwo), password, true);

		checkActivePasswordOperation(identity, systemTwo, 0, password);
		checkActivePasswordOperation(identity, system, 0, password);
		checkPassword(prepareUid(identity, system), password, true);
		checkPassword(prepareUid(identity, systemTwo), password, true);
		loginToIdm(identity, password, true);
	}

	@Test
	public void testChangeAllFromIdmWithoutUnifomPassword() {
		SysSystemDto system = createSystem(false);
		SysSystemDto systemTwo = createSystem(false);
		IdmIdentityDto identity = createIdentity(system);
		assignSystem(identity, systemTwo);

		setPasswordFilter(system, true);
		setPasswordFilter(systemTwo, true);

		cleanProvivisioning(identity, system, systemTwo);

		checkEcho(identity, system, EchoCheck.DOESNT_EXIST);
		checkEcho(identity, systemTwo, EchoCheck.DOESNT_EXIST);

		String password = getHelper().createName();

		PasswordChangeDto passwordChangeDto = new PasswordChangeDto();
		passwordChangeDto.setAll(true);
		passwordChangeDto.setNewPassword(new GuardedString(password));

		List<OperationResult> results = identityService.passwordChange(identity, passwordChangeDto);
		assertEquals(2, results.size());

		checkEcho(identity, system, EchoCheck.VALIDATE_AND_CHANGE);
		checkEcho(identity, systemTwo, EchoCheck.VALIDATE_AND_CHANGE);

		checkPassword(prepareUid(identity, system), password, true);
		checkPassword(prepareUid(identity, systemTwo), password, true);

		checkActivePasswordOperation(identity, systemTwo, 0, password);
		checkActivePasswordOperation(identity, system, 0, password);
		checkPassword(prepareUid(identity, system), password, true);
		checkPassword(prepareUid(identity, systemTwo), password, true);
		loginToIdm(identity, password, false);
	}

	@Test
	public void testReadonlySystemChangeFromPasswordFilter() {
		SysSystemDto system = createSystem(false);
		SysSystemDto systemTwo = createSystem(false);
		IdmIdentityDto identity = createIdentity(system);
		assignSystem(identity, systemTwo);
		assignSystem(createUniformDefinition(false), system, systemTwo);

		setPasswordFilter(system, true);
		setPasswordFilter(systemTwo, true);

		cleanProvivisioning(identity, system, systemTwo);

		systemTwo.setReadonly(true);
		systemTwo.setQueue(true);
		systemService.save(systemTwo);

		checkEcho(identity, system, EchoCheck.DOESNT_EXIST);
		checkEcho(identity, systemTwo, EchoCheck.DOESNT_EXIST);
		
		String password = getHelper().createName();

		PasswordRequest request = new PasswordRequest(password, identity.getUsername(), system.getCode());
		processValidate(request, true);

		checkEcho(identity, system, EchoCheck.VALIDATE);
		checkEcho(identity, systemTwo, EchoCheck.VALIDATE);

		checkPassword(prepareUid(identity, system), password, false);
		checkPassword(prepareUid(identity, systemTwo), password, false);
		
		processChange(request, true);

		checkEcho(identity, system, EchoCheck.VALIDATE_AND_CHANGE);
		checkEcho(identity, systemTwo, EchoCheck.VALIDATE);

		checkActivePasswordOperation(identity, system, 0, password);
		checkActivePasswordOperation(identity, systemTwo, 1, password);
	}

	@Test
	public void testExpiredEchoOneSystem() throws InterruptedException {
		SysSystemDto system = createSystem(false);
		SysSystemDto systemTwo = createSystem(false);
		IdmIdentityDto identity = createIdentity(system);
		assignSystem(identity, systemTwo);
		SysSystemAttributeMappingDto passwordFilter = setPasswordFilter(system, true);
		passwordFilter.setEchoTimeout(2);
		passwordFilter = systemAttributeMappingService.save(passwordFilter);
		passwordFilter = setPasswordFilter(system, true);
		passwordFilter.setEchoTimeout(2);
		passwordFilter = systemAttributeMappingService.save(passwordFilter);

		setPasswordFilter(systemTwo, true);

		assignSystem(createUniformDefinition(false), system, systemTwo);

		cleanProvivisioning(identity, system, systemTwo);

		checkProcessedPasswordOperation(identity, system, 0, null);
		checkProcessedPasswordOperation(identity, systemTwo, 0, null);

		String password = getHelper().createName();
		PasswordRequest request = prepareRequest(identity.getUsername(), system.getCode(), password);

		checkEcho(identity, system, EchoCheck.DOESNT_EXIST);
		checkEcho(identity, systemTwo, EchoCheck.DOESNT_EXIST);
		checkProcessedPasswordOperation(identity, system, 0, password);
		checkProcessedPasswordOperation(identity, systemTwo, 0, password);

		processValidate(request, true);

		checkEcho(identity, system, EchoCheck.VALIDATE);
		checkEcho(identity, systemTwo, EchoCheck.VALIDATE);
		checkProcessedPasswordOperation(identity, system, 0, password);
		checkProcessedPasswordOperation(identity, systemTwo, 0, password);

		processChange(request, true);

		checkEcho(identity, system, EchoCheck.VALIDATE_AND_CHANGE);
		checkEcho(identity, systemTwo, EchoCheck.VALIDATE_AND_CHANGE);
		checkProcessedPasswordOperation(identity, system, 0, password);
		checkProcessedPasswordOperation(identity, systemTwo, 1, password);

		// Wait 3 seconds
		Thread.sleep(3000);

		// Just cahnge again
		processChange(request, true);

		checkEcho(identity, system, EchoCheck.VALIDATE_AND_CHANGE);
		checkEcho(identity, systemTwo, EchoCheck.VALIDATE_AND_CHANGE);
		checkProcessedPasswordOperation(identity, system, 0, password);
		checkProcessedPasswordOperation(identity, systemTwo, 2, password);
	}

	@Test
	public void testChangeInternal() {
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString)null);
		PasswordChangeDto passwordChangeDto = new PasswordChangeDto();
		passwordChangeDto.setAll(true);
		passwordChangeDto.setIdm(true);
		String password = getHelper().createName();
		passwordChangeDto.setNewPassword(new GuardedString(password));
		loginToIdm(identity, password, false);
		List<OperationResult> passwordChange = identityService.passwordChange(identity, passwordChangeDto);
		assertEquals(1, passwordChange.size());
		checkChangeInIdm(identity, 1);
		loginToIdm(identity, password, true);
	}

	@Test
	public void testChangeInternalWithOneSystem() {
		SysSystemDto system = createSystem(true);

		IdmIdentityDto identity = createIdentity(system);
		cleanProvivisioning(identity, system);

		String password = getHelper().createName();
		
		PasswordChangeDto passwordChangeDto = new PasswordChangeDto();
		passwordChangeDto.setAll(true);
		passwordChangeDto.setIdm(true);
		passwordChangeDto.setNewPassword(new GuardedString(password));

		List<OperationResult> passwordChange = identityService.passwordChange(identity, passwordChangeDto);
		assertEquals(2, passwordChange.size());

		loginToIdm(identity, password, true);
		checkEcho(identity, system, EchoCheck.VALIDATE_AND_CHANGE);
		checkPassword(prepareUid(identity, system), password, true);
	}

	@Test
	public void testChangeInternalWithTwoSystem() {
		SysSystemDto system = createSystem(false);
		SysSystemDto systemTwo = createSystem(false);

		IdmIdentityDto identity = createIdentity(system);
		assignSystem(identity, systemTwo);
		cleanProvivisioning(identity, system, systemTwo);

		setPasswordFilter(system, true);
		setPasswordFilter(systemTwo, true);

		checkEcho(identity, system, EchoCheck.DOESNT_EXIST);
		checkEcho(identity, systemTwo, EchoCheck.DOESNT_EXIST);

		String password = getHelper().createName();
		
		PasswordChangeDto passwordChangeDto = new PasswordChangeDto();
		passwordChangeDto.setIdm(true);
		passwordChangeDto.setAll(false);
		passwordChangeDto.setAccounts(Lists.newArrayList(getAccount(identity, system).getId().toString()));
		passwordChangeDto.setNewPassword(new GuardedString(password));

		List<OperationResult> passwordChange = identityService.passwordChange(identity, passwordChangeDto);
		assertEquals(2, passwordChange.size());

		loginToIdm(identity, password, true);
		checkEcho(identity, system, EchoCheck.VALIDATE_AND_CHANGE);
		checkEcho(identity, systemTwo, EchoCheck.DOESNT_EXIST);
		checkPassword(prepareUid(identity, system), password, true);
		checkPassword(prepareUid(identity, systemTwo), password, false);
	}

	@Test
	public void testChangeInternalWithTwoSystemUniformPassword() {
		SysSystemDto system = createSystem(true);
		SysSystemDto systemTwo = createSystem(true);

		IdmIdentityDto identity = createIdentity(system);
		assignSystem(identity, systemTwo);
		cleanProvivisioning(identity, system, systemTwo);
		
		assignSystem(createUniformDefinition(true), system, systemTwo);

		String password = getHelper().createName();
		
		PasswordChangeDto passwordChangeDto = new PasswordChangeDto();
		passwordChangeDto.setIdm(false);
		passwordChangeDto.setAll(false);
		passwordChangeDto.setAccounts(Lists.newArrayList(getAccount(identity, system).getId().toString()));
		passwordChangeDto.setNewPassword(new GuardedString(password));

		List<OperationResult> passwordChange = identityService.passwordChange(identity, passwordChangeDto);
		assertEquals(3, passwordChange.size());

		loginToIdm(identity, password, true);
		checkEcho(identity, system, EchoCheck.VALIDATE_AND_CHANGE);
		checkEcho(identity, systemTwo, EchoCheck.VALIDATE_AND_CHANGE);
		checkPassword(prepareUid(identity, system), password, true);
		checkPassword(prepareUid(identity, systemTwo), password, true);
	}

	@Test
	public void testCreateIdentityInSystem() {
		SysSystemDto system = createSystem(true);
		IdmPasswordPolicyDto passwordPolicy = new IdmPasswordPolicyDto();
		passwordPolicy.setMinPasswordLength(2);
		passwordPolicy.setMinLowerChar(2);
		passwordPolicy.setName(getHelper().createName());
		passwordPolicy.setGenerateType(IdmPasswordPolicyGenerateType.RANDOM);
		passwordPolicy.setType(IdmPasswordPolicyType.GENERATE);
		passwordPolicy.setLowerCharBase("a");
		passwordPolicy = passwordPolicyService.save(passwordPolicy);
		
		system.setPasswordPolicyGenerate(passwordPolicy.getId());
		system = systemService.save(system);

		IdmIdentityDto identity = createIdentity(system);

		checkEcho(identity, system, EchoCheck.CREATE);
		checkPassword(prepareUid(identity, system), "aa", true);

		PasswordRequest request = prepareRequest(prepareUid(identity, system), system.getCode(), "aa");
		processValidate(request, true);
		checkEcho(identity, system, EchoCheck.CREATE);
		
		processChange(request, true);
		checkEcho(identity, system, EchoCheck.CREATE);
	}

	@Test
	public void testTwoValidation() {
		SysSystemDto system = createSystem(false);
		IdmIdentityDto identity = createIdentity(system);
		setPasswordFilter(system, true);
		String password = getHelper().createName();
		PasswordRequest request = prepareRequest(identity.getUsername(), system.getCode(), password);
		
		cleanProvivisioning(identity, system);

		checkEcho(identity, system, EchoCheck.DOESNT_EXIST);
		checkProcessedPasswordOperation(identity, system, 0, null);

		processValidate(request, true);

		checkEcho(identity, system, EchoCheck.VALIDATE);
		checkPassword(prepareUid(identity, system), password, false);
		checkProcessedPasswordOperation(identity, system, 0, null);

		processValidate(request, true);

		checkEcho(identity, system, EchoCheck.VALIDATE);
		checkPassword(prepareUid(identity, system), password, false);
	}

	@Test
	public void testEmptyResource() {
		SysSystemDto system = createSystem(false);
		IdmIdentityDto identity = createIdentity(system);
		setPasswordFilter(system, true);
		String password = getHelper().createName();
		PasswordRequest request = prepareRequest(identity.getUsername(), "", password);
		checkEcho(identity, system, EchoCheck.DOESNT_EXIST);

		IdmResponse processValidate = processValidate(request, false);
		assertEquals(CoreResultCode.BAD_REQUEST.getStatus(), processValidate.status);
		assertTrue(String.valueOf(processValidate.message).contains(CoreResultCode.BAD_REQUEST.getCode()));
		checkEcho(identity, system, EchoCheck.DOESNT_EXIST);
	}

	@Test
	public void testNullResource() {
		SysSystemDto system = createSystem(false);
		IdmIdentityDto identity = createIdentity(system);
		setPasswordFilter(system, true);
		String password = getHelper().createName();
		PasswordRequest request = prepareRequest(identity.getUsername(), null, password);
		checkEcho(identity, system, EchoCheck.DOESNT_EXIST);

		IdmResponse processValidate = processValidate(request, false);
		assertEquals(CoreResultCode.BAD_REQUEST.getStatus(), processValidate.status);
		assertTrue(String.valueOf(processValidate.message).contains(CoreResultCode.BAD_REQUEST.getCode()));
		checkEcho(identity, system, EchoCheck.DOESNT_EXIST);
	}

	@Test
	public void testIdResource() {
		SysSystemDto system = createSystem(false);
		IdmIdentityDto identity = createIdentity(system);
		setPasswordFilter(system, true);
		String password = getHelper().createName();
		PasswordRequest request = prepareRequest(identity.getUsername(), system.getId().toString(), password);
		checkEcho(identity, system, EchoCheck.DOESNT_EXIST);

		processValidate(request, true);
		checkEcho(identity, system, EchoCheck.VALIDATE);
	}

	@Test
	public void testEmptyUsername() {
		SysSystemDto system = createSystem(false);
		IdmIdentityDto identity = createIdentity(system);
		setPasswordFilter(system, true);
		String password = getHelper().createName();
		PasswordRequest request = prepareRequest("", system.getCode(), password);
		checkEcho(identity, system, EchoCheck.DOESNT_EXIST);

		IdmResponse processValidate = processValidate(request, false);
		assertEquals(CoreResultCode.BAD_REQUEST.getStatus(), processValidate.status);
		assertTrue(String.valueOf(processValidate.message).contains(CoreResultCode.BAD_REQUEST.getCode()));
		checkEcho(identity, system, EchoCheck.DOESNT_EXIST);
	}

	@Test
	public void testNullUsername() {
		SysSystemDto system = createSystem(false);
		IdmIdentityDto identity = createIdentity(system);
		setPasswordFilter(system, true);
		String password = getHelper().createName();
		PasswordRequest request = prepareRequest(null, system.getCode(), password);
		checkEcho(identity, system, EchoCheck.DOESNT_EXIST);

		IdmResponse processValidate = processValidate(request, false);
		assertEquals(CoreResultCode.BAD_REQUEST.getStatus(), processValidate.status);
		assertTrue(String.valueOf(processValidate.message).contains(CoreResultCode.BAD_REQUEST.getCode()));
		checkEcho(identity, system, EchoCheck.DOESNT_EXIST);
	}

	@Test
	public void testUTF8CharactersPassword() {
		SysSystemDto system = createSystem(false);
		SysSystemDto systemTwo = createSystem(false);
		IdmIdentityDto identity = createIdentity(system);
		String password = "+ěščřžýáíé=)ů§.-ǒǐďǚǎǧȟǰǩľě😊⌛";
		PasswordRequest request = prepareRequest(identity.getUsername(), system.getCode(), password);

		AccUniformPasswordDto uniformDefinition = createUniformDefinition(true);
		assignSystem(uniformDefinition, system, systemTwo);
		assignSystem(identity, systemTwo);

		setPasswordFilter(system, true);
		setPasswordFilter(systemTwo, true);

		checkEcho(identity, system, EchoCheck.DOESNT_EXIST);
		checkEcho(identity, systemTwo, EchoCheck.DOESNT_EXIST);

		processValidate(request, true);

		checkEcho(identity, system, EchoCheck.VALIDATE);
		checkEcho(identity, systemTwo, EchoCheck.VALIDATE);
		checkPassword(prepareUid(identity, system), password, false);

		processChange(request, true);

		checkEcho(identity, system, EchoCheck.VALIDATE_AND_CHANGE);
		checkEcho(identity, systemTwo, EchoCheck.VALIDATE_AND_CHANGE);
		checkPassword(prepareUid(identity, system), password, false); // Password will not be same!
		checkPassword(prepareUid(identity, systemTwo), password, true);
	}

	@Test
	public void testUTF8CharactersUsername() {
		SysSystemDto system = createSystem(false);
		SysSystemDto systemTwo = createSystem(false);
		IdmIdentityDto identity = createIdentity(system);
		identity.setUsername("+ěščřžýáíé=)ů§.-ǒǐďǚǎǧȟǰǩľě😊⌛");
		identity = identityService.save(identity);
		String password = getHelper().createName();
		PasswordRequest request = prepareRequest(identity.getUsername(), system.getCode(), password);

		AccUniformPasswordDto uniformDefinition = createUniformDefinition(true);
		assignSystem(uniformDefinition, system, systemTwo);
		assignSystem(identity, systemTwo);

		setPasswordFilter(system, true);
		setPasswordFilter(systemTwo, true);

		checkEcho(identity, system, EchoCheck.DOESNT_EXIST);
		checkEcho(identity, systemTwo, EchoCheck.DOESNT_EXIST);

		processValidate(request, true);

		checkEcho(identity, system, EchoCheck.VALIDATE);
		checkEcho(identity, systemTwo, EchoCheck.VALIDATE);
		checkPassword(prepareUid(identity, system), password, false);

		processChange(request, true);

		checkEcho(identity, system, EchoCheck.VALIDATE_AND_CHANGE);
		checkEcho(identity, systemTwo, EchoCheck.VALIDATE_AND_CHANGE);
		checkPassword(prepareUid(identity, system), password, false); // Password will not be same!
		checkPassword(prepareUid(identity, systemTwo), password, true);
	}

	@Test
	public void testCheckEchoCreateOperation() {
		SysSystemDto system = createSystem(true);
		IdmIdentityDto identity = createIdentity(system);

		checkEcho(identity, system, EchoCheck.CREATE);
		
		String passwordOnTargetSystem = getPasswordOnTargetSystem(prepareUid(identity, system));
		assertNotNull(passwordOnTargetSystem);
	}

	@Test
	public void testTwoStandaloneSystem() {
		SysSystemDto systemOne = createSystem(false);
		IdmIdentityDto identity = createIdentity(systemOne);
		setPasswordFilter(systemOne, true);
		
		// Second system
		SysSystemDto systemTwo = createSystem(false);
		assignSystem(identity, systemTwo);
		setPasswordFilter(systemTwo, true);
	
		String password = getHelper().createName();
		PasswordRequest request = prepareRequest(identity.getUsername(), systemOne.getCode(), password);

		checkEcho(identity, systemOne, EchoCheck.DOESNT_EXIST);
		checkEcho(identity, systemTwo, EchoCheck.DOESNT_EXIST);

		processValidate(request, true);

		checkEcho(identity, systemOne, EchoCheck.VALIDATE);
		checkEcho(identity, systemTwo, EchoCheck.DOESNT_EXIST);
		checkPassword(prepareUid(identity, systemOne), password, false);

		processChange(request, true);

		checkEcho(identity, systemOne, EchoCheck.VALIDATE_AND_CHANGE);
		checkEcho(identity, systemTwo, EchoCheck.DOESNT_EXIST);
		checkPassword(prepareUid(identity, systemOne), password, false); // Password will not be same!
	}

	@Test
	public void testTwoSystemWithUniform() {
		SysSystemDto systemOne = createSystem(false);
		IdmIdentityDto identity = createIdentity(systemOne);
		setPasswordFilter(systemOne, true);

		// Second system
		SysSystemDto systemTwo = createSystem(false);
		assignSystem(identity, systemTwo);
		setPasswordFilter(systemTwo, true);

		assignSystem(createUniformDefinition(false), systemOne, systemTwo);

		String password = getHelper().createName();
		PasswordRequest request = prepareRequest(identity.getUsername(), systemOne.getCode(), password);

		checkEcho(identity, systemOne, EchoCheck.DOESNT_EXIST);
		checkEcho(identity, systemTwo, EchoCheck.DOESNT_EXIST);

		processValidate(request, true);

		checkEcho(identity, systemOne, EchoCheck.VALIDATE);
		checkEcho(identity, systemTwo, EchoCheck.VALIDATE); // System is validate with the first one
		checkPassword(prepareUid(identity, systemOne), password, false);

		processChange(request, true);

		checkEcho(identity, systemOne, EchoCheck.VALIDATE_AND_CHANGE);
		checkEcho(identity, systemTwo, EchoCheck.VALIDATE_AND_CHANGE); // Processed by first system in uniform definition
		checkPassword(prepareUid(identity, systemOne), password, false);
		checkPassword(prepareUid(identity, systemTwo), password, true); // There must be changed
	}

	@Test
	public void testMoreSystemsUniformPasswordSystem() {
		SysSystemDto systemOne = createSystem(false);
		IdmIdentityDto identity = createIdentity(systemOne);
		setPasswordFilter(systemOne, true);

		List<SysSystemDto> systems = Lists.newArrayList(systemOne);
		AccUniformPasswordDto uniformPasswordDto = createUniformDefinition(false);
		assignSystem(uniformPasswordDto, systemOne);
		for (int index = 0; index < 11; index++) {
			SysSystemDto system = createSystem(false);
			assignSystem(identity, system);
			setPasswordFilter(system, true);
			assignSystem(uniformPasswordDto, system);
			
			systems.add(system);
		}
		

		String password = getHelper().createName();
		PasswordRequest request = prepareRequest(identity.getUsername(), systemOne.getCode(), password);

		systems.forEach(s -> {
			checkEcho(identity, s, EchoCheck.DOESNT_EXIST);
		});

		processValidate(request, true);

		systems.forEach(s -> {
			checkEcho(identity, s, EchoCheck.VALIDATE);
			checkPassword(prepareUid(identity, systemOne), password, false);
		});

		processChange(request, true);

		systems.forEach(s -> {
			checkEcho(identity, s, EchoCheck.VALIDATE_AND_CHANGE);
			if (!s.getId().equals(systemOne.getId())) {
				checkPassword(prepareUid(identity, systemOne), password, false);
			} else {
				checkPassword(prepareUid(identity, s), password, false);
			}
		});
	}

	@Test
	public void testSystemsWithAndWithoutUniform() {
		SysSystemDto systemOne = createSystem(false);
		IdmIdentityDto identity = createIdentity(systemOne);
		setPasswordFilter(systemOne, true);

		// Second system
		SysSystemDto systemTwo = createSystem(false);
		assignSystem(identity, systemTwo);
		setPasswordFilter(systemTwo, true);

		assignSystem(createUniformDefinition(false), systemOne, systemTwo);

		String password = getHelper().createName();
		PasswordRequest request = prepareRequest(identity.getUsername(), systemOne.getCode(), password);

		checkEcho(identity, systemOne, EchoCheck.DOESNT_EXIST);
		checkEcho(identity, systemTwo, EchoCheck.DOESNT_EXIST);

		processValidate(request, true);

		checkEcho(identity, systemOne, EchoCheck.VALIDATE);
		checkEcho(identity, systemTwo, EchoCheck.VALIDATE); // System is validate with the first one
		checkPassword(prepareUid(identity, systemOne), password, false);

		processChange(request, true);

		checkEcho(identity, systemOne, EchoCheck.VALIDATE_AND_CHANGE);
		checkEcho(identity, systemTwo, EchoCheck.VALIDATE_AND_CHANGE); // Processed by first system in uniform definition
		checkPassword(prepareUid(identity, systemOne), password, false);
		checkPassword(prepareUid(identity, systemTwo), password, true); // There must be changed
	}
	
	@Test
	public void testTwoSystemWithUniformCheckProvisioning() {
		SysSystemDto systemOne = createSystem(false);
		IdmIdentityDto identity = createIdentity(systemOne);
		setPasswordFilter(systemOne, true);

		// Second system
		SysSystemDto systemTwo = createSystem(false);
		assignSystem(identity, systemTwo);
		setPasswordFilter(systemTwo, true);

		assignSystem(createUniformDefinition(false), systemOne, systemTwo);
		
		cleanProvivisioning(identity, systemOne, systemTwo);

		String password = getHelper().createName();
		PasswordRequest request = prepareRequest(identity.getUsername(), systemOne.getCode(), password);

		checkEmptyProvisioning(identity, systemOne, systemTwo);
		
		processValidate(request, true);

		checkEmptyProvisioning(identity, systemOne, systemTwo);

		processChange(request, true);

		checkEmptyProvisioning(identity, systemOne);
		checkProcessedPasswordOperation(identity, systemTwo, 1, password);
	}

	@Test
	public void testMoreSystemDifferentUniformPassword() {
		SysSystemDto systemOne = createSystem(false);
		IdmIdentityDto identity = createIdentity(systemOne);
		setPasswordFilter(systemOne, true);

		// Second system
		SysSystemDto systemTwo = createSystem(false);
		assignSystem(identity, systemTwo);
		setPasswordFilter(systemTwo, true);

		SysSystemDto systemThree = createSystem(false);
		assignSystem(identity, systemThree);
		setPasswordFilter(systemThree, true);
		
		assignSystem(createUniformDefinition(false), systemOne);
		assignSystem(createUniformDefinition(false), systemTwo);
		assignSystem(createUniformDefinition(false), systemThree);
		
		cleanProvivisioning(identity, systemOne, systemTwo, systemThree);

		String password = getHelper().createName();
		PasswordRequest request = prepareRequest(identity.getUsername(), systemOne.getCode(), password);

		checkEmptyProvisioning(identity, systemOne, systemTwo, systemThree);
		checkEcho(identity, systemOne, EchoCheck.DOESNT_EXIST);
		checkEcho(identity, systemTwo, EchoCheck.DOESNT_EXIST);
		checkEcho(identity, systemThree, EchoCheck.DOESNT_EXIST);

		processValidate(request, true);
		
		checkEcho(identity, systemOne, EchoCheck.VALIDATE);
		checkEcho(identity, systemTwo, EchoCheck.DOESNT_EXIST);
		checkEcho(identity, systemThree, EchoCheck.DOESNT_EXIST);

		checkEmptyProvisioning(identity, systemOne, systemTwo, systemThree);

		processChange(request, true);
		
		checkEcho(identity, systemOne, EchoCheck.VALIDATE_AND_CHANGE);
		checkEcho(identity, systemTwo, EchoCheck.DOESNT_EXIST);
		checkEcho(identity, systemThree, EchoCheck.DOESNT_EXIST);

		checkEmptyProvisioning(identity, systemOne, systemTwo, systemThree);
	}

	@Test
	public void testIdmAndUniformPassword() {
		SysSystemDto system = createSystem(false);
		IdmIdentityDto identity = createIdentity(system);
		setPasswordFilter(system, true);

		assignSystem(createUniformDefinition(true), system);

		cleanProvivisioning(identity, system);
		checkChangeInIdm(identity, 0);

		String password = getHelper().createName();
		PasswordRequest request = prepareRequest(identity.getUsername(), system.getCode(), password);

		loginToIdm(identity, password, false);
		checkEmptyProvisioning(identity, system);
		checkEcho(identity, system, EchoCheck.DOESNT_EXIST);
		checkChangeInIdm(identity, 0);

		processValidate(request, true);

		loginToIdm(identity, password, false);
		checkEcho(identity, system, EchoCheck.VALIDATE);
		checkChangeInIdm(identity, 0);
		checkEmptyProvisioning(identity, system);

		processChange(request, true);
		
		checkEcho(identity, system, EchoCheck.VALIDATE_AND_CHANGE);
		checkChangeInIdm(identity, 1);
		checkEmptyProvisioning(identity, system);
		loginToIdm(identity, password, true);
	}

	@Test
	public void testWithoutIdmAndUniformPassword() {
		SysSystemDto system = createSystem(false);
		IdmIdentityDto identity = createIdentity(system);
		setPasswordFilter(system, true);

		assignSystem(createUniformDefinition(true), system);

		cleanProvivisioning(identity, system);
		checkChangeInIdm(identity, 0);

		String password = getHelper().createName();
		PasswordRequest request = prepareRequest(identity.getUsername(), system.getCode(), password);

		loginToIdm(identity, password, false);
		checkEmptyProvisioning(identity, system);
		checkEcho(identity, system, EchoCheck.DOESNT_EXIST);
		checkChangeInIdm(identity, 0);

		processValidate(request, true);

		loginToIdm(identity, password, false);
		checkEcho(identity, system, EchoCheck.VALIDATE);
		checkChangeInIdm(identity, 0);
		checkEmptyProvisioning(identity, system);

		processChange(request, true);
		
		checkEcho(identity, system, EchoCheck.VALIDATE_AND_CHANGE);
		checkChangeInIdm(identity, 1);
		checkEmptyProvisioning(identity, system);
		loginToIdm(identity, password, true);
	}

	@Test
	public void testMoreSystemWithoutPasswordFilter() {
		SysSystemDto systemOne = createSystem(false);
		IdmIdentityDto identity = createIdentity(systemOne);
		setPasswordFilter(systemOne, true);

		// Second system
		SysSystemDto systemTwo = createSystem(false);
		assignSystem(identity, systemTwo);

		SysSystemDto systemThree = createSystem(false);
		assignSystem(identity, systemThree);

		assignSystem(createUniformDefinition(false), systemOne, systemTwo, systemThree);

		cleanProvivisioning(identity, systemOne, systemTwo, systemThree);

		String password = getHelper().createName();
		PasswordRequest request = prepareRequest(identity.getUsername(), systemOne.getCode(), password);

		checkEmptyProvisioning(identity, systemOne, systemTwo, systemThree);
		checkEcho(identity, systemOne, EchoCheck.DOESNT_EXIST);
		checkEcho(identity, systemTwo, EchoCheck.DOESNT_EXIST);
		checkEcho(identity, systemThree, EchoCheck.DOESNT_EXIST);

		processValidate(request, true);

		checkEcho(identity, systemOne, EchoCheck.VALIDATE);
		checkEcho(identity, systemTwo, EchoCheck.DOESNT_EXIST);
		checkEcho(identity, systemThree, EchoCheck.DOESNT_EXIST);

		checkEmptyProvisioning(identity, systemOne, systemTwo, systemThree);

		processChange(request, true);

		checkEcho(identity, systemOne, EchoCheck.VALIDATE_AND_CHANGE);
		checkEcho(identity, systemTwo, EchoCheck.DOESNT_EXIST);
		checkEcho(identity, systemThree, EchoCheck.DOESNT_EXIST);

		checkEmptyProvisioning(identity, systemOne);
		checkProcessedPasswordOperation(identity, systemTwo, 1, password);
		checkProcessedPasswordOperation(identity, systemThree, 1, password);
	}

	@Test
	public void testMoreSystemWithoutAndWithPasswordFilter() {
		SysSystemDto systemOne = createSystem(false);
		IdmIdentityDto identity = createIdentity(systemOne);
		setPasswordFilter(systemOne, true);

		SysSystemDto systemTwo = createSystem(false);
		assignSystem(identity, systemTwo);

		SysSystemDto systemThree = createSystem(false);
		assignSystem(identity, systemThree);
		
		SysSystemDto systemFour = createSystem(false);
		assignSystem(identity, systemFour);
		setPasswordFilter(systemFour, true);

		assignSystem(createUniformDefinition(false), systemOne, systemTwo, systemThree, systemFour);

		cleanProvivisioning(identity, systemOne, systemTwo, systemThree, systemFour);

		String password = getHelper().createName();
		PasswordRequest request = prepareRequest(identity.getUsername(), systemOne.getCode(), password);

		checkEmptyProvisioning(identity, systemOne, systemTwo, systemThree, systemFour);
		checkEcho(identity, systemOne, EchoCheck.DOESNT_EXIST);
		checkEcho(identity, systemTwo, EchoCheck.DOESNT_EXIST);
		checkEcho(identity, systemThree, EchoCheck.DOESNT_EXIST);
		checkEcho(identity, systemFour, EchoCheck.DOESNT_EXIST);

		processValidate(request, true);

		checkEcho(identity, systemOne, EchoCheck.VALIDATE);
		checkEcho(identity, systemTwo, EchoCheck.DOESNT_EXIST);
		checkEcho(identity, systemThree, EchoCheck.DOESNT_EXIST);
		checkEcho(identity, systemFour, EchoCheck.VALIDATE);

		checkEmptyProvisioning(identity, systemOne, systemTwo, systemThree, systemFour);

		processChange(request, true);

		checkEcho(identity, systemOne, EchoCheck.VALIDATE_AND_CHANGE);
		checkEcho(identity, systemTwo, EchoCheck.DOESNT_EXIST);
		checkEcho(identity, systemThree, EchoCheck.DOESNT_EXIST);
		checkEcho(identity, systemFour, EchoCheck.VALIDATE_AND_CHANGE);

		checkEmptyProvisioning(identity, systemOne);
		checkProcessedPasswordOperation(identity, systemTwo, 1, password);
		checkProcessedPasswordOperation(identity, systemThree, 1, password);
		checkProcessedPasswordOperation(identity, systemFour, 1, password);
		checkPassword(prepareUid(identity, systemOne), password, false);
		checkPassword(prepareUid(identity, systemTwo), password, true);
		checkPassword(prepareUid(identity, systemThree), password, true);
		checkPassword(prepareUid(identity, systemFour), password, true);

		// Change from four - same password
		request = prepareRequest(identity.getUsername(), systemFour.getCode(), password);

		processValidate(request, true);
		
		checkEcho(identity, systemOne, EchoCheck.VALIDATE_AND_CHANGE);
		checkEcho(identity, systemTwo, EchoCheck.DOESNT_EXIST);
		checkEcho(identity, systemThree, EchoCheck.DOESNT_EXIST);
		checkEcho(identity, systemFour, EchoCheck.VALIDATE_AND_CHANGE);

		processChange(request, true);

		checkEcho(identity, systemOne, EchoCheck.VALIDATE_AND_CHANGE);
		checkEcho(identity, systemTwo, EchoCheck.DOESNT_EXIST);
		checkEcho(identity, systemThree, EchoCheck.DOESNT_EXIST);
		checkEcho(identity, systemFour, EchoCheck.VALIDATE_AND_CHANGE);

		checkEmptyProvisioning(identity, systemOne);
		checkProcessedPasswordOperation(identity, systemTwo, 1, password);
		checkProcessedPasswordOperation(identity, systemThree, 1, password);
		checkProcessedPasswordOperation(identity, systemFour, 1, password);

		checkPassword(prepareUid(identity, systemOne), password, false);
		checkPassword(prepareUid(identity, systemTwo), password, true);
		checkPassword(prepareUid(identity, systemThree), password, true);
		checkPassword(prepareUid(identity, systemFour), password, true);
	}

	@Test
	public void testCallChangeWithoutValidate() {
		SysSystemDto system = createSystem(false);
		IdmIdentityDto identity = createIdentity(system);
		setPasswordFilter(system, true);
		String password = getHelper().createName();
		PasswordRequest request = prepareRequest(identity.getUsername(), system.getCode(), password);

		checkEcho(identity, system, EchoCheck.DOESNT_EXIST);

		checkPassword(prepareUid(identity, system), password, false);

		IdmResponse processChange = processChange(request, false);
		assertEquals(AccResultCode.PASSWORD_FILTER_NOT_VALID_CHANGE_REQUEST.getStatus(), processChange.status);
		assertTrue(String.valueOf(processChange.message).contains(AccResultCode.PASSWORD_FILTER_NOT_VALID_CHANGE_REQUEST.getCode()));

		checkEcho(identity, system, EchoCheck.DOESNT_EXIST);
	}

	@Test
	public void testValidWithoutValidSystem() {
		IdmIdentityDto identity = createIdentity(null);
		String createName = getHelper().createName();
		String password = getHelper().createName();
		PasswordRequest request = prepareRequest(identity.getUsername(), createName, password);

		IdmResponse processChange = processChange(request, false);
		assertEquals(AccResultCode.PASSWORD_FILTER_SYSTEM_NOT_FOUND.getStatus(), processChange.status);
		assertTrue(String.valueOf(processChange.message).contains(AccResultCode.PASSWORD_FILTER_SYSTEM_NOT_FOUND.getCode()));
	}

	@Test
	public void testCallChangeWithoutPasswordFilter() {
		SysSystemDto system = createSystem(false);
		IdmIdentityDto identity = createIdentity(system);
		String password = getHelper().createName();
		SysSystemAttributeMappingDto passwordAttribute = setPasswordFilter(system, true);
		systemAttributeMappingService.delete(passwordAttribute);
		assignSystem(identity, system);
		PasswordRequest request = prepareRequest(identity.getUsername(), system.getCode(), password);

		checkEcho(identity, system, EchoCheck.DOESNT_EXIST);

		checkPassword(prepareUid(identity, system), password, false);

		IdmResponse processChange = processChange(request, false);
		assertEquals(AccResultCode.PASSWORD_FILTER_DEFINITION_NOT_FOUND.getStatus(), processChange.status);
		assertTrue(String.valueOf(processChange.message).contains(AccResultCode.PASSWORD_FILTER_DEFINITION_NOT_FOUND.getCode()));

		checkEcho(identity, system, EchoCheck.DOESNT_EXIST);
	}

	@Test
	public void testCallChangeWithoutActivePasswordFilter() {
		SysSystemDto system = createSystem(false);
		IdmIdentityDto identity = createIdentity(system);
		String password = getHelper().createName();
		setPasswordFilter(system, false);
		assignSystem(identity, system);
		PasswordRequest request = prepareRequest(identity.getUsername(), system.getCode(), password);

		checkEcho(identity, system, EchoCheck.DOESNT_EXIST);

		checkPassword(prepareUid(identity, system), password, false);

		IdmResponse processChange = processChange(request, false);
		assertEquals(AccResultCode.PASSWORD_FILTER_DEFINITION_NOT_FOUND.getStatus(), processChange.status);
		assertTrue(String.valueOf(processChange.message).contains(AccResultCode.PASSWORD_FILTER_DEFINITION_NOT_FOUND.getCode()));

		checkEcho(identity, system, EchoCheck.DOESNT_EXIST);
	}

	@Test
	public void testFindIdentityByUsername() {
		SysSystemDto system = createSystem(false);
		IdmIdentityDto identity = createIdentity(system);
		setPasswordFilter(system, true);
		assignSystem(identity, system);

		String password = getHelper().createName();
		PasswordRequest request = prepareRequest(identity.getUsername(), system.getCode(), password);

		checkEcho(identity, system, EchoCheck.DOESNT_EXIST);
		processValidate(request, true);

		checkEcho(identity, system, EchoCheck.VALIDATE);
		processChange(request, true);
		checkEcho(identity, system, EchoCheck.VALIDATE_AND_CHANGE);
		checkPassword(prepareUid(identity, system), password, false);
	}

	@Test
	public void testFindIdentityByUid() {
		SysSystemDto system = createSystem(false);
		IdmIdentityDto identity = createIdentity(system);
		setPasswordFilter(system, true);
		assignSystem(identity, system);

		String password = getHelper().createName();
		PasswordRequest request = prepareRequest(prepareUid(identity, system), system.getCode(), password);

		checkEcho(identity, system, EchoCheck.DOESNT_EXIST);
		processValidate(request, true);

		checkEcho(identity, system, EchoCheck.VALIDATE);
		processChange(request, true);
		checkEcho(identity, system, EchoCheck.VALIDATE_AND_CHANGE);
		checkPassword(prepareUid(identity, system), password, false);
	}

	@Test
	public void testFindIdentityByTransformationScriptFirstNameAndLastName() {
		SysSystemDto system = createSystem(false);
		IdmIdentityDto identity = createIdentity(system);
		SysSystemAttributeMappingDto passwodAttribute = setPasswordFilter(system, true);
		assertNotNull(getAccount(identity, system));
		
		IdmScriptDto script = getHelper().createScript(getHelper().createName(), IdmScriptCategory.TRANSFORM_FROM,
				"import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;",
				"import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityFilter;",
				"",
				"IdmIdentityFilter filter = new IdmIdentityFilter();",
				"",
				"filter.setFirstName('" + identity.getFirstName() + "');",
				"filter.setLastName('" + identity.getLastName() + "');",
				"",
				"List<IdmIdentityDto> identities = identityService.find(filter, null).getContent();",
				"return identities.get(0);");

		getHelper().createScriptAuthority(script.getId(), ScriptAuthorityType.SERVICE, null, "identityService");
		getHelper().createScriptAuthority(script.getId(), ScriptAuthorityType.CLASS_NAME, IdmIdentityDto.class.getCanonicalName(), null);
		getHelper().createScriptAuthority(script.getId(), ScriptAuthorityType.CLASS_NAME, IdmIdentityFilter.class.getCanonicalName(), null);
		getHelper().createScriptAuthority(script.getId(), ScriptAuthorityType.CLASS_NAME, PageImpl.class.getCanonicalName(), null);
		getHelper().createScriptAuthority(script.getId(), ScriptAuthorityType.CLASS_NAME, "java.util.Collections$UnmodifiableRandomAccessList", null);

		passwodAttribute.setTransformationUidScript(prepareScriptExecutor(script.getCode()));
		systemAttributeMappingService.save(passwodAttribute);

		assignSystem(identity, system);

		String password = getHelper().createName();
		PasswordRequest request = prepareRequest(prepareUid(identity, system), system.getCode(), password);

		checkEcho(identity, system, EchoCheck.DOESNT_EXIST);
		processValidate(request, true);

		checkEcho(identity, system, EchoCheck.VALIDATE);
		processChange(request, true);
		checkEcho(identity, system, EchoCheck.VALIDATE_AND_CHANGE);
		checkPassword(prepareUid(identity, system), password, false);
	}

	@Test
	public void testFindIdentityByTransformationScriptNotFound() {
		SysSystemDto system = createSystem(false);
		IdmIdentityDto identity = createIdentity(system);
		SysSystemAttributeMappingDto passwodAttribute = setPasswordFilter(system, true);
		assertNotNull(getAccount(identity, system));
		
		IdmScriptDto script = getHelper().createScript(getHelper().createName(), IdmScriptCategory.TRANSFORM_FROM,
				"return null;");

		getHelper().createScriptAuthority(script.getId(), ScriptAuthorityType.SERVICE, null, "identityService");
		getHelper().createScriptAuthority(script.getId(), ScriptAuthorityType.CLASS_NAME, IdmIdentityDto.class.getCanonicalName(), null);
		getHelper().createScriptAuthority(script.getId(), ScriptAuthorityType.CLASS_NAME, IdmIdentityFilter.class.getCanonicalName(), null);
		getHelper().createScriptAuthority(script.getId(), ScriptAuthorityType.CLASS_NAME, PageImpl.class.getCanonicalName(), null);
		getHelper().createScriptAuthority(script.getId(), ScriptAuthorityType.CLASS_NAME, "java.util.Collections$UnmodifiableRandomAccessList", null);

		passwodAttribute.setTransformationUidScript(prepareScriptExecutor(script.getCode()));
		systemAttributeMappingService.save(passwodAttribute);

		assignSystem(identity, system);

		String password = getHelper().createName();
		PasswordRequest request = prepareRequest(prepareUid(identity, system), system.getCode(), password);

		checkEcho(identity, system, EchoCheck.DOESNT_EXIST);
		IdmResponse processValidate = processValidate(request, false);
		assertEquals(AccResultCode.PASSWORD_FILTER_IDENTITY_NOT_FOUND.getStatus(), processValidate.status);
		assertTrue(String.valueOf(processValidate.message).contains(AccResultCode.PASSWORD_FILTER_IDENTITY_NOT_FOUND.getCode()));
		checkEcho(identity, system, EchoCheck.DOESNT_EXIST);
	}

	@Test
	public void testFindIdentityByTransformationScriptIdentityWithoutAccount() {
		SysSystemDto system = createSystem(false);
		SysSystemDto systemTwo = createSystem(false);
		IdmIdentityDto identity = createIdentity(system);
		IdmIdentityDto identityTwo = createIdentity(systemTwo);
		SysSystemAttributeMappingDto passwodAttribute = setPasswordFilter(system, true);
		
		IdmScriptDto script = getHelper().createScript(getHelper().createName(), IdmScriptCategory.TRANSFORM_FROM,
				"import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;",
				"import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityFilter;",
				"",
				"IdmIdentityFilter filter = new IdmIdentityFilter();",
				"",
				"filter.setUsername('" + identityTwo.getUsername() + "');",
				"",
				"List<IdmIdentityDto> identities = identityService.find(filter, null).getContent();",
				"return identities.get(0);");

		getHelper().createScriptAuthority(script.getId(), ScriptAuthorityType.SERVICE, null, "identityService");
		getHelper().createScriptAuthority(script.getId(), ScriptAuthorityType.CLASS_NAME, IdmIdentityDto.class.getCanonicalName(), null);
		getHelper().createScriptAuthority(script.getId(), ScriptAuthorityType.CLASS_NAME, IdmIdentityFilter.class.getCanonicalName(), null);
		getHelper().createScriptAuthority(script.getId(), ScriptAuthorityType.CLASS_NAME, PageImpl.class.getCanonicalName(), null);
		getHelper().createScriptAuthority(script.getId(), ScriptAuthorityType.CLASS_NAME, "java.util.Collections$UnmodifiableRandomAccessList", null);

		passwodAttribute.setTransformationUidScript(prepareScriptExecutor(script.getCode()));
		systemAttributeMappingService.save(passwodAttribute);

		String password = getHelper().createName();
		PasswordRequest request = prepareRequest(prepareUid(identityTwo, system), system.getCode(), password);

		checkEcho(identity, system, EchoCheck.DOESNT_EXIST);
		checkEcho(identityTwo, system, EchoCheck.DOESNT_EXIST);

		// Validation for identity, that hasn't accounts on given system
		processValidate(request, true);

		checkEcho(identity, system, EchoCheck.DOESNT_EXIST);
		checkEcho(identityTwo, system, EchoCheck.DOESNT_EXIST);
	}

	@Test
	public void testFindIdentityByTransformationScriptDifferentIdentity() {
		SysSystemDto system = createSystem(false);
		IdmIdentityDto identity = createIdentity(system);
		IdmIdentityDto identityTwo = createIdentity(system);
		assertNotNull(getAccount(identity, system));
		assertNotNull(getAccount(identityTwo, system));

		SysSystemAttributeMappingDto passwodAttribute = setPasswordFilter(system, true);

		IdmScriptDto script = getHelper().createScript(getHelper().createName(), IdmScriptCategory.TRANSFORM_FROM,
				"import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;",
				"import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityFilter;",
				"",
				"IdmIdentityFilter filter = new IdmIdentityFilter();",
				"",
				"filter.setFirstName('" + identityTwo.getFirstName() + "');",
				"filter.setLastName('" + identityTwo.getLastName() + "');",
				"",
				"List<IdmIdentityDto> identities = identityService.find(filter, null).getContent();",
				"return identities.get(0);");

		getHelper().createScriptAuthority(script.getId(), ScriptAuthorityType.SERVICE, null, "identityService");
		getHelper().createScriptAuthority(script.getId(), ScriptAuthorityType.CLASS_NAME, IdmIdentityDto.class.getCanonicalName(), null);
		getHelper().createScriptAuthority(script.getId(), ScriptAuthorityType.CLASS_NAME, IdmIdentityFilter.class.getCanonicalName(), null);
		getHelper().createScriptAuthority(script.getId(), ScriptAuthorityType.CLASS_NAME, PageImpl.class.getCanonicalName(), null);
		getHelper().createScriptAuthority(script.getId(), ScriptAuthorityType.CLASS_NAME, "java.util.Collections$UnmodifiableRandomAccessList", null);


		passwodAttribute.setTransformationUidScript(prepareScriptExecutor(script.getCode()));
		systemAttributeMappingService.save(passwodAttribute);

		assignSystem(identity, system);
		assignSystem(identityTwo, system);

		String password = getHelper().createName();
		PasswordRequest request = prepareRequest(prepareUid(identity, system), system.getCode(), password);

		checkEcho(identity, system, EchoCheck.DOESNT_EXIST);
		checkEcho(identityTwo, system, EchoCheck.DOESNT_EXIST);
		processValidate(request, true);

		checkEcho(identity, system, EchoCheck.DOESNT_EXIST);
		checkEcho(identityTwo, system, EchoCheck.VALIDATE);
		processChange(request, true);
		checkEcho(identity, system, EchoCheck.DOESNT_EXIST);
		checkEcho(identityTwo, system, EchoCheck.VALIDATE_AND_CHANGE);
		checkPassword(prepareUid(identityTwo, system), password, false);
		checkPassword(prepareUid(identity, system), password, false);
	}

	@Test
	public void testPasswordPolicyOneSystem() {
		SysSystemDto system = createSystem(false);
		SysSystemDto systemTwo = createSystem(false);
		IdmIdentityDto identity = createIdentity(system);
		assignSystem(identity, systemTwo);
		setPasswordFilter(system, true);
		setPasswordFilter(systemTwo, true);

		createPasswordPolicy(system, 10, null, null, null, false);
		
		assignSystem(createUniformDefinition(true), system, systemTwo);
		
		cleanProvivisioning(identity, system);
		checkChangeInIdm(identity, 0);

		String password = "12345";
		PasswordRequest request = prepareRequest(identity.getUsername(), system.getCode(), password);

		loginToIdm(identity, password, false);
		checkEmptyProvisioning(identity, system);
		checkEcho(identity, system, EchoCheck.DOESNT_EXIST);
		checkEcho(identity, systemTwo, EchoCheck.DOESNT_EXIST);
		checkChangeInIdm(identity, 0);

		IdmResponse processValidate = processValidate(request, false);
		assertEquals(CoreResultCode.PASSWORD_DOES_NOT_MEET_POLICY.getStatus(), processValidate.status);
		assertTrue(String.valueOf(processValidate.message).contains(CoreResultCode.PASSWORD_DOES_NOT_MEET_POLICY.getCode()));

		loginToIdm(identity, password, false);
		checkEcho(identity, system, EchoCheck.VALIDATE_FAILED);
		checkEcho(identity, systemTwo, EchoCheck.VALIDATE_FAILED);
		checkChangeInIdm(identity, 0);
		checkEmptyProvisioning(identity, system);

		password = getHelper().createName();
		request.setPassword(password);
		processValidate(request, true);

		loginToIdm(identity, password, false);
		checkEcho(identity, system, EchoCheck.VALIDATE);
		checkEcho(identity, systemTwo, EchoCheck.VALIDATE);
		checkChangeInIdm(identity, 0);
		checkEmptyProvisioning(identity, system);
	}

	@Test
	public void testPasswordPolicyTwoSystem() {
		SysSystemDto system = createSystem(false);
		SysSystemDto systemTwo = createSystem(false);
		IdmIdentityDto identity = createIdentity(system);
		assignSystem(identity, systemTwo);
		setPasswordFilter(system, true);
		setPasswordFilter(systemTwo, true);

		createPasswordPolicy(system, 10, null, null, null, false);
		createPasswordPolicy(system, null, 1, null, null, false);
		
		assignSystem(createUniformDefinition(true), system, systemTwo);
		
		cleanProvivisioning(identity, system);
		checkChangeInIdm(identity, 0);

		String password = "12345";
		PasswordRequest request = prepareRequest(identity.getUsername(), system.getCode(), password);

		loginToIdm(identity, password, false);
		checkEmptyProvisioning(identity, system);
		checkEcho(identity, system, EchoCheck.DOESNT_EXIST);
		checkEcho(identity, systemTwo, EchoCheck.DOESNT_EXIST);
		checkChangeInIdm(identity, 0);

		IdmResponse processValidate = processValidate(request, false);
		assertEquals(CoreResultCode.PASSWORD_DOES_NOT_MEET_POLICY.getStatus(), processValidate.status);
		assertTrue(String.valueOf(processValidate.message).contains(CoreResultCode.PASSWORD_DOES_NOT_MEET_POLICY.getCode()));

		loginToIdm(identity, password, false);
		checkEcho(identity, system, EchoCheck.VALIDATE_FAILED);
		checkEcho(identity, systemTwo, EchoCheck.VALIDATE_FAILED);
		checkChangeInIdm(identity, 0);
		checkEmptyProvisioning(identity, system);

		password = getHelper().createName();

		processValidate = processValidate(request, false);
		assertEquals(CoreResultCode.PASSWORD_DOES_NOT_MEET_POLICY.getStatus(), processValidate.status);
		assertTrue(String.valueOf(processValidate.message).contains(CoreResultCode.PASSWORD_DOES_NOT_MEET_POLICY.getCode()));

		loginToIdm(identity, password, false);
		checkEcho(identity, system, EchoCheck.VALIDATE_FAILED);
		checkEcho(identity, systemTwo, EchoCheck.VALIDATE_FAILED);
		checkChangeInIdm(identity, 0);
		checkEmptyProvisioning(identity, system);

		password = getHelper().createName() + "*";
		request.setPassword(password);

		processValidate(request, true);

		loginToIdm(identity, password, false);
		checkEcho(identity, system, EchoCheck.VALIDATE);
		checkEcho(identity, systemTwo, EchoCheck.VALIDATE);
		checkChangeInIdm(identity, 0);
		checkEmptyProvisioning(identity, system);
	}

	@Test
	public void testPasswordPolicyDefault() {
		SysSystemDto system = createSystem(false);
		SysSystemDto systemTwo = createSystem(false);
		IdmIdentityDto identity = createIdentity(system);
		assignSystem(identity, systemTwo);
		setPasswordFilter(system, true);
		setPasswordFilter(systemTwo, true);

		createPasswordPolicy(system, null, null, null, 2, true);
		
		assignSystem(createUniformDefinition(true), system, systemTwo);
		
		cleanProvivisioning(identity, system);
		checkChangeInIdm(identity, 0);

		String password = "12345";
		PasswordRequest request = prepareRequest(identity.getUsername(), system.getCode(), password);

		loginToIdm(identity, password, false);
		checkEmptyProvisioning(identity, system);
		checkEcho(identity, system, EchoCheck.DOESNT_EXIST);
		checkEcho(identity, systemTwo, EchoCheck.DOESNT_EXIST);
		checkChangeInIdm(identity, 0);

		IdmResponse processValidate = processValidate(request, false);
		assertEquals(CoreResultCode.PASSWORD_DOES_NOT_MEET_POLICY.getStatus(), processValidate.status);
		assertTrue(String.valueOf(processValidate.message).contains(CoreResultCode.PASSWORD_DOES_NOT_MEET_POLICY.getCode()));

		loginToIdm(identity, password, false);
		checkEcho(identity, system, EchoCheck.VALIDATE_FAILED);
		checkEcho(identity, systemTwo, EchoCheck.VALIDATE_FAILED);
		checkChangeInIdm(identity, 0);
		checkEmptyProvisioning(identity, system);

		password = "1234AA";
		request.setPassword(password);

		processValidate(request, true);

		loginToIdm(identity, password, false);
		checkEcho(identity, system, EchoCheck.VALIDATE);
		checkEcho(identity, systemTwo, EchoCheck.VALIDATE);
		checkChangeInIdm(identity, 0);
		checkEmptyProvisioning(identity, system);
	}

	@Test
	public void testPasswordPolicyDefaultWithoutIdm() {
		SysSystemDto system = createSystem(false);
		SysSystemDto systemTwo = createSystem(false);
		IdmIdentityDto identity = createIdentity(system);
		assignSystem(identity, systemTwo);
		setPasswordFilter(system, true);
		setPasswordFilter(systemTwo, true);

		createPasswordPolicy(null, null, null, null, 2, true);
		
		assignSystem(createUniformDefinition(false), system, systemTwo);
		
		cleanProvivisioning(identity, system);
		checkChangeInIdm(identity, 0);

		String password = "12345";
		PasswordRequest request = prepareRequest(identity.getUsername(), system.getCode(), password);

		loginToIdm(identity, password, false);
		checkEmptyProvisioning(identity, system);
		checkEcho(identity, system, EchoCheck.DOESNT_EXIST);
		checkEcho(identity, systemTwo, EchoCheck.DOESNT_EXIST);
		checkChangeInIdm(identity, 0);

		processValidate(request, true);

		loginToIdm(identity, password, false);
		checkEcho(identity, system, EchoCheck.VALIDATE);
		checkEcho(identity, systemTwo, EchoCheck.VALIDATE);
		checkChangeInIdm(identity, 0);
		checkEmptyProvisioning(identity, system);
	}

	@Test
	public void testTwoSamePasswordPolicies() {
		SysSystemDto system = createSystem(false);
		SysSystemDto systemTwo = createSystem(false);
		IdmIdentityDto identity = createIdentity(system);
		assignSystem(identity, systemTwo);
		setPasswordFilter(system, true);
		setPasswordFilter(systemTwo, true);

		IdmPasswordPolicyDto passwordPolicy = createPasswordPolicy(system, 10, 2, null, null, false);
		systemTwo.setPasswordPolicyValidate(passwordPolicy.getId());
		systemTwo = systemService.save(systemTwo);
		
		assignSystem(createUniformDefinition(false), system, systemTwo);
		
		String password = "12345";
		PasswordRequest request = prepareRequest(identity.getUsername(), system.getCode(), password);

		checkEcho(identity, system, EchoCheck.DOESNT_EXIST);
		checkEcho(identity, systemTwo, EchoCheck.DOESNT_EXIST);

		IdmResponse processValidate = processValidate(request, false);
		assertEquals(CoreResultCode.PASSWORD_DOES_NOT_MEET_POLICY.getStatus(), processValidate.status);
		assertTrue(String.valueOf(processValidate.message).contains(CoreResultCode.PASSWORD_DOES_NOT_MEET_POLICY.getCode()));

		checkEcho(identity, system, EchoCheck.VALIDATE_FAILED);
		checkEcho(identity, systemTwo, EchoCheck.VALIDATE_FAILED);

		password = "1234567890**";
		request.setPassword(password);

		processValidate(request, true);
		checkEcho(identity, system, EchoCheck.VALIDATE);
		checkEcho(identity, systemTwo, EchoCheck.VALIDATE);
	}

	@Test
	public void testTwoSamePasswordPoliciesAndDefault() {
		SysSystemDto system = createSystem(false);
		SysSystemDto systemTwo = createSystem(false);
		IdmIdentityDto identity = createIdentity(system);
		assignSystem(identity, systemTwo);
		setPasswordFilter(system, true);
		setPasswordFilter(systemTwo, true);
		
		assignSystem(createUniformDefinition(true), system, systemTwo);

		IdmPasswordPolicyDto passwordPolicy = createPasswordPolicy(system, 5, 1, null, null, true);
		systemTwo.setPasswordPolicyValidate(passwordPolicy.getId());
		systemTwo = systemService.save(systemTwo);
		
		assignSystem(createUniformDefinition(false), system, systemTwo);
		
		String password = "12345";
		PasswordRequest request = prepareRequest(identity.getUsername(), system.getCode(), password);

		checkEcho(identity, system, EchoCheck.DOESNT_EXIST);
		checkEcho(identity, systemTwo, EchoCheck.DOESNT_EXIST);

		IdmResponse processValidate = processValidate(request, false);
		assertEquals(CoreResultCode.PASSWORD_DOES_NOT_MEET_POLICY.getStatus(), processValidate.status);
		assertTrue(String.valueOf(processValidate.message).contains(CoreResultCode.PASSWORD_DOES_NOT_MEET_POLICY.getCode()));

		checkEcho(identity, system, EchoCheck.VALIDATE_FAILED);
		checkEcho(identity, systemTwo, EchoCheck.VALIDATE_FAILED);

		password = "12345*";
		request.setPassword(password);

		processValidate(request, true);
		checkEcho(identity, system, EchoCheck.VALIDATE);
		checkEcho(identity, systemTwo, EchoCheck.VALIDATE);
	}

	@Test
	public void testCheckPasswordValidityNull() {
		IdmPasswordPolicyDto passwordPolicy = createPasswordPolicy(null, null, null, null, null, true);
		passwordPolicy.setMaxPasswordAge(null);
		passwordPolicy = passwordPolicyService.save(passwordPolicy);
		
		SysSystemDto system = createSystem(false);
		IdmIdentityDto identity = createIdentity(system);
		assignSystem(createUniformDefinition(true), system);

		setPasswordFilter(system, true);

		String password = getHelper().createName();

		PasswordChangeDto passwordChangeDto = new PasswordChangeDto();
		passwordChangeDto.setAll(true);
		passwordChangeDto.setNewPassword(new GuardedString(password));

		List<OperationResult> results = identityService.passwordChange(identity, passwordChangeDto);
		assertEquals(2, results.size());

		checkEcho(identity, system, EchoCheck.VALIDATE_AND_CHANGE);

		checkPassword(prepareUid(identity, system), password, true);
		checkActivePasswordOperation(identity, system, 0, password);
		loginToIdm(identity, password, true);

		IdmPasswordDto passwordDto = passwordService.findOneByIdentity(identity.getId());
		assertNotNull(passwordDto);
		assertNull(passwordDto.getValidTill());
		Assert.assertNull(passwordDto.getValidFrom());
	}

	@Test
	public void testCheckPasswordValidity10Days() {
		IdmPasswordPolicyDto passwordPolicy = createPasswordPolicy(null, null, null, null, null, true);
		passwordPolicy.setMaxPasswordAge(10);
		passwordPolicy = passwordPolicyService.save(passwordPolicy);
		
		SysSystemDto system = createSystem(false);
		IdmIdentityDto identity = createIdentity(system);
		
		assignSystem(createUniformDefinition(true), system);

		String password = getHelper().createName();

		PasswordChangeDto passwordChangeDto = new PasswordChangeDto();
		passwordChangeDto.setAll(true);
		passwordChangeDto.setNewPassword(new GuardedString(password));

		List<OperationResult> results = identityService.passwordChange(identity, passwordChangeDto);
		assertEquals(2, results.size());

		checkEcho(identity, system, EchoCheck.DOESNT_EXIST);

		checkPassword(prepareUid(identity, system), password, true);
		checkActivePasswordOperation(identity, system, 0, password);
		loginToIdm(identity, password, true);

		IdmPasswordDto passwordDto = passwordService.findOneByIdentity(identity.getId());
		assertNotNull(passwordDto);
		assertEquals(LocalDate.now().plusDays(10), passwordDto.getValidTill());
		Assert.assertNull(passwordDto.getValidFrom());
	}

	@Test
	public void testCheckPasswordValidityWithoutUniformPassword() {
		IdmPasswordPolicyDto passwordPolicy = createPasswordPolicy(null, null, null, null, null, true);
		passwordPolicy.setMaxPasswordAge(20);
		passwordPolicy = passwordPolicyService.save(passwordPolicy);
		
		SysSystemDto system = createSystem(true);
		IdmIdentityDto identity = createIdentity(system);

		setPasswordFilter(system, true);

		String password = getHelper().createName();

		PasswordChangeDto passwordChangeDto = new PasswordChangeDto();
		passwordChangeDto.setAll(true);
		passwordChangeDto.setIdm(true);
		passwordChangeDto.setNewPassword(new GuardedString(password));

		List<OperationResult> results = identityService.passwordChange(identity, passwordChangeDto);
		assertEquals(2, results.size());

		checkEcho(identity, system, EchoCheck.VALIDATE_AND_CHANGE);

		checkPassword(prepareUid(identity, system), password, true);
		checkActivePasswordOperation(identity, system, 0, password);
		loginToIdm(identity, password, true);

		IdmPasswordDto passwordDto = passwordService.findOneByIdentity(identity.getId());
		assertNotNull(passwordDto);
		assertEquals(LocalDate.now().plusDays(20), passwordDto.getValidTill());
		Assert.assertNull(passwordDto.getValidFrom());
	}

	@Test
	public void testCheckPasswordValidityPasswordFilterNullUniformPassword() {
		SysSystemDto system = createSystem(false);
		IdmIdentityDto identity = createIdentity(system);
		setPasswordFilter(system, true);

		IdmPasswordPolicyDto passwordPolicyDto = createPasswordPolicy(null, null, null, null,null, true);
		passwordPolicyDto.setMaxPasswordAge(null);
		passwordPolicyDto = passwordPolicyService.save(passwordPolicyDto);
		
		assignSystem(createUniformDefinition(true), system);
		
		cleanProvivisioning(identity, system);
		checkChangeInIdm(identity, 0);

		String password = getHelper().createName();
		PasswordRequest request = prepareRequest(identity.getUsername(), system.getCode(), password);

		loginToIdm(identity, password, false);
		checkEcho(identity, system, EchoCheck.DOESNT_EXIST);
		checkChangeInIdm(identity, 0);

		processValidate(request, true);
		
		checkEcho(identity, system, EchoCheck.VALIDATE);

		processChange(request, true);

		loginToIdm(identity, password, true);
		checkEcho(identity, system, EchoCheck.VALIDATE_AND_CHANGE);

		IdmPasswordDto passwordDto = passwordService.findOneByIdentity(identity.getId());
		assertNotNull(passwordDto);
		assertNull(passwordDto.getValidTill());
		assertEquals(LocalDate.now(), passwordDto.getValidFrom());
	}

	@Test
	public void testCheckPasswordValidityPasswordFilter50daysUniformPassword() {
		SysSystemDto system = createSystem(false);
		IdmIdentityDto identity = createIdentity(system);
		setPasswordFilter(system, true);

		IdmPasswordPolicyDto passwordPolicyDto = createPasswordPolicy(null, null, null, null,null, true);
		passwordPolicyDto.setMaxPasswordAge(50);
		passwordPolicyDto = passwordPolicyService.save(passwordPolicyDto);
		
		assignSystem(createUniformDefinition(true), system);
		
		cleanProvivisioning(identity, system);
		checkChangeInIdm(identity, 0);

		String password = getHelper().createName();
		PasswordRequest request = prepareRequest(identity.getUsername(), system.getCode(), password);

		loginToIdm(identity, password, false);
		checkEcho(identity, system, EchoCheck.DOESNT_EXIST);
		checkChangeInIdm(identity, 0);

		processValidate(request, true);

		checkEcho(identity, system, EchoCheck.VALIDATE);

		processChange(request, true);

		loginToIdm(identity, password, true);
		checkEcho(identity, system, EchoCheck.VALIDATE_AND_CHANGE);

		IdmPasswordDto passwordDto = passwordService.findOneByIdentity(identity.getId());
		assertNotNull(passwordDto);
		assertEquals(LocalDate.now().plusDays(50), passwordDto.getValidTill());
		assertEquals(LocalDate.now(), passwordDto.getValidFrom());
	}

	@Test
	public void testCheckPasswordValidityPasswordFilterWithoutUniformPassword() {
		SysSystemDto system = createSystem(false);
		IdmIdentityDto identity = createIdentity(system);
		setPasswordFilter(system, true);

		IdmPasswordPolicyDto passwordPolicyDto = createPasswordPolicy(null, null, null, null,null, true);
		passwordPolicyDto.setMaxPasswordAge(50);
		passwordPolicyDto = passwordPolicyService.save(passwordPolicyDto);
		
		assignSystem(createUniformDefinition(false), system);
		
		cleanProvivisioning(identity, system);
		checkChangeInIdm(identity, 0);

		String password = getHelper().createName();
		PasswordRequest request = prepareRequest(identity.getUsername(), system.getCode(), password);

		loginToIdm(identity, password, false);
		checkEcho(identity, system, EchoCheck.DOESNT_EXIST);
		checkChangeInIdm(identity, 0);

		processValidate(request, true);
		
		checkEcho(identity, system, EchoCheck.VALIDATE);

		processChange(request, true);

		loginToIdm(identity, password, false);
		checkEcho(identity, system, EchoCheck.VALIDATE_AND_CHANGE);

		IdmPasswordDto passwordDto = passwordService.findOneByIdentity(identity.getId());
		assertNull(passwordDto);
	}

	@Test
	@Ignore
	public void stressTestPasswordEndcoding() {
		long sum = 0;
		int iteration = 100;
		for (int index = 0; index < iteration+1; index++) {
			long before = System.currentTimeMillis();
			passwordFilterManager.createEcho(UUID.randomUUID(), new GuardedString(getHelper().createName()));
			long after = System.currentTimeMillis();
			
			long diff = after - before;
			sum += diff;
		}
		System.out.println(MessageFormat.format("[{0}] iteration takes [{1}]. Average for one iteration [{2}].", iteration, sum, sum/iteration));
	}

}
