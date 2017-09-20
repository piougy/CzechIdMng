package eu.bcvsolutions.idm.core.security.auth.filter;

import static eu.bcvsolutions.idm.InitTestData.HAL_CONTENT_TYPE;
import static eu.bcvsolutions.idm.InitTestData.TEST_ADMIN_USERNAME;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;

import org.joda.time.DateTime;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.security.api.dto.IdmJwtAuthenticationDto;
import eu.bcvsolutions.idm.core.security.api.utils.IdmAuthorityUtils;
import eu.bcvsolutions.idm.core.security.service.impl.JwtAuthenticationMapper;
import eu.bcvsolutions.idm.test.api.AbstractRestTest;
import eu.bcvsolutions.idm.test.api.utils.AuthenticationTestUtils;

/**
 * JWT authentication test.
 * @author Jan Helbich
 *
 */
public class JwtIdmAuthenticationFilterTest extends AbstractRestTest {
	
	@Autowired protected JwtAuthenticationMapper jwtMapper;
	@Autowired private IdmIdentityService identityService;
	
	@Autowired
	@Qualifier("objectMapper")
	protected ObjectMapper mapper;

	@Test
	public void testAuthSuccess() throws Exception {
		String token = getAuthToken(AuthenticationTestUtils.getAuthDto(identityService.getByUsername(TEST_ADMIN_USERNAME),
				Lists.newArrayList(IdmAuthorityUtils.getAdminAuthority())));
		
		getMockMvc().perform(get(AuthenticationTestUtils.getSelfPath(TEST_ADMIN_USERNAME))
				.header(JwtAuthenticationMapper.AUTHENTICATION_TOKEN_NAME, token)
				.contentType(HAL_CONTENT_TYPE))
			.andExpect(status().isOk())
			.andExpect(content().contentType(HAL_CONTENT_TYPE))
			.andExpect(jsonPath("$.username", equalTo(TEST_ADMIN_USERNAME)));
	}

	@Test
	public void testTokenModified() throws Exception {
		IdmJwtAuthenticationDto authDto = AuthenticationTestUtils.getAuthDto(identityService.getByUsername(TEST_ADMIN_USERNAME),
				Lists.newArrayList(IdmAuthorityUtils.getAdminAuthority()));
		String tokenOriginal = getAuthToken(authDto);
		
		// mix two different tokens - payload from second, signature from first
		authDto.setExpiration(DateTime.now().plus(10000000));
		String[] token2Split = getAuthToken(authDto).split("\\.");
		String[] tokenOrigSplit = tokenOriginal.split("\\.");
		
		String token = token2Split[0] + "." + token2Split[1] + "." + tokenOrigSplit[2];
		getMockMvc().perform(get(AuthenticationTestUtils.getSelfPath(TEST_ADMIN_USERNAME))
				.header(JwtAuthenticationMapper.AUTHENTICATION_TOKEN_NAME, token)
				.contentType(HAL_CONTENT_TYPE))
			.andExpect(status().is(403));
	}
	
	protected String getAuthToken(IdmJwtAuthenticationDto d) throws IOException {
		return jwtMapper.writeToken(d);
	}

}
