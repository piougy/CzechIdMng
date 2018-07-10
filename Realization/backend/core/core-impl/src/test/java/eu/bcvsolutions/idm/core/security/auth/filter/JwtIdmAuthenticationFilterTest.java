package eu.bcvsolutions.idm.core.security.auth.filter;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;

import org.joda.time.DateTime;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.security.api.dto.IdmJwtAuthenticationDto;
import eu.bcvsolutions.idm.core.security.api.dto.LoginDto;
import eu.bcvsolutions.idm.core.security.api.filter.IdmAuthenticationFilter;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;
import eu.bcvsolutions.idm.core.security.service.impl.JwtAuthenticationMapper;
import eu.bcvsolutions.idm.test.api.AbstractRestTest;
import eu.bcvsolutions.idm.test.api.TestHelper;

/**
 * JWT authentication test.
 * 
 * @author Jan Helbich
 * @author Radek Tomiška
 */
@Transactional
public class JwtIdmAuthenticationFilterTest extends AbstractRestTest {
	
	@Autowired private JwtAuthenticationMapper jwtMapper;
	@Autowired private SecurityService securityService;

	@Test
	public void testAuthSuccess() throws Exception {
		LoginDto login = getHelper().loginAdmin();
		securityService.logout(); // not disable token
		//
		getMockMvc().perform(get(getSelfPath(TestHelper.ADMIN_USERNAME))
				.header(JwtAuthenticationMapper.AUTHENTICATION_TOKEN_NAME, login.getToken())
				.contentType(TestHelper.HAL_CONTENT_TYPE))
			.andExpect(status().isOk())
			.andExpect(content().contentType(TestHelper.HAL_CONTENT_TYPE))
			.andExpect(jsonPath("$.username", equalTo(TestHelper.ADMIN_USERNAME)));
	}
	
	@Test
	public void testLogoutSuccess() throws Exception {
		LoginDto login = getHelper().loginAdmin();
		logout(); // disable token
		//
		getMockMvc().perform(get(getSelfPath(TestHelper.ADMIN_USERNAME))
				.header(JwtAuthenticationMapper.AUTHENTICATION_TOKEN_NAME, login.getToken())
				.contentType(TestHelper.HAL_CONTENT_TYPE))
			.andExpect(status().isUnauthorized());
	}
	
	@Test
	public void testAuthSuccessWithUrlToken() throws Exception {
		LoginDto login = getHelper().loginAdmin();
		securityService.logout();
		//		
		getMockMvc().perform(get(getSelfPath(TestHelper.ADMIN_USERNAME))
				.param(IdmAuthenticationFilter.AUTHENTICATION_TOKEN_NAME, login.getToken())
				.contentType(TestHelper.HAL_CONTENT_TYPE))
			.andExpect(status().isOk())
			.andExpect(content().contentType(TestHelper.HAL_CONTENT_TYPE))
			.andExpect(jsonPath("$.username", equalTo(TestHelper.ADMIN_USERNAME)));
	}

	@Test
	public void testTokenModified() throws Exception {
		LoginDto login = getHelper().loginAdmin();
		securityService.logout();
		String tokenOriginal = login.getToken();
		
		// mix two different tokens - payload from second, signature from first
		IdmJwtAuthenticationDto authDto = login.getAuthentication();
		authDto.setExpiration(DateTime.now().plus(10000000));
		String[] token2Split = getAuthToken(authDto).split("\\.");
		String[] tokenOrigSplit = tokenOriginal.split("\\.");
		
		String token = token2Split[0] + "." + token2Split[1] + "." + tokenOrigSplit[2];
		getMockMvc().perform(get(getSelfPath(TestHelper.ADMIN_USERNAME))
				.header(JwtAuthenticationMapper.AUTHENTICATION_TOKEN_NAME, token)
				.contentType(TestHelper.HAL_CONTENT_TYPE))
			.andExpect(status().is(403));
	}
	
	protected String getAuthToken(IdmJwtAuthenticationDto d) throws IOException {
		return jwtMapper.writeToken(d);
	}
	
	private String getSelfPath(String user) {
		return BaseDtoController.BASE_PATH + "/identities/" + user;
	}
}
