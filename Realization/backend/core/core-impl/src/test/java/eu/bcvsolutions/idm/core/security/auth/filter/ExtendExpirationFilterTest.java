package eu.bcvsolutions.idm.core.security.auth.filter;

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
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.api.dto.IdmTokenDto;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.api.service.IdmTokenService;
import eu.bcvsolutions.idm.core.security.api.dto.IdmJwtAuthenticationDto;
import eu.bcvsolutions.idm.core.security.api.dto.LoginDto;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;
import eu.bcvsolutions.idm.core.security.service.impl.JwtAuthenticationMapper;
import eu.bcvsolutions.idm.test.api.AbstractRestTest;
import eu.bcvsolutions.idm.test.api.TestHelper;

/**
 * Tests authentication token expiration time extension.
 * 
 * @author Jan Helbich
 * @author Radek Tomiška
 */
@Transactional
public class ExtendExpirationFilterTest extends AbstractRestTest {

	@Autowired private JwtAuthenticationMapper jwtMapper;
	@Autowired private IdmTokenService tokenService; 
	@Autowired private SecurityService securityService; 
	
	/**
	 * Token is not prolonged in the same minute
	 * 
	 * @throws Exception
	 */
	@Test
	public void testReuseTokenExtension() throws Exception {
		LoginDto login = getHelper().loginAdmin();
		securityService.logout();
		//
		MvcResult result = getMockMvc().perform(get(getSelfPath(TestHelper.ADMIN_USERNAME))
				.header(JwtAuthenticationMapper.AUTHENTICATION_TOKEN_NAME, login.getToken())
				.contentType(TestHelper.HAL_CONTENT_TYPE))
			.andExpect(status().isOk())
			.andExpect(content().contentType(TestHelper.HAL_CONTENT_TYPE))
			.andExpect(jsonPath("$.username", equalTo(TestHelper.ADMIN_USERNAME)))
			.andReturn();
		
		IdmJwtAuthenticationDto extendedDto = getIdmJwtDto(result);
		
		Assert.assertEquals(login.getAuthentication().getId(), extendedDto.getId());
		Assert.assertEquals(login.getToken(), result.getResponse().getHeader(JwtAuthenticationMapper.AUTHENTICATION_TOKEN_NAME));
		Assert.assertEquals(login.getAuthentication().getIssuedAt().getMillis(), extendedDto.getIssuedAt().getMillis());
		Assert.assertEquals(login.getAuthentication().getExpiration().getMillis(), extendedDto.getExpiration().getMillis());
	}
	
	/**
	 * Token is prolonged, when original expiration differs more than 60 seconds
	 * @throws Exception
	 */
	@Test
	public void testSuccessfulTokenExtension() throws Exception {
		LoginDto login = getHelper().loginAdmin();
		securityService.logout();
		//
		IdmTokenDto originalToken = tokenService.get(login.getAuthentication().getId());
		originalToken.setExpiration(originalToken.getExpiration().minusMinutes(2));
		originalToken = tokenService.save(originalToken);
		//
		MvcResult result = getMockMvc().perform(get(getSelfPath(TestHelper.ADMIN_USERNAME))
				.header(JwtAuthenticationMapper.AUTHENTICATION_TOKEN_NAME, login.getToken())
				.contentType(TestHelper.HAL_CONTENT_TYPE))
			.andExpect(status().isOk())
			.andExpect(content().contentType(TestHelper.HAL_CONTENT_TYPE))
			.andExpect(jsonPath("$.username", equalTo(TestHelper.ADMIN_USERNAME)))
			.andReturn();
		
		IdmJwtAuthenticationDto extended = getIdmJwtDto(result);
		
		Assert.assertEquals(originalToken.getOwnerId(), extended.getCurrentIdentityId());
		Assert.assertEquals(originalToken.getIssuedAt().getMillis(), extended.getIssuedAt().getMillis());
		
		// token expiration - orignal exp. time is lower or equal to new one 
		Assert.assertTrue(originalToken.getExpiration().getMillis() < extended.getExpiration().getMillis());
	}

