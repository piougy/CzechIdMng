package eu.bcvsolutions.idm.core.model.service.impl;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import java.time.ZonedDateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmPasswordDto;
import eu.bcvsolutions.idm.core.api.dto.IdmPasswordHistoryDto;
import eu.bcvsolutions.idm.core.api.dto.PasswordChangeDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmPasswordHistoryFilter;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.IdmPasswordHistoryService;
import eu.bcvsolutions.idm.core.api.service.IdmPasswordService;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;
import eu.bcvsolutions.idm.test.api.TestHelper;

/**
 * Test class for {@link DefaultIdmPasswordHistoryService}
 * 
 * @author Ondrej Kopr
 *
 */
public class DefaultPasswordHistoryIntegrationTest extends AbstractIntegrationTest {

	@Autowired
	private TestHelper testHelper;
	@Autowired
	private IdmPasswordHistoryService passwordHistoryService;
	@Autowired
	private IdmIdentityService identityService;
	@Autowired
	private IdmPasswordService passwordService;
	
	@Before
	public void init() {
		loginAsAdmin();
	}
	
	@After 
	public void logout() {
		super.logout();
	}
	
	@Test
	public void testCreateNewPasswordHistoryRecord() {
		String password = "test-password-" + System.currentTimeMillis();
		String originalPassword = "test-password-" + System.currentTimeMillis();
		
		List<IdmPasswordHistoryDto> content = passwordHistoryService.find(null).getContent();
		int beforeSize = content.size();

		IdmIdentityDto newIdentity = testHelper.createIdentity(new GuardedString(originalPassword));
		IdmIdentityDto newIdentity2 = testHelper.createIdentity(new GuardedString(originalPassword));

		content = passwordHistoryService.find(null).getContent();
		// after create identity is create only one password history record
		assertEquals(beforeSize + 2, content.size());
		
		PasswordChangeDto passwordChange = new PasswordChangeDto();
		passwordChange.setIdm(true);
		passwordChange.setNewPassword(new GuardedString(password));
		identityService.passwordChange(newIdentity, passwordChange);
		
		content = passwordHistoryService.find(null).getContent();
		assertEquals(beforeSize + 3, content.size());
		
		passwordChange = new PasswordChangeDto();
		passwordChange.setIdm(true);
		passwordChange.setNewPassword(new GuardedString(password));
		identityService.passwordChange(newIdentity2, passwordChange);
		
		content = passwordHistoryService.find(null).getContent();
		assertEquals(beforeSize + 4, content.size());
	}
	
	@Test
	public void testFilteringByIdentity() {
		passwordHistoryService.find(null).getContent();
		
		String password = "test-password-" + System.currentTimeMillis();
		String originalPassword = "test-password-" + System.currentTimeMillis();

		IdmIdentityDto identity = testHelper.createIdentity(new GuardedString(originalPassword));
		
		PasswordChangeDto passwordChange = new PasswordChangeDto();
		passwordChange.setIdm(true);
		passwordChange.setNewPassword(new GuardedString(password));
		identityService.passwordChange(identity, passwordChange);
		
		IdmPasswordHistoryFilter filter = new IdmPasswordHistoryFilter();
		filter.setIdentityId(identity.getId());
		List<IdmPasswordHistoryDto> content = passwordHistoryService.find(filter, null).getContent();
		
		// after create identity is create only one password history record
		assertEquals(2, content.size());
		
		IdmPasswordDto identityPassword = passwordService.findOneByIdentity(identity.getId());
		
		// behavior with content.get(0) is not good for use, order comes from DB!
		boolean existsSamePassword = false;
		for (IdmPasswordHistoryDto pass : content) {
			if (pass.getPassword().equals(identityPassword.getPassword())) {
				existsSamePassword = true;
			}			
		}
		assertTrue(existsSamePassword);
	}
	
	@Test
	public void testCreateNewPasswordAsNoAdmin() {
		String password = "test-password-" + System.currentTimeMillis();
		String originalPassword = "test-password-" + System.currentTimeMillis();

		IdmIdentityDto identity = testHelper.createIdentity(new GuardedString(originalPassword));
		
		IdmPasswordHistoryFilter filter = new IdmPasswordHistoryFilter();
		filter.setIdentityId(identity.getId());
		List<IdmPasswordHistoryDto> content = passwordHistoryService.find(filter, null).getContent();
		
		// after create identity is only one record
		assertEquals(1, content.size());
		
		this.loginAsNoAdmin(identity.getUsername());
		
		PasswordChangeDto passwordChange = new PasswordChangeDto();
		passwordChange.setOldPassword(new GuardedString(originalPassword));
		passwordChange.setAll(true);
		passwordChange.setIdm(true);
		passwordChange.setNewPassword(new GuardedString(password));
		identityService.passwordChange(identity, passwordChange);
		
		content = passwordHistoryService.find(filter, null).getContent();
		assertEquals(2, content.size());
	}

