package eu.bcvsolutions.idm.acc.service.impl;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.mapping.Array;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.acc.TestHelper;
import eu.bcvsolutions.idm.acc.domain.AccountType;
import eu.bcvsolutions.idm.acc.domain.AttributeMappingStrategyType;
import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.AccIdentityAccountDto;
import eu.bcvsolutions.idm.acc.dto.ProvisioningAttributeDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.AccIdentityAccountService;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmPasswordPolicyDto;
import eu.bcvsolutions.idm.core.api.dto.PasswordChangeDto;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.IdmPasswordPolicyService;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

public class PasswordValidationIntegrationTest extends AbstractIntegrationTest{

	@Autowired
	private IdmPasswordPolicyService passwordPolicyService;
	@Autowired private IdmIdentityService idmIdentityService;
	@Autowired private TestHelper testHelper;
	@Autowired private AccAccountService accountService;
	@Autowired private AccIdentityAccountService accountIdentityService;
	@Autowired private DefaultSysSystemMappingService mappingService;
	
	@Before
	public void init() {
		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
	}
	
	@After 
	public void logout() {
		super.logout();
	}
	@Test
	public void testLenght() {
		IdmIdentityDto identity = new IdmIdentityDto();
		identity.setUsername("norand");
		identity.setFirstName("nor");
		identity.setLastName("and");
		identity = idmIdentityService.save(identity);
		//
		SysSystemDto system = testHelper.createTestResourceSystem(true);
		ProvisioningAttributeDto usernameAttribute = getProvisioningAttribute(TestHelper.ATTRIBUTE_MAPPING_NAME);
		ProvisioningAttributeDto firstNameAttribute = getProvisioningAttribute(TestHelper.ATTRIBUTE_MAPPING_FIRSTNAME);
		ProvisioningAttributeDto lastNameAttribute = getProvisioningAttribute(TestHelper.ATTRIBUTE_MAPPING_LASTNAME);
		ProvisioningAttributeDto passwordAttribute = getProvisioningAttribute(TestHelper.ATTRIBUTE_MAPPING_PASSWORD);		
		//
		AccAccountDto acc= new AccAccountDto();
		acc.setId(UUID.randomUUID());
		acc.setUid(System.currentTimeMillis() + "");
		acc.setAccountType(AccountType.PERSONAL);
		acc.setSystem(system.getId());
		//
		acc = accountService.save(acc);
		//
		AccIdentityAccountDto account = testHelper.createIdentityAccount(system, identity);	
		account.setAccount(acc.getId());
		account = accountIdentityService.save(account);
		List<String> accounts = new ArrayList<String>();
		accounts.add(account.getId() + "");
		//password policy default
		IdmPasswordPolicyDto policyDefault = new IdmPasswordPolicyDto();
		policyDefault.setName(System.currentTimeMillis() + "test1");
		policyDefault.setDefaultPolicy(true);
		policyDefault.setMinPasswordLength(5);
		policyDefault.setMaxPasswordLength(10);
		//password policy
		IdmPasswordPolicyDto policy = new IdmPasswordPolicyDto();
		policy.setName(System.currentTimeMillis() + "test2");
		policy.setDefaultPolicy(false);
		policy.setMinPasswordLength(6);
		policy.setMaxPasswordLength(11);
		
		passwordPolicyService.save(policyDefault);
		passwordPolicyService.save(policy);
		system.setPasswordPolicyValidate(policy.getId());
		
		PasswordChangeDto passwordChange = new PasswordChangeDto();
		passwordChange.setIdm(true);
		passwordChange.setAccounts(accounts);
		
		try {
			idmIdentityService.validate(passwordChange, identity);
			} catch (ResultCodeException ex) {
				assertEquals(6, ex.getError().getError().getParameters().get("minLength"));
				assertEquals(10, ex.getError().getError().getParameters().get("maxLength"));
				assertEquals(policyDefault.getName() + ", " +policy.getName(), ex.getError().getError().getParameters().get("policiesNamesPreValidation"));
				// special char base -> 4
				assertEquals(4, ex.getError().getError().getParameters().size());
				
			}
	}
	
	/**
	 * Return provisiong attribute by default mapping and strategy
	 * 
	 * @return
	 */
	private ProvisioningAttributeDto getProvisioningAttribute(String name) {
		// load attribute mapping is not needed now - name is the same on both (tree) sides
		return new ProvisioningAttributeDto(name, AttributeMappingStrategyType.SET);
	}
}
