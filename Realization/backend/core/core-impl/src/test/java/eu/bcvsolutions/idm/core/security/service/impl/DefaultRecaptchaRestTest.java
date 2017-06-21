package eu.bcvsolutions.idm.core.security.service.impl;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;

import java.io.IOException;
import java.io.StringWriter;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.common.collect.Lists;
import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.core.api.config.domain.RecaptchaConfiguration;
import eu.bcvsolutions.idm.core.api.dto.IdmConfigurationDto;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.model.service.api.IdmConfigurationService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityService;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.security.api.domain.IdmJwtAuthentication;
import eu.bcvsolutions.idm.core.security.api.dto.RecaptchaRequest;
import eu.bcvsolutions.idm.core.security.api.utils.IdmAuthorityUtils;
import eu.bcvsolutions.idm.core.security.rest.impl.RecaptchaController;
import eu.bcvsolutions.idm.test.api.AbstractRestTest;

/**
 * Rest system test for RecaptchaController
 *
 * @author Peter Sourek <peter.sourek@bcvsolutions.eu>
 */
public class DefaultRecaptchaRestTest extends AbstractRestTest {

	private static final String TEST_REMOTE_IP = "192.168.1.200";
	private static final String TEST_RESPONSE = "SOME RESPONSE";
	private static final String TEST_SECRET_KEY = "SOME_KEY";
	// private static final String TEST_HOSTNAME = "TestHost";

	@Autowired
	private IdmIdentityService identityService;

	@Autowired
	private IdmConfigurationService configService;

	@Mock
	private RestTemplate template;

	@Mock
	RecaptchaConfiguration config;

	@InjectMocks
	private DefaultRecaptchaService service;

	@Before
	public void setup() throws Exception {
		super.setup();
		MockitoAnnotations.initMocks(this);
		when(config.getSecretKey()).thenReturn(new GuardedString(TEST_SECRET_KEY));
	}

	@Test
	public void testNoPropsSet() throws Exception {
		deleteRecaptchaProperties();
		final String jsonContent = getRequest();
		//
		MockHttpServletResponse response = getMockHttpServletResponse(jsonContent);
		//
		Assert.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatus());
	}

	private MockHttpServletResponse getMockHttpServletResponse(String jsonContent) throws Exception {
		MockMvc mvc = getMockMvc();
		ResultActions actions = mvc.perform(MockMvcRequestBuilders.post(BaseDtoController.BASE_PATH + RecaptchaController.URL_PATH)
			.with(authentication(getAuthentication()))
			.contentType(MediaTypes.HAL_JSON)
			.content(jsonContent));
		MvcResult res = actions.andReturn();
		return res.getResponse();
	}

	private String getRequest() throws IOException {
		final RecaptchaRequest request = new RecaptchaRequest();
		request.setRemoteIp(TEST_REMOTE_IP);
		request.setResponse(TEST_RESPONSE);
		return jsonify(request);
	}

	private String jsonify(RecaptchaRequest dto) throws IOException {
		ObjectMapper m = new ObjectMapper();
		StringWriter sw = new StringWriter();
		ObjectWriter writer = m.writerFor(RecaptchaRequest.class);
		writer.writeValue(sw, dto);
		return sw.toString();
	}

	private Authentication getAuthentication() {
		return new IdmJwtAuthentication(
			identityService.getByUsername(InitTestData.TEST_ADMIN_USERNAME),
			null,
			Lists.newArrayList(IdmAuthorityUtils.getAdminAuthority()),
			"test");
	}

	private void deleteRecaptchaProperties() {
		IdmConfigurationDto keyProp = configService.getByCode(RecaptchaConfiguration.PROPERTY_SECRET_KEY);
		if (keyProp != null) {
			configService.delete(keyProp);
		}

		IdmConfigurationDto urlProp = configService.getByCode(RecaptchaConfiguration.PROPERTY_URL);
		if (urlProp != null) {
			configService.delete(urlProp);
		}
	}
}
