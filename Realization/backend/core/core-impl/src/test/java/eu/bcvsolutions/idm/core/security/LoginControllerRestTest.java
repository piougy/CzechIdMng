package eu.bcvsolutions.idm.core.security;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import eu.bcvsolutions.idm.core.api.config.domain.RoleConfiguration;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTokenDto;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.IdmPasswordService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.security.api.domain.IdentityBasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmGroupPermission;
import eu.bcvsolutions.idm.core.security.api.dto.DefaultGrantedAuthorityDto;
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
	@Autowired private JwtAuthenticationMapper jwtTokenMapper;
	@Autowired private IdmIdentityService identityService;
	@Autowired private RoleConfiguration roleConfiguration;
	//
	private ObjectMapper mapper = new ObjectMapper();
	
	@Before
	public void init() {
		this.logout();
	}
	
	@After
	public void after() {
		this.logout();
	}

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
				.contentType(TestHelper.HAL_CONTENT_TYPE))
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
			.contentType(TestHelper.HAL_CONTENT_TYPE))
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
				.contentType(TestHelper.HAL_CONTENT_TYPE))
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
			.contentType(TestHelper.HAL_CONTENT_TYPE))
			.andExpect(status().isNoContent());
		//
		tokenDto = tokenManager.getToken(tokenId);
		Assert.assertTrue(tokenDto.isDisabled());
	}
	
	@Test
	public void testSwitchUser() throws Exception {
		IdmIdentityDto manager = getHelper().createIdentity();
		getHelper().createIdentityRole(manager, roleConfiguration.getAdminRole());
		//
		// login as manager		
		Map<String, String> login = new HashMap<>();
		login.put("username", manager.getUsername());
		login.put("password", manager.getPassword().asString());
		String response = getMockMvc()
				.perform(post(BaseController.BASE_PATH + "/authentication")
				.content(serialize(login))
				.contentType(TestHelper.HAL_CONTENT_TYPE))
				.andExpect(status().isOk())
				.andExpect(content().contentType(TestHelper.HAL_CONTENT_TYPE))
                .andReturn()
                .getResponse()
                .getContentAsString();
		UUID tokenId = getTokenId(response);
		String token = getToken(response);
		//
		Assert.assertNotNull(tokenId);
		IdmTokenDto tokenDto = tokenManager.getToken(tokenId);
		Assert.assertFalse(tokenDto.isDisabled());
		List<DefaultGrantedAuthorityDto> dtoAuthorities = jwtTokenMapper.getDtoAuthorities(tokenDto);
		//
		// check token authorities - APP_ADMIN
		Assert.assertTrue(dtoAuthorities.stream().anyMatch(a -> a.getAuthority().equals(IdmGroupPermission.APP_ADMIN)));
		//
		// create different identity - identity create
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmRoleDto role = getHelper().createRole();
		getHelper().createIdentityRole(identity, role);
		getHelper().createBasePolicy(role.getId(), CoreGroupPermission.IDENTITY, IdmIdentity.class, IdmBasePermission.ADMIN);
		response = getMockMvc()
				.perform(put(BaseController.BASE_PATH + "/authentication/switch-user?username=" + identity.getUsername())
						.param(IdmAuthenticationFilter.AUTHENTICATION_TOKEN_NAME, token))
				.andExpect(status().isOk())
				.andExpect(content().contentType(TestHelper.HAL_CONTENT_TYPE))
                .andReturn()
                .getResponse()
                .getContentAsString();
		//
		// preserve token id
		UUID switchTokenId = getTokenId(response);
		token = getToken(response);
		Assert.assertEquals(tokenId, switchTokenId);
		IdmTokenDto switchTokenDto = tokenManager.getToken(switchTokenId);
		Assert.assertFalse(switchTokenDto.isDisabled());
		dtoAuthorities = jwtTokenMapper.getDtoAuthorities(switchTokenDto);
		//
		// check authorities - no APP_ADMIN
		Assert.assertTrue(dtoAuthorities.stream().allMatch(a -> !a.getAuthority().equals(IdmGroupPermission.APP_ADMIN)));
		//
		// check token => same owner, same id, different username in properties
		Assert.assertEquals(tokenDto.getOwnerId(), switchTokenDto.getOwnerId());
		Assert.assertEquals(identity.getUsername(), switchTokenDto.getProperties().getString(JwtAuthenticationMapper.PROPERTY_CURRENT_USERNAME));
		Assert.assertEquals(manager.getUsername(), switchTokenDto.getProperties().getString(JwtAuthenticationMapper.PROPERTY_ORIGINAL_USERNAME));
		//
		// test create identity with switched token + check audit fields
		IdmIdentityDto createIdentity = new IdmIdentityDto(getHelper().createName());
		getMockMvc()
				.perform(post(BaseController.BASE_PATH + "/identities")
						.param(IdmAuthenticationFilter.AUTHENTICATION_TOKEN_NAME, token)
						.content(mapper.writeValueAsString(createIdentity))
						.contentType(TestHelper.HAL_CONTENT_TYPE))
				.andExpect(status().isCreated())
				.andExpect(content().contentType(TestHelper.HAL_CONTENT_TYPE))
	            .andReturn()
	            .getResponse()
	            .getContentAsString();
		IdmIdentityDto createdIdentity = identityService.getByUsername(createIdentity.getUsername());
		Assert.assertEquals(manager.getUsername(), createdIdentity.getOriginalCreator());
		Assert.assertEquals(manager.getId(), createdIdentity.getOriginalCreatorId());
		Assert.assertEquals(identity.getUsername(), createdIdentity.getCreator());
		Assert.assertEquals(identity.getId(), createdIdentity.getCreatorId());
		//
		// rename identity - use id in logout phase
		manager.setUsername(getHelper().createName());
		manager = identityService.save(manager);
		//
		// switch logout => test token, authorities
		response = getMockMvc()
				.perform(delete(BaseController.BASE_PATH + "/authentication/switch-user")
						.param(IdmAuthenticationFilter.AUTHENTICATION_TOKEN_NAME, token))
				.andExpect(status().isOk())
				.andExpect(content().contentType(TestHelper.HAL_CONTENT_TYPE))
                .andReturn()
                .getResponse()
                .getContentAsString();
		tokenId = getTokenId(response);
		token = getToken(response);
		//
		Assert.assertNotNull(tokenId);
		tokenDto = tokenManager.getToken(tokenId);
		Assert.assertFalse(tokenDto.isDisabled());
		dtoAuthorities = jwtTokenMapper.getDtoAuthorities(tokenDto);
		//
		// check token authorities - APP_ADMIN
		Assert.assertTrue(dtoAuthorities.stream().anyMatch(a -> a.getAuthority().equals(IdmGroupPermission.APP_ADMIN)));
		Assert.assertEquals(tokenDto.getOwnerId(), switchTokenDto.getOwnerId());
		Assert.assertEquals(manager.getUsername(), tokenDto.getProperties().getString(JwtAuthenticationMapper.PROPERTY_CURRENT_USERNAME));
		Assert.assertEquals(manager.getUsername(), tokenDto.getProperties().getString(JwtAuthenticationMapper.PROPERTY_ORIGINAL_USERNAME));
	}
	
	@Test
	public void testSwitchWrongUser() throws Exception {
		// login as admin		
		Map<String, String> login = new HashMap<>();
		login.put("username", TestHelper.ADMIN_USERNAME);
		login.put("password", TestHelper.ADMIN_PASSWORD);
		String response = getMockMvc()
				.perform(post(BaseController.BASE_PATH + "/authentication")
				.content(serialize(login))
				.contentType(TestHelper.HAL_CONTENT_TYPE))
				.andExpect(status().isOk())
				.andExpect(content().contentType(TestHelper.HAL_CONTENT_TYPE))
                .andReturn()
                .getResponse()
                .getContentAsString();
		String token = getToken(response);
		//
		getMockMvc()
			.perform(put(BaseController.BASE_PATH + "/authentication/switch-user?username=" + getHelper().createName())
					.param(IdmAuthenticationFilter.AUTHENTICATION_TOKEN_NAME, token))
			.andExpect(status().isNotFound());
	}
	
	/**
	 * Login as user without SWITCHUSER permission to all user - just subordinateOne.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testSwitchWithoutPermission() throws Exception {
		IdmIdentityDto manager = getHelper().createIdentity();
		IdmIdentityDto subordinateOne = getHelper().createIdentity((GuardedString) null);
		IdmIdentityDto otherIdentity = getHelper().createIdentity((GuardedString) null);
		//
		IdmRoleDto managerRole = getHelper().createRole();
		getHelper().createUuidPolicy(managerRole, subordinateOne, IdentityBasePermission.SWITCHUSER);
		getHelper().createIdentityRole(manager, managerRole);
		//
		// login
		Map<String, String> login = new HashMap<>();
		login.put("username", manager.getUsername());
		login.put("password", manager.getPassword().asString());
		String response = getMockMvc()
				.perform(post(BaseController.BASE_PATH + "/authentication")
				.content(serialize(login))
				.contentType(TestHelper.HAL_CONTENT_TYPE))
				.andExpect(status().isOk())
				.andExpect(content().contentType(TestHelper.HAL_CONTENT_TYPE))
                .andReturn()
                .getResponse()
                .getContentAsString();
		String token = getToken(response);
		//
		// cannot switch as other identity
		getMockMvc()
			.perform(put(BaseController.BASE_PATH + "/authentication/switch-user?username=" + otherIdentity.getUsername())
					.param(IdmAuthenticationFilter.AUTHENTICATION_TOKEN_NAME, token))
			.andExpect(status().isForbidden());
		//
		// can switch as subordinate
		getMockMvc()
		.perform(put(BaseController.BASE_PATH + "/authentication/switch-user?username=" + subordinateOne.getUsername())
				.param(IdmAuthenticationFilter.AUTHENTICATION_TOKEN_NAME, token))
		.andExpect(status().isOk());
		//
		logout();
	}
	
	private ResultActions tryLogin(String username, String password) throws Exception {
		Map<String, String> login = new HashMap<>();
		login.put("username", username);
		login.put("password", "jkasldjkh");
		return getMockMvc()
				.perform(post(BaseController.BASE_PATH + "/authentication")
				.content(serialize(login))
				.contentType(TestHelper.HAL_CONTENT_TYPE));
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
