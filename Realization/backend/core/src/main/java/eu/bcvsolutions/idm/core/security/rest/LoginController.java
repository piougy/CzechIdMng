package eu.bcvsolutions.idm.core.security.rest;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import eu.bcvsolutions.idm.core.exception.CoreResultCode;
import eu.bcvsolutions.idm.core.exception.RestApplicationException;
import eu.bcvsolutions.idm.core.model.domain.ResourceWrapper;
import eu.bcvsolutions.idm.core.security.dto.LoginDto;
import eu.bcvsolutions.idm.core.security.service.LoginService;

@RestController
@RequestMapping(value = "/api/authentication")
public class LoginController {

	@Autowired
	private LoginService loginService;

	@RequestMapping(method = RequestMethod.POST)
	public ResourceWrapper<LoginDto> login(@Valid @RequestBody(required = true) LoginDto loginDto) {
		if(loginDto == null || loginDto.getUsername() == null || loginDto.getPassword() == null){
			throw new RestApplicationException(CoreResultCode.AUTH_FAILED, "Username and password must be filled");
		}
		return new ResourceWrapper<LoginDto>(loginService.login(loginDto.getUsername(), loginDto.getPassword()));
	}	
}
