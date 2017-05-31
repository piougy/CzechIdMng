package eu.bcvsolutions.idm.core.security.api.service;


import eu.bcvsolutions.idm.core.security.api.dto.RecaptchaRequest;
import eu.bcvsolutions.idm.core.security.api.dto.RecaptchaResponse;

/**
 * Service for evaluating ReCaptcha.
 * 
 * @author Filip Mestanek
 */
public interface RecaptchaService {
	
	RecaptchaResponse checkRecaptcha(RecaptchaRequest data);
}
