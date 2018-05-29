package eu.bcvsolutions.idm.core.model.service.impl;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmPasswordDto;
import eu.bcvsolutions.idm.core.api.dto.IdmPasswordHistoryDto;
import eu.bcvsolutions.idm.core.api.dto.PasswordChangeDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmPasswordHistoryFilter;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.IdmPasswordHistoryService;
import eu.bcvsolutions.idm.core.api.service.IdmPasswordService;
import eu.bcvsolutions.idm.core.model.service.impl.DefaultIdmPasswordHistoryService;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;
import eu.bcvsolutions.idm.test.api.TestHelper;

/**
 * Test class for {@link DefaultIdmPasswordHistoryService}
 * TODO: now is during create identity save password twice.
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
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
		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
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
		
		IdmPasswordHistoryDto passwordHistory1 = content.get(0);
		IdmPasswordHistoryDto passwordHistory2 = content.get(1);
		
		IdmPasswordDto identityPassword = passwordService.findOneByIdentity(identity.getId());
		assertNotEquals(identityPassword.getPassword(), passwordHistory1.getPassword());
		assertEquals(identityPassword.getPassword(), passwordHistory2.getPassword());
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
}