	@Test
	public void testFilteringByCreatorOne() {
		String password = "password-" + System.currentTimeMillis();
		GuardedString passwordAsGuardedString = new GuardedString(password);

		IdmIdentityDto admin = testHelper.createIdentity();
		loginAsAdmin(admin.getUsername());

		IdmIdentityDto identity = testHelper.createIdentity(passwordAsGuardedString); // Change 1

		PasswordChangeDto passwordChange = new PasswordChangeDto();
		passwordChange.setOldPassword(passwordAsGuardedString);
		passwordChange.setAll(true);
		passwordChange.setIdm(true);
		passwordChange.setNewPassword(new GuardedString(password));
		identityService.passwordChange(identity, passwordChange); // Change 2
		identityService.passwordChange(identity, passwordChange); // Change 3
		identityService.passwordChange(identity, passwordChange); // Change 4

		IdmPasswordHistoryFilter filter = new IdmPasswordHistoryFilter();
		filter.setCreator(admin.getCode());
		List<IdmPasswordHistoryDto> content = passwordHistoryService.find(filter, null).getContent();
		assertEquals(4, content.size());
	}

	@Test
	public void testFilteringByCreatorTwo() {
		String password = "password-" + System.currentTimeMillis();
		GuardedString passwordAsGuardedString = new GuardedString(password);

		IdmIdentityDto adminOne = testHelper.createIdentity();
		IdmIdentityDto adminTwo = testHelper.createIdentity();
		loginAsAdmin(adminOne.getUsername());

		IdmIdentityDto identity = testHelper.createIdentity(passwordAsGuardedString); // Change 1

		PasswordChangeDto passwordChange = new PasswordChangeDto();
		passwordChange.setOldPassword(passwordAsGuardedString);
		passwordChange.setAll(true);
		passwordChange.setIdm(true);
		passwordChange.setNewPassword(new GuardedString(password));
		identityService.passwordChange(identity, passwordChange); // Change 2

		logout();
		loginAsAdmin(adminTwo.getUsername());
		
		identityService.passwordChange(identity, passwordChange); // Change 3
		identityService.passwordChange(identity, passwordChange); // Change 4
		identityService.passwordChange(identity, passwordChange); // Change 5

		IdmPasswordHistoryFilter filter = new IdmPasswordHistoryFilter();
		filter.setCreator(adminOne.getCode());
		List<IdmPasswordHistoryDto> content = passwordHistoryService.find(filter, null).getContent();
		assertEquals(2, content.size());

		filter.setCreator(adminTwo.getCode());
		content = passwordHistoryService.find(filter, null).getContent();
		assertEquals(3, content.size());

		filter = new IdmPasswordHistoryFilter();
		filter.setIdentityId(identity.getId());
		content = passwordHistoryService.find(filter, null).getContent();
		assertEquals(5, content.size());
	}

	@Test
	public void testFilteringByIdentityUsername() {
		String password = "password-" + System.currentTimeMillis();
		GuardedString passwordAsGuardedString = new GuardedString(password);

		IdmIdentityDto identity = testHelper.createIdentity(passwordAsGuardedString); // Change 1

		PasswordChangeDto passwordChange = new PasswordChangeDto();
		passwordChange.setOldPassword(passwordAsGuardedString);
		passwordChange.setAll(true);
		passwordChange.setIdm(true);
		passwordChange.setNewPassword(new GuardedString(password));
		identityService.passwordChange(identity, passwordChange); // Change 2
		identityService.passwordChange(identity, passwordChange); // Change 3
		identityService.passwordChange(identity, passwordChange); // Change 4

		IdmPasswordHistoryFilter filter = new IdmPasswordHistoryFilter();
		filter.setIdentityUsername(identity.getUsername());
		List<IdmPasswordHistoryDto> content = passwordHistoryService.find(filter, null).getContent();
		assertEquals(4, content.size());

		filter = new IdmPasswordHistoryFilter();
		filter.setIdentityId(identity.getId());
		content = passwordHistoryService.find(filter, null).getContent();
		assertEquals(4, content.size());
	}

