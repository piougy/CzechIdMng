package eu.bcvsolutions.idm.core.security.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import eu.bcvsolutions.idm.core.api.config.domain.RecaptchaConfiguration;
import eu.bcvsolutions.idm.core.security.api.dto.RecaptchaRequest;
import eu.bcvsolutions.idm.core.security.api.dto.RecaptchaResponse;
import eu.bcvsolutions.idm.core.security.api.service.RecaptchaService;

/**
 * @author Filip Mestanek
 */
@Service
public class DefaultRecaptchaService implements RecaptchaService {

	protected final RestTemplate restTemplate = new RestTemplate();
	protected final RecaptchaConfiguration config;
	
	@Autowired
	public DefaultRecaptchaService(RecaptchaConfiguration config) {
		this.config = config;
	}
	
	@Override
	public RecaptchaResponse checkRecaptcha(RecaptchaRequest req) {

		// If i tried to send is as an Entity, google API didn't understand the message
		// and returned errors. Therefore this map.
		MultiValueMap<String, String> body = createBody(config.getSecretKey().asString(), req.getRemoteIp(), req.getResponse());
		
        try {
        	RecaptchaResponse response = restTemplate
        			.postForEntity(config.getUrl(), body, RecaptchaResponse.class)
                    .getBody();
            return response;
        } catch (RestClientException e) {
            throw new RuntimeException("Recaptcha API not available due to exception", e);
        }
	}
	
	private MultiValueMap<String, String> createBody(String secret, String remoteIp, String response) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("secret", secret);
        form.add("remoteip", remoteIp);
        form.add("response", response);
        return form;
    }
}
