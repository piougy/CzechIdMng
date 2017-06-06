package eu.bcvsolutions.idm.core.security.rest.impl;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.security.api.dto.RecaptchaRequest;
import eu.bcvsolutions.idm.core.security.api.dto.RecaptchaResponse;
import eu.bcvsolutions.idm.core.security.api.service.RecaptchaService;

/**
 * Controller for checking ReCaptcha.
 * 
 * @author Filip Mestanek
 */
@Controller
@RequestMapping(value = BaseController.BASE_PATH + RecaptchaController.URL_PATH)
public class RecaptchaController implements BaseController {
	
	public static final String URL_PATH = "/public/recaptcha";
	
	protected final RecaptchaService recaptchaService;
	
	@Autowired
	public RecaptchaController(RecaptchaService recaptchaService) {
		Assert.notNull(recaptchaService);
		//
		this.recaptchaService = recaptchaService;
	}
	
	/**
	 * ReCaptcha confirmation.
	 */
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST)
	public ResponseEntity<RecaptchaResponse> confirmRecaptcha(@RequestBody @Valid RecaptchaRequest request) {
		RecaptchaResponse response = recaptchaService.checkRecaptcha(request);
		//
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
}