	@Test
	public void testFilteringByValidTill() {
		String password = "password-" + System.currentTimeMillis();
		GuardedString passwordAsGuardedString = new GuardedString(password);

		IdmIdentityDto identity = testHelper.createIdentity(passwordAsGuardedString); // Change 1
		
		ZonedDateTime tillOne = ZonedDateTime.now();

		PasswordChangeDto passwordChange = new PasswordChangeDto();
		passwordChange.setOldPassword(passwordAsGuardedString);
		passwordChange.setAll(true);
		passwordChange.setIdm(true);
		passwordChange.setNewPassword(new GuardedString(password));
		identityService.passwordChange(identity, passwordChange); // Change 2
		ZonedDateTime tillTwo = ZonedDateTime.now();
		identityService.passwordChange(identity, passwordChange); // Change 3
		ZonedDateTime tillThree = ZonedDateTime.now();
		identityService.passwordChange(identity, passwordChange); // Change 4
		ZonedDateTime tillFour = ZonedDateTime.now();
		
		IdmPasswordHistoryFilter filter = new IdmPasswordHistoryFilter();
		filter.setIdentityId(identity.getId());
		filter.setTill(tillOne);
		List<IdmPasswordHistoryDto> content = passwordHistoryService.find(filter, null).getContent();
		assertEquals(1, content.size());

		filter.setTill(tillTwo);
		content = passwordHistoryService.find(filter, null).getContent();
		assertEquals(2, content.size());

		filter.setTill(tillThree);
		content = passwordHistoryService.find(filter, null).getContent();
		assertEquals(3, content.size());
		

		filter.setTill(tillFour);
		content = passwordHistoryService.find(filter, null).getContent();
		assertEquals(4, content.size());
	}

	@Test
	public void testFilteringByValidFrom() {
		String password = "password-" + System.currentTimeMillis();
		GuardedString passwordAsGuardedString = new GuardedString(password);

		IdmIdentityDto identity = testHelper.createIdentity(passwordAsGuardedString); // Change 1
		ZonedDateTime fromOne = ZonedDateTime.now();

		PasswordChangeDto passwordChange = new PasswordChangeDto();
		passwordChange.setOldPassword(passwordAsGuardedString);
		passwordChange.setAll(true);
		passwordChange.setIdm(true);
		passwordChange.setNewPassword(new GuardedString(password));
		identityService.passwordChange(identity, passwordChange); // Change 2
		ZonedDateTime fromTwo = ZonedDateTime.now();
		identityService.passwordChange(identity, passwordChange); // Change 3
		ZonedDateTime fromThree = ZonedDateTime.now();
		identityService.passwordChange(identity, passwordChange); // Change 4
		ZonedDateTime fromFour = ZonedDateTime.now();
		
		IdmPasswordHistoryFilter filter = new IdmPasswordHistoryFilter();
		filter.setIdentityId(identity.getId());
		filter.setFrom(fromOne);
		List<IdmPasswordHistoryDto> content = passwordHistoryService.find(filter, null).getContent();
		assertEquals(3, content.size());

		filter.setFrom(fromTwo);
		content = passwordHistoryService.find(filter, null).getContent();
		assertEquals(2, content.size());

		filter.setFrom(fromThree);
		content = passwordHistoryService.find(filter, null).getContent();
		assertEquals(1, content.size());
		

		filter.setFrom(fromFour);
		content = passwordHistoryService.find(filter, null).getContent();
		assertEquals(0, content.size());
	}

	@Test
	public void testFilteringByValidFromAndTillCombination() {
		String password = "password-" + System.currentTimeMillis();
		GuardedString passwordAsGuardedString = new GuardedString(password);

		IdmIdentityDto identity = testHelper.createIdentity(passwordAsGuardedString); // Change 1
		
		ZonedDateTime from = ZonedDateTime.now();

		PasswordChangeDto passwordChange = new PasswordChangeDto();
		passwordChange.setOldPassword(passwordAsGuardedString);
		passwordChange.setAll(true);
		passwordChange.setIdm(true);
		passwordChange.setNewPassword(new GuardedString(password));
		identityService.passwordChange(identity, passwordChange); // Change 2
		identityService.passwordChange(identity, passwordChange); // Change 3
		ZonedDateTime till = ZonedDateTime.now();
		identityService.passwordChange(identity, passwordChange); // Change 4
		
		IdmPasswordHistoryFilter filter = new IdmPasswordHistoryFilter();
		filter.setIdentityId(identity.getId());
		filter.setFrom(from);
		filter.setTill(till);
		List<IdmPasswordHistoryDto> content = passwordHistoryService.find(filter, null).getContent();
		assertEquals(2, content.size());
	}
}
