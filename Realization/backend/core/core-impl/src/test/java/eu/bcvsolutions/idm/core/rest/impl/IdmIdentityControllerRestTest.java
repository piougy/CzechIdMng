package eu.bcvsolutions.idm.core.rest.impl;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.net.URLEncoder;
import java.util.List;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoControllerRestTest;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.bulk.action.impl.IdentityDisableBulkAction;
import eu.bcvsolutions.idm.test.api.TestHelper;

/**
 * Identity controller tests
 * - TODO: move filters here
 * 
 * @author Radek Tomiška
 *
 */
public class IdmIdentityControllerRestTest extends AbstractReadWriteDtoControllerRestTest<IdmIdentityDto> {

	@Autowired private IdmIdentityController controller;
	
	@Override
	protected AbstractReadWriteDtoController<IdmIdentityDto, ?> getController() {
		return controller;
	}

	@Override
	protected IdmIdentityDto prepareDto() {
		IdmIdentityDto dto = new IdmIdentityDto();
		dto.setUsername(getHelper().createName());
		return dto;
	}
	
	@Test
    public void userNotFound() throws Exception {
		getMockMvc().perform(get(getDetailUrl("n_a_user"))
        		.with(authentication(getAdminAuthentication()))
                .contentType(InitTestData.HAL_CONTENT_TYPE))
                .andExpect(status().isNotFound());
    }
	
	@Test
    public void userFoundByUsername() throws Exception {
		getMockMvc().perform(get(getDetailUrl(InitTestData.TEST_ADMIN_USERNAME))
        		.with(authentication(getAdminAuthentication()))
                .contentType(InitTestData.HAL_CONTENT_TYPE))
                .andExpect(status().isOk())
                .andExpect(content().contentType(InitTestData.HAL_CONTENT_TYPE))
                .andExpect(jsonPath("$.username", equalTo(InitTestData.TEST_ADMIN_USERNAME)));
    }
	
	@Test
    public void testUsernameWithDotCharacter() throws Exception {
		IdmIdentityDto identity = new IdmIdentityDto();
		identity.setUsername("admin.com");
		identity.setFirstName("test");
		identity.setLastName("test");
		identity = getHelper().getService(IdmIdentityService.class).save(identity);
		//
		getMockMvc().perform(get(getDetailUrl(URLEncoder.encode(identity.getUsername(), "UTF-8")))
        		.with(authentication(getAdminAuthentication()))
                .contentType(InitTestData.HAL_CONTENT_TYPE))
                .andExpect(status().isOk())
                .andExpect(content().contentType(InitTestData.HAL_CONTENT_TYPE))
                .andExpect(jsonPath("$.username", equalTo(identity.getUsername())));
    }
	
	@Test
	@Ignore // TODO: surefire parameters are ignored ... why?
    public void testUsernameWithSlashCharacter() throws Exception {
		IdmIdentityDto identity = new IdmIdentityDto();
		identity.setUsername("admin/com");
		identity.setFirstName("test");
		identity.setLastName("test");
		identity = getHelper().getService(IdmIdentityService.class).save(identity);
		//
		getMockMvc().perform(get(getDetailUrl(URLEncoder.encode(identity.getUsername(), "UTF-8")))
        		.with(authentication(getAdminAuthentication()))
                .contentType(InitTestData.HAL_CONTENT_TYPE))
                .andExpect(status().isOk())
                .andExpect(content().contentType(InitTestData.HAL_CONTENT_TYPE))
                .andExpect(jsonPath("$.username", equalTo(identity.getUsername())));
    }
	
	@Test
	@Transactional
	public void testDisableEnable() throws Exception {
		IdmIdentityDto dto = createDto();
		Assert.assertFalse(dto.isDisabled());
		//
		getMockMvc().perform(patch(String.format("%s/disable", getDetailUrl(dto.getId())))
        		.with(authentication(getAdminAuthentication()))
                .contentType(TestHelper.HAL_CONTENT_TYPE))
				.andExpect(status().isOk())
                .andExpect(content().contentType(TestHelper.HAL_CONTENT_TYPE))
                .andExpect(jsonPath("$.disabled", equalTo(true)));
		Assert.assertTrue(getDto(dto.getId()).isDisabled());	
		//
		getMockMvc().perform(patch(String.format("%s/enable", getDetailUrl(dto.getId())))
        		.with(authentication(getAdminAuthentication()))
                .contentType(TestHelper.HAL_CONTENT_TYPE))
				.andExpect(status().isOk())
                .andExpect(content().contentType(TestHelper.HAL_CONTENT_TYPE))
                .andExpect(jsonPath("$.disabled", equalTo(false)));
		//
		Assert.assertFalse(getDto(dto.getId()).isDisabled());
	}
	
	@Test
	@Transactional
	public void testBulkDisable() {
		IdmIdentityDto identity = createDto();
		Assert.assertFalse(identity.isDisabled());
		// check disable operation is available
		List<IdmBulkActionDto> availableBulkActions = getAvailableBulkActions();
		//
		IdmBulkActionDto disableAction = availableBulkActions
				.stream()
				.filter(action -> action.getName().equals(IdentityDisableBulkAction.NAME))
				.findFirst()
				.orElse(null);
		Assert.assertNotNull(disableAction);
		//
		disableAction.getIdentifiers().add(identity.getId());
		//
		IdmBulkActionDto result = bulkAction(disableAction);
		//
		Assert.assertNotNull(result);
		Assert.assertTrue(getDto(identity.getId()).isDisabled());
	}
}
