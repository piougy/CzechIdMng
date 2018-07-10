package eu.bcvsolutions.idm.core.security;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTokenDto;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.service.IdmPasswordService;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.security.api.dto.LoginDto;
import eu.bcvsolutions.idm.core.security.api.filter.IdmAuthenticationFilter;
import eu.bcvsolutions.idm.core.security.api.service.TokenManager;
import eu.bcvsolutions.idm.core.security.rest.impl.LoginController;
import eu.bcvsolutions.idm.core.security.service.impl.JwtAuthenticationMapper;
import eu.bcvsolutions.idm.test.api.AbstractRestTest;
import eu.bcvsolutions.idm.test.api.TestHelper;

/**
 * Password service integration test.
 *
 * @author Petr Hanák
 * @author Radek Tomiška
 */
@Transactional
public class LoginControllerRestTest extends AbstractRestTest {

	@Autowired private IdmPasswordService passwordService;
	@Autowired private LoginController loginController;
	@Autowired private TokenManager tokenManager;
	//
	private ObjectMapper mapper = new ObjectMapper();

	@Test
	public void testFailLoginCounter() throws Exception {
		IdmIdentityDto identity = getHelper().createIdentity(new GuardedString("SafePassword"));
		
		// Unsuccessful attempts
		tryLogin(identity.getUsername(), "hgjgjh").andExpect(status().is(HttpStatus.UNAUTHORIZED.value()));
		tryLogin(identity.getUsername(), "hgjgjh").andExpect(status().is(HttpStatus.UNAUTHORIZED.value()));
		tryLogin(identity.getUsername(), "hgjgjh").andExpect(status().is(HttpStatus.UNAUTHORIZED.value()));

		assertEquals(3, passwordService.findOneByIdentity(identity.getUsername()).getUnsuccessfulAttempts());

		// Successful attempt
		LoginDto loginDto = new LoginDto();
		loginDto.setUsername(identity.getUsername());
		loginDto.setPassword(identity.getPassword());
		loginController.login(loginDto);
		//
		assertEquals(0, passwordService.findOneByIdentity(identity.getUsername()).getUnsuccessfulAttempts());
		//
		logout();
	}
	
	@Test
	public void testLogoutWithHeader() throws Exception {
		IdmIdentityDto identity = getHelper().createIdentity();
		
		Map<String, String> login = new HashMap<>();
		login.put("username", identity.getUsername());
		login.put("password", identity.getPassword().asString());
		String response = getMockMvc()
				.perform(post(BaseController.BASE_PATH + "/authentication")
				.content(serialize(login))
				.contentType(InitTestData.HAL_CONTENT_TYPE))
				.andExpect(status().isOk())
				.andExpect(content().contentType(TestHelper.HAL_CONTENT_TYPE))
                .andReturn()
                .getResponse()
                .getContentAsString();
		UUID tokenId = getTokenId(response);
		String token = getToken(response);
		//
		Assert.assertNotNull(tokenId);
		//
		IdmTokenDto tokenDto = tokenManager.getToken(tokenId);
		Assert.assertFalse(tokenDto.isDisabled());
		//
		getMockMvc()
			.perform(delete(BaseController.BASE_PATH + "/logout")
			.header(JwtAuthenticationMapper.AUTHENTICATION_TOKEN_NAME, token)
			.contentType(InitTestData.HAL_CONTENT_TYPE))
			.andExpect(status().isNoContent());
		//
		tokenDto = tokenManager.getToken(tokenId);
		Assert.assertTrue(tokenDto.isDisabled());
	}
	
	@Test
	public void testLogoutWithParameter() throws Exception {
		IdmIdentityDto identity = getHelper().createIdentity();
		
		Map<String, String> login = new HashMap<>();
		login.put("username", identity.getUsername());
		login.put("password", identity.getPassword().asString());
		String response = getMockMvc()
				.perform(post(BaseController.BASE_PATH + "/authentication")
				.content(serialize(login))
				.contentType(InitTestData.HAL_CONTENT_TYPE))
				.andExpect(status().isOk())
				.andExpect(content().contentType(TestHelper.HAL_CONTENT_TYPE))
                .andReturn()
                .getResponse()
                .getContentAsString();
		UUID tokenId = getTokenId(response);
		String token = getToken(response);
		//
		Assert.assertNotNull(tokenId);
		//
		IdmTokenDto tokenDto = tokenManager.getToken(tokenId);
		Assert.assertFalse(tokenDto.isDisabled());
		//
		getMockMvc()
			.perform(delete(BaseController.BASE_PATH + "/logout")
					.param(IdmAuthenticationFilter.AUTHENTICATION_TOKEN_NAME, token)
			.contentType(InitTestData.HAL_CONTENT_TYPE))
			.andExpect(status().isNoContent());
		//
		tokenDto = tokenManager.getToken(tokenId);
		Assert.assertTrue(tokenDto.isDisabled());
	}
	
	private ResultActions tryLogin(String username, String password) throws Exception {
		Map<String, String> login = new HashMap<>();
		login.put("username", username);
		login.put("password", "jkasldjkh");
		return getMockMvc()
				.perform(post(BaseController.BASE_PATH + "/authentication")
				.content(serialize(login))
				.contentType(InitTestData.HAL_CONTENT_TYPE));
	}

	private String serialize(Map<String,String> login) throws IOException {
		StringWriter sw = new StringWriter();
		ObjectWriter writer = mapper.writerFor(HashMap.class);
		writer.writeValue(sw, login);
		//
		return sw.toString();
	}
	
	private UUID getTokenId(String response) throws Exception {
		return UUID.fromString(mapper.readTree(response).get("authentication").get("id").asText());
	}
	
	private String getToken(String response) throws Exception {
		return mapper.readTree(response).get("token").asText();
	}
}
