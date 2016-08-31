package eu.bcvsolutions.idm.security.rest;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import eu.bcvsolutions.idm.core.exception.CoreResultCode;
import eu.bcvsolutions.idm.core.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.model.domain.ResourceWrapper;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowProcessInstanceService;
import eu.bcvsolutions.idm.security.dto.LoginDto;
import eu.bcvsolutions.idm.security.service.LoginService;

/**
 * Identity authentication
 * 
 * @author Radek Tomiška <radek.tomiska@bcvsolutions.eu>
 *
 */
@RestController
@RequestMapping(value = "/api/authentication")
public class LoginController {

	@Autowired
	private LoginService loginService;
	
	@Autowired
	private WorkflowProcessInstanceService workflowProcessInstanceService;

	@RequestMapping(method = RequestMethod.POST)
	public ResourceWrapper<LoginDto> login(@Valid @RequestBody(required = true) LoginDto loginDto) {
		if(loginDto == null || loginDto.getUsername() == null || loginDto.getPassword() == null){
			throw new ResultCodeException(CoreResultCode.AUTH_FAILED, "Username and password must be filled");
		}
		return new ResourceWrapper<LoginDto>(loginService.login(loginDto.getUsername(), loginDto.getPassword()));
	}
	
}
