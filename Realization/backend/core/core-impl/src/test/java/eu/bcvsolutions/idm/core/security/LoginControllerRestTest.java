package eu.bcvsolutions.idm.core.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.IdmPasswordService;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.security.api.dto.LoginDto;
import eu.bcvsolutions.idm.core.security.rest.impl.LoginController;
import eu.bcvsolutions.idm.test.api.AbstractRestTest;
import eu.bcvsolutions.idm.test.api.TestHelper;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.ResultActions;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Password service integration test.
 *
 * @author Petr Hanák
 */
public class LoginControllerRestTest extends AbstractRestTest {

	@Autowired
	private IdmPasswordService passwordService;
	@Autowired private TestHelper testHelper;
	@Autowired private IdmIdentityService identityService;
	@Autowired private LoginController loginController;

	@Test
	public void testFailLoginCounter() throws Exception {
		IdmIdentityDto identity = testHelper.createIdentity();
		identity.setUsername("Hanka");
		identity.setPassword(new GuardedString("SafePassword"));
		identityService.save(identity);

		// Unsuccessful attempts
		tryLogin("Hanka", "hgjgjh").andExpect(status().is(HttpStatus.UNAUTHORIZED.value()));
		tryLogin("Hanka", "hgjgjh").andExpect(status().is(HttpStatus.UNAUTHORIZED.value()));
		tryLogin("Hanka", "hgjgjh").andExpect(status().is(HttpStatus.UNAUTHORIZED.value()));

		assertEquals(3, passwordService.findOneByIdentity("Hanka").getUnsuccessfulAttempts());

		// Successful attempt
		LoginDto loginDto = new LoginDto();
		loginDto.setUsername("Hanka");
		loginDto.setPassword(new GuardedString("SafePassword"));
		Resource<LoginDto> response = loginController.login(loginDto);

		System.out.println(passwordService.findOneByIdentity("Hanka").getLastSuccessfulLogin());
		assertEquals(0, passwordService.findOneByIdentity("Hanka").getUnsuccessfulAttempts());
	}


	private ResultActions tryLogin(String username, String password) throws Exception {
		Map<String, String> login = new HashMap<>();
		login.put("username", "Hanka");
		login.put("password", "jkasldjkh");
		return getMockMvc()
				.perform(post(BaseController.BASE_PATH + "/authentication")
				.content(serialize(login))
				.contentType(InitTestData.HAL_CONTENT_TYPE));
	}

	private String serialize(Map<String,String> login) throws IOException {
		ObjectMapper m = new ObjectMapper();
		StringWriter sw = new StringWriter();
		ObjectWriter writer = m.writerFor(HashMap.class);
		writer.writeValue(sw, login);
		System.out.println(sw.toString());
		return sw.toString();
	}
}
