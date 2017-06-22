package eu.bcvsolutions.idm.core.security.service.impl;

import static eu.bcvsolutions.idm.core.security.service.impl.RecaptchaTestUtil.getRecaptchaRequest;
import static eu.bcvsolutions.idm.core.security.service.impl.RecaptchaTestUtil.getRecaptchaResponse;
import static eu.bcvsolutions.idm.core.security.service.impl.RecaptchaTestUtil.getResponse;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestTemplate;

import eu.bcvsolutions.idm.core.api.config.domain.RecaptchaConfiguration;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.security.api.dto.RecaptchaResponse;
import eu.bcvsolutions.idm.test.api.AbstractUnitTest;

/**
 * Unit test for {@link DefaultRecaptchaService}
 *
 * @author Peter Sourek <peter.sourek@bcvsolutions.eu>
 */
public class DefaultRecaptchaServiceUnitTest extends AbstractUnitTest {

	private static final String TEST_HOSTNAME = "";
	private static final String TEST_REMOTE_IP = "";
	private static final String TEST_REQUEST = "";
	private static final String TEST_SECRET_KEY = "";

	@Mock
	RestTemplate template;

	@Mock
	RecaptchaConfiguration config;

	@InjectMocks
	DefaultRecaptchaService service;



	@Test()
	public void testNotValid() {
		RecaptchaResponse response = getRecaptchaResponse(TEST_HOSTNAME, false, "a", "b");
		when(config.getSecretKey()).thenReturn(new GuardedString(TEST_SECRET_KEY));
		when(template.postForEntity(anyString(), any(), eq(RecaptchaResponse.class))).thenReturn(getResponse(response, HttpStatus.OK));
		try {
			service.checkRecaptcha(getRecaptchaRequest(TEST_REMOTE_IP, TEST_REQUEST));
		} catch (ResultCodeException O_o) {
			Assert.assertEquals(O_o.getStatus(), CoreResultCode.RECAPTCHA_CHECK_FAILED.getStatus());
		}
		verify(template).postForEntity(anyString(), any(), eq(RecaptchaResponse.class));
		verifyNoMoreInteractions(template);
	}

	@Test()
	public void testNotValid2() {
		RecaptchaResponse response = getRecaptchaResponse(TEST_HOSTNAME, false, "a");
		when(config.getSecretKey()).thenReturn(new GuardedString(TEST_SECRET_KEY));
		when(template.postForEntity(anyString(), any(), eq(RecaptchaResponse.class))).thenReturn(getResponse(response, HttpStatus.OK));
		try {
			service.checkRecaptcha(getRecaptchaRequest(TEST_REMOTE_IP, TEST_REQUEST));
		} catch (ResultCodeException O_o) {
			Assert.assertEquals(O_o.getStatus(), CoreResultCode.RECAPTCHA_CHECK_FAILED.getStatus());
		}
		verify(template).postForEntity(anyString(), any(),eq(RecaptchaResponse.class));
		verifyNoMoreInteractions(template);
	}

	@Test()
	public void testValid() {
		RecaptchaResponse response = getRecaptchaResponse(TEST_HOSTNAME, true);
		when(config.getSecretKey()).thenReturn(new GuardedString(TEST_SECRET_KEY));
		when(template.postForEntity(anyString(), any(), eq(RecaptchaResponse.class))).thenReturn(getResponse(response, HttpStatus.OK));
		RecaptchaResponse returned = service.checkRecaptcha(getRecaptchaRequest(TEST_REMOTE_IP, TEST_REQUEST));
		//
		verify(template).postForEntity(anyString(), any(), eq(RecaptchaResponse.class));
		verifyNoMoreInteractions(template);
		Assert.assertNotNull(returned);
		Assert.assertEquals(returned.getHostname(), TEST_HOSTNAME);
	}

}
