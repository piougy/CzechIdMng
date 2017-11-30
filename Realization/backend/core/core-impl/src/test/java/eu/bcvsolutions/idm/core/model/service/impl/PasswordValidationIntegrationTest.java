package eu.bcvsolutions.idm.core.model.service.impl;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmPasswordPolicyDto;
import eu.bcvsolutions.idm.core.api.dto.PasswordChangeDto;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.IdmPasswordPolicyService;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;
import eu.bcvsolutions.idm.test.api.TestHelper;

public class PasswordValidationIntegrationTest extends AbstractIntegrationTest{

	@Autowired
	private IdmPasswordPolicyService passwordPolicyService;
	@Autowired private IdmIdentityService idmIdentityService;
	
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
		IdmPasswordPolicyDto policy = new IdmPasswordPolicyDto();
		policy.setName(System.currentTimeMillis() + "");
		policy.setDefaultPolicy(true);
		policy.setMinPasswordLength(5);
		policy.setMaxPasswordLength(10);
		IdmIdentityDto identity = new IdmIdentityDto();
		PasswordChangeDto passwordChange = new PasswordChangeDto();
		passwordChange.setIdm(true);
		
		passwordPolicyService.save(policy);
		try {
		idmIdentityService.validate(passwordChange, identity);
		} catch (ResultCodeException ex) {
			assertEquals(5, ex.getError().getError().getParameters().get("minLength"));
			assertEquals(10, ex.getError().getError().getParameters().get("maxLength"));
			assertEquals(policy.getName(), ex.getError().getError().getParameters().get("policiesNamesPreValidation"));
			// special char base -> 4
			assertEquals(4, ex.getError().getError().getParameters().size());
			
		}
	}
	
	@Test
	public void testMinChar() {
		IdmPasswordPolicyDto policy = new IdmPasswordPolicyDto();
		policy.setName(System.currentTimeMillis() + "");
		policy.setDefaultPolicy(true);
		policy.setMinUpperChar(5);
		policy.setMinLowerChar(10);
		IdmIdentityDto identity = new IdmIdentityDto();
		PasswordChangeDto passwordChange = new PasswordChangeDto();
		passwordChange.setIdm(true);
		
		passwordPolicyService.save(policy);
		try {
		idmIdentityService.validate(passwordChange, identity);
		} catch (ResultCodeException ex) {
			assertEquals(5, ex.getError().getError().getParameters().get("minUpperChar"));
			assertEquals(10, ex.getError().getError().getParameters().get("minLowerChar"));
			assertEquals(policy.getName(), ex.getError().getError().getParameters().get("policiesNamesPreValidation"));
			// special char base -> 4
			assertEquals(4, ex.getError().getError().getParameters().size());
			
		}
	}
	
	@Test
	public void testNumberSpecialChar() {
		IdmPasswordPolicyDto policy = new IdmPasswordPolicyDto();
		policy.setName(System.currentTimeMillis() + "");
		policy.setDefaultPolicy(true);
		policy.setMinNumber(5);
		policy.setMinSpecialChar(10);
		IdmIdentityDto identity = new IdmIdentityDto();
		PasswordChangeDto passwordChange = new PasswordChangeDto();
		passwordChange.setIdm(true);
		
		passwordPolicyService.save(policy);
		try {
		idmIdentityService.validate(passwordChange, identity);
		} catch (ResultCodeException ex) {
			assertEquals(5, ex.getError().getError().getParameters().get("minNumber"));
			assertEquals(10, ex.getError().getError().getParameters().get("minSpecialChar"));
			assertEquals(policy.getName(), ex.getError().getError().getParameters().get("policiesNamesPreValidation"));
			// special char base -> 4
			assertEquals(4, ex.getError().getError().getParameters().size());
			
		}
	}
	
	@Test
	public void testAdvancedEnabled() {
		IdmPasswordPolicyDto policy = new IdmPasswordPolicyDto();
		policy.setName(System.currentTimeMillis() + "");
		policy.setDefaultPolicy(true);
		policy.setMinPasswordLength(10);
		policy.setMaxPasswordLength(20);
		policy.setPasswordLengthRequired(true);
		policy.setMinUpperChar(5);
		policy.setUpperCharRequired(true);
		policy.setMinLowerChar(4);
		policy.setLowerCharRequired(true);
		policy.setEnchancedControl(true);
		policy.setMinRulesToFulfill(1);
		policy.setMinNumber(3);
		policy.setMinSpecialChar(2);
		IdmIdentityDto identity = new IdmIdentityDto();
		PasswordChangeDto passwordChange = new PasswordChangeDto();
		passwordChange.setIdm(true);
		
		passwordPolicyService.save(policy);
		try {
		idmIdentityService.validate(passwordChange, identity);
		} catch (ResultCodeException ex) {
			assertEquals(10, ex.getError().getError().getParameters().get("minLength"));
			assertEquals(20, ex.getError().getError().getParameters().get("maxLength"));
			assertEquals(5, ex.getError().getError().getParameters().get("minUpperChar"));
			assertEquals(4, ex.getError().getError().getParameters().get("minLowerChar"));
			assertEquals(3, ex.getError().getError().getParameters().get("minNumber"));
			assertEquals(2, ex.getError().getError().getParameters().get("minSpecialChar"));
			assertEquals(1, ex.getError().getError().getParameters().get("minRulesToFulfillCount"));
			assertEquals(policy.getName(), ex.getError().getError().getParameters().get("policiesNamesPreValidation"));
			// special char base -> 4
			assertEquals(7, ex.getError().getError().getParameters().size());
			
		}
	}
}
