package eu.bcvsolutions.idm.core.security.service.impl;

import java.util.Arrays;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import eu.bcvsolutions.idm.core.security.api.dto.RecaptchaRequest;
import eu.bcvsolutions.idm.core.security.api.dto.RecaptchaResponse;

/**
 * Some usefull methods for Recaptcha testing
 *
 * @author Peter Sourek <peter.sourek@bcvsolutions.eu>
 */
public class RecaptchaTestUtil {

	public static RecaptchaRequest getRecaptchaRequest(String remoteIp, String response) {
		final RecaptchaRequest request = new RecaptchaRequest();
		request.setRemoteIp(remoteIp);
		request.setResponse(response);
		return request;

	}

	public static RecaptchaResponse getRecaptchaResponse(String hostName, boolean success, String... errorCodes) {
		final RecaptchaResponse response = new RecaptchaResponse();
		response.setErrorCodes(Arrays.asList(errorCodes));
		response.setHostname(hostName);
		response.setSuccess(success);
		return response;
	}

	public static ResponseEntity<RecaptchaResponse> getResponse(RecaptchaResponse response, HttpStatus status) {
		return new ResponseEntity<>(response, status);
	}

}
