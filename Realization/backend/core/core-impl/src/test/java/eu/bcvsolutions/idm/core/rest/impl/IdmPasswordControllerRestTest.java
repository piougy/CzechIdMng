package eu.bcvsolutions.idm.core.rest.impl;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmPasswordDto;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoControllerRestTest;
import eu.bcvsolutions.idm.core.api.service.IdmPasswordService;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.test.api.TestHelper;

/**
 * Rest tests for password controller {@link IdmPasswordController}.
 *
 * @author Ondrej Kopr
 *
 */
public class IdmPasswordControllerRestTest extends AbstractReadWriteDtoControllerRestTest<IdmPasswordDto> {

	public static final GuardedString DEFAULT_PASSWORD = new GuardedString("password123*");

	@Autowired
	private IdmPasswordController controller;
	@Autowired
	private IdmPasswordService passwordService;
	
	@Override
	protected AbstractReadWriteDtoController<IdmPasswordDto, ?> getController() {
		return controller;
	}

	@Override
	protected IdmPasswordDto prepareDto() {
		IdmIdentityDto identity = getHelper().createIdentity(DEFAULT_PASSWORD);
		return passwordService.findOneByIdentity(identity.getId());
	}

	@Override
	protected boolean supportsAutocomplete() {
		return false;
	}

	@Override
	protected boolean supportsDelete() {
		return false;
	}

	@Override
	public void testPost() throws Exception {
		IdmPasswordDto dto = prepareDto();
		ObjectMapper mapper = getMapper();

		String response = getMockMvc().perform(post(getBaseUrl())
        		.with(authentication(getAdminAuthentication()))
        		.content(mapper.writeValueAsString(dto))
                .contentType(TestHelper.HAL_CONTENT_TYPE))
                .andExpect(content().contentType(TestHelper.HAL_CONTENT_TYPE))
                .andReturn()
                .getResponse()
                .getContentAsString();
		IdmPasswordDto password = (IdmPasswordDto) mapper.readValue(response, dto.getClass());
		Assert.assertNotNull(password);
		Assert.assertNotNull(password.getId());

		password = getDto(password.getId());
		Assert.assertNotNull(password.getCreator());
		Assert.assertNull(password.getPassword());

		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmPasswordDto newPassword = new IdmPasswordDto();
		newPassword.setIdentity(identity.getId());
		newPassword.setPassword("testPassword");

		// Create new password
		getMockMvc().perform(post(getBaseUrl())
        		.with(authentication(getAdminAuthentication()))
        		.content(mapper.writeValueAsString(newPassword))
                .contentType(TestHelper.HAL_CONTENT_TYPE))
				.andExpect(status().isBadRequest());
	}
}