	@Test
	public void testSuccBasicAuthNoExtension() throws Exception {
		String basicAuth = getBasicAuth(TestHelper.ADMIN_USERNAME, TestHelper.ADMIN_PASSWORD);
		
		MvcResult result = getMockMvc().perform(get(getSelfPath(TestHelper.ADMIN_USERNAME))
				.header("Authorization", "Basic " + basicAuth)
				.contentType(TestHelper.HAL_CONTENT_TYPE))
			.andExpect(status().isOk())
			.andExpect(content().contentType(TestHelper.HAL_CONTENT_TYPE))
			.andExpect(jsonPath("$.username", equalTo(TestHelper.ADMIN_USERNAME)))
			.andReturn();
		
		Assert.assertNull(result.getResponse().getHeader(JwtAuthenticationMapper.AUTHENTICATION_TOKEN_NAME));
	}
	
	@Test
	public void testReuseBasicAuthTokenExtension() throws Exception {
		LoginDto login = getHelper().loginAdmin();
		securityService.logout();
		//
		String basicAuth = getBasicAuth(TestHelper.ADMIN_USERNAME, TestHelper.ADMIN_PASSWORD);

		MvcResult result = getMockMvc().perform(get(getSelfPath(TestHelper.ADMIN_USERNAME))
				.header("Authorization", "Basic " + basicAuth)
				.header(JwtAuthenticationMapper.AUTHENTICATION_TOKEN_NAME, login.getToken())
				.contentType(TestHelper.HAL_CONTENT_TYPE))
			.andExpect(status().isOk())
			.andExpect(content().contentType(TestHelper.HAL_CONTENT_TYPE))
			.andExpect(jsonPath("$.username", equalTo(TestHelper.ADMIN_USERNAME)))
			.andReturn();
		
		IdmJwtAuthenticationDto extendedDto = getIdmJwtDto(result);
		
		Assert.assertEquals(login.getAuthentication().getIssuedAt().getMillis(), extendedDto.getIssuedAt().getMillis());
		Assert.assertEquals(login.getAuthentication().getExpiration().getMillis(), extendedDto.getExpiration().getMillis());
	}

	@Test
	public void testSuccBasicAuthTokenExtension() throws Exception {
		LoginDto login = getHelper().loginAdmin();
		securityService.logout();
		//
		String basicAuth = getBasicAuth(TestHelper.ADMIN_USERNAME, TestHelper.ADMIN_PASSWORD);
		//
		IdmTokenDto originalToken = tokenService.get(login.getAuthentication().getId());
		originalToken.setExpiration(originalToken.getExpiration().minusMinutes(2));
		originalToken = tokenService.save(originalToken);
		//
		MvcResult result = getMockMvc().perform(get(getSelfPath(TestHelper.ADMIN_USERNAME))
				.header("Authorization", "Basic " + basicAuth)
				.header(JwtAuthenticationMapper.AUTHENTICATION_TOKEN_NAME, login.getToken())
				.contentType(TestHelper.HAL_CONTENT_TYPE))
			.andExpect(status().isOk())
			.andExpect(content().contentType(TestHelper.HAL_CONTENT_TYPE))
			.andExpect(jsonPath("$.username", equalTo(TestHelper.ADMIN_USERNAME)))
			.andReturn();
		
		IdmJwtAuthenticationDto extended = getIdmJwtDto(result);
		
		Assert.assertEquals(originalToken.getOwnerId(), extended.getCurrentIdentityId());
		Assert.assertEquals(originalToken.getIssuedAt().getMillis(), extended.getIssuedAt().getMillis());
		
		// token expiration - orignal exp. time is lower or equal to new one 
		Assert.assertTrue(originalToken.getExpiration().getMillis() < extended.getExpiration().getMillis());
	}

	private IdmJwtAuthenticationDto getIdmJwtDto(MvcResult result) throws IOException {
		String extended = result.getResponse().getHeader(JwtAuthenticationMapper.AUTHENTICATION_TOKEN_NAME);
		Assert.assertNotNull(extended);
		Jwt decoded = JwtHelper.decode(extended);
		decoded.verifySignature(jwtMapper.getVerifier());

		IdmJwtAuthenticationDto extendedDto = jwtMapper.getClaims(decoded);
		return extendedDto;
	}
	
	private String getSelfPath(String user) {
		return BaseDtoController.BASE_PATH + "/identities/" + user;
	}
}
