package eu.bcvsolutions.idm.core.security.auth.filter;

import static eu.bcvsolutions.idm.InitTestData.HAL_CONTENT_TYPE;
import static eu.bcvsolutions.idm.InitTestData.TEST_ADMIN_PASSWORD;
import static eu.bcvsolutions.idm.InitTestData.TEST_ADMIN_USERNAME;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityService;
import eu.bcvsolutions.idm.core.security.api.dto.IdmJwtAuthenticationDto;
import eu.bcvsolutions.idm.core.security.api.utils.IdmAuthorityUtils;
import eu.bcvsolutions.idm.core.security.service.impl.JwtAuthenticationMapper;
import eu.bcvsolutions.idm.test.api.AbstractRestTest;
import eu.bcvsolutions.idm.test.api.utils.AuthenticationTestUtils;

/**
 * Tests authentication token expiration time extension.
 * @author Jan Helbich
 *
 */
public class ExtendExpirationFilterTest extends AbstractRestTest {
	
	@Autowired protected JwtAuthenticationMapper jwtMapper;
	@Autowired private IdmIdentityService identityService;

	@Test
	public void testSuccessfulTokenExtension() throws Exception {
		IdmJwtAuthenticationDto authDto = AuthenticationTestUtils.getAuthDto(identityService.getByUsername(TEST_ADMIN_USERNAME),
				Lists.newArrayList(IdmAuthorityUtils.getAdminAuthority()));
		String token = getAuthToken(authDto);
		
		sleep();
		
		MvcResult result = getMockMvc().perform(get(AuthenticationTestUtils.getSelfPath(TEST_ADMIN_USERNAME))
				.header(JwtAuthenticationMapper.AUTHENTICATION_TOKEN_NAME, token)
				.contentType(HAL_CONTENT_TYPE))
			.andExpect(status().isOk())
			.andExpect(content().contentType(HAL_CONTENT_TYPE))
			.andExpect(jsonPath("$.username", equalTo(TEST_ADMIN_USERNAME)))
			.andReturn();
		
		IdmJwtAuthenticationDto extendedDto = getIdmJwtDto(result);
		checkSuccessfulTokenExtension(authDto, extendedDto);
	}

	@Test
	public void testSuccBasicAuthNoExtension() throws Exception {
		String basicAuth = AuthenticationTestUtils.getBasicAuth(TEST_ADMIN_USERNAME, TEST_ADMIN_PASSWORD);
		
		MvcResult result = getMockMvc().perform(get(AuthenticationTestUtils.getSelfPath(TEST_ADMIN_USERNAME))
				.header("Authorization", "Basic " + basicAuth)
				.contentType(HAL_CONTENT_TYPE))
			.andExpect(status().isOk())
			.andExpect(content().contentType(HAL_CONTENT_TYPE))
			.andExpect(jsonPath("$.username", equalTo(TEST_ADMIN_USERNAME)))
			.andReturn();
		
		Assert.assertNull(result.getResponse().getHeader(JwtAuthenticationMapper.AUTHENTICATION_TOKEN_NAME));
	}
	
	@Test
	public void testSuccBasicAuthTokenExtension() throws Exception {
		IdmJwtAuthenticationDto authDto = AuthenticationTestUtils.getAuthDto(identityService.getByUsername(TEST_ADMIN_USERNAME),
				Lists.newArrayList(IdmAuthorityUtils.getAdminAuthority()));
		String token = getAuthToken(authDto);
		String basicAuth = AuthenticationTestUtils.getBasicAuth(TEST_ADMIN_USERNAME, TEST_ADMIN_PASSWORD);
		
		sleep();
		
		MvcResult result = getMockMvc().perform(get(AuthenticationTestUtils.getSelfPath(TEST_ADMIN_USERNAME))
				.header("Authorization", "Basic " + basicAuth)
				.header(JwtAuthenticationMapper.AUTHENTICATION_TOKEN_NAME, token)
				.contentType(HAL_CONTENT_TYPE))
			.andExpect(status().isOk())
			.andExpect(content().contentType(HAL_CONTENT_TYPE))
			.andExpect(jsonPath("$.username", equalTo(TEST_ADMIN_USERNAME)))
			.andReturn();
		
		IdmJwtAuthenticationDto extendedDto = getIdmJwtDto(result);
		checkSuccessfulTokenExtension(authDto, extendedDto);
	}

	private void checkSuccessfulTokenExtension(IdmJwtAuthenticationDto original,
			IdmJwtAuthenticationDto extended) {
		Assert.assertEquals(original.getCurrentUsername(), extended.getCurrentUsername());
		Assert.assertEquals(original.getOriginalUsername(), extended.getOriginalUsername());
		Assert.assertEquals(original.getAuthorities(), extended.getAuthorities());
		Assert.assertEquals(original.getCurrentIdentityId(), extended.getCurrentIdentityId());
		Assert.assertEquals(original.getIssuedAt().getMillis(), extended.getIssuedAt().getMillis());
		
		// token expiration - orignal exp. time is lower or equal to new one 
		Assert.assertTrue(original.getExpiration().getMillis() < extended.getExpiration().getMillis());
	}

	private IdmJwtAuthenticationDto getIdmJwtDto(MvcResult result)
			throws JsonParseException, JsonMappingException, IOException {
		String extended = result.getResponse().getHeader(JwtAuthenticationMapper.AUTHENTICATION_TOKEN_NAME);
		Assert.assertNotNull(extended);
		Jwt decoded = JwtHelper.decode(extended);
		decoded.verifySignature(jwtMapper.getVerifier());

		IdmJwtAuthenticationDto extendedDto = jwtMapper.getClaims(decoded);
		return extendedDto;
	}

	
	private String getAuthToken(IdmJwtAuthenticationDto d) throws IOException {
		return jwtMapper.writeToken(d);
	}

	private void sleep() throws InterruptedException {
		// simulation of time passed between requests
		Thread.sleep(10);
	}
}
